import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderService } from '../services/order.service';
import { ProductService } from '../../products/services/product.service';
import { OrderRequest } from '../models/order-request.model';

@Component({
  selector: 'app-quote-request',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './quote-request.component.html'
  // Removed ChangeDetectionStrategy to allow native form updates
})
export class QuoteRequestComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private orderService = inject(OrderService);
  productService = inject(ProductService);

  quoteForm = this.fb.group({
    customerName: ['', Validators.required],
    customerEmail: ['', [Validators.required, Validators.email]],
    customerPhone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
    deliveryAddress: ['', Validators.required],
    productId: ['', Validators.required],
    quantity: ['', [Validators.required, Validators.min(200)]]
  });

  // Helper method for easy access to form fields in HTML
  get f() { return this.quoteForm.controls; }

ngOnInit(): void {
    // 1. Fetch the products from the database
    this.productService.getAllProducts().subscribe({
      next: () => {

        // 2. Check the URL for the selected product
        this.route.queryParams.subscribe(params => {
          const selectedProductId = params['product'];

          if (selectedProductId) {
            // 3. The Magic Fix: Wait 50ms for the HTML <option> tags to render before selecting
            setTimeout(() => {
              this.quoteForm.patchValue({ productId: selectedProductId });
            }, 50);
          }
        });

      }
    });
  }

  submitQuote() {
    // 1. Check if the form is invalid first
    if (this.quoteForm.invalid) {
      this.quoteForm.markAllAsTouched(); // Highlights the missing fields in red
      return;
    }

    const formValue = this.quoteForm.value;

    // Explicitly check for 'undefined' to prevent backend crash
    if (!formValue.productId || formValue.productId === 'undefined') {
      alert('Please select a valid brick type from the list.');
      return;
    }

    const requestData: OrderRequest = {
      customerName: formValue.customerName!,
      customerEmail: formValue.customerEmail!,
      customerPhone: formValue.customerPhone!,
      deliveryAddress: formValue.deliveryAddress!,
      items: [{
        productId: formValue.productId,
        quantity: Number(formValue.quantity!)
      }]
    };

    console.log('Sending Quote Request:', requestData);

    this.orderService.requestPublicQuote(requestData).subscribe({
      next: () => {
        alert('Quote requested successfully! Our team will contact you shortly.');
        this.quoteForm.reset();

        // 2. Redirect back to the catalog
        this.router.navigate(['/products']);
      },
      error: (err) => {
        console.error('Quote failed', err);
        alert('Server Error: Failed to submit the quote. Please try again.');
      }
    });
  }
}
