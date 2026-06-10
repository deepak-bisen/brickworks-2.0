import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../products/services/product.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-production-stage-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './production-stage-management.component.html'
})
export class ProductionStageManagementComponent implements OnInit {
  activeLogs: any[] = [];
  completedLogs: any[] = [];
  isLoading = false;
  activeTab: 'active' | 'completed' = 'active'; // Tab toggle karne ke liye

  constructor(
    private productService: ProductService,
    private notification: NotificationService,
  ) {}

  ngOnInit(): void {
    this.loadProductionLogs();
  }

loadProductionLogs() {
    this.isLoading = true;
    this.productService.getProductionLogs().subscribe({
      next: (logs: any[]) => {

        // 1. ACTIVE LOGS: Saare pending items (Molded, In_Kiln) dikhayenge
        this.activeLogs = logs
          .filter((log: any) => log.stage !== 'BAKED')
          .map((log: any) => ({ ...log, isUpdating: false }));

        // NAYA LOGIC: Aaj ki Date (Midnight) set kar rahe hain
        const startOfToday = new Date();
        startOfToday.setHours(0, 0, 0, 0);

        // 2. COMPLETED LOGS: Sirf aaj (Today) ke BAKED logs dikhayenge
        this.completedLogs = logs
          .filter((log: any) => {
            if (log.stage !== 'BAKED') return false;

            // Backend se aane wali date check karo (kabhi 'createdAt' hota hai, kabhi 'productionDate')
            const logDateString = log.createdAt || log.productionDate;

            // Agar date string nahi hai toh dikha do (fallback), warna check karo ki date aaj ki hai ya nahi
            if (!logDateString) return true;

            const logDate = new Date(logDateString);
            return logDate >= startOfToday; // Condition: Sirf aaj ke records pass honge
          })
          // Latest logs ko upar dikhane ke liye sort kar rahe hain
          .sort((a: any, b: any) => new Date(b.createdAt || b.productionDate).getTime() - new Date(a.createdAt || a.productionDate).getTime());

        this.isLoading = false;
      },
      error: () => {
        this.notification.error('Failed to load production logs.');
        this.isLoading = false;
      },
    });
  }

  moveToNextStage(log: any) {
    let nextStage = '';

    if (log.stage === 'MOLDED') {
      nextStage = 'IN_KILN';
    } else if (log.stage === 'IN_KILN') {
      nextStage = 'BAKED';
    }

    if (!nextStage) return;

    // FIX 1: DTO structure se ID nikalne ka safe tareeka (kabhi kabhi 'id' hota hai, kabhi 'productionLogId')
    const recordId = log.id || log.productionLogId;

    if (!recordId) {
      console.error('Error: Log ID is missing!', log);
      return;
    }

    log.isUpdating = true;
    const payload = { stage: nextStage };

    // FIX 2: Sahi ID pass karna
    this.productService.updateProductionStage(recordId, payload).subscribe({
      next: () => {
        log.stage = nextStage;
        log.isUpdating = false;
        this.notification.success(`Stage updated to ${nextStage.replace('_', ' ')}.`);

        // Instant shift logic (agar tabbed UI banaya hai)
        if (nextStage === 'BAKED') {
          this.activeLogs = this.activeLogs.filter((l: any) => (l.id || l.productionLogId) !== recordId);
          this.completedLogs.unshift(log);
        }
      },
      error: () => {
        this.notification.error('Failed to update production stage.');
        log.isUpdating = false;
      },
    });
  }
}
