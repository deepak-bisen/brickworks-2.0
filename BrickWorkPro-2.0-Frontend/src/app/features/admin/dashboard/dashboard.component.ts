import { Component, inject, OnInit, OnDestroy, signal, computed, ChangeDetectionStrategy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AdminDashboardService } from '../service/admin-dashboard.service';
import { UtrVerificationModalComponent } from '../models/utr-verification-modal.component';
import { Subject, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartType } from 'chart.js';
import { ProductService } from '../../products/services/product.service';
import { Product } from '../../products/models/product.model';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';
import { NotificationService } from '../../../core/services/notification.service';
import { ConfirmDialogService } from '../../../core/services/confirm-dialog.service';
import { formatOrderStatus } from '../../../shared/utils/order-status.util';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { SkeletonBlockComponent } from '../../../shared/components/skeleton-block/skeleton-block.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, BaseChartDirective, UtrVerificationModalComponent, StatusBadgeComponent, LoadingSpinnerComponent, SkeletonBlockComponent],
  templateUrl: './dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent implements OnInit, OnDestroy {
  @ViewChild(UtrVerificationModalComponent) utrModal!: UtrVerificationModalComponent;

  authService = inject(AuthService);
  private dashboardService = inject(AdminDashboardService);
  private destroy$ = new Subject<void>();
  private productService = inject(ProductService);
  private platformId = inject(PLATFORM_ID);
  private notification = inject(NotificationService);
  private confirmDialog = inject(ConfirmDialogService);
  allProducts = signal<Product[]>([]);

  activeTab = signal<string>('overview');
  orderSubTab = signal<'orders' | 'quotes' | 'utr'>('orders');

  currentTimeframe = signal<string>('monthly');
  salesData = signal<any[]>([]);
  topProducts = signal<any[]>([]);
  productionLogs = signal<any[]>([]);
  isLoading = signal<boolean>(true);
  isLoadingOrders = signal<boolean>(false);
  isLoadingProducts = signal<boolean>(false);
  isLoadingMessages = signal<boolean>(false);

  orders = signal<any[]>([]);
  orderPayments = signal<Record<string, any>>({});
  messages = signal<any[]>([]);
  loadError = signal<string>('');
  processingOrderId = signal<string | number | null>(null);
  openActionMenuId = signal<string | null>(null);

  totalRevenue = computed(() => this.salesData().reduce((sum, item) => sum + (Number(item.totalRevenue || 0)), 0));
  totalProfit = computed(() => this.salesData().reduce((sum, item) => sum + (Number(item.totalProfit || item.profit || 0)), 0));
  totalOrdersCount = computed(() => this.salesData().reduce((sum, item) => sum + (Number(item.totalOrders || 0)), 0));
  totalBricksProduced = signal<number>(0);

  // Profit Margin %
  profitMarginPercent = computed(() => {
    const rev = this.totalRevenue();
    const prof = this.totalProfit();
    return rev > 0 ? (prof / rev) * 100 : 0;
  });

  // Revenue by Payment Method from backend analytics (paid orders only)
  revenueByPaymentMethod = signal<Record<string, number>>({});

  // Load revenue by payment method from backend analytics
  private loadRevenueByPaymentMethod() {
    this.dashboardService.getRevenueByPaymentMethod().subscribe({
      next: (data: any[]) => {
        const breakdown: Record<string, number> = {};
        (data || []).forEach((item: any) => {
          if (item.paymentMethod) {
            breakdown[item.paymentMethod] = item.totalRevenue || 0;
          }
        });
        this.revenueByPaymentMethod.set(breakdown);
      },
      error: () => this.revenueByPaymentMethod.set({})
    });
  }

  activeOrdersList = computed(() => this.orders().filter(o => o.requestType !== 'QUOTE LEAD'));
  quoteRequestsList = computed(() => this.orders().filter(o => o.requestType === 'QUOTE LEAD'));

  utrQueueList = computed(() =>
    this.activeOrdersList().filter((o) => {
      if (o.status !== 'PENDING_PAYMENT') return false;
      const payment = this.orderPayments()[o.orderId];
      return payment?.paymentMethod === 'BANK_TRANSFER' && payment?.paymentStatus === 'PENDING';
    })
  );

  displayedOrdersList = computed(() => {
    switch (this.orderSubTab()) {
      case 'quotes':
        return this.quoteRequestsList();
      case 'utr':
        return this.utrQueueList();
      default:
        return this.activeOrdersList();
    }
  });

  // Current open order value (includes PENDING_PAYMENT)
  financeOrderValue = computed(() =>
    this.activeOrdersList().reduce((sum, o) => sum + Number(o.totalAmount || o.totalCost || 0), 0)
  );

  // Pipeline Value: all active orders (including PENDING_PAYMENT)
  pipelineOrderValue = computed(() =>
    this.activeOrdersList().reduce((sum, o) => sum + Number(o.totalAmount || o.totalCost || 0), 0)
  );

  // Realized Value: only paid / live orders (excludes PENDING_PAYMENT and CANCELLED)
  realizedOrderValue = computed(() =>
    this.activeOrdersList()
      .filter(o => ['PAYMENT_RECEIVED', 'CONFIRMED_COD', 'IN_PRODUCTION', 'DISPATCHED', 'DELIVERED'].includes(o.status))
      .reduce((sum, o) => sum + Number(o.totalAmount || o.totalCost || 0), 0)
  );

  financePendingPaymentCount = computed(() =>
    this.activeOrdersList().filter((o) => o.status === 'PENDING_PAYMENT').length
  );

  financePaymentBreakdown = computed(() => {
    const breakdown: Record<string, number> = {
      CASH_ON_DELIVERY: 0,
      BANK_TRANSFER: 0,
      ONLINE: 0,
      UNPAID: 0,
    };
    for (const order of this.activeOrdersList()) {
      const payment = this.orderPayments()[order.orderId];
      if (!payment?.paymentMethod) {
        if (order.status === 'PENDING_PAYMENT') breakdown['UNPAID']++;
        continue;
      }
      breakdown[payment.paymentMethod] = (breakdown[payment.paymentMethod] || 0) + 1;
    }
    return breakdown;
  });

  financePaymentEntries = computed(() =>
    Object.entries(this.financePaymentBreakdown()).filter(([, count]) => count > 0)
  );

  isRealizedOrder(status: string): boolean {
    return ['PAYMENT_RECEIVED', 'CONFIRMED_COD', 'IN_PRODUCTION', 'DISPATCHED', 'DELIVERED'].includes(status);
  }

  // Search + Sort for admin live orders / quotes / UTR list
  searchTerm = signal<string>('');
  sortMode = signal<'newest' | 'oldest' | 'status'>('newest');
  expandedOrderId = signal<string | null>(null);

  onSearch(event: Event) {
    this.searchTerm.set((event.target as HTMLInputElement).value || '');
  }

  onSortChange(mode: string) {
    this.sortMode.set(mode as any);
  }

  toggleOrderDetails(orderId: string) {
    this.expandedOrderId.set(this.expandedOrderId() === orderId ? null : orderId);
  }

  // Processed list: applies search (name/phone/address) + sort on top of the sub-tab filtered list.
  // Keeps original counts for tab labels.
  filteredAndSortedOrders = computed(() => {
    let list = [...this.displayedOrdersList()];
    const term = this.searchTerm().toLowerCase().trim();
    if (term) {
      list = list.filter((o: any) => {
        const name = (o.customerName || '').toLowerCase();
        const phone = (o.customerPhone || '').toLowerCase();
        const addr = (o.deliveryAddress || o.deliveryLocation || '').toLowerCase();
        return name.includes(term) || phone.includes(term) || addr.includes(term);
      });
    }
    const mode = this.sortMode();
    list.sort((a: any, b: any) => {
      if (mode === 'newest') {
        return new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime();
      } else if (mode === 'oldest') {
        return new Date(a.createdAt || 0).getTime() - new Date(b.createdAt || 0).getTime();
      } else if (mode === 'status') {
        const priority: Record<string, number> = {
          'PENDING_PAYMENT': 1, 'CONFIRMED_COD': 2, 'PAYMENT_RECEIVED': 2,
          'IN_PRODUCTION': 3, 'DISPATCHED': 4, 'DELIVERED': 5, 'CANCELLED': 6, 'QUOTE_REQUEST': 0
        };
        return (priority[a.status] || 99) - (priority[b.status] || 99);
      }
      return 0;
    });
    return list;
  });

  ngOnInit(): void {
    this.loadDashboardMetrics();
    this.loadMessages();
    this.loadRevenueByPaymentMethod();
  }

  switchTab(tab: string) {
    this.activeTab.set(tab);
    if ((tab === 'orders' || tab === 'finance') && this.orders().length === 0) {
      this.loadOrders();
    } else if (tab === 'finance' && this.orders().length > 0) {
      this.loadPaymentsForAllOrders(this.orders());
    }
    if (tab === 'messages' && this.messages().length === 0) {
      this.loadMessages();
    }
    if (tab === 'inventory' && this.allProducts().length === 0) {
      this.loadProducts();
    }
  }

  switchOrderSubTab(tab: 'orders' | 'quotes' | 'utr') {
    this.orderSubTab.set(tab);
    this.openActionMenuId.set(null);
    this.searchTerm.set('');
    this.expandedOrderId.set(null);
    if (tab === 'utr' && this.orders().length > 0) {
      this.loadPaymentsForPendingOrders(this.orders());
    }
  }

  paymentForOrder(orderId: string) {
    return this.orderPayments()[orderId];
  }

  async approveAsCOD(order: any) {
    const ok = await this.confirmDialog.confirm({
      title: 'Confirm Cash on Delivery',
      message: `Approve this order as Cash on Delivery?\n\nOrder: ${order.orderId}`,
      confirmLabel: 'Confirm COD',
    });
    if (ok) this.updateOrderStatus(order.orderId, 'CONFIRMED_COD');
  }

  async convertToOrder(order: any) {
    const ok = await this.confirmDialog.confirm({
      title: 'Approve Quote',
      message: `Convert this quote into a live order?\n\nCustomer: ${order.customerName}`,
      confirmLabel: 'Approve Quote',
    });
    if (ok) this.updateOrderStatus(order.orderId, 'PENDING_PAYMENT');
  }

  onUtrModalClose() {
    this.processingOrderId.set(null);
    this.loadOrders();
  }

  public barChartType: ChartType = 'bar';
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: { y: { beginAtZero: true } },
    plugins: { legend: { display: false } }
  };
  public barChartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: [{ data: [], label: 'Revenue (₹)', backgroundColor: '#b91c1c', borderRadius: 6 }]
  };

  loadDashboardMetrics(): void {
    this.isLoading.set(true);
    const tf = this.currentTimeframe();

    this.dashboardService.getSalesAnalytics(tf).subscribe({
      next: (res: any) => {
        const data = Array.isArray(res) ? res : (res.data || res.content || []);
        const normalizedData = data.map((item: any) => ({
          ...item,
          totalRevenue: item.totalRevenue || item.revenue || 0,
          totalProfit: item.totalProfit || item.profit || 0,
          period: item.period || item.month || item.label || 'Unknown'
        }));
        this.salesData.set(normalizedData);
        this.updateChart(normalizedData);
        this.isLoading.set(false);
      },
      error: () => {
        this.loadError.set('Failed to load sales data.');
        this.isLoading.set(false);
      }
    });

    this.dashboardService.getTopProducts(tf).subscribe({
      next: (res: any) => {
        const products = Array.isArray(res) ? res : (res.data || []);
        this.topProducts.set(products.slice(0, 5));
      }
    });

    this.dashboardService.getProductionLogs().subscribe({
      next: (res: any) => {
        const logs = Array.isArray(res) ? res : (res.data || []);
        const total = logs.reduce((sum: number, log: any) => sum + (Number(log.quantityProduced || log.quantity || 0)), 0);
        this.totalBricksProduced.set(total);
        const sortedLogs = logs.sort((a: any, b: any) => new Date(b.productionDate).getTime() - new Date(a.productionDate).getTime());
        this.productionLogs.set(sortedLogs.slice(0, 7));
      }
    });
  }

  updateChart(data: any[]) {
    this.barChartData = {
      labels: data.map(d => d.period || d.month || 'Unknown'),
      datasets: [{
        data: data.map(d => Number(d.totalRevenue || d.revenue || 0)),
        backgroundColor: '#b91c1c',
        borderRadius: 6
      }]
    };
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  changeTimeframe(timeframe: string): void {
    this.currentTimeframe.set(timeframe);
    this.loadDashboardMetrics();
  }

  loadOrders() {
    this.isLoadingOrders.set(true);
    this.dashboardService.getAllOrders().subscribe({
      next: (orderList: any) => {
        this.orders.set(orderList);
        this.loadError.set('');
        if (this.activeTab() === 'finance') {
          this.loadPaymentsForAllOrders(orderList);
        } else {
          this.loadPaymentsForPendingOrders(orderList);
        }
        this.isLoadingOrders.set(false);
      },
      error: () => {
        this.orders.set([]);
        this.loadError.set('Failed to load order requests. Please refresh or try again later.');
        this.isLoadingOrders.set(false);
      }
    });
  }

  private loadPaymentsForPendingOrders(orderList: any[]) {
    const pending = orderList.filter(
      (o) => o.status === 'PENDING_PAYMENT' && o.requestType !== 'QUOTE LEAD'
    );
    if (pending.length === 0) {
      this.orderPayments.set({});
      return;
    }
    this.fetchPaymentsIntoMap(pending, {});
  }

  private loadPaymentsForAllOrders(orderList: any[]) {
    const orders = orderList.filter((o) => o.requestType !== 'QUOTE LEAD');
    if (orders.length === 0) {
      this.orderPayments.set({});
      return;
    }
    this.fetchPaymentsIntoMap(orders, { ...this.orderPayments() });
  }

  private fetchPaymentsIntoMap(orders: any[], existing: Record<string, any>) {
    forkJoin(
      orders.map((o) =>
        this.dashboardService.getOrderPaymentDetails(o.orderId).pipe(catchError(() => of(null)))
      )
    ).subscribe((results) => {
      const map = { ...existing };
      orders.forEach((o, i) => {
        if (results[i]) map[o.orderId] = results[i];
      });
      this.orderPayments.set(map);
    });
  }

  requestUtrVerification(order: any) {
    if (order.status === 'QUOTE_REQUEST' || order.status === 'PENDING_PAYMENT') {
      this.processingOrderId.set(order.orderId);
      this.openActionMenuId.set(null);
      setTimeout(() => {
        if (this.utrModal) {
          this.utrModal.openModal(order.orderId, order.totalCost || order.totalAmount);
        }
      }, 0);
    }
  }

  async quickApproveOrder(order: any) {
    const confirmMsg = order.requestType === 'QUOTE_REQUEST'
      ? 'Convert this quote to a confirmed order?'
      : 'Approve and confirm this order for production?';

    const ok = await this.confirmDialog.confirm({
      title: 'Approve Order',
      message: `${confirmMsg}\n\nOrder: ${order.orderId}\nAmount: ₹${order.totalCost || order.totalAmount}`,
      confirmLabel: 'Approve',
    });
    if (ok) this.updateOrderStatus(order.orderId, 'CONFIRMED_COD');
  }

  updateOrderStatus(orderId: string | number, status: 'PENDING_PAYMENT' | 'CONFIRMED_COD' | 'CANCELLED' | 'DISPATCHED' | 'DELIVERED' | 'IN_PRODUCTION' | 'APPROVED' | 'REJECTED') {
    const resolvedStatus = status === 'APPROVED' ? 'CONFIRMED_COD' : status === 'REJECTED' ? 'CANCELLED' : status;

    this.dashboardService.updateOrderStatus(orderId, resolvedStatus as any).subscribe({
      next: () => {
        this.processingOrderId.set(null);
        this.openActionMenuId.set(null);
        this.notification.success('Order status updated.');
        this.loadOrders();
      },
      error: () => {
        this.processingOrderId.set(null);
      }
    });
  }

  async rejectOrder(order: any) {
    const ok = await this.confirmDialog.confirm({
      title: 'Cancel Order',
      message: `Are you sure you want to reject this order?\n\nOrder: ${order.orderId}\nThis action cannot be undone.`,
      confirmLabel: 'Reject Order',
      destructive: true,
    });
    if (ok) this.updateOrderStatus(order.orderId, 'CANCELLED');
  }

  loadMessages() {
    this.isLoadingMessages.set(true);
    this.dashboardService.getAllMessages().subscribe({
      next: (data) => {
        this.messages.set(data || []);
        this.loadError.set('');
        this.isLoadingMessages.set(false);
      },
      error: () => {
        this.loadError.set('Failed to load messages. Please try again.');
        this.isLoadingMessages.set(false);
      }
    });
  }

  markMessageAsRead(msg: any) {
    if (msg.status === 'UNREAD') {
      this.dashboardService.updateContactMessageStatus(msg.id, 'READ').subscribe({
        next: () => {
          msg.status = 'READ';
        }
      });
    }
  }

  async resolveMessage(msg: any) {
    const ok = await this.confirmDialog.confirm({
      title: 'Resolve Inquiry',
      message: 'Mark this inquiry as resolved and remove from active inbox?',
      confirmLabel: 'Resolve',
    });
    if (ok) {
      this.dashboardService.updateContactMessageStatus(msg.id, 'RESOLVED').subscribe({
        next: () => {
          msg.status = 'RESOLVED';
        }
      });
    }
  }

  loadProducts() {
    this.isLoadingProducts.set(true);
    this.productService.getAllProducts().subscribe({
      next: (res: any) => {
        const prodArray = Array.isArray(res) ? res : (res.data || res.content || []);
        this.allProducts.set(prodArray);
        this.isLoadingProducts.set(false);
      },
      error: (err) => {
        console.error('Failed to load inventory', err);
        this.isLoadingProducts.set(false);
      }
    });
  }

  getImageSrc(product: Product): string {
    if (this.productService.hasProductImage(product) && product.productId) {
      return this.productService.getProductImageUrl(product.productId);
    }

    if (!product.imageData) return '';

    let base64String = '';
    if (typeof product.imageData === 'string') {
      if (product.imageData === 'null' || product.imageData.trim() === '') return '';
      base64String = product.imageData;
    } else if (Array.isArray(product.imageData) && isPlatformBrowser(this.platformId)) {
      try {
        base64String = btoa(String.fromCharCode(...(product.imageData as number[])));
      } catch {
        return '';
      }
    }

    if (!base64String) return '';
    return base64String.startsWith('data:image') ? base64String : `data:${product.imageType || 'image/jpeg'};base64,${base64String}`;
  }

  async deleteProduct(productId: string, productName: string) {
    const ok = await this.confirmDialog.confirm({
      title: 'Delete Product',
      message: `Are you sure you want to completely delete "${productName}" from the inventory?`,
      confirmLabel: 'Delete',
      destructive: true,
    });
    if (ok) {
      this.productService.deleteProduct(productId).subscribe({
        next: () => {
          this.notification.success(`${productName} successfully removed!`);
          this.loadProducts();
        },
        error: () => {}
      });
    }
  }

  async processRefund(order: any) {
    const ok = await this.confirmDialog.confirm({
      title: 'Process Refund',
      message: `Initiate official refund of ₹${order.totalAmount || order.totalCost} for Order: ${order.orderId}?`,
      confirmLabel: 'Initiate Refund',
      destructive: true,
    });
    if (ok) {
      this.dashboardService.initiateRefund(order.orderId).subscribe({
        next: (res: any) => {
          this.notification.success(String(res));
          this.loadOrders();
        },
        error: () => {}
      });
    }
  }

  formatStatus(status: string) {
    return formatOrderStatus(status);
  }

  quoteMailtoLink(order: any): string {
    const email = order.customerEmail || '';
    const subject = encodeURIComponent(`BrickWorks Pro — Quote follow-up for Order ${order.orderId}`);
    const body = encodeURIComponent(
      `Hi ${order.customerName || 'there'},\n\nThank you for your quote request (Order ${order.orderId}).\n\nWe would like to discuss your requirements and pricing.\n\nBest regards,\nBrickWorks Pro Team`
    );
    return `mailto:${email}?subject=${subject}&body=${body}`;
  }

  inboxQuickReplyLink(msg: any): string {
    const email = (msg?.email || '').trim();
    if (!email) return '';

    const subject = encodeURIComponent('Re: Your Inquiry at BrickWorks Pro');
    const body = encodeURIComponent(
      `Hi ${msg.name || 'there'},\n\nThank you for contacting BrickWorks Pro.\n\nRegarding your message:\n"${msg.message || ''}"\n\n\nBest regards,\nBrickWorks Pro Team`
    );
    return `mailto:${email}?subject=${subject}&body=${body}`;
  }

  quickReply(msg: any, event: Event): void {
    event.stopPropagation();
    event.preventDefault();

    const link = this.inboxQuickReplyLink(msg);
    if (!link) {
      this.notification.error('No email address on this message.');
      return;
    }

    this.markMessageAsRead(msg);

    if (isPlatformBrowser(this.platformId)) {
      window.location.href = link;
    }
  }

  paymentMethodLabel(method: string): string {
    const labels: Record<string, string> = {
      CASH_ON_DELIVERY: 'Cash on Delivery',
      BANK_TRANSFER: 'Bank Transfer (UTR)',
      ONLINE: 'UPI / Card',
      UNPAID: 'Awaiting Payment',
    };
    return labels[method] ?? method.replace(/_/g, ' ');
  }

  toggleActionMenu(orderId: string) {
    this.openActionMenuId.update((id) => (id === orderId ? null : orderId));
  }

  closeActionMenu() {
    this.openActionMenuId.set(null);
  }
}