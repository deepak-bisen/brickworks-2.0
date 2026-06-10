import { Injectable, signal, computed, effect } from '@angular/core';
import { Product } from '../../products/models/product.model';

export interface CartItem {
  product: Product;
  quantity: number;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private readonly CART_STORAGE_KEY = 'brickworks_user_cart';

  // 1. Initialize signal by checking LocalStorage first!
  items = signal<CartItem[]>(this.loadCartFromStorage());

  constructor() {
    // 2. MAGIC FIX: Automatically sync signal changes to Local Storage
    effect(() => {
      // Whenever this.items() changes, this effect will run and save the new state
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem(this.CART_STORAGE_KEY, JSON.stringify(this.items()));
      }
    });
  }

  // Helper method to load data safely
  private loadCartFromStorage(): CartItem[] {
    if (typeof localStorage !== 'undefined') {
      const savedCart = localStorage.getItem(this.CART_STORAGE_KEY);
      if (savedCart) {
        try {
          return JSON.parse(savedCart);
        } catch (error) {
          console.error('Error reading cart data from storage', error);
          return [];
        }
      }
    }
    return []; // Return empty array if nothing is found
  }

  // --- COMPUTED PROPERTIES (No changes needed here) ---

  totalItems = computed(() => {
    return this.items().reduce((total, item) => total + item.quantity, 0);
  });

  grossTotal = computed(() => {
    return this.items().reduce((total, item) => total + (item.product.unitPrice * item.quantity), 0);
  });

  totalDiscount = computed(() => {
    return this.items().reduce((total, item) => {
      const itemTotal = item.product.unitPrice * item.quantity;
      if (item.product.bulkDiscountThreshold && item.quantity >= item.product.bulkDiscountThreshold) {
        return total + (itemTotal * 0.02);
      }
      return total;
    }, 0);
  });

  cartTotal = computed(() => {
    return this.grossTotal() - this.totalDiscount();
  });

  // --- CART ACTIONS (No changes needed here!) ---

  addToCart(product: Product, quantity: number = 500) {
    this.items.update(currentItems => {
      const existingItem = currentItems.find(item => item.product.productId === product.productId);

      if (existingItem) {
        return currentItems.map(item =>
          item.product.productId === product.productId
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      } else {
        return [...currentItems, { product, quantity }];
      }
    });
  }

  removeFromCart(productId: string) {
    this.items.update(currentItems =>
      currentItems.filter(item => item.product.productId !== productId)
    );
  }

  increaseQuantity(productId: string, amount: number = 100) {
    this.items.update(currentItems =>
      currentItems.map(item =>
        item.product.productId === productId
          ? { ...item, quantity: item.quantity + amount }
          : item
      )
    );
  }

  decreaseQuantity(productId: string, amount: number = 100) {
    this.items.update(currentItems =>
      currentItems.map(item =>
        item.product.productId === productId
          ? { ...item, quantity: Math.max(500, item.quantity - amount) }
          : item
      )
    );
  }

  updateQuantity(productId: string, newQuantity: number) {
    this.items.update(currentItems =>
      currentItems.map(item =>
        item.product.productId === productId
          ? { ...item, quantity: Math.max(500, newQuantity) }
          : item
      )
    );
  }

  clearCart() {
    this.items.set([]);
  }
}
