import { Component, computed, inject, NgZone, OnInit, signal } from '@angular/core';
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
import { extractApiErrorMessage } from '../../../shared/utils/api-error.util';

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
  private zone = inject(NgZone);

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
        this.isSubmitting.set(false);
        const apiMessage = extractApiErrorMessage(err);
        if (apiMessage) {
          this.checkoutError.set(apiMessage);
          return;
        }
        if (err?.status === 400 && err?.error && typeof err.error === 'object') {
          const errorMessages = Object.values(err.error).join(' | ');
          this.checkoutError.set(errorMessages);
          return;
        }
        this.checkoutError.set('Failed to create your order. No order or payment record was saved. Please try again or contact support.');
      },
    });
  }

  submitPayment() {
    const orderId = this.createdOrderId();
    const totalAmount = this.cartService.cartTotal();

    if (this.paymentMethod() === 'COD') {
      this.orderService.selectCashOnDelivery(orderId, totalAmount).subscribe({
        next: () => {
          this.checkoutError.set('');
          this.finalizeCheckout('COD');
        },
        error: (err) => {
          const msg = extractApiErrorMessage(err) || 'Temporary issue registering COD on server.';
          // The order was already created. Backend now treats most downstream failures as non-fatal
          // for the payment record. Give the user a recoverable message + still complete the flow.
          this.checkoutError.set(
            `${msg} Your order (${orderId}) was created and payment is being processed. ` +
            `Please check "My Orders" or track this order shortly.`
          );
          // Still advance the user — the important records (order + likely payment tx) exist on the backend.
          this.finalizeCheckout('COD');
        },
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
          next: () => {
            this.checkoutError.set('');
            this.finalizeCheckout('UTR');
          },
          error: (err) => {
            const msg = extractApiErrorMessage(err) || 'Temporary issue submitting UTR on server.';
            this.checkoutError.set(
              `${msg} Your order (${orderId}) was created and the UTR submission is being processed. ` +
              `Check "My Orders" for status (admin verification is required for bank transfers).`
            );
            this.finalizeCheckout('UTR');
          },
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
        this.checkoutError.set('');  // clear any previous error before opening popup
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
              next: () => this.zone.run(() => {
                this.checkoutError.set('');
                this.finalizeCheckout('UPI');
              }),
              error: (err) => this.zone.run(() => {
                const msg = extractApiErrorMessage(err) || 'Payment verification step encountered an issue.';
                // The /create-order step already created a PaymentTransaction (CREATED state).
                // The actual Razorpay payment may have succeeded (money moved). The webhook
                // (or later reconcile) can still mark it SUCCESS and advance the order.
                // Do not leave the user stuck on the checkout screen.
                this.checkoutError.set(
                  `${msg} Order ${orderId} and the Razorpay order were recorded. ` +
                  `If your payment went through on Razorpay, it will be confirmed via webhook shortly. ` +
                  `Check "My Orders" or the Track Order page.`
                );
                this.finalizeCheckout('UPI');
              }),
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
          this.zone.run(() => {
            this.checkoutError.set(`Payment Failed: ${response.error.description}`);
          });
        });
        rzp.open();
      },
      error: (err) => {
        const msg = extractApiErrorMessage(err) || 'Failed to initialize payment gateway with the server.';
        this.zone.run(() => {
          this.checkoutError.set(
            `${msg} Your order (${orderId}) was already created successfully. ` +
            `You can try another payment method or check "My Orders". No payment transaction was started yet.`
          );
        });
        // Here we do NOT auto-finalize because the Razorpay create (which creates the tx row) failed.
        // The user is still on the payment selection step and can choose COD/UTR instead.
      },
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