import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { OrderService } from '../../orders/services/order.service';

@Component({
  selector: 'app-customer-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html'
})
export class CustomerDashboardComponent implements OnInit {
  orders: any[] = [];
  invoiceErrors: Record<string, string> = {};
  generatingInvoice: Record<string, boolean> = {};

  constructor(private orderService: OrderService, private authService: AuthService) {}

  ngOnInit() {
    const tryFetch = (attemptsLeft: number) => {
      const customerId = this.authService.getUserId();
      if (customerId) {
        console.debug('Dashboard: fetching orders for', customerId);
        this.orderService.getOrdersByCustomer(customerId).subscribe({
          next: (data: any) => {
            this.orders = Array.isArray(data) ? data : (data?.data || data?.content || []);
          },
          error: (err) => console.error('API Error fetching orders: ', err)
        });
      } else if (attemptsLeft > 0) {
        console.debug('Dashboard: no customerId yet, retrying...', attemptsLeft);
        setTimeout(() => tryFetch(attemptsLeft - 1), 250);
      } else {
        console.warn('Dashboard: no customerId found after retries; user likely not logged in.');
      }
    };

    tryFetch(5);
  }

  downloadInvoice(orderId: string) {
    this.invoiceErrors[orderId] = '';
    this.orderService.downloadInvoice(orderId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `BrickWorks_Invoice_${orderId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        const maybeBlob = err?.error;
        if (maybeBlob instanceof Blob && maybeBlob.type === 'application/json') {
          maybeBlob.text().then((txt) => {
            try {
              const json = JSON.parse(txt);
              this.invoiceErrors[orderId] = json.error || 'Invoice download failed.';
              if (err?.status === 404) {
                this.invoiceErrors[orderId] = 'Invoice not generated yet. You can generate it now.';
              }
            } catch {
              this.invoiceErrors[orderId] = 'Invoice download failed.';
            }
          });
          return;
        }

        this.invoiceErrors[orderId] = 'Could not download invoice. Please try again.';
        console.error('Could not download invoice', err);
      }
    });
  }

  generateInvoice(orderId: string) {
    this.invoiceErrors[orderId] = '';
    this.generatingInvoice[orderId] = true;

    this.orderService.generateInvoice(orderId).subscribe({
      next: () => {
        this.invoiceErrors[orderId] = 'Invoice generated successfully. Downloading now...';
        this.generatingInvoice[orderId] = false;
        this.downloadInvoice(orderId);
      },
      error: (err) => {
        this.generatingInvoice[orderId] = false;
        this.invoiceErrors[orderId] = 'Could not generate invoice. Please try again.';
        console.error('Could not generate invoice', err);
      }
    });
  }
}
