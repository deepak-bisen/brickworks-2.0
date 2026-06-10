import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { OrderService } from '../services/order.service';
import { formatOrderStatus } from '../../../shared/utils/order-status.util';
@Component({
  selector: 'app-track-order',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './track-order.component.html'
})
export class TrackOrderComponent implements OnInit {
  private orderService = inject(OrderService);
  private route = inject(ActivatedRoute);

  orderId = signal('');
  phone = signal('');

  isLoading = signal(false);
  errorMsg = signal('');
  orderData = signal<any>(null);

  ngOnInit() {
    const q = this.route.snapshot.queryParamMap;
    const orderId = q.get('orderId');
    const phone = q.get('phone');
    if (orderId) this.orderId.set(orderId);
    if (phone) this.phone.set(phone);
    if (orderId && phone) this.track();
  }

  track() {
    if (!this.orderId() || !this.phone()) {
      this.errorMsg.set('Please enter both Order ID and Phone Number.');
      return;
    }

    this.isLoading.set(true);
    this.errorMsg.set('');
    this.orderData.set(null);

    this.orderService.trackOrder(this.orderId().trim(), this.phone().trim()).subscribe({
      next: (res) => {
        this.orderData.set(res);
        this.isLoading.set(false);
      },
      error: (err) => {
        const msg = typeof err.error === 'string' ? err.error : null;
        this.errorMsg.set(msg || 'Could not find tracking details. Please verify your inputs.');
        this.isLoading.set(false);
      }
    });
  }

  formatStatus(status: string) {
    return formatOrderStatus(status);
  }

  getStepLabel(step: number, currentStatus: string): string {
    if (step === 1 && currentStatus === 'PENDING_PAYMENT') return 'Awaiting Payment';
    if (step === 1) return 'Order Placed';
    if (step === 2) return 'Processing';
    if (step === 3) return 'Dispatched';
    return 'Delivered';
  }

  getStepStatus(step: number, currentStatus: string): 'completed' | 'active' | 'pending' {
    if (currentStatus === 'CANCELLED') return 'pending';

    if (currentStatus === 'PENDING_PAYMENT') {
      if (step === 1) return 'active';
      return 'pending';
    }

    let currentStep = 2;
    if (currentStatus === 'CONFIRMED_COD' || currentStatus === 'PAYMENT_RECEIVED' || currentStatus === 'IN_PRODUCTION') {
      currentStep = 2;
    } else if (currentStatus === 'DISPATCHED') {
      currentStep = 3;
    } else if (currentStatus === 'DELIVERED') {
      currentStep = 4;
    }

    if (step < currentStep) return 'completed';
    if (step === currentStep) {
      return step === 4 ? 'completed' : 'active';
    }
    return 'pending';
  }

  getProgressBarWidth(status: string): string {
    if (status === 'DELIVERED') return '100%';
    if (status === 'DISPATCHED') return '66.66%';
    if (status === 'CONFIRMED_COD' || status === 'PAYMENT_RECEIVED' || status === 'IN_PRODUCTION') return '33.33%';
    if (status === 'PENDING_PAYMENT') return '0%';
    return '0%';
  }

  isAwaitingPayment(status: string): boolean {
    return status === 'PENDING_PAYMENT';
  }
}