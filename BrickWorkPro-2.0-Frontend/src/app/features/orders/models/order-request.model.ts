// FIX: OrderResponseDTO from the backend returns these fields.
// Added OrderResponse interface so typed subscriptions work correctly
// instead of using 'any' everywhere in the customer orders component.

export interface OrderItemRequest {
  productId: string;
  quantity: number;
}

export interface OrderRequest {
  // FIX: customerId is optional — public-quote doesn't require it,
  // but createOrder can attach it when a customer is logged in.
  customerId?: string;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  deliveryAddress: string;
  items: OrderItemRequest[];
}

export interface OrderItemResponse {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
}

// Matches backend OrderResponseDTO
export interface OrderResponse {
  orderId: string;
  customerId: string | null;
  status: string;
  createdAt: string;
  customerName?: string;
  customerEmail?: string;
  customerPhone?: string;
  deliveryAddress?: string;
  totalAmount: number | null;
  grossAmount?: number | null;
  discountApplied: number | null;
  netProfit: number | null;
  items?: OrderItemResponse[];
  requestType?: string;
  totalCost?: number;
}
