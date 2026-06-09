import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ProductService } from '../../products/services/product.service';

@Component({
  selector: 'app-staff-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html'
})
export class StaffDashboardComponent implements OnInit {
  private productService = inject(ProductService);

  productionStats = signal({ dailyProduction: 0, lastUpdated: '' });
  productionLogs = signal<any[]>([]);
  products = signal<any[]>([]);
  isLoading = signal(true);
  errorMessage = signal('');

  recentLogs = computed(() =>
    this.productionLogs()
      // .slice(0, 5)
      .map((log) => ({
        ...log,
        productName: log.productName || this.productNameMap()[log.productId] || null,
        managerName: log.managerName || log.managerId || null,
      }))
  );
  logsCount = computed(() => this.productionLogs().length);
  productNameMap = computed(() =>
    this.products().reduce((map, product) => {
      map[product.productId] = product.name;
      return map;
    }, {} as Record<string, string>)
  );

  ngOnInit() {
    this.loadStaffDashboard();
  }

  loadStaffDashboard() {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.productService.getTodayProductionStats().subscribe({
      next: (data: any) => {
        this.productionStats.set({
          dailyProduction: Number(data?.dailyProduction ?? 0),
          lastUpdated: data?.lastUpdated || new Date().toISOString(),
        });
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load production stats', err);
        this.errorMessage.set('Unable to load production metrics.');
        this.isLoading.set(false);
      }
    });

    this.productService.getAllProducts().subscribe({
      next: (products: any[]) => {
        this.products.set(Array.isArray(products) ? products : []);
      },
      error: (err) => {
        console.error('Failed to load product names for logs', err);
      }
    });

    this.productService.getProductionLogs().subscribe({
      next: (logs: any[]) => {
        this.productionLogs.set(Array.isArray(logs) ? logs : []);
      },
      error: (err) => {
        console.error('Failed to load production logs', err);
      }
    });
  }
}
