import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // CRITICAL for input binding

@Component({
  selector: 'app-brick-calculator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './brick-calculator.component.html'
})
export class BrickCalculatorComponent {
  area = signal<number | null>(null);
  bricksNeeded = signal<number>(0);

  compute() {
    const sqFt = this.area() || 0;
    // Industry standard: 7 bricks per sq ft + 10% wastage allowance
    const result = Math.ceil(sqFt * 7 * 1.1);
    this.bricksNeeded.set(result);
  }
}
