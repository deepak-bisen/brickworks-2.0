import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { OrderService } from '../../orders/services/order.service';
import { InvoiceService } from '../../../core/services/invoice.service';
import { AuthService } from '../../../core/services/auth.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { SkeletonBlockComponent } from '../../../shared/components/skeleton-block/skeleton-block.component';
import {
  getPaymentBadge,
  PaymentDetails,
} from '../../../shared/utils/order-status.util';

@Component({
  selector: 'app-customer-orders',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, LoadingSpinnerComponent, SkeletonBlockComponent],
  templateUrl: './orders.component.html',
})
export class CustomerOrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  private invoiceService = inject(InvoiceService);
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  orders: any[] = [];
  payments: Record<string, PaymentDetails | null> = {};
  loading = true;
  loadError = '';
  invoiceErrors: Record<string, string> = {};
  generatingInvoice: Record<string, boolean> = {};

  ngOnInit() {
    this.fetchWithRetry(5);
  }

  private fetchWithRetry(attemptsLeft: number) {
    const userId = this.authService.getUserId();

    if (userId) {
      this.executeApiCall(userId);
    } else if (attemptsLeft > 0) {
      setTimeout(() => this.fetchWithRetry(attemptsLeft - 1), 300);
    } else {
      this.loading = false;
      this.cdr.detectChanges();
    }
  }

  private executeApiCall(userId: string) {
    this.loading = true;
    this.cdr.detectChanges();

    const cacheBuster = new Date().getTime();

    this.orderService.getOrdersByCustomer(userId, cacheBuster).subscribe({
      next: (data) => {
        this.orders = Array.isArray(data) ? data : [];
        this.loadPaymentsForOrders();
      },
      error: () => {
        this.orders = [];
        this.loadError = 'Could not load your orders. Please refresh and try again.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  private loadPaymentsForOrders() {
    if (this.orders.length === 0) {
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }

    const requests = this.orders.map((order) =>
      this.orderService.getPaymentByOrderId(order.orderId).pipe(catchError(() => of(null))),
    );

    forkJoin(requests).subscribe({
      next: (results) => {
        results.forEach((payment, index) => {
          this.payments[this.orders[index].orderId] = payment;
        });
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  paymentBadge(orderId: string, orderStatus: string) {
    return getPaymentBadge(this.payments[orderId], orderStatus);
  }

  isUtrPending(orderId: string): boolean {
    const p = this.payments[orderId];
    return p?.paymentMethod === 'BANK_TRANSFER' && p?.paymentStatus === 'PENDING';
  }

  trackPhone(order: any): string {
    const phone = order.customerPhone ?? '';
    const digits = phone.replace(/\D/g, '');
    return digits.length >= 10 ? digits.slice(-10) : digits;
  }

  downloadInvoice(orderId: string) {
    this.invoiceErrors[orderId] = '';
    this.invoiceService.downloadAndSave(orderId).subscribe({
      error: async (err) => {
        const details = await this.invoiceService.resolveDownloadError(err);
        this.invoiceErrors[orderId] = details.message;
        this.cdr.detectChanges();
      },
    });
  }

  generateInvoice(orderId: string) {
    this.invoiceErrors[orderId] = '';
    this.generatingInvoice[orderId] = true;
    this.cdr.detectChanges();

    this.invoiceService.generateAndDownload(orderId).subscribe({
      next: () => {
        this.generatingInvoice[orderId] = false;
        this.invoiceErrors[orderId] = 'Invoice generated! Downloading...';
        this.cdr.detectChanges();
      },
      error: async (err) => {
        this.generatingInvoice[orderId] = false;
        const details = await this.invoiceService.resolveDownloadError(err);
        this.invoiceErrors[orderId] = details.message || 'Generation failed. Try again.';
        this.cdr.detectChanges();
      },
    });
  }
}