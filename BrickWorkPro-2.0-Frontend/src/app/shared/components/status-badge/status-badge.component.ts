import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { formatOrderStatus, orderStatusBadgeClasses } from '../../utils/order-status.util';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span [class]="badgeClasses()">{{ label() }}</span>
  `,
})
export class StatusBadgeComponent {
  status = input.required<string>();

  label = computed(() => formatOrderStatus(this.status()));
  badgeClasses = computed(() => orderStatusBadgeClasses(this.status()));
}