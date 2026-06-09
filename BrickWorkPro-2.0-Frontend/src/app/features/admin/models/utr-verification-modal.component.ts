import { Component, inject, signal, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminDashboardService } from '../service/admin-dashboard.service';

export interface UtrModalData {
  orderId: string | number;
  orderAmount: number;
  isOpen: boolean;
  customerUtr?: string; // Naya field UTR store karne ke liye
  isLoadingUtr?: boolean;
}

@Component({
  selector: 'app-utr-verification-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    @if (modalData().isOpen) {
      <div class="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4 animate-fade-in">
        <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full animate-slide-up overflow-hidden">

          <div class="bg-gradient-to-r from-gray-800 to-gray-900 px-6 py-4">
            <h2 class="text-lg font-black text-white flex items-center gap-2">
              <span>🏦</span> Bank Transfer Verification
            </h2>
          </div>

          <div class="p-6 space-y-5">

            <div class="grid grid-cols-2 gap-4">
                <div class="bg-gray-50 p-3 rounded-xl border border-gray-100">
                  <p class="text-[10px] text-gray-500 font-bold uppercase tracking-wider">Order ID</p>
                  <p class="text-sm font-bold text-gray-900 mt-1 truncate" [title]="modalData().orderId">
                    #{{ modalData().orderId.toString().substring(0, 8) }}...
                  </p>
                </div>

                <div class="bg-green-50 p-3 rounded-xl border border-green-100">
                  <p class="text-[10px] text-green-700 font-bold uppercase tracking-wider">Expected Amount</p>
                  <p class="text-lg font-black text-green-800 mt-1">₹{{ modalData().orderAmount | number:'1.0-2' }}</p>
                </div>
            </div>

            <div class="bg-blue-50/50 p-4 rounded-xl border border-blue-100 relative">
              <p class="text-xs text-blue-800 font-bold mb-2 flex items-center gap-2">
                <span>📝</span> Customer's Submitted UTR:
              </p>

              @if (modalData().isLoadingUtr) {
                <div class="h-8 flex items-center gap-2 text-gray-500 text-sm font-medium">
                  <span class="animate-spin">⟳</span> Fetching bank details...
                </div>
              } @else {
                <div class="bg-white px-4 py-3 rounded-lg border border-blue-200 shadow-sm font-mono text-lg font-black text-gray-800 tracking-wider text-center">
                  {{ modalData().customerUtr || 'No UTR Found' }}
                </div>
              }
            </div>

            @if (verificationError()) {
              <div class="bg-red-50 border border-red-200 p-3 rounded-lg flex gap-2 items-start">
                <span class="text-red-500">❌</span>
                <p class="text-red-800 text-sm font-medium">{{ verificationError() }}</p>
              </div>
            }
            @if (verificationSuccess()) {
              <div class="bg-green-50 border border-green-200 p-3 rounded-lg flex gap-2 items-start">
                <span class="text-green-500">✅</span>
                <p class="text-green-800 text-sm font-medium">{{ verificationSuccess() }}</p>
              </div>
            }
          </div>

          <div class="bg-gray-50 px-6 py-4 flex justify-between items-center border-t border-gray-100">
             <button
              (click)="closeModal()"
              [disabled]="isVerifying()"
              class="px-4 py-2 font-bold text-xs uppercase tracking-wider text-gray-600 hover:text-gray-900 transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            <div class="flex gap-2">
              <button
                (click)="rejectUtr()"
                [disabled]="isVerifying() || modalData().isLoadingUtr"
                class="px-5 py-2.5 font-bold text-xs uppercase tracking-wider rounded-lg bg-red-100 text-red-700 hover:bg-red-200 transition-all disabled:opacity-50 shadow-sm"
              >
                Reject UTR
              </button>
              <button
                (click)="verifyUtr()"
                [disabled]="isVerifying() || modalData().isLoadingUtr"
                class="px-5 py-2.5 font-bold text-xs uppercase tracking-wider rounded-lg bg-green-600 text-white hover:bg-green-700 transition-all disabled:opacity-50 shadow-sm shadow-green-600/20 flex items-center gap-2"
              >
                @if (isVerifying()) {
                  <span class="animate-spin">⟳</span> Verifying
                } @else {
                  ✓ Approve
                }
              </button>
            </div>
          </div>
        </div>
      </div>
    }
  `
})
export class UtrVerificationModalComponent implements OnInit {
  @Output() close = new EventEmitter<void>();
  private dashboardService = inject(AdminDashboardService);

  modalData = signal<UtrModalData>({ orderId: '', orderAmount: 0, isOpen: false, customerUtr: '', isLoadingUtr: false });
  isVerifying = signal(false);
  verificationError = signal('');
  verificationSuccess = signal('');

  ngOnInit() {}

  openModal(orderId: string | number, orderAmount: number) {
    this.verificationError.set('');
    this.verificationSuccess.set('');

    // Initial state with loading true
    this.modalData.set({ orderId, orderAmount, isOpen: true, customerUtr: '', isLoadingUtr: true });

    // FIX: Fetch the actual UTR number from the Finance Service
    this.dashboardService.getOrderPaymentDetails(orderId).subscribe({
      next: (paymentData) => {
        // Find the PENDING bank transfer transaction
        const pendingUtr = Array.isArray(paymentData)
          ? paymentData.find(p => p.paymentMethod === 'BANK_TRANSFER' && p.paymentStatus === 'PENDING')
          : paymentData;

        this.modalData.update(d => ({
          ...d,
          customerUtr: pendingUtr?.utrNumber || pendingUtr?.transactionReference || 'N/A',
          isLoadingUtr: false
        }));
      },
      error: (err) => {
        this.modalData.update(d => ({ ...d, customerUtr: 'Error Fetching UTR', isLoadingUtr: false }));
      }
    });
  }

  closeModal() {
    this.modalData.set({ ...this.modalData(), isOpen: false });
    this.close.emit();
  }

  verifyUtr() {
    this.processUtrApproval(true);
  }

  rejectUtr() {
    if(confirm('Are you sure you want to REJECT this payment? The order will be cancelled.')) {
      this.processUtrApproval(false);
    }
  }

  // Common method for both Approve and Reject
  private processUtrApproval(isApproved: boolean) {
    this.isVerifying.set(true);
    this.verificationError.set('');
    this.verificationSuccess.set('');

    const orderId = this.modalData().orderId;

    // Send the API request with ?approved=true or ?approved=false
    this.dashboardService.verifyUtrAndApproveOrder(orderId, isApproved).subscribe({
      next: () => {
        this.isVerifying.set(false);
        this.verificationSuccess.set(isApproved ? `Payment Approved!` : `Payment Rejected.`);
        setTimeout(() => this.closeModal(), 1500);
      },
      error: (error) => {
        this.isVerifying.set(false);
        const errorMsg = error?.error?.message || error?.error || 'Action failed. Try again.';
        this.verificationError.set(errorMsg);
      }
    });
  }
}
