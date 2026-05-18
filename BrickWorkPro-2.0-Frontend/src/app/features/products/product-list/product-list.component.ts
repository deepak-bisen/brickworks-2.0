import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService } from '../services/product.service';
import { Product } from '../models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-list.component.html'
})
export class ProductListComponent implements OnInit {
  productService = inject(ProductService);
  products: Product[] = [];
  isLoading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.productService.getAllProducts().subscribe({
      next: (response: any) => {
        console.log('Raw Data received from Backend:', response); // Keep this for debugging!
        const productArray = Array.isArray(response) ? response : (response.data || response.content || []);
        this.products = productArray;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load products.';
        this.isLoading = false;
      }
    });
  }

  // NEW: Bulletproof image renderer
  getImageSrc(product: Product): string {
    if (!product.imageData || product.imageData === 'null') {
      return ''; // No image data exists
    }

    // If the backend already included the base64 prefix, return it as-is
    if (product.imageData.startsWith('data:image')) {
      return product.imageData;
    }

    // Otherwise, safely attach the prefix
    return `data:${product.imageType || 'image/jpeg'};base64,${product.imageData}`;
  }
}
