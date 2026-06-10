import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Product } from '../models/product.model';
import { RawMaterial } from '../models/raw-material.model';
import { Observable, map, of, tap } from 'rxjs';
import { shareReplay } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/products`;
  private rawMaterialUrl = `${environment.apiUrl}/api/raw-materials`;
  private productionLogUrl = `${environment.apiUrl}/api/production-logs`;
  private cachedProducts$?: Observable<Product[]>;

  products = signal<Product[]>([]);

  private invalidateCache(): void {
    this.cachedProducts$ = undefined;
    this.products.set([]);
  }

  getAllProducts(): Observable<Product[]> {
    if (!this.cachedProducts$) {
      this.cachedProducts$ = this.http.get<any>(`${this.apiUrl}/all`).pipe(
        map((response) => {
          let products: Product[] = [];
          if (Array.isArray(response)) {
            products = response;
          } else if (response?.data && Array.isArray(response.data)) {
            products = response.data;
          } else if (response?.content && Array.isArray(response.content)) {
            products = response.content;
          }
          return products;
        }),
        tap((data: Product[]) => {
          this.products.set(data);
        }),
        shareReplay(1)
      );
    }
    return this.cachedProducts$;
  }

  getProductById(productId: string): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${productId}`);
  }

  addProduct(productData: any, imageFile: File): Observable<Product> {
    const formData = new FormData();
    Object.keys(productData).forEach((key) => {
      if (productData[key] !== null && productData[key] !== undefined)
        formData.append(key, productData[key].toString());
    });
    formData.append('imageFile', imageFile);
    return this.http
      .post<Product>(this.apiUrl, formData)
      .pipe(tap(() => {
        this.invalidateCache();
        this.getAllProducts().subscribe();
      }));
  }

  updateProduct(
    productId: string,
    productData: any,
    imageFile?: File | null
  ): Observable<Product> {
    const formData = new FormData();
    Object.keys(productData).forEach((key) => {
      if (
        productData[key] !== null &&
        productData[key] !== undefined &&
        productData[key] !== ''
      ) {
        formData.append(key, productData[key].toString());
      }
    });
    if (imageFile) formData.append('imageFile', imageFile);
    return this.http
      .patch<Product>(`${this.apiUrl}/${productId}`, formData)
      .pipe(tap(() => {
        this.invalidateCache();
        this.getAllProducts().subscribe();
      }));
  }

  deleteProduct(productId: string): Observable<any> {
    return this.http
      .delete(`${this.apiUrl}/${productId}`)
      .pipe(tap(() => {
        this.invalidateCache();
        this.getAllProducts().subscribe();
      }));
  }

  // FIX: Backend ProductionLogController is mapped to /api/production-logs (NOT /api/products/production-logs)
  // The old URL /api/products/production-logs returns 404.
  submitProductionLog(logData: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/api/production-logs`, logData);
  }

  getProductionLogs(): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/api/production-logs`);
  }

  // FIX: Correct analytics endpoint — /api/products/analytics/stats/daily
  getTodayProductionStats(): Observable<any> {
    return this.http.get(`${this.apiUrl}/analytics/stats/daily`);
  }

  getRawMaterials(): Observable<RawMaterial[]> {
    return this.http.get<RawMaterial[]>(`${this.rawMaterialUrl}/all`);
  }

  addRawMaterial(rawMaterial: RawMaterial): Observable<RawMaterial> {
    return this.http.post<RawMaterial>(`${this.rawMaterialUrl}/add`, rawMaterial);
  }

  updateRawMaterialStock(materialId: string, stockAdded: number): Observable<RawMaterial> {
    return this.http.patch<RawMaterial>(`${this.rawMaterialUrl}/update/${materialId}`, null, {
      params: { stockAdded: stockAdded.toString() },
    });
  }

updateProductionStage(id: string, payload: any) {
    // FIX: URL '/api/production-logs' hona chahiye
    return this.http.put(`${environment.apiUrl}/api/production-logs/${id}/stage`, payload);
  }
}
