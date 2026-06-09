// FIX: SalesAnalyticsProjection interface in backend returns:
//   getPeriod() -> 'period'
//   getTotalRevenue() -> 'totalRevenue'
//   getTotalProfit() -> 'totalProfit'  <-- was missing from frontend model
//
// TopProductResponseDTO returns:
//   period, productId, productName, totalQuantitySold
//   NOTE: 'totalRevenueGenerated' does NOT exist in backend TopProductResponseDTO!
//   AdminDashboardService was mapping a non-existent field.

export interface SalesAnalytics {
  period: string;
  totalRevenue: number;
  // FIX: Added missing profit field from backend projection
  totalProfit: number;
  // Legacy alias kept for backward compat in templates
  totalOrders?: number;
}

export interface TopProductResponse {
  period: string;
  productId: string;
  productName: string;
  totalQuantitySold: number;
  // NOTE: totalRevenueGenerated is NOT in the backend DTO — removed to avoid confusion
}

// FIX: ProductionLogDTO from backend (products-brickwork) has:
//   id, managerId, productId, stage, quantity, createdAt
// NOT: logId, quantityProduced, shiftType, productionDate
// The old model used staff-invented field names that never existed.
export interface ProductionLog {
  id: string;
  managerId: string;
  productId: string;
  stage: string;          // e.g. "MORNING" — used as shift indicator
  quantity: number;       // FIX: backend field is 'quantity', not 'quantityProduced'
  createdAt: string;      // FIX: backend field is 'createdAt', not 'productionDate'
}
