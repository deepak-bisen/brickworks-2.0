import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [class]="containerClass()" role="status" aria-live="polite" [attr.aria-label]="label()">
      <div
        class="rounded-full border-2 border-gray-200 border-t-red-700 animate-spin"
        [class]="sizeClass()">
      </div>
      @if (label()) {
        <p class="text-sm font-bold text-gray-500 animate-pulse mt-3">{{ label() }}</p>
      }
    </div>
  `,
})
export class LoadingSpinnerComponent {
  size = input<'sm' | 'md' | 'lg'>('md');
  label = input<string>('');
  centered = input<boolean>(true);

  sizeClass = () => {
    const sizes = { sm: 'w-5 h-5', md: 'w-8 h-8', lg: 'w-12 h-12' };
    return sizes[this.size()];
  };

  containerClass = () => {
    const base = 'flex flex-col items-center';
    return this.centered() ? `${base} justify-center` : base;
  };
}