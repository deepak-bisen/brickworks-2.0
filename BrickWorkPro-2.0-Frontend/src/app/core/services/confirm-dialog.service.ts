import { Injectable, signal } from '@angular/core';

export interface ConfirmDialogOptions {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  destructive?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  visible = signal(false);
  title = signal('');
  message = signal('');
  confirmLabel = signal('Confirm');
  cancelLabel = signal('Cancel');
  destructive = signal(false);

  private resolver: ((value: boolean) => void) | null = null;

  confirm(options: ConfirmDialogOptions): Promise<boolean> {
    this.title.set(options.title);
    this.message.set(options.message);
    this.confirmLabel.set(options.confirmLabel ?? 'Confirm');
    this.cancelLabel.set(options.cancelLabel ?? 'Cancel');
    this.destructive.set(options.destructive ?? false);
    this.visible.set(true);

    return new Promise<boolean>((resolve) => {
      this.resolver = resolve;
    });
  }

  accept(): void {
    this.resolver?.(true);
    this.close();
  }

  dismiss(): void {
    this.resolver?.(false);
    this.close();
  }

  private close(): void {
    this.visible.set(false);
    this.resolver = null;
  }
}