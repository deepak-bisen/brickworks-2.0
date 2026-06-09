import { Component, OnInit, OnDestroy, inject, ChangeDetectionStrategy, signal, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ProductService } from '../services/product.service';
import { Product } from '../models/product.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../orders/services/cart.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductListComponent implements OnInit, OnDestroy {
  productService = inject(ProductService);
  authService = inject(AuthService); // Add this!
  private router = inject(Router);   // Added for Quote routing
  private platformId = inject(PLATFORM_ID);
  private cartService = inject(CartService); // INJECT THE CART SERVICE
  private destroy$ = new Subject<void>();

  products = signal<Product[]>([]);
  isLoading = signal(true);
  errorMessage = signal('');

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.productService.getAllProducts().pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        console.log('🔴 Component: Raw response from service:', response);
        console.log('🔴 Component: Is Array?', Array.isArray(response));
        console.log('🔴 Component: Response type:', typeof response);
        console.log('🔴 Component: Response keys:', Object.keys(response || {}));
        console.log('🔴 Component: Response length:', response?.length);

        const productArray = Array.isArray(response) ? response : (response.data || response.content || []);
        console.log('🔴 Component: Final product array:', productArray);
        console.log('🔴 Component: Final array length:', productArray?.length);
        console.log('🔴 Component: Product IDs:', productArray?.map((p: any) => p.productId));

        this.products.set(productArray);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.errorMessage.set('Failed to load products from server catalog.');
        this.isLoading.set(false);
      }
    });
  }

  // --- NEW ACTIONS --- //

 goToQuote(productId: string) {
    // FIX: Changed from /quote-request to /get-quote to match app.routes.ts
    this.router.navigate(['/get-quote'], { queryParams: { product: productId } });
  }

  // --- NEW: WIRED UP ADD TO CART --- //
  addToCart(product: Product) {
    // Add the default minimum brick order (500 units) to the cart
    this.cartService.addToCart(product, 500);

    // Provide user feedback
    alert(`Added 500 units of ${product.name} to your cart! You can adjust the exact quantity during checkout.`);
  }

  deleteProduct(productId: string, productName: string) {
    // Option C: Safe Deletion with Confirmation
    const isConfirmed = confirm(`Are you sure you want to permanently delete "${productName}"?`);
    if (isConfirmed) {
      this.productService.deleteProduct(productId).subscribe({
        next: () => alert(`${productName} successfully removed from catalog.`),
        error: (err) => {
          console.error('Delete failed', err);
          alert('Failed to delete product. It may be tied to existing orders.');
        }
      });
    }
  }

  // Dual-format safe image constructor
  getImageSrc(product: Product): string {
    if (!product.imageData) {
      return '';
    }

    let base64String = '';

    if (typeof product.imageData === 'string') {
      if (product.imageData === 'null' || product.imageData.trim() === '') {
        return '';
      }
      base64String = product.imageData;
    } else if (Array.isArray(product.imageData) && isPlatformBrowser(this.platformId)) {
      try {
        // SAFE CONVERSION FOR LARGE ARRAYS
        const bytes = new Uint8Array(product.imageData as number[]);
        let binary = '';
        const len = bytes.byteLength;
        for (let i = 0; i < len; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        base64String = btoa(binary);
      } catch (e) {
        console.error('Failed to convert image data:', e);
        return '';
      }
    }

    if (!base64String) return '';

    if (base64String.startsWith('data:image')) {
      return base64String;
    }

    return `data:${product.imageType || 'image/jpeg'};base64,${base64String}`;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
