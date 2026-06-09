import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../services/cart.service';
import { OrderService } from '../services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import { OrderRequest } from '../models/order-request.model';

declare var Razorpay: any;

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './checkout.component.html',
})
export class CheckoutComponent {
  cartService = inject(CartService);
  private orderService = inject(OrderService);
  authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private router = inject(Router);

  checkoutError = signal<string>('');
  isSubmitting = signal<boolean>(false);
  orderCreated = signal<boolean>(false);
  createdOrderId = signal<string>('');
  paymentMethod = signal<'COD' | 'UTR' | 'UPI' | null>(null);

  // NAYE GETTERS HTML TEMPLATE KE LIYE:
  get totalPrice() { return this.cartService.cartTotal(); }
  get grossTotal() { return this.cartService.grossTotal(); }
  get totalDiscount() { return this.cartService.totalDiscount(); }

  utrForm = this.fb.group({
    // FIX: Backend UtrSubmissionDTO only has utrNumber (not bankName).
    // Removed 'bankName' field to match the DTO exactly.
    utrNumber: ['', Validators.required],
  });

  checkoutForm = this.fb.group({
    customerName: ['', Validators.required],
    customerEmail: ['', [Validators.required, Validators.email]],
    customerPhone: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    deliveryAddress: ['', Validators.required],
  });

  removeItem(productId: string) {
    this.cartService.removeFromCart(productId);
  }

  increaseQuantity(productId: string) {
    this.cartService.increaseQuantity(productId);
  }

  decreaseQuantity(productId: string) {
    this.cartService.decreaseQuantity(productId);
  }

  // NAYA METHOD: HTML Input se aane wali value ko handle karne ke liye
  onQuantityChange(productId: string, event: Event) {
    const inputElement = event.target as HTMLInputElement;
    let newQuantity = parseInt(inputElement.value, 10);

    // Agar user ne galat input diya ya 500 se kam type kiya
    if (isNaN(newQuantity) || newQuantity < 500) {
      newQuantity = 500; // Default minimum quantity
      inputElement.value = '500'; // UI ko wapas minimum par set kar do
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

    // FIX: Attach customerId when the customer is logged in so the order
    // appears under their account in the customer dashboard.
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
        next: () =>
          this.finalizeCheckout('Order placed successfully via Cash on Delivery!'),
        error: () => this.checkoutError.set('Failed to register COD payment.'),
      });
    } else if (this.paymentMethod() === 'UTR') {
      if (this.utrForm.invalid) {
        this.checkoutError.set('Please provide your UTR Number.');
        return;
      }
      const { utrNumber } = this.utrForm.value;
      // FIX: submitUtrPayment no longer sends bankName (removed from backend DTO)
      this.orderService
        .submitUtrPayment(orderId, totalAmount, utrNumber!)
        .subscribe({
          next: () =>
            this.finalizeCheckout(
              'Bank Transfer recorded! Waiting for Admin verification.'
            ),
          error: () => this.checkoutError.set('Failed to submit UTR details.'),
        });
    } else if (this.paymentMethod() === 'UPI') {
      this.processRazorpayPayment(orderId, totalAmount);
    }
  }

  private processRazorpayPayment(orderId: string, amount: number) {
    this.orderService.createRazorpayOrder(orderId, amount).subscribe({
      next: (razorpayOrder) => {
        const options = {
          key: 'rzp_test_SlCAfQpHujynkq',
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
              next: () =>
                this.finalizeCheckout(
                  'Payment Successful! Your order is now officially confirmed.'
                ),
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

  private finalizeCheckout(message: string) {
    alert(message);
    this.cartService.clearCart();
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/customer/orders']);
    } else {
      this.router.navigate(['/home']);
    }
  }
}
