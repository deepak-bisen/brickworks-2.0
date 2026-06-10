import { Component, inject, input } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'bw-button',
  standalone: true,
  host: {
    '[class.bw-btn-full]': 'fullWidth()',
  },
  template: `
    <button
      [type]="routerLink() ? 'button' : type()"
      [disabled]="disabled()"
      class="bw-btn inline-flex items-center justify-center font-bold uppercase tracking-wider transition-colors rounded-xl disabled:opacity-50 disabled:cursor-not-allowed"
      [class.w-full]="fullWidth()"
      [class.text-xs]="size() === 'sm'"
      [class.px-4]="size() === 'sm'"
      [class.py-2]="size() === 'sm'"
      [class.min-h-10]="size() === 'sm'"
      [class.text-sm]="size() === 'md' || size() === 'lg'"
      [class.px-6]="size() === 'md' || size() === 'lg'"
      [class.py-3]="size() === 'md'"
      [class.min-h-11]="size() === 'md'"
      [class.py-4]="size() === 'lg'"
      [class.min-h-12]="size() === 'lg'"
      [class.md:text-base]="size() === 'lg'"
      [class.bg-red-700]="variant() === 'primary'"
      [class.text-white]="variant() === 'primary'"
      [class.hover:bg-red-800]="variant() === 'primary'"
      [class.shadow-sm]="variant() === 'primary'"
      [class.bg-gray-50]="variant() === 'secondary'"
      [class.text-gray-800]="variant() === 'secondary'"
      [class.border]="variant() === 'secondary'"
      [class.border-gray-200]="variant() === 'secondary'"
      [class.hover:bg-gray-100]="variant() === 'secondary'"
      [class.bg-transparent]="variant() === 'ghost'"
      [class.text-gray-700]="variant() === 'ghost'"
      [class.hover:bg-gray-50]="variant() === 'ghost'"
      (click)="onClick()">
      <ng-content />
    </button>
  `,
  styles: `
    :host {
      display: inline-block;
    }
    :host(.bw-btn-full) {
      display: block;
      width: 100%;
    }
    .bw-btn {
      width: 100%;
      cursor: pointer;
    }
    :host(:not(.bw-btn-full)) .bw-btn {
      width: auto;
    }
  `,
})
export class BwButtonComponent {
  private router = inject(Router);

  variant = input<'primary' | 'secondary' | 'ghost'>('primary');
  size = input<'sm' | 'md' | 'lg'>('md');
  disabled = input(false);
  fullWidth = input(false);
  type = input<'button' | 'submit'>('button');
  routerLink = input<string | string[] | null>(null);
  queryParams = input<Record<string, string> | null>(null);

  onClick(): void {
    const link = this.routerLink();
    if (!link || this.disabled()) return;
    const commands = Array.isArray(link) ? link : [link];
    void this.router.navigate(commands, { queryParams: this.queryParams() ?? undefined });
  }
}