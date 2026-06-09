import { Component, inject, signal, ChangeDetectionStrategy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ProductService } from '../../products/services/product.service';
import { ActivatedRoute, Router } from '@angular/router'; // NEW IMPORTS

@Component({
  selector: 'app-product-manager',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './product-manager.component.html'
})
export class ProductManagerComponent implements OnInit {
  private fb = inject(FormBuilder);
  productService = inject(ProductService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  isEditMode = signal<boolean>(false);
  editingProductId = signal<string | null>(null);
  selectedFile = signal<File | null>(null);

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

  ngOnInit() {
    // Check the URL for an ID parameter
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode.set(true);
        this.editingProductId.set(id);
        this.loadProductForEditing(id);
      }
    });
  }

  loadProductForEditing(id: string) {
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        // Pre-fill the form with the database values
        this.productForm.patchValue(product as any);
      },
      error: (err) => console.error('Failed to load product', err)
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) this.selectedFile.set(file);
  }

  onSubmit() {
    // Block if it's a NEW product and missing an image
    if (!this.isEditMode() && !this.selectedFile()) {
      alert('Please upload an image file for the new product.');
      return;
    }

    // Block if required text fields are completely cleared out
    if (this.productForm.invalid) {
      alert('Form is incomplete! Please check the fields.');
      this.productForm.markAllAsTouched();
      return;
    }

    const productData = this.productForm.value;

    if (this.isEditMode()) {
      // Send the PATCH request
      this.productService.updateProduct(this.editingProductId()!, productData, this.selectedFile()).subscribe({
        next: () => {
          alert('Product updated successfully!');
          this.router.navigate(['/products']);
        },
        error: (err) => {
          console.error('Update failed', err);
          alert('Failed to update product. Check console.');
        }
      });
    } else {
      // Send the POST request
      this.productService.addProduct(productData, this.selectedFile()!).subscribe({
        next: () => {
          alert('Product added successfully!');
          this.productForm.reset({
            unitPrice: 0, estimatedCost: 0, stockQuantity: 0, bulkDiscountThreshold: 0
          });
          this.selectedFile.set(null);
        },
        error: (err) => console.error('Add failed', err)
      });
    }
  }
}
