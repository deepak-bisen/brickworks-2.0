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

// Matches backend OrderResponseDTO exactly
export interface OrderResponse {
  orderId: string;
  customerId: string | null;
  status: string;
  createdAt: string;
  totalAmount: number | null;
  discountApplied: number | null;
  netProfit: number | null;
}
