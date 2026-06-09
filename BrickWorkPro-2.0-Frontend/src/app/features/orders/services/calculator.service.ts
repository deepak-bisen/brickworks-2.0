import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class CalculatorService {
  // Logic: (Area to cover * Wall thickness multiplier) / Brick surface area
  calculateBricks(areaSqFt: number, brickType: string): number {
    // Assuming standard brick size + 10% for wastage/mortar
    const bricksPerSqFt = 7;
    return Math.ceil(areaSqFt * bricksPerSqFt * 1.1);
  }
}
