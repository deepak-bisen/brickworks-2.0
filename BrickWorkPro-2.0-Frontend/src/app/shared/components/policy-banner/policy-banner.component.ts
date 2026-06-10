import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ORDER_POLICY } from '../../constants/order-policy';

@Component({
  selector: 'app-policy-banner',
  standalone: true,
  imports: [RouterLink],
  template: `
    <p class="bw-policy-banner" [class.bw-policy-banner-dark]="variant() === 'dark'">
      @if (showIcon()) {
        <span aria-hidden="true">ℹ️</span>
      }
      <span>
        {{ message() || ORDER_POLICY.full }}
        @if (showQuoteLink()) {
          <a routerLink="/get-quote" class="bw-policy-link">{{ ORDER_POLICY.quoteCta }}</a>
        }
      </span>
    </p>
  `,
  styles: `
    .bw-policy-banner {
      display: flex;
      align-items: center;
      gap: 0.35rem;
      font-size: 0.75rem;
      line-height: 1.35;
      color: #4b5563;
      background: #f9fafb;
      border: 1px solid #e5e7eb;
      border-radius: 0.5rem;
      padding: 0.35rem 0.65rem;
    }
    .bw-policy-banner-dark {
      color: #d1d5db;
      background: rgba(255, 255, 255, 0.06);
      border-color: rgba(255, 255, 255, 0.12);
    }
    .bw-policy-link {
      margin-left: 0.25rem;
      font-weight: 700;
      color: #b91c1c;
      text-decoration: underline;
      text-underline-offset: 2px;
    }
    .bw-policy-banner-dark .bw-policy-link {
      color: #fca5a5;
    }
  `,
})
export class PolicyBannerComponent {
  readonly ORDER_POLICY = ORDER_POLICY;
  message = input<string>('');
  showQuoteLink = input(true);
  showIcon = input(true);
  variant = input<'light' | 'dark'>('light');
}