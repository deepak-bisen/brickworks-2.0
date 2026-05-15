export interface OrderItemRequest {
  productId: string;
  quantity: number;
}

export interface OrderRequest {
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  deliveryAddress: string;
  items: OrderItemRequest[];
}
