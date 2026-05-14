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
  private productService = inject(ProductService);
  products: Product[] = [];
  isLoading = true;

ngOnInit(): void {
  console.log('Product List Component Initialized'); // Check if this appears in console
  this.loadProducts();
}

  loadProducts(): void {
  this.productService.getAllProducts().subscribe({
    next: (data) => {
      console.log('Data received from Backend:', data); // Should show your bricks
      this.products = data;
      this.isLoading = false;
    },
    error: (err) => {
      console.error('HTTP Error:', err); // This will tell us if the 401 is back
      this.isLoading = false;
    }
  });
}
}
