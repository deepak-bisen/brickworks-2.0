import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CartService } from '../services/cart.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cart.component.html',
})
export class CartComponent {
  cartService = inject(CartService);

  lineTotal(unitPrice: number, quantity: number): number {
    return unitPrice * quantity;
  }

  hasBulkDiscount(quantity: number, threshold?: number): boolean {
    return !!threshold && quantity >= threshold;
  }

  lineDiscount(unitPrice: number, quantity: number, threshold?: number): number {
    if (!this.hasBulkDiscount(quantity, threshold)) return 0;
    return unitPrice * quantity * 0.02;
  }
}