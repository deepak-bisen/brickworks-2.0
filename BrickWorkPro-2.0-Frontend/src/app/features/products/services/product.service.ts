import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Product } from '../models/product.model';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/products`;

  products = signal<Product[]>([]);

  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/all`).pipe(
      tap(data => this.products.set(data))
    );
  }

  // UPDATED METHOD
  addProduct(productData: any, imageFile: File): Observable<Product> {
    const formData = new FormData();

    // 1. Loop through the productData object and append each key/value pair separately
    // This perfectly matches the Spring Boot @ModelAttribute requirement
    Object.keys(productData).forEach(key => {
      if (productData[key] !== null && productData[key] !== undefined) {
        formData.append(key, productData[key].toString());
      }
    });

    // 2. Use 'imageFile' as the exact key to match @RequestParam("imageFile")
    formData.append('imageFile', imageFile);

    // Hit the Admin POST endpoint
    return this.http.post<Product>(this.apiUrl, formData).pipe(
      tap(() => this.getAllProducts().subscribe())
    );
  }
}
