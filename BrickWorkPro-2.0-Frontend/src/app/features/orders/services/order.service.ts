import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { OrderRequest } from '../models/order-request.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/orders`;

  requestPublicQuote(quoteData: OrderRequest): Observable<any> {
    // Connects to OrderController.requestPublicQuote
    return this.http.post(`${this.apiUrl}/public-quote`, quoteData);
  }
}
