import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-brick-calculator',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './brick-calculator.component.html'
})
export class BrickCalculatorComponent {
  area = signal<number | null>(null);
  bricksNeeded = signal<number>(0);

  onAreaChange(value: number | string | null) {
    if (value === '' || value === null || value === undefined) {
      this.area.set(null);
      return;
    }
    const parsed = typeof value === 'number' ? value : Number(value);
    this.area.set(Number.isFinite(parsed) ? parsed : null);
  }

  compute() {
    const sqFt = this.area() || 0;
    const result = Math.ceil(sqFt * 7 * 1.1);
    this.bricksNeeded.set(result);
    if (result > 0) {
      sessionStorage.setItem('brickEstimate', String(result));
    }
  }
}