import { Component, input } from '@angular/core';

export type CheckoutStep = 'shipping' | 'payment' | 'confirmation';

@Component({
  selector: 'app-checkout-steps',
  standalone: true,
  template: `
    <nav aria-label="Checkout progress" class="mb-8">
      <ol class="flex items-center justify-between gap-2 max-w-2xl">
        @for (step of steps; track step.id; let i = $index) {
          <li class="flex items-center flex-1 min-w-0">
            <div class="flex flex-col items-center flex-1 min-w-0">
              <span
                class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-black shrink-0 transition-colors"
                [class.bg-red-700]="isActive(step.id) || isComplete(step.id)"
                [class.text-white]="isActive(step.id) || isComplete(step.id)"
                [class.bg-gray-200]="!isActive(step.id) && !isComplete(step.id)"
                [class.text-gray-500]="!isActive(step.id) && !isComplete(step.id)">
                @if (isComplete(step.id) && !isActive(step.id)) { ✓ } @else { {{ i + 1 }} }
              </span>
              <span
                class="text-[10px] font-bold uppercase tracking-wider mt-1.5 text-center truncate w-full"
                [class.text-red-700]="isActive(step.id)"
                [class.text-gray-500]="!isActive(step.id)">
                {{ step.label }}
              </span>
            </div>
            @if (i < steps.length - 1) {
              <div
                class="h-0.5 flex-1 mx-1 rounded min-w-[12px] mb-5"
                [class.bg-red-600]="stepIndex(step.id) < stepIndex(current())"
                [class.bg-gray-200]="stepIndex(step.id) >= stepIndex(current())"></div>
            }
          </li>
        }
      </ol>
    </nav>
  `,
})
export class CheckoutStepsComponent {
  current = input<CheckoutStep>('shipping');

  readonly steps: { id: CheckoutStep; label: string }[] = [
    { id: 'shipping', label: 'Shipping' },
    { id: 'payment', label: 'Payment' },
    { id: 'confirmation', label: 'Done' },
  ];

  stepIndex(id: CheckoutStep): number {
    return this.steps.findIndex((s) => s.id === id);
  }

  isActive(id: CheckoutStep): boolean {
    return this.current() === id;
  }

  isComplete(id: CheckoutStep): boolean {
    return this.stepIndex(id) < this.stepIndex(this.current());
  }
}