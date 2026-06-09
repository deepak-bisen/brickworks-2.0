import { Component, inject, OnInit, OnDestroy, signal, computed, ChangeDetectionStrategy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AdminDashboardService } from '../service/admin-dashboard.service';
import { UtrVerificationModalComponent } from '../models/utr-verification-modal.component';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
// Add these to the very top of your file
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartType } from 'chart.js';
import { ProductService } from '../../products/services/product.service';
import { Product } from '../../products/models/product.model';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, BaseChartDirective, UtrVerificationModalComponent],
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
  allProducts = signal<Product[]>([]);

  // TABS: 'overview' | 'orders' | 'messages'
  activeTab = signal<string>('overview');
  orderSubTab = signal<'orders' | 'quotes'>('orders'); // Naya Sub-tab signal

  // States managed with high-efficiency Signals
  currentTimeframe = signal<string>('monthly');
  salesData = signal<any[]>([]);
  topProducts = signal<any[]>([]);
  productionLogs = signal<any[]>([]);
  isLoading = signal<boolean>(true);

  // NEW Inbox Signals
  orders = signal<any[]>([]);
  messages = signal<any[]>([]);
  loadError = signal<string>('');
  processingOrderId = signal<string | number | null>(null);

  // BULLETPROOF COMPUTED METRICS: Safely checks multiple possible DTO field names
 // Computed Metrics
  totalRevenue = computed(() => this.salesData().reduce((sum, item) => sum + (Number(item.totalRevenue || 0)), 0));
  totalProfit = computed(() => this.salesData().reduce((sum, item) => sum + (Number(item.totalProfit || item.profit || 0)), 0));
  totalOrdersCount = computed(() => this.salesData().reduce((sum, item) => sum + (Number(item.totalOrders || 0)), 0));
  totalBricksProduced = computed(() => this.productionLogs().reduce((sum, log) => sum + (Number(log.quantityProduced || 0)), 0));

 ngOnInit(): void {
    // 🚀 LAZY LOADING: Sirf Overview ka data load karo dashboard khulte hi
    this.loadDashboardMetrics();

    // Messages pehle hi load kar lenge taaki Inbox tab par Red Notification Dot dikh sake
    this.loadMessages();

    // NAYA: Inventory load karne ke liye
    this.loadProducts();
  }

  switchTab(tab: string) {
    this.activeTab.set(tab);
   // 🚀 CACHING: Data tabhi load hoga jab Array khali ho. Network calls bach jayengi!
    if (tab === 'orders' && this.orders().length === 0) {
      this.loadOrders();
    }
    if (tab === 'messages' && this.messages().length === 0) {
      this.loadMessages();
    }
    // NAYA: Inventory ke liye lazy caching
    if (tab === 'inventory' && this.allProducts().length === 0) {
      this.loadProducts();
    }
  }

  // NAYA: Order aur Quote list ko alag-alag filter karne ke liye Compute functions
  activeOrdersList = computed(() => this.orders().filter(o => o.requestType !== 'QUOTE LEAD'));
  quoteRequestsList = computed(() => this.orders().filter(o => o.requestType === 'QUOTE LEAD'));

  // NAYA: COD Approve karne ka method
  approveAsCOD(order: any) {
    if (confirm(`Approve this order as Cash on Delivery (COD)?\n\nOrder: ${order.orderId}`)) {
      this.updateOrderStatus(order.orderId, 'CONFIRMED_COD');
    }
  }

  // NAYA: Quote ko Order mein badalne ka method
  convertToOrder(order: any) {
    if (confirm(`Convert this Quote into a live Order?\n\nCustomer: ${order.customerName}`)) {
      this.updateOrderStatus(order.orderId, 'PENDING_PAYMENT');
    }
  }

  // NAYA METHOD: Modal close hone par loading state clear karne ke liye
  onUtrModalClose() {
    this.processingOrderId.set(null); // Button ko wapas enable karne ke liye
    this.loadOrders(); // List ko refresh karne ke liye
  }


  // --- 1. NEW: Chart Configuration ---
  public barChartType: ChartType = 'bar';
  public barChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: { y: { beginAtZero: true } },
    plugins: { legend: { display: false } } // Hide legend for cleaner look
  };
  public barChartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: [{ data: [], label: 'Revenue (₹)',backgroundColor: '#b91c1c', borderRadius: 6 }] // Red-700
  };


  // --- EXISTING LOADERS ---
  loadDashboardMetrics(): void {
    this.isLoading.set(true);
    const tf = this.currentTimeframe();

    // 1. Sales Analytics Load karo
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
      error: (err) => {
        console.error("Sales Analytics Error:", err);
        this.loadError.set('Failed to load sales data.');
        this.isLoading.set(false);
      }
    });

    // 2. 🚀 FIX: Top Products missing the, uski call yahan daal di hai
    this.dashboardService.getTopProducts(tf).subscribe({
      next: (res: any) => {
         const products = Array.isArray(res) ? res : (res.data || []);
         this.topProducts.set(products.slice(0, 5)); // Top 5 dikhayenge
      }
    });

    // 3. Production Logs Load karo
    this.dashboardService.getProductionLogs().subscribe({
      next: (res: any) => {
        const logs = Array.isArray(res) ? res : (res.data || []);

        // 🚀 FIX: Pehle pure data se 'Total Bricks Produced' nikal lo
        const total = logs.reduce((sum: number, log: any) => sum + (Number(log.quantityProduced || log.quantity || 0)), 0);
        this.totalBricksProduced.apply(() => total);

        // Phir sirf latest 7 logs ko sort karke dashboard UI ke liye set karo
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

  // --- NEW LOADERS & ACTIONS ---
  loadOrders() {
    this.dashboardService.getAllOrders().subscribe({
      next: (data: any) => {
        const orderList = Array.isArray(data) ? data : (data.data || data.content || []);
        // Service already normalizes all fields - just use as-is
        this.orders.set(orderList);
        this.loadError.set('');
        console.log('DEBUG: Orders Data Received:', orderList);
      },
      error: (err) => {
        console.error('Failed to load orders', err);
        this.orders.set([]);
        this.loadError.set('Failed to load order requests. Please refresh or try again later.');
      }
    });
  }

  // FIX: Require UTR verification for payment-pending orders
  // Prevent automatic processing
  requestUtrVerification(order: any) {
    if (order.status === 'QUOTE_REQUEST' || order.status === 'PENDING_PAYMENT') {
      this.processingOrderId.set(order.orderId);
      // Open UTR verification modal
      setTimeout(() => {
        if (this.utrModal) {
          this.utrModal.openModal(order.orderId, order.totalCost);
        }
      }, 0);
    }
  }

  // Alternative: Quick approve without UTR (for COD orders only)
  quickApproveOrder(order: any) {
    const confirmMsg = order.requestType === 'QUOTE_REQUEST'
      ? 'Convert this quote to a confirmed order?'
      : 'Approve and confirm this order for production?';

    if (confirm(confirmMsg + '\n\nOrder: ' + order.orderId + '\nAmount: ₹' + order.totalCost)) {
      this.updateOrderStatus(order.orderId, 'CONFIRMED_COD');
    }
  }

  updateOrderStatus(orderId: string | number, status: 'PENDING_PAYMENT' | 'CONFIRMED_COD' | 'CANCELLED' | 'DISPATCHED' | 'DELIVERED' | 'APPROVED' | 'REJECTED') {
    const resolvedStatus = status === 'APPROVED' ? 'CONFIRMED_COD' : status === 'REJECTED' ? 'CANCELLED' : status;

    this.dashboardService.updateOrderStatus(orderId, resolvedStatus as any).subscribe({
      next: () => {
        this.processingOrderId.set(null);
        this.loadOrders();
      },
      error: (err) => {
        console.error('Status update error:', err);
        this.processingOrderId.set(null); // FIX: Error aane par bhi button enable ho jaye
        alert('Failed to update order status: ' + (err?.error?.message || 'Unknown error'));
      }
    });
  }

  rejectOrder(order: any) {
    if (confirm(`Are you sure you want to reject this order?\n\nOrder: ${order.orderId}\nThis action cannot be undone.`)) {
      this.updateOrderStatus(order.orderId, 'CANCELLED');
    }
  }

  loadMessages() {
    this.dashboardService.getAllMessages().subscribe({
      next: (data) => {
        this.messages.set(data || []);
        this.loadError.set('');
      },
      error: (err) => {
        console.error('Failed to load messages', err);
        this.loadError.set('Failed to load messages. Please try again.');
      }
    });
  }

  markMessageAsRead(msg: any) {
    // Agar message UNREAD hai, tabhi API call karenge
    if (msg.status === 'UNREAD') {
      this.dashboardService.updateContactMessageStatus(msg.messageId, 'READ').subscribe({
        next: () => {
          msg.status = 'READ'; // UI instantly update hoga bina reload ke
        }
      });
    }
  }

  resolveMessage(msg: any) {
    if (confirm('Mark this inquiry as resolved and remove from active inbox?')) {
      this.dashboardService.updateContactMessageStatus(msg.messageId, 'RESOLVED').subscribe({
        next: () => {
          msg.status = 'RESOLVED';
        }
      });
    }
  }

// ==========================================
  // NAYE METHODS: INVENTORY & CATALOG KE LIYE
  // ==========================================

  loadProducts() {
    this.productService.getAllProducts().subscribe({
      next: (res: any) => {
        const prodArray = Array.isArray(res) ? res : (res.data || res.content || []);
        this.allProducts.set(prodArray);
      },
      error: (err) => console.error('Failed to load inventory', err)
    });
  }

  getImageSrc(product: Product): string {
    if (!product.imageData) return 'assets/images/placeholder.jpg';

    let base64String = '';
    if (typeof product.imageData === 'string') {
      if (product.imageData === 'null' || product.imageData.trim() === '') return 'assets/images/placeholder.jpg';
      base64String = product.imageData;
    } else if (Array.isArray(product.imageData) && isPlatformBrowser(this.platformId)) {
      try {
        base64String = btoa(String.fromCharCode(...(product.imageData as number[])));
      } catch (e) {
        return 'assets/images/placeholder.jpg';
      }
    }

    if (!base64String) return 'assets/images/placeholder.jpg';
    return base64String.startsWith('data:image') ? base64String : `data:${product.imageType || 'image/jpeg'};base64,${base64String}`;
  }

  deleteProduct(productId: string, productName: string) {
    if (confirm(`Are you sure you want to completely delete "${productName}" from the inventory?`)) {
      this.productService.deleteProduct(productId).subscribe({
        next: () => {
          alert(`${productName} successfully removed!`);
          this.loadProducts(); // Table automatically refresh ho jayegi
        },
        error: (err) => {
          console.error('Delete failed', err);
          alert('Failed to delete product. It may be linked to existing orders.');
        }
      });
    }
  }

  processRefund(order: any) {
    if (confirm(`Initiate official refund of ₹${order.totalAmount || order.totalCost} for Order: ${order.orderId}?`)) {
      this.dashboardService.initiateRefund(order.orderId).subscribe({
        next: (res: any) => {
          alert('✅ ' + res);
          this.loadOrders(); // Refresh table automatically
        },
        error: (err: any) => {
          alert('❌ Refund Failed: ' + (err.error || 'Payment may not be eligible.'));
        }
      });
    }
  }

}
