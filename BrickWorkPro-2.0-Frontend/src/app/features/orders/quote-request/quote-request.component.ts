import { Component, inject, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { OrderService } from '../services/order.service';
import { ProductService } from '../../products/services/product.service';
import { OrderRequest } from '../models/order-request.model';

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
  this.productService.getAllProducts().subscribe({
    next: (data) => {
      console.log('Products loaded for dropdown:', data);
      // Verify if 'id' exists in the data. If your backend uses 'productId' instead of 'id',
      // you must change [value]="product.productId" in the HTML.
    }
  });
}

quoteForm = this.fb.group({
  customerName: ['', Validators.required],
  customerEmail: ['', [Validators.required, Validators.email]],
  customerPhone: ['', Validators.required],
  deliveryAddress: ['', Validators.required],
  // FIX: Match the value="" of the first <option>
  productId: ['', Validators.required],
  quantity: [1000, [Validators.required, Validators.min(500)]]
});

  submitQuote() {
    if (this.quoteForm.valid) {
    const formValue = this.quoteForm.value;

    // FIX: Explicitly check for 'undefined' to prevent backend crash
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
        productId: formValue.productId, // Now verified as a real ID
        quantity: Number(formValue.quantity!)
      }]
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
