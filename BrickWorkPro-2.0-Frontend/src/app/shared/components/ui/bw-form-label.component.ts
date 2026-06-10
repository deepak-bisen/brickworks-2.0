import { Component, input } from '@angular/core';

@Component({
  selector: 'bw-form-label',
  standalone: true,
  template: `
    <label class="bw-label" [attr.for]="htmlFor()">
      <ng-content />
    </label>
  `,
  styles: `
    .bw-label {
      display: block;
      font-size: 0.68rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.07em;
      color: #6b7280;
      margin-bottom: 0.35rem;
    }
  `,
})
export class BwFormLabelComponent {
  htmlFor = input<string>('');
}