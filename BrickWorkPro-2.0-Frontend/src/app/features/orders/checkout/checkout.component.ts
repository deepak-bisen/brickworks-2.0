import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../services/cart.service';
import { OrderService } from '../services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { OrderRequest } from '../models/order-request.model';
import { ConfirmationPaymentMethod } from '../order-confirmation/order-confirmation.component';
import { environment } from '../../../../environments/environment';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CheckoutStepsComponent, CheckoutStep } from '../../../shared/components/checkout-steps/checkout-steps.component';
import { PolicyBannerComponent } from '../../../shared/components/policy-banner/policy-banner.component';
import { BwInputDirective } from '../../../shared/components/ui/bw-input.directive';
import { BwFormLabelComponent } from '../../../shared/components/ui/bw-form-label.component';

declare var Razorpay: any;

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    LoadingSpinnerComponent,
    CheckoutStepsComponent,
    PolicyBannerComponent,
    BwInputDirective,
    BwFormLabelComponent,
  ],
  templateUrl: './checkout.component.html',
})
export class CheckoutComponent implements OnInit {
  cartService = inject(CartService);
  private orderService = inject(OrderService);
  authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private notification = inject(NotificationService);

  checkoutError = signal<string>('');
  isSubmitting = signal<boolean>(false);
  orderCreated = signal<boolean>(false);
  createdOrderId = signal<string>('');
  paymentMethod = signal<'COD' | 'UTR' | 'UPI' | null>(null);
  profileLoading = signal(false);

  checkoutStep = computed<CheckoutStep>(() => {
    if (this.orderCreated()) return 'payment';
    return 'shipping';
  });

  get totalPrice() { return this.cartService.cartTotal(); }
  get grossTotal() { return this.cartService.grossTotal(); }
  get totalDiscount() { return this.cartService.totalDiscount(); }

  utrForm = this.fb.group({
    utrNumber: ['', Validators.required],
  });

  checkoutForm = this.fb.group({
    customerName: ['', Validators.required],
    customerEmail: ['', [Validators.required, Validators.email]],
    customerPhone: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    deliveryAddress: ['', Validators.required],
  });

  ngOnInit() {
    this.prefillFromProfile();
  }

  private prefillFromProfile() {
    if (!this.authService.isAuthenticated() || !this.authService.isCustomer()) return;

    const user = this.authService.getCurrentUser();
    const username = user?.username;
    if (!username) return;

    this.profileLoading.set(true);
    this.authService.getProfile(username).subscribe({
      next: (profile) => {
        const phone = this.normalizePhone(profile.phoneNumber ?? '');
        this.checkoutForm.patchValue({
          customerName: profile.fullName ?? '',
          customerEmail: profile.email ?? '',
          customerPhone: phone,
          deliveryAddress: profile.address ?? '',
        });
        this.profileLoading.set(false);
      },
      error: () => this.profileLoading.set(false),
    });
  }

  private normalizePhone(phone: string): string {
    const digits = phone.replace(/\D/g, '');
    return digits.length >= 10 ? digits.slice(-10) : digits;
  }

  removeItem(productId: string) {
    this.cartService.removeFromCart(productId);
  }

  increaseQuantity(productId: string) {
    this.cartService.increaseQuantity(productId);
  }

  decreaseQuantity(productId: string) {
    this.cartService.decreaseQuantity(productId);
  }

  onQuantityChange(productId: string, event: Event) {
    const inputElement = event.target as HTMLInputElement;
    let newQuantity = parseInt(inputElement.value, 10);

    if (isNaN(newQuantity) || newQuantity < 500) {
      newQuantity = 500;
      inputElement.value = '500';
    }

    this.cartService.updateQuantity(productId, newQuantity);
  }

  confirmOrder() {
    this.checkoutError.set('');

    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      this.checkoutError.set(
        'Please check your details. Phone must be exactly 10 digits, and all fields are required.'
      );
      return;
    }

    if (this.cartService.items().length === 0) {
      this.checkoutError.set('Your cart is empty.');
      return;
    }

    this.isSubmitting.set(true);
    const formValue = this.checkoutForm.value;

