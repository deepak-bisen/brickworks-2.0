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

  // Signal storing the current list of products
  products = signal<Product[]>([]);

  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/all`).pipe(
      tap(data => this.products.set(data))
    );
  }

  // New method for creating products with images
  addProduct(productData: any, imageFile: File): Observable<Product> {
    const formData = new FormData();

    // Append the JSON part as a Blob with type application/json
    formData.append('product', new Blob([JSON.stringify(productData)], {
      type: 'application/json'
    }));

    // Append the actual file
    formData.append('image', imageFile);

    // Hit the Admin POST endpoint
    return this.http.post<Product>(this.apiUrl, formData).pipe(
      // Refresh the signal immediately after successful creation
      tap(() => this.getAllProducts().subscribe())
    );
  }
}
