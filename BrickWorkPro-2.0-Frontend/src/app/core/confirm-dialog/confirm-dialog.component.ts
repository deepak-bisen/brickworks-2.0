import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmDialogService } from '../services/confirm-dialog.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (dialog.visible()) {
      <div class="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/50" (click)="dialog.dismiss()">
        <div
          class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6 animate-fade-in-up"
          (click)="$event.stopPropagation()"
          role="dialog"
          aria-modal="true">
          <h2 class="text-lg font-black text-gray-900">{{ dialog.title() }}</h2>
          <p class="text-sm text-gray-600 mt-3 whitespace-pre-line leading-relaxed">{{ dialog.message() }}</p>
          <div class="flex gap-3 mt-6 justify-end">
            <button
              type="button"
              (click)="dialog.dismiss()"
              class="px-4 py-2.5 rounded-xl text-sm font-bold text-gray-600 hover:bg-gray-100 transition-colors">
              {{ dialog.cancelLabel() }}
            </button>
            <button
              type="button"
              (click)="dialog.accept()"
              [class]="dialog.destructive()
                ? 'px-4 py-2.5 rounded-xl text-sm font-bold text-white bg-red-700 hover:bg-red-800 transition-colors'
                : 'px-4 py-2.5 rounded-xl text-sm font-bold text-white bg-gray-900 hover:bg-red-700 transition-colors'">
              {{ dialog.confirmLabel() }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class ConfirmDialogComponent {
  dialog = inject(ConfirmDialogService);
}