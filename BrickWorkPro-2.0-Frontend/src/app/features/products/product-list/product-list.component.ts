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
import { ORDER_POLICY } from '../../../shared/constants/order-policy';


type SortOption = 'name' | 'price-asc' | 'price-desc' | 'stock';

const FILTER_STORAGE_KEY = 'brickworks_catalog_filters';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductListComponent implements OnInit, OnDestroy {
  readonly ORDER_POLICY = ORDER_POLICY;
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

  /** Bricks in cart toward calculator target */
  cartBricksTowardEstimate = computed(() =>
    this.cartService.items().reduce((sum, item) => sum + item.quantity, 0)
  );

  estimateProgressPercent = computed(() => {
    const target = this.brickEstimate();
    if (!target || target <= 0) return 0;
    return Math.min(100, Math.round((this.cartBricksTowardEstimate() / target) * 100));
  });

  ngOnInit(): void {
    this.restoreFilters();
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

  private restoreFilters(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    try {
      const raw = sessionStorage.getItem(FILTER_STORAGE_KEY);
      if (!raw) return;
      const saved = JSON.parse(raw);
      if (typeof saved.searchQuery === 'string') this.searchQuery.set(saved.searchQuery);
      if (typeof saved.selectedCategory === 'string') this.selectedCategory.set(saved.selectedCategory);
      if (saved.sortBy) this.sortBy.set(saved.sortBy);
      if (typeof saved.inStockOnly === 'boolean') this.inStockOnly.set(saved.inStockOnly);
    } catch {
      /* ignore corrupt storage */
    }
  }

  private persistFilters(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    sessionStorage.setItem(
      FILTER_STORAGE_KEY,
      JSON.stringify({
        searchQuery: this.searchQuery(),
        selectedCategory: this.selectedCategory(),
        sortBy: this.sortBy(),
        inStockOnly: this.inStockOnly(),
      }),
    );
  }

  onSearchChange(value: string) {
    this.searchQuery.set(value);
    this.persistFilters();
  }

  onCategoryChange(value: string) {
    this.selectedCategory.set(value);
    this.persistFilters();
  }

  onSortChange(value: SortOption) {
    this.sortBy.set(value);
    this.persistFilters();
  }

  onInStockOnlyChange(value: boolean) {
    this.inStockOnly.set(value);
    this.persistFilters();
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
    this.persistFilters();
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
      this.notification.success(`Added ${qty} units of ${product.name}. Open your cart to checkout.`);
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
    if (this.productService.hasProductImage(product) && product.productId) {
      return this.productService.getProductImageUrl(product.productId);
    }

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