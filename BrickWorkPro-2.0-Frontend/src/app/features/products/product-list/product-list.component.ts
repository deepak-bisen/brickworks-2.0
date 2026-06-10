import { Component, OnInit, OnDestroy, inject, ChangeDetectionStrategy, signal, computed, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../services/product.service';
import { Product } from '../models/product.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '../../orders/services/cart.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ConfirmDialogService } from '../../../core/services/confirm-dialog.service';

type SortOption = 'name' | 'price-asc' | 'price-desc' | 'stock';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './product-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductListComponent implements OnInit, OnDestroy {
  productService = inject(ProductService);
  authService = inject(AuthService);
  cartService = inject(CartService);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private notification = inject(NotificationService);
  private confirmDialog = inject(ConfirmDialogService);
  private destroy$ = new Subject<void>();

  products = signal<Product[]>([]);
  isLoading = signal(true);
  errorMessage = signal('');
  brickEstimate = signal<number | null>(null);

  searchQuery = signal('');
  selectedCategory = signal('all');
  sortBy = signal<SortOption>('name');
  inStockOnly = signal(false);
  expandedProductId = signal<string | null>(null);
  quantities = signal<Record<string, number>>({});

  categories = computed(() => {
    const cats = new Set(this.products().map((p) => p.category).filter(Boolean));
    return ['all', ...Array.from(cats).sort()];
  });

  filteredProducts = computed(() => {
    let list = [...this.products()];
    const query = this.searchQuery().trim().toLowerCase();
    const category = this.selectedCategory();

    if (query) {
      list = list.filter((p) =>
        [p.name, p.brickType, p.category, p.description, p.dimensions]
          .filter(Boolean)
          .some((field) => field.toLowerCase().includes(query))
      );
    }

    if (category !== 'all') {
      list = list.filter((p) => p.category === category);
    }

    if (this.inStockOnly()) {
      list = list.filter((p) => p.stockQuantity >= 500);
    }

    const sort = this.sortBy();
    list.sort((a, b) => {
      switch (sort) {
        case 'price-asc':
          return a.unitPrice - b.unitPrice;
        case 'price-desc':
          return b.unitPrice - a.unitPrice;
        case 'stock':
          return b.stockQuantity - a.stockQuantity;
        default:
          return a.name.localeCompare(b.name);
      }
    });

    return list;
  });

  inStockCount = computed(() =>
    this.products().filter((p) => p.stockQuantity >= 500).length
  );

  ngOnInit(): void {
    this.loadProducts();
    if (isPlatformBrowser(this.platformId)) {
      const estimate = sessionStorage.getItem('brickEstimate');
      if (estimate) {
        const qty = Number(estimate);
        if (Number.isFinite(qty) && qty > 0) {
          this.brickEstimate.set(qty);
        }
      }
    }
  }

  dismissEstimate() {
    this.brickEstimate.set(null);
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.removeItem('brickEstimate');
    }
  }

  loadProducts(): void {
    this.isLoading.set(true);
    this.productService.getAllProducts().pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        const productArray = Array.isArray(response) ? response : (response.data || response.content || []);
        this.products.set(productArray);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load products from server catalog.');
        this.isLoading.set(false);
      }
    });
  }

  clearFilters() {
    this.searchQuery.set('');
    this.selectedCategory.set('all');
    this.sortBy.set('name');
    this.inStockOnly.set(false);
  }

  hasActiveFilters(): boolean {
    return !!this.searchQuery().trim() || this.selectedCategory() !== 'all' || this.inStockOnly();
  }

  getQuantity(productId: string): number {
    return this.quantities()[productId] ?? 500;
  }

  setQuantity(productId: string, value: number) {
    const parsed = Number(value);
    if (!Number.isFinite(parsed)) return;
    const qty = Math.max(500, Math.round(parsed / 100) * 100);
    this.quantities.update((map) => ({ ...map, [productId]: qty }));
  }

  cartQuantity(productId: string): number {
    const item = this.cartService.items().find((i) => i.product.productId === productId);
    return item?.quantity ?? 0;
  }

  lineTotal(product: Product, quantity: number): number {
    const gross = product.unitPrice * quantity;
    if (product.bulkDiscountThreshold && quantity >= product.bulkDiscountThreshold) {
      return gross * 0.98;
    }
    return gross;
  }

  hasBulkDiscount(product: Product, quantity: number): boolean {
    return !!product.bulkDiscountThreshold && quantity >= product.bulkDiscountThreshold;
  }

  toggleDetails(productId: string) {
    this.expandedProductId.update((id) => (id === productId ? null : productId));
  }

  goToQuote(productId: string) {
    this.router.navigate(['/get-quote'], { queryParams: { product: productId } });
  }

  addToCart(product: Product) {
    const qty = this.getQuantity(product.productId);
    if (product.stockQuantity < qty) {
      this.notification.error(`Only ${product.stockQuantity} units available for ${product.name}.`);
      return;
    }

    const existing = this.cartService.items().find((i) => i.product.productId === product.productId);
    if (existing) {
      this.cartService.updateQuantity(product.productId, existing.quantity + qty);
      this.notification.success(`Updated cart: ${existing.quantity + qty} units of ${product.name}.`);
    } else {
      this.cartService.addToCart(product, qty);
      this.notification.success(`Added ${qty} units of ${product.name} to your cart!`);
    }
  }

  async deleteProduct(productId: string, productName: string) {
    const ok = await this.confirmDialog.confirm({
      title: 'Delete Product',
      message: `Are you sure you want to permanently delete "${productName}"?`,
      confirmLabel: 'Delete',
      destructive: true,
    });
    if (ok) {
      this.productService.deleteProduct(productId).subscribe({
        next: () => {
          this.notification.success(`${productName} successfully removed from catalog.`);
          this.products.update((list) => list.filter((p) => p.productId !== productId));
        },
        error: () => {}
      });
    }
  }

  getImageSrc(product: Product): string {
    if (!product.imageData) return '';

    let base64String = '';

    if (typeof product.imageData === 'string') {
      if (product.imageData === 'null' || product.imageData.trim() === '') return '';
      base64String = product.imageData;
    } else if (Array.isArray(product.imageData) && isPlatformBrowser(this.platformId)) {
      try {
        const bytes = new Uint8Array(product.imageData as number[]);
        let binary = '';
        for (let i = 0; i < bytes.byteLength; i++) {
          binary += String.fromCharCode(bytes[i]);
        }
        base64String = btoa(binary);
      } catch {
        return '';
      }
    }

    if (!base64String) return '';
    if (base64String.startsWith('data:image')) return base64String;
    return `data:${product.imageType || 'image/jpeg'};base64,${base64String}`;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}