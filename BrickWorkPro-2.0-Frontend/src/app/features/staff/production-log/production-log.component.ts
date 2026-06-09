import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../products/services/product.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-production-log',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink],
  templateUrl: './production-log.component.html',
})
export class ProductionLogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private productService = inject(ProductService);
  private authService = inject(AuthService);

  products: any[] = [];
  productionLogs: any[] = [];
  isLoadingProducts = false;
  productLoadError = '';
  // Raw materials for manual adjustment
  rawMaterials: any[] = [];
  manualAdjust = false;
  selectedMaterialId: string | null = null;
  manualAmount = 0; // positive to add, negative to deduct
  isAdjustingMaterial = false;
  adjustMessage = '';
  stageOptions = [
    { value: 'MOLDED', label: 'Molded' },
    { value: 'IN_KILN', label: 'In Kiln' },
    { value: 'BAKED', label: 'Baked / Finished' },
  ];

  successMessage = '';
  errorMessage = '';
  isSubmitting = false;

  productionForm = this.fb.group({
    productId: ['', Validators.required],
    quantity: [null as number | null, [Validators.required, Validators.min(1)]],
    stage: ['MOLDED', Validators.required],
  });

  ngOnInit() {
    this.loadProductOptions();
    this.loadProductionLogs();
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
      error: (err) => {
        console.error('Failed to load products for production log', err);
        this.productLoadError = 'Unable to load product options. Please refresh the page.';
        this.isLoadingProducts = false;
      },
    });
  }

  // Load raw materials when user wants to adjust inventory manually
  loadRawMaterials() {
    this.rawMaterials = [];
    this.productService.getRawMaterials().subscribe({
      next: (m) => (this.rawMaterials = Array.isArray(m) ? m : []),
      error: (err) => console.error('Failed to load raw materials', err),
    });
  }

  onSubmit() {
    if (this.productionForm.invalid) return;

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const managerId = this.authService.getUserId();
    if (!managerId) {
      this.errorMessage = 'User not authenticated. Please log in again.';
      this.isSubmitting = false;
      return;
    }

    const logData = {
      ...this.productionForm.value,
      managerId,
    };

    this.productService.submitProductionLog(logData).subscribe({
      next: () => {
        this.successMessage =
          'Production log saved! Raw materials are managed separately and must be adjusted manually if needed.';
        this.productionForm.reset({ stage: 'MOLDED' });
        // Open manual adjustment panel so staff can immediately adjust inventory
        this.manualAdjust = true;
        this.loadRawMaterials();
        this.isSubmitting = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = this.formatProductionLogError(err);
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
        this.adjustMessage = `Material ${updated.materialName} updated.`;
        this.isAdjustingMaterial = false;
        // refresh local list
        this.loadRawMaterials();
        this.manualAmount = 0;
      },
      error: (err) => {
        console.error('Material update failed', err);
        this.adjustMessage = err?.error?.message || 'Failed to update material.';
        this.isAdjustingMaterial = false;
      }
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
          return parsed.message ? `Failed to save log${statusPart}: ${parsed.message}` : `Failed to save log${statusPart}: ${error.error}`;
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


  loadProductionLogs() {
  this.productService.getProductionLogs().subscribe({

    next: (logs: any) => {
      this.productionLogs = logs;
    },
    error: (err) => {
      console.error(err);
    }

  });
}

moveToNextStage(log: any) {

  let nextStage = '';

  if (log.stage === 'MOLDED') {
    nextStage = 'IN_KILN';
  }
  else if (log.stage === 'IN_KILN') {
    nextStage = 'BAKED';
  }

  if (!nextStage) return;

  this.productService
    .updateProductionStage(log.productionLogId, nextStage)
    .subscribe({
      next: () => {
        this.loadProductionLogs();
      },
      error: (err) => {
        console.error(err);
      }
    });

}

}
