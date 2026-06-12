import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Observable, catchError, forkJoin, map, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AdminDashboardService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  // FIX: SalesAnalyticsProjection returns: period, totalRevenue, totalProfit
  // Old code mapped 's.revenue' which is not a field — only totalRevenue exists.
  getSalesAnalytics(timeframe: string = 'monthly'): Observable<any[]> {
    return this.http
      .get<any[]>(`${this.baseUrl}/api/orders/analytics/sales?timeframe=${timeframe}`)
      .pipe(
        map((sales) =>
          (sales || []).map((s) => ({
            ...s,
            period: s.period || 'Unknown',
            totalRevenue: s.totalRevenue ?? 0,
            totalProfit: s.totalProfit ?? 0,
            totalOrders: s.totalOrders ?? 0,
          })),
        ),
      );
  }

  // Top products now correctly filtered to only paid/live orders in backend.
  // totalRevenueGenerated = SUM(qty * price_per_unit) from paid order line items.
  getTopProducts(timeframe: string = 'monthly'): Observable<any[]> {
    return this.http
      .get<any[]>(`${this.baseUrl}/api/orders/analytics/top-products?timeframe=${timeframe}`)
      .pipe(
        map((products) =>
          (products || []).map((p) => ({
            ...p,
            productName: p.productName || `Product ${p.productId}`,
            totalQuantitySold: p.totalQuantitySold ?? 0,
            totalRevenueGenerated: p.totalRevenueGenerated ?? 0,
          })),
        ),
      );
  }

  // FIX: ProductionLogController is at /api/production-logs (not /api/products/production-logs)
  // ProductionLogDTO fields: id, managerId, productId, stage, quantity, createdAt
  getProductionLogs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/production-logs`).pipe(
      map((logs) =>
        (logs || []).map((log) => ({
          ...log,
          // FIX: normalize to consistent names for templates
          logId: log.id || log.logId,
          productionDate: log.createdAt || log.productionDate,
          quantityProduced: log.quantity ?? log.quantityProduced ?? 0,
          shiftType: log.stage || log.shiftType || 'N/A',
          productName: log.productName || `Product ${log.productId}`,
        })),
      ),
    );
  }

  private normalizeStatus(status: string | null | undefined): string {
    const raw = (status || '').toString().trim();
    const normalized = raw.toUpperCase().replace(/[-\s]+/g, '_');
    const statusMap: Record<string, string> = {
      APPROVED: 'CONFIRMED_COD',
      CONFIRMED: 'CONFIRMED_COD',
      CONFIRMED_COD: 'CONFIRMED_COD',
      PAYMENT_RECEIVED: 'PAYMENT_RECEIVED',
      IN_PRODUCTION: 'IN_PRODUCTION',
      DISPATCHED: 'DISPATCHED',
      DELIVERED: 'DELIVERED',
      CANCELLED: 'CANCELLED',
      REJECTED: 'CANCELLED',
      PENDING_PAYMENT: 'PENDING_PAYMENT',
      QUOTE_REQUEST: 'QUOTE_REQUEST',
      QUOTE_REQUESTED: 'QUOTE_REQUEST',
      PENDING: 'PENDING_PAYMENT',
    };
    return statusMap[normalized] || normalized || 'QUOTE_REQUEST';
  }

  private normalizeOrder(order: any): any {
    const status = this.normalizeStatus(order.status || 'QUOTE_REQUEST');
    const displayStatusMap: Record<string, string> = {
      CONFIRMED_COD: 'Confirmed COD',
      PAYMENT_RECEIVED: 'Payment Received',
      IN_PRODUCTION: 'In Production',
      QUOTE_REQUEST: 'Quote Request',
      PENDING_PAYMENT: 'Pending Payment',
      CANCELLED: 'Cancelled',
      DELIVERED: 'Delivered',
      DISPATCHED: 'Dispatched',
    };

    // Build normalized order - backend now provides customer details directly
    const normalized = {
      ...order,
      // OrderId alignment
      orderId: order.orderId || order.id || 'unknown',
      id: order.orderId || order.id || 'unknown',

      // Customer details - now come from backend OrderResponseDTO
      customerName: order.customerName || 'Guest Customer',
      customerEmail: order.customerEmail || 'No Email',
      customerPhone: order.customerPhone || 'No Phone',

      // Delivery location - now from backend OrderResponseDTO
      deliveryLocation: order.deliveryAddress || 'N/A',

      // Amount
      totalCost: order.totalAmount ?? order.totalCost ?? order.total ?? 0,
      netProfit: order.netProfit ?? order.totalProfit ?? 0, // NAYA
      status,
      displayStatus: displayStatusMap[status] || status,
      createdAt: order.createdAt || new Date().toISOString(),

      // Ensure requestType is preserved
      requestType: order.requestType || 'DIRECT ORDER',
    };

    return normalized;
  }

  getAllOrders(): Observable<any[]> {
    const actualOrders$ = this.http
      .get<any[]>(`${this.baseUrl}/api/orders/all/orders`)
      .pipe(catchError(() => of([])));

    const quotes$ = this.http
      .get<any[]>(`${this.baseUrl}/api/orders/all/get/public-quote`)
      .pipe(catchError(() => of([])));

    return forkJoin({ actual: actualOrders$, quotes: quotes$ }).pipe(
      map((results) => {
        const actual = (results.actual || []).map((o) =>
          this.normalizeOrder({ ...o, requestType: 'DIRECT ORDER' }),
        );

        const quotes = (results.quotes || []).map((o) =>
          this.normalizeOrder({ ...o, requestType: 'QUOTE LEAD' }),
        );

        return [...actual, ...quotes].sort(
          (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
        );
      }),
    );
  }

  // FIX: Backend updateOrderStatus uses PUT with query param 'status'.
  // Valid values are the OrderStatus enum values. The admin dashboard was
  // calling this with 'APPROVED'/'REJECTED' which are NOT valid enum values
  // — they would cause a 400 error. The mapping now lives in the component.
  updateOrderStatus(
    orderId: string | number,
    status:
      | 'PENDING_PAYMENT'
      | 'CONFIRMED_COD'
      | 'CANCELLED'
      | 'DISPATCHED'
      | 'DELIVERED'
      | 'IN_PRODUCTION'
      | 'PAYMENT_RECEIVED',
    driverDetails?: string,
  ): Observable<any> {
    let url = `${this.baseUrl}/api/orders/${orderId}/status?status=${status}`;
    if (driverDetails) {
      url += `&driverDetails=${encodeURIComponent(driverDetails)}`;
    }
    return this.http.put(
      url,
      {},
      { responseType: 'text' },
    );
  }

  resendNotifications(orderId: string | number, driverDetails?: string): Observable<any> {
    let url = `${this.baseUrl}/api/orders/${orderId}/resend-notifications`;
    if (driverDetails) {
      url += `?driverDetails=${encodeURIComponent(driverDetails)}`;
    }
    return this.http.post(url, {}, { responseType: 'text' });
  }

  getAllMessages(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/users/contact`).pipe(
      map((messages) =>
        (messages || []).map((msg) => ({
          ...msg,
          // FIX: ContactMessage entity uses 'id' as the JPA @Id field
          id: msg.id || msg.messageId || msg.contactId,
        })),
      ),
    );
  }

  // FIX: Backend ContactController PATCH /{messageId} expects a Map<String,String>
  // The service was sending { status: 'READ' } which is correct. Kept as-is.
  markMessageAsRead(messageId: string | number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/api/users/contact/${messageId}`, {
      status: 'READ',
    });
  }

  updateContactMessageStatus(messageId: string, status: string) {
    // Ye tumhare existing PATCH /api/users/contact/{messageId} ko hit karega
    return this.http.patch(`${this.baseUrl}/api/users/contact/${messageId}`, { status });
  }

  // FIX: Backend doesn't expect a JSON body for UTR verification.
  // It only needs the orderId in the URL, ?approved=true as query param, and returns plain text.
  // Admin UTR Approval / Rejection
  verifyUtrAndApproveOrder(orderId: string | number, isApproved: boolean = true): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/api/finance/payments/utr/verify/${orderId}?approved=${isApproved}`,
      {},
      { responseType: 'text' },
    );
  }

  // Get payment details for an order to show UTR history
  getOrderPaymentDetails(orderId: string | number): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/finance/payments/order/${orderId}`);
  }

  // NAYA: Initiate Refund API Call
  initiateRefund(orderId: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/finance/payments/refund/${orderId}`, {}, { responseType: 'text' });
  }

  /**
   * Revenue by Payment Method as proper backend analytics endpoint.
   */
  getRevenueByPaymentMethod(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/orders/analytics/revenue-by-payment-method`);
  }
}
