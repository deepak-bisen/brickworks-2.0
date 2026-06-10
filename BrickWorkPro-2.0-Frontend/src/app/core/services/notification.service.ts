import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface Toast {
  id: number;
  type: ToastType;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private nextId = 0;
  toasts = signal<Toast[]>([]);

  success(message: string, durationMs = 4000) {
    this.show('success', message, durationMs);
  }

  error(message: string, durationMs = 5000) {
    this.show('error', message, durationMs);
  }

  info(message: string, durationMs = 4000) {
    this.show('info', message, durationMs);
  }

  warning(message: string, durationMs = 4500) {
    this.show('warning', message, durationMs);
  }

  dismiss(id: number) {
    this.toasts.update((items) => items.filter((t) => t.id !== id));
  }

  private show(type: ToastType, message: string, durationMs: number) {
    const id = ++this.nextId;
    this.toasts.update((items) => [...items, { id, type, message }]);
    setTimeout(() => this.dismiss(id), durationMs);
  }
}