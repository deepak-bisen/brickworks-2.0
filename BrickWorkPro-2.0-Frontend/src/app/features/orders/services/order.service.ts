import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable } from 'rxjs';
import { PaymentDetails } from '../../../shared/utils/order-status.util';
import { OrderRequest, OrderResponse } from '../models/order-request.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private http = inject(HttpClient);
  private orderApiUrl = `${environment.apiUrl}/api/orders`;
  private paymentApiUrl = `${environment.apiUrl}/api/finance/payments`;
  // --- QUOTES ---
  requestPublicQuote(data: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.orderApiUrl}/public-quote`, data);
  }

  // --- ACTUAL ORDERS ---
  createActualOrder(data: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.orderApiUrl}/create`, data);
  }

  // --- ADMIN ---
  getAllActualOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.orderApiUrl}/all/orders`);
  }

  getAllPublicQuotes(): Observable<OrderResponse[]> {
    // FIX: Backend route has a typo: /all/get/public-qoute (missing 'u' in quote)
    // This must match exactly what the backend registered.
    return this.http.get<OrderResponse[]>(`${this.orderApiUrl}/all/get/public-quote`);
  }

  // FIX: Backend PUT /api/orders/{id}/status accepts 'status' as a query param
  // AND an optional 'driverDetails' param. Method signature kept aligned.
  updateOrderStatus(
    orderId: string,
    status: string,
    driverDetails?: string
  ): Observable<any> {
    let url = `${this.orderApiUrl}/${orderId}/status?status=${status}`;
    if (driverDetails) url += `&driverDetails=${encodeURIComponent(driverDetails)}`;
    return this.http.put(url, {});
  }

 getOrdersByCustomer(customerId: string, cacheBuster?: number): Observable<OrderResponse[]> {
    // FIX 2: Append cacheBuster if provided
    const url = cacheBuster
        ? `${this.orderApiUrl}/customer/${customerId}?cb=${cacheBuster}`
        : `${this.orderApiUrl}/customer/${customerId}`;

    return this.http.get<OrderResponse[]>(url);
  }

  getOrderById(orderId: string): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.orderApiUrl}/${orderId}`);
  }

  // --- PAYMENTS ---

  // FIX: Backend CodRequestDTO expects { orderId, amount } — matched correctly.
  selectCashOnDelivery(orderId: string, amount: number): Observable<string> {
    return this.http.post(
      `${this.paymentApiUrl}/cod/select`,
      { orderId, amount },
      { responseType: 'text' }
    );
  }

  // FIX: Backend UtrSubmissionDTO expects { orderId, utrNumber, amount }
  // The old call also sent 'bankName' which doesn't exist in the DTO — removed.
  submitUtrPayment(
    orderId: string,
    amount: number,
    utrNumber: string
  ): Observable<string> {
    return this.http.post(
      `${this.paymentApiUrl}/utr/submit`,
      { orderId, amount, utrNumber },
      { responseType: 'text' }
    );
  }

  // FIX: Backend OrderPaymentRequestDTO expects { orderId, amount }
  createRazorpayOrder(orderId: string, amount: number): Observable<any> {
    return this.http.post(`${this.paymentApiUrl}/create-order`, { orderId, amount });
  }

  verifyRazorpayPayment(verificationData: {
    razorpayOrderId: string;
    razorpayPaymentId: string;
    razorpaySignature: string;
    orderId: string;
  }): Observable<string> {
    return this.http.post(`${this.paymentApiUrl}/verify`, verificationData, {
      responseType: 'text',
    });
  }

  // Guest Order Tracking API
  trackOrder(orderId: string, phone: string): Observable<any> {
    return this.http.get(`${this.orderApiUrl}/track`, { params: { orderId, phone } });
  }

  getPaymentByOrderId(orderId: string): Observable<PaymentDetails> {
    return this.http.get<PaymentDetails>(`${this.paymentApiUrl}/order/${orderId}`);
  }
}
