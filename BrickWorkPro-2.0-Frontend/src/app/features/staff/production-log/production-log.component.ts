import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../products/services/product.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { SkeletonBlockComponent } from '../../../shared/components/skeleton-block/skeleton-block.component';

@Component({
  selector: 'app-production-log',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink, LoadingSpinnerComponent, SkeletonBlockComponent],
  templateUrl: './production-log.component.html',
})
export class ProductionLogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private productService = inject(ProductService);
  private authService = inject(AuthService);
  private notification = inject(NotificationService);

  products: any[] = [];
  isLoadingProducts = false;
  productLoadError = '';
  rawMaterials: any[] = [];
  manualAdjust = false;
  selectedMaterialId: string | null = null;
  manualAmount = 0;
  isAdjustingMaterial = false;
  adjustMessage = '';
  stageOptions = [
    { value: 'MOLDED', label: 'Molded' },
    { value: 'IN_KILN', label: 'In Kiln' },
    { value: 'BAKED', label: 'Baked / Finished' },
  ];

  isSubmitting = false;

  productionForm = this.fb.group({
    productId: ['', Validators.required],
    quantity: [null as number | null, [Validators.required, Validators.min(1)]],
    stage: ['MOLDED', Validators.required],
  });

  ngOnInit() {
    this.loadProductOptions();
  }

  private loadProductOptions() {
    this.productLoadError = '';
    const cachedProducts = this.productService.products();
    if (cachedProducts && cachedProducts.length > 0) {
      this.products = cachedProducts;
      return;
    }

    this.isLoadingProducts = true;
    this.productService.getAllProducts().subscribe({
      next: (products) => {
        this.products = Array.isArray(products) ? products : [];
        this.isLoadingProducts = false;
      },
      error: () => {
        this.productLoadError = 'Unable to load product options. Please refresh the page.';
        this.isLoadingProducts = false;
      },
    });
  }

  loadRawMaterials() {
    this.rawMaterials = [];
    this.productService.getRawMaterials().subscribe({
      next: (m) => (this.rawMaterials = Array.isArray(m) ? m : []),
      error: () => this.notification.error('Failed to load raw materials.'),
    });
  }

  onSubmit() {
    if (this.productionForm.invalid) return;

    this.isSubmitting = true;

    const managerId = this.authService.getUserId();
    if (!managerId) {
      this.notification.error('User not authenticated. Please log in again.');
      this.isSubmitting = false;
      return;
    }

    const logData = {
      ...this.productionForm.value,
      managerId,
    };

    this.productService.submitProductionLog(logData).subscribe({
      next: () => {
        this.notification.success(
          'Production log saved! Adjust raw materials manually if needed.',
        );
        this.productionForm.reset({ stage: 'MOLDED' });
        this.manualAdjust = true;
        this.loadRawMaterials();
        this.isSubmitting = false;
      },
      error: (err) => {
        this.notification.error(this.formatProductionLogError(err));
        this.isSubmitting = false;
      },
    });
  }

  toggleManualAdjust(checked: boolean) {
    this.manualAdjust = checked;
    if (checked) this.loadRawMaterials();
    this.adjustMessage = '';
  }

  adjustMaterial() {
    if (!this.selectedMaterialId) {
      this.adjustMessage = 'Select a material to adjust.';
      return;
    }
    if (!this.manualAmount || this.manualAmount === 0) {
      this.adjustMessage = 'Enter a non-zero amount (positive to add, negative to deduct).';
      return;
    }
    this.isAdjustingMaterial = true;
    this.productService.updateRawMaterialStock(this.selectedMaterialId, this.manualAmount).subscribe({
      next: (updated) => {
        this.notification.success(`Material ${updated.materialName} updated.`);
        this.adjustMessage = '';
        this.isAdjustingMaterial = false;
        this.loadRawMaterials();
        this.manualAmount = 0;
      },
      error: (err) => {
        this.adjustMessage = err?.error?.message || 'Failed to update material.';
        this.notification.error(this.adjustMessage);
        this.isAdjustingMaterial = false;
      },
    });
  }

  private formatProductionLogError(error: any): string {
    if (!error) {
      return 'Failed to save log. Please try again.';
    }

    const statusPart = error.status ? ` [${error.status}]` : '';

    if (error.error) {
      if (typeof error.error === 'string') {
        try {
          const parsed = JSON.parse(error.error);
          return parsed.message
            ? `Failed to save log${statusPart}: ${parsed.message}`
            : `Failed to save log${statusPart}: ${error.error}`;
        } catch {
          return `Failed to save log${statusPart}: ${error.error}`;
        }
      }
      if (typeof error.error === 'object') {
        const message = error.error.message || error.error.error || null;
        if (message) {
          return `Failed to save log${statusPart}: ${message}`;
        }
        return `Failed to save log${statusPart}: ${JSON.stringify(error.error)}`;
      }
    }

    if (error.message) {
      return `Failed to save log${statusPart}: ${error.message}`;
    }

    return 'Failed to save log. Please ensure sufficient raw materials exist in inventory.';
  }
}