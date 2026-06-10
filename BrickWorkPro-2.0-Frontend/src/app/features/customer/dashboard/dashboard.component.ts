import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { InvoiceService } from '../../../core/services/invoice.service';
import { OrderService } from '../../orders/services/order.service';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { SkeletonBlockComponent } from '../../../shared/components/skeleton-block/skeleton-block.component';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, StatusBadgeComponent, SkeletonBlockComponent],
  templateUrl: './dashboard.component.html',
})
export class CustomerDashboardComponent implements OnInit {
  private orderService = inject(OrderService);
  private authService = inject(AuthService);
  private invoiceService = inject(InvoiceService);

  orders = signal<any[]>([]);
  isLoadingOrders = signal(true);
  recentOrders = computed(() => this.orders().slice(0, 3));
  invoiceErrors: Record<string, string> = {};
  generatingInvoice: Record<string, boolean> = {};

  ngOnInit() {
    const tryFetch = (attemptsLeft: number) => {
      const customerId = this.authService.getUserId();
      if (customerId) {
        this.orderService.getOrdersByCustomer(customerId).subscribe({
          next: (data: any) => {
            const list = Array.isArray(data) ? data : data?.data || data?.content || [];
            this.orders.set(list);
            this.isLoadingOrders.set(false);
          },
          error: () => this.isLoadingOrders.set(false),
        });
      } else if (attemptsLeft > 0) {
        setTimeout(() => tryFetch(attemptsLeft - 1), 250);
      } else {
        this.isLoadingOrders.set(false);
      }
    };

    tryFetch(5);
  }

  downloadInvoice(orderId: string) {
    this.invoiceErrors[orderId] = '';
    this.invoiceService.downloadAndSave(orderId, 'BrickWorks_Invoice').subscribe({
      error: async (err) => {
        const details = await this.invoiceService.resolveDownloadError(err);
        this.invoiceErrors[orderId] = details.message;
        console.error('Could not download invoice', err);
      },
    });
  }

  generateInvoice(orderId: string) {
    this.invoiceErrors[orderId] = '';
    this.generatingInvoice[orderId] = true;

    this.invoiceService.generateAndDownload(orderId, 'BrickWorks_Invoice').subscribe({
      next: () => {
        this.invoiceErrors[orderId] = 'Invoice generated successfully. Downloading now...';
        this.generatingInvoice[orderId] = false;
      },
      error: (err) => {
        this.generatingInvoice[orderId] = false;
        this.invoiceErrors[orderId] = 'Could not generate invoice. Please try again.';
        console.error('Could not generate invoice', err);
      },
    });
  }
}