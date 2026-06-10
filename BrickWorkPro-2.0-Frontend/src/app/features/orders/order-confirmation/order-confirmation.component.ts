import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { formatOrderStatus } from '../../../shared/utils/order-status.util';
import { CheckoutStepsComponent } from '../../../shared/components/checkout-steps/checkout-steps.component';
import { BwButtonComponent } from '../../../shared/components/ui/bw-button.component';

export type ConfirmationPaymentMethod = 'UPI' | 'COD' | 'UTR' | 'NONE';

@Component({
  selector: 'app-order-confirmation',
  standalone: true,
  imports: [CommonModule, RouterLink, CheckoutStepsComponent, BwButtonComponent],
  templateUrl: './order-confirmation.component.html',
})
export class OrderConfirmationComponent implements OnInit {
  private route = inject(ActivatedRoute);

  orderId = signal('');
  phone = signal('');
  totalAmount = signal(0);
  paymentMethod = signal<ConfirmationPaymentMethod>('NONE');
  utrPending = signal(false);

  statusLabel = computed(() => {
    if (this.utrPending()) return 'Awaiting UTR Verification';
    if (this.paymentMethod() === 'COD') return 'COD Confirmed — In Production';
    if (this.paymentMethod() === 'UPI') return 'Payment Received — In Production';
    return 'Awaiting Payment';
  });

  headline = computed(() => {
    if (this.utrPending()) return 'UTR Submitted Successfully';
    if (this.paymentMethod() === 'NONE') return 'Order Placed';
    return 'Order Confirmed!';
  });

  ngOnInit() {
    const q = this.route.snapshot.queryParamMap;
    this.orderId.set(q.get('orderId') ?? '');
    this.phone.set(q.get('phone') ?? '');
    this.totalAmount.set(Number(q.get('amount') ?? 0));
    const method = (q.get('payment') ?? 'NONE').toUpperCase() as ConfirmationPaymentMethod;
    this.paymentMethod.set(method);
    this.utrPending.set(q.get('utrPending') === 'true');
  }

  formatStatus(status: string) {
    return formatOrderStatus(status);
  }

  copyOrderId() {
    const id = this.orderId();
    if (!id) return;
    navigator.clipboard?.writeText(id);
  }

  trackQueryParams() {
    return { orderId: this.orderId(), phone: this.phone() };
  }
}