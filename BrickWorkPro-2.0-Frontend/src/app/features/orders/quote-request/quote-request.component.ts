import { Component, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { OrderService } from '../services/order.service';
import { ProductService } from '../../products/services/product.service';

@Component({
  selector: 'app-quote-request',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './quote-request.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush // Follows best practices
})
export class QuoteRequestComponent implements OnInit {
  private fb = inject(FormBuilder);
  private orderService = inject(OrderService);
  productService = inject(ProductService); // To list brick types in dropdown

  ngOnInit(): void {
    // Fetch products so they appear in the dropdown
    this.productService.getAllProducts().subscribe();
  }

  quoteForm = this.fb.group({
    customerName: ['', Validators.required],
    customerEmail: ['', [Validators.required, Validators.email]],
    customerPhone: ['', Validators.required],
    deliveryAddress: ['', Validators.required],
    productId: ['', Validators.required],
    quantity: [1000, [Validators.required, Validators.min(500)]]
  });

  submitQuote() {
    if (this.quoteForm.valid) {
      const formValue = this.quoteForm.value;

      // DEBUG: Check what the form is actually holding
     console.log('Form Value:', formValue);

     // 1. Verify the ID isn't null or "undefined"
    if (!formValue.productId || formValue.productId === 'undefined') {
      console.error('Validation Error: Product ID is undefined');
      alert('Please select a brick type from the list.');
      return;
    }

      const requestData = {
        customerName: formValue.customerName!,
        customerEmail: formValue.customerEmail!,
        customerPhone: formValue.customerPhone!,
        deliveryAddress: formValue.deliveryAddress!,
        items: [{ productId: formValue.productId!, quantity: Number(formValue.quantity!) }]
      };

      console.log('Sending Quote Request:', requestData);

     this.orderService.requestPublicQuote(requestData).subscribe({
      next: () => {
        alert('Quote requested successfully!');
        this.quoteForm.reset();
      },
      error: (err) => {
        console.error('Quote failed', err);
        alert('Server Error: The selected product might be invalid.');
      }
    });
    }
  }
}