    const requestData: OrderRequest = {
      customerName: formValue.customerName!,
      customerEmail: formValue.customerEmail!,
      customerPhone: formValue.customerPhone!,
      deliveryAddress: formValue.deliveryAddress!,
      items: this.cartService.items().map((item) => ({
        productId: item.product.productId,
        quantity: item.quantity,
      })),
    };

    const customerId = this.authService.getUserId();
    if (customerId) {
      requestData.customerId = customerId;
    }

    this.orderService.createActualOrder(requestData).subscribe({
      next: (response) => {
        this.createdOrderId.set(response.orderId);
        this.orderCreated.set(true);
        this.isSubmitting.set(false);
      },
      error: (err) => {
        console.error('Checkout failed:', err);
        this.isSubmitting.set(false);
        if (err.status === 400 && err.error) {
          const errorMessages = Object.values(err.error).join(' | ');
          this.checkoutError.set(`Validation Error: ${errorMessages}`);
        } else {
          this.checkoutError.set(
            'Server Error: Failed to process your order. Please try again.'
          );
        }
      },
    });
  }

  submitPayment() {
    const orderId = this.createdOrderId();
    const totalAmount = this.cartService.cartTotal();

    if (this.paymentMethod() === 'COD') {
      this.orderService.selectCashOnDelivery(orderId, totalAmount).subscribe({
        next: () => this.finalizeCheckout('COD'),
        error: () => this.checkoutError.set('Failed to register COD payment.'),
      });
    } else if (this.paymentMethod() === 'UTR') {
      if (this.utrForm.invalid) {
        this.checkoutError.set('Please provide your UTR Number.');
        return;
      }
      const { utrNumber } = this.utrForm.value;
      this.orderService
        .submitUtrPayment(orderId, totalAmount, utrNumber!)
        .subscribe({
          next: () => this.finalizeCheckout('UTR'),
          error: () => this.checkoutError.set('Failed to submit UTR details.'),
        });
    } else if (this.paymentMethod() === 'UPI') {
      this.processRazorpayPayment(orderId, totalAmount);
    }
  }

  private processRazorpayPayment(orderId: string, amount: number) {
    const razorpayKeyId = environment.razorpayKeyId;
    if (!razorpayKeyId) {
      this.checkoutError.set(
        'Payment gateway is not configured. Please contact support or choose another payment method.',
      );
      return;
    }

    this.orderService.createRazorpayOrder(orderId, amount).subscribe({
      next: (razorpayOrder) => {
        const options = {
          key: razorpayKeyId,
          amount: razorpayOrder.amount,
          currency: razorpayOrder.currency || 'INR',
          name: 'BrickWorks Pro',
          description: 'Industrial Brick Order',
          order_id: razorpayOrder.razorpayOrderId,
          handler: (response: any) => {
            const verificationPayload = {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
              orderId: orderId,
            };
            this.orderService.verifyRazorpayPayment(verificationPayload).subscribe({
              next: () => this.finalizeCheckout('UPI'),
              error: () =>
                this.checkoutError.set(
                  'Payment verification failed. Please contact support.'
                ),
            });
          },
          prefill: {
            name: this.checkoutForm.value.customerName,
            email: this.checkoutForm.value.customerEmail,
            contact: this.checkoutForm.value.customerPhone,
          },
          theme: { color: '#b91c1c' },
        };
        const rzp = new Razorpay(options);
        rzp.on('payment.failed', (response: any) => {
          this.checkoutError.set(`Payment Failed: ${response.error.description}`);
        });
        rzp.open();
      },
      error: () =>
        this.checkoutError.set('Failed to initialize payment gateway.'),
    });
  }

  private finalizeCheckout(method: ConfirmationPaymentMethod) {
    const orderId = this.createdOrderId();
    const phone = this.checkoutForm.value.customerPhone ?? '';
    const amount = this.cartService.cartTotal();
    const utrPending = method === 'UTR';

    this.cartService.clearCart();

    this.router.navigate(['/order-confirmation'], {
      queryParams: {
        orderId,
        phone,
        amount,
        payment: method,
        utrPending: utrPending ? 'true' : 'false',
      },
    });
  }
}