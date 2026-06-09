import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../orders/services/order.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-customer-orders',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './orders.component.html',
})
export class CustomerOrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  private authService = inject(AuthService);

  // MAGIC FIX 1: ChangeDetectorRef Angular ko forcefully UI update karne par majboor karta hai
  private cdr = inject(ChangeDetectorRef);

  orders: any[] = [];
  loading = true;
  invoiceErrors: Record<string, string> = {};
  generatingInvoice: Record<string, boolean> = {};

  ngOnInit() {
    this.fetchWithRetry(5);
  }

  // Dashboard ki tarah retry logic add kiya taaki instant refresh par user ID fetch ho sake
  private fetchWithRetry(attemptsLeft: number) {
    const userId = this.authService.getUserId();

    if (userId) {
      this.executeApiCall(userId);
    } else if (attemptsLeft > 0) {
      setTimeout(() => this.fetchWithRetry(attemptsLeft - 1), 300);
    } else {
      this.loading = false;
      this.cdr.detectChanges(); // UI forcefully update
    }
  }

  private executeApiCall(userId: string) {
    this.loading = true;
    this.cdr.detectChanges();

    const cacheBuster = new Date().getTime();

    // Hum explicitly next aur error dono mein spinner rok rahe hain (no pipe/finalize magic needed)
    this.orderService.getOrdersByCustomer(userId, cacheBuster).subscribe({
      next: (data) => {
        this.orders = Array.isArray(data) ? data : [];
        this.loading = false;
        this.cdr.detectChanges(); // MAGIC FIX 2: Spinner strictly band hoga
      },
      error: (err) => {
        console.error('API Error:', err);
        this.orders = [];
        this.loading = false;
        this.cdr.detectChanges(); // Error aane par bhi spinner rukega
      }
    });
  }

  downloadInvoice(orderId: string) {
    this.invoiceErrors[orderId] = '';
    this.orderService.downloadInvoice(orderId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `BrickWork_Invoice_${orderId}.pdf`;
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
            } catch {
              this.invoiceErrors[orderId] = 'Invoice download failed.';
            }
          });
          return;
        }
        this.invoiceErrors[orderId] = 'Could not download invoice. Please try again.';
      },
    });
  }

  generateInvoice(orderId: string) {
    this.invoiceErrors[orderId] = '';
    this.generatingInvoice[orderId] = true;
    this.cdr.detectChanges();

    this.orderService.generateInvoice(orderId).subscribe({
      next: () => {
        this.generatingInvoice[orderId] = false;
        this.invoiceErrors[orderId] = 'Invoice generated! Downloading...';
        this.cdr.detectChanges();
        this.downloadInvoice(orderId);
      },
      error: (err) => {
        this.generatingInvoice[orderId] = false;
        this.invoiceErrors[orderId] = 'Generation failed. Try again.';
        this.cdr.detectChanges();
      },
    });
  }
}
