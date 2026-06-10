import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { OrderService } from '../services/order.service';
import { ProductService } from '../../products/services/product.service';
import { OrderRequest } from '../models/order-request.model';
import { NotificationService } from '../../../core/services/notification.service';
import { ORDER_POLICY } from '../../../shared/constants/order-policy';
import { extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-quote-request',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './quote-request.component.html',
  styleUrl: './quote-request.css',
})
export class QuoteRequestComponent implements OnInit {
  readonly ORDER_POLICY = ORDER_POLICY;
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private orderService = inject(OrderService);
  productService = inject(ProductService);
  private notification = inject(NotificationService);

  isSubmitting = signal(false);
  submitError = signal('');

  quoteForm = this.fb.group({
    customerName: ['', Validators.required],
    customerEmail: ['', [Validators.required, Validators.email]],
    customerPhone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
    deliveryAddress: ['', Validators.required],
    productId: ['', Validators.required],
    quantity: ['', [Validators.required, Validators.min(500)]]
  });

  get f() {
    return this.quoteForm.controls;
  }

  get selectedProduct() {
    const productId = this.quoteForm.get('productId')?.value;
    if (!productId) return null;
    return this.productService.products().find((p) => p.productId === productId) ?? null;
  }

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
      this.notification.warning('Please select a valid brick type from the list.');
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

    this.submitError.set('');
    this.isSubmitting.set(true);

    this.orderService.requestPublicQuote(requestData).subscribe({
      next: () => {
        this.notification.success('Quote requested successfully! Our team will contact you shortly.');
        this.quoteForm.reset();
        this.isSubmitting.set(false);
        this.router.navigate(['/products']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const message = extractApiErrorMessage(err);
        if (message) {
          this.submitError.set(message);
        }
      },
    });
  }
}
