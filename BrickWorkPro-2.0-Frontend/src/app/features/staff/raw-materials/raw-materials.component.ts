import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../products/services/product.service';
import { RawMaterial } from '../../products/models/raw-material.model';

@Component({
  selector: 'app-raw-materials',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './raw-materials.component.html'
})
export class RawMaterialsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private productService = inject(ProductService);

  rawMaterials = signal<RawMaterial[]>([]);
  isLoading = signal(true);
  statusMessage = signal('');
  statusType = signal<'success' | 'error' | ''>('');
  stockAddedInputs = signal<Record<string, number>>({});

  addForm = this.fb.group({
    materialName: ['', Validators.required],
    unitOfMeasure: ['kg', Validators.required],
    currentStockLevel: [0, [Validators.required, Validators.min(0)]],
  });

  readonly statusClass = computed(() => {
    if (this.statusType() === 'success') return 'text-emerald-700 bg-emerald-50 border-emerald-200';
    if (this.statusType() === 'error') return 'text-red-700 bg-red-50 border-red-200';
    return 'hidden';
  });

  ngOnInit() {
    this.loadMaterials();
  }

  loadMaterials() {
    this.isLoading.set(true);
    this.setStatus('', '');

    this.productService.getRawMaterials().subscribe({
      next: (materials: RawMaterial[]) => {
        this.rawMaterials.set(Array.isArray(materials) ? materials : []);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('Unable to load raw materials', error);
        this.setStatus('Unable to load raw materials.', 'error');
        this.isLoading.set(false);
      }
    });
  }

  submitNewMaterial() {
    if (this.addForm.invalid) {
      this.setStatus('Please complete all required fields.', 'error');
      return;
    }

    const payload: RawMaterial = {
      materialName: this.addForm.value.materialName?.trim() ?? '',
      unitOfMeasure: this.addForm.value.unitOfMeasure?.trim() ?? 'kg',
      currentStockLevel: Number(this.addForm.value.currentStockLevel ?? 0),
    };

    if (!payload.materialName) {
      this.setStatus('Raw material name is required.', 'error');
      return;
    }

    this.productService.addRawMaterial(payload).subscribe({
      next: () => {
        this.setStatus('Raw material added successfully.', 'success');
        this.addForm.reset({ materialName: '', unitOfMeasure: 'kg', currentStockLevel: 0 });
        this.loadMaterials();
      },
      error: (err) => {
        const message = err?.error?.message || err?.message || 'Failed to add raw material.';
        this.setStatus(message, 'error');
      }
    });
  }

  setStockAdded(materialId: string, rawValue: string) {
    const parsed = Number(rawValue);
    this.stockAddedInputs.update((current) => ({
      ...current,
      [materialId]: Number.isFinite(parsed) ? parsed : 0,
    }));
  }

  updateStock(material: RawMaterial) {
    const materialId = material.rawMaterialId;
    if (!materialId) {
      this.setStatus('Raw material identifier is missing.', 'error');
      return;
    }

    const amount = this.stockAddedInputs()[materialId] ?? 0;
    if (amount === 0) {
      this.setStatus('Enter a non-zero amount to adjust (positive to add, negative to deduct).', 'error');
      return;
    }

    this.productService.updateRawMaterialStock(materialId, amount).subscribe({
      next: (updated) => {
        this.rawMaterials.update((materials) =>
          materials.map((item) =>
            item.rawMaterialId === updated.rawMaterialId ? updated : item
          )
        );
        this.stockAddedInputs.update((current) => ({ ...current, [materialId]: 0 }));
        this.setStatus(`Stock updated for ${updated.materialName}.`, 'success');
      },
      error: (err) => {
        const message = err?.error?.message || err?.message || 'Failed to update stock.';
        this.setStatus(message, 'error');
      }
    });
  }

  private setStatus(message: string, type: 'success' | 'error' | '') {
    this.statusMessage.set(message);
    this.statusType.set(type);
  }

  trackByMaterial(_: number, material: RawMaterial) {
    return material.rawMaterialId ?? material.materialName;
  }
}
