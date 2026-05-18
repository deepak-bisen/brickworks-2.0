import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ProductService } from '../../products/services/product.service';

@Component({
  selector: 'app-product-manager',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './product-manager.component.html'
})
export class ProductManagerComponent {
  private fb = inject(FormBuilder);
  productService = inject(ProductService);

 // Expanded to match ProductDTO exactly
  productForm = this.fb.group({
    name: ['', Validators.required],
    description: ['', Validators.required],
    category: ['', Validators.required],
    brickType: ['', Validators.required],
    dimensions: ['', Validators.required],
    unitPrice: [0, [Validators.required, Validators.min(1)]],
    estimatedCost: [0, [Validators.required, Validators.min(0)]],
    stockQuantity: [0, [Validators.required, Validators.min(0)]],
    bulkDiscountThreshold: [0, [Validators.required, Validators.min(0)]]
  });

  // Track the selected file
  selectedFile = signal<File | null>(null);

  // Handle file input changes
  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile.set(file);
    }
  }

  onSubmit() {
    if (this.productForm.valid && this.selectedFile()) {
      // Pass the exact form value object which now perfectly matches the DTO
      const productData = this.productForm.value;

      this.productService.addProduct(productData, this.selectedFile()!).subscribe({
        next: () => {
          alert('Product added successfully!');
          this.productForm.reset({
            unitPrice: 0, estimatedCost: 0, stockQuantity: 0, bulkDiscountThreshold: 0
          });
          this.selectedFile.set(null);
        },
        error: (err) => console.error('Failed to add product', err)
      });
    } else {
      alert('Please fill all fields and select an image.');
    }
  }
}
