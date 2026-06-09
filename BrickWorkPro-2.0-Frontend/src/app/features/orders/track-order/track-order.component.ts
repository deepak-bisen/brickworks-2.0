import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../services/order.service';

@Component({
  selector: 'app-track-order',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './track-order.component.html'
})
export class TrackOrderComponent {
  private orderService = inject(OrderService);

  orderId = signal('');
  phone = signal('');

  isLoading = signal(false);
  errorMsg = signal('');
  orderData = signal<any>(null);

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
        console.log("TRACKING DATA FROM BACKEND: ", res);
      },
      error: (err) => {
        this.errorMsg.set(err.error || 'Could not find tracking details. Please verify your inputs.');
        this.isLoading.set(false);
      }
    });
  }

  // Timeline UI ke liye helper
  // NAYA: Status ki calculation ekdum clear karne ke liye
  getStepStatus(step: number, currentStatus: string): 'completed' | 'active' | 'pending' {
    if (currentStatus === 'CANCELLED') return 'pending';

    let currentStep = 1;
    if (currentStatus === 'CONFIRMED_COD' || currentStatus === 'PAYMENT_RECEIVED' || currentStatus === 'IN_PRODUCTION') {
      currentStep = 2;
    } else if (currentStatus === 'DISPATCHED') {
      currentStep = 3;
    } else if (currentStatus === 'DELIVERED') {
      currentStep = 4;
    }

    if (step < currentStep) return 'completed';
    if (step === currentStep) {
      // Agar Delivered hai toh aakhiri step bhi 'completed' dikhayega (Checkmark ke sath)
      return step === 4 ? 'completed' : 'active';
    }
    return 'pending';
  }

  // NAYA: Line ko kitna fill karna hai (CSS width percentage)
  getProgressBarWidth(status: string): string {
    if (status === 'DELIVERED') return '100%';
    if (status === 'DISPATCHED') return '66.66%';
    if (status === 'CONFIRMED_COD' || status === 'PAYMENT_RECEIVED' || status === 'IN_PRODUCTION') return '33.33%';
    return '0%';
  }
}
