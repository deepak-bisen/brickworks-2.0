import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skeleton-block',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
      class="animate-pulse bg-gray-200/80 rounded-xl"
      [class]="heightClass()"
      [style.width]="width()">
    </div>
  `,
})
export class SkeletonBlockComponent {
  height = input<'sm' | 'md' | 'lg' | 'xl'>('md');
  width = input<string>('100%');

  heightClass = () => {
    const heights = { sm: 'h-4', md: 'h-8', lg: 'h-12', xl: 'h-24' };
    return heights[this.height()];
  };
}