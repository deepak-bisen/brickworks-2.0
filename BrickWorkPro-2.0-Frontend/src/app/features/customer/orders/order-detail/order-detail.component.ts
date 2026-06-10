import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderService } from '../../../orders/services/order.service';
import { InvoiceService } from '../../../../core/services/invoice.service';
import { OrderResponse } from '../../../orders/models/order-request.model';
import { StatusBadgeComponent } from '../../../../shared/components/status-badge/status-badge.component';
import { LoadingSpinnerComponent } from '../../../../shared/components/loading-spinner/loading-spinner.component';
import { SkeletonBlockComponent } from '../../../../shared/components/skeleton-block/skeleton-block.component';
import {
  formatOrderStatus,
  getPaymentBadge,
  PaymentDetails,
} from '../../../../shared/utils/order-status.util';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, LoadingSpinnerComponent, SkeletonBlockComponent],
  templateUrl: './order-detail.component.html',
})
export class OrderDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private orderService = inject(OrderService);
  private invoiceService = inject(InvoiceService);

  order = signal<OrderResponse | null>(null);
  payment = signal<PaymentDetails | null>(null);
  loading = signal(true);
  error = signal('');
  invoiceError = signal('');
  generatingInvoice = signal(false);

  ngOnInit() {
    const orderId = this.route.snapshot.paramMap.get('orderId');
    if (!orderId) {
      this.loading.set(false);
      this.error.set('Invalid order link.');
      return;
    }

    this.orderService.getOrderById(orderId).subscribe({
      next: (data) => {
        this.order.set(data);
        this.loading.set(false);
        this.loadPayment(orderId);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Could not load order details. It may not exist or you may not have access.');
      },
    });
  }

  private loadPayment(orderId: string) {
    this.orderService.getPaymentByOrderId(orderId).subscribe({
      next: (p) => this.payment.set(p),
      error: () => this.payment.set(null),
    });
  }

  formatStatus(status: string) {
    return formatOrderStatus(status);
  }

  paymentBadge() {
    const o = this.order();
    return getPaymentBadge(this.payment(), o?.status);
  }

  isUtrPending(): boolean {
    const p = this.payment();
    return p?.paymentMethod === 'BANK_TRANSFER' && p?.paymentStatus === 'PENDING';
  }

  trackPhone(): string {
    const phone = this.order()?.customerPhone ?? '';
    const digits = phone.replace(/\D/g, '');
    return digits.length >= 10 ? digits.slice(-10) : digits;
  }

  canTrack(): boolean {
    const status = this.order()?.status ?? '';
    return ['DISPATCHED', 'DELIVERED', 'IN_PRODUCTION', 'CONFIRMED_COD', 'PAYMENT_RECEIVED'].includes(
      status,
    );
  }

  canInvoice(): boolean {
    const status = this.order()?.status ?? '';
    return ['CONFIRMED_COD', 'PAYMENT_RECEIVED', 'IN_PRODUCTION', 'DISPATCHED', 'DELIVERED'].includes(
      status,
    );
  }

  downloadInvoice() {
    const orderId = this.order()?.orderId;
    if (!orderId) return;

    this.invoiceError.set('');
    this.invoiceService.downloadAndSave(orderId).subscribe({
      error: async (err) => {
        const details = await this.invoiceService.resolveDownloadError(err);
        this.invoiceError.set(details.message);
      },
    });
  }

  generateInvoice() {
    const orderId = this.order()?.orderId;
    if (!orderId) return;

    this.invoiceError.set('');
    this.generatingInvoice.set(true);
    this.invoiceService.generateAndDownload(orderId).subscribe({
      next: () => this.generatingInvoice.set(false),
      error: () => {
        this.generatingInvoice.set(false);
        this.invoiceError.set('Invoice generation failed.');
      },
    });
  }
}