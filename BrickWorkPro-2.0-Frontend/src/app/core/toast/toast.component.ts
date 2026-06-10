import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, ToastType } from '../services/notification.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="fixed top-4 right-4 z-[100] flex flex-col gap-3 max-w-sm w-full pointer-events-none px-4">
      @for (toast of notification.toasts(); track toast.id) {
        <div
          class="pointer-events-auto flex items-start gap-3 px-4 py-3 rounded-xl shadow-lg border text-sm font-medium animate-in slide-in-from-right duration-300"
          [ngClass]="styleMap[toast.type]"
          role="alert"
        >
          <span class="text-lg leading-none mt-0.5">{{ iconMap[toast.type] }}</span>
          <p class="flex-1 leading-snug">{{ toast.message }}</p>
          <button
            type="button"
            class="text-current opacity-60 hover:opacity-100 transition-opacity text-lg leading-none"
            (click)="notification.dismiss(toast.id)"
            aria-label="Dismiss"
          >
            ×
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastComponent {
  notification = inject(NotificationService);

  iconMap: Record<ToastType, string> = {
    success: '✓',
    error: '✕',
    info: 'ℹ',
    warning: '⚠',
  };

  styleMap: Record<ToastType, string> = {
    success: 'bg-green-50 border-green-200 text-green-800',
    error: 'bg-red-50 border-red-200 text-red-800',
    info: 'bg-blue-50 border-blue-200 text-blue-800',
    warning: 'bg-amber-50 border-amber-200 text-amber-800',
  };
}