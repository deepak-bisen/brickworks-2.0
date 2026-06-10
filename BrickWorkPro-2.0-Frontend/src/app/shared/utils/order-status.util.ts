export const ORDER_STATUS_LABELS: Record<string, string> = {
  PENDING_PAYMENT: 'Awaiting Payment',
  PAYMENT_RECEIVED: 'Payment Received',
  CONFIRMED_COD: 'COD Confirmed',
  IN_PRODUCTION: 'In Production',
  DISPATCHED: 'Dispatched',
  DELIVERED: 'Delivered',
  CANCELLED: 'Cancelled',
  QUOTE_REQUEST: 'Quote Requested',
};

export function formatOrderStatus(status: string | null | undefined): string {
  if (!status) return 'Unknown';
  return ORDER_STATUS_LABELS[status] ?? status.replace(/_/g, ' ');
}

export function orderStatusBadgeClasses(status: string): string {
  const base = 'px-3 py-1.5 rounded-md text-[10px] font-black tracking-widest uppercase border';
  switch (status) {
    case 'PENDING_PAYMENT':
      return `${base} bg-amber-50 text-amber-800 border-amber-200`;
    case 'CONFIRMED_COD':
    case 'PAYMENT_RECEIVED':
    case 'IN_PRODUCTION':
      return `${base} bg-blue-50 text-blue-800 border-blue-200`;
    case 'DISPATCHED':
      return `${base} bg-indigo-50 text-indigo-800 border-indigo-200`;
    case 'DELIVERED':
      return `${base} bg-green-50 text-green-800 border-green-200`;
    case 'CANCELLED':
      return `${base} bg-red-50 text-red-800 border-red-200`;
    case 'QUOTE_REQUEST':
      return `${base} bg-purple-50 text-purple-800 border-purple-200`;
    default:
      return `${base} bg-gray-50 text-gray-700 border-gray-200`;
  }
}

export interface PaymentDetails {
  paymentMethod?: string;
  paymentStatus?: string;
  utrNumber?: string;
}

export interface PaymentBadgeView {
  label: string;
  classes: string;
}

export function getPaymentBadge(
  payment: PaymentDetails | null | undefined,
  orderStatus?: string
): PaymentBadgeView {
  const base = 'text-[10px] font-extrabold px-2 py-1 rounded-md border shadow-sm inline-block';

  if (!payment?.paymentMethod) {
    if (orderStatus === 'PENDING_PAYMENT') {
      return { label: 'Payment Not Started', classes: `${base} bg-gray-50 text-gray-600 border-gray-200` };
    }
    return { label: '—', classes: `${base} bg-gray-50 text-gray-500 border-gray-200` };
  }

  const method = payment.paymentMethod;
  const status = payment.paymentStatus;

  if (method === 'CASH_ON_DELIVERY') {
    if (status === 'SUCCESS') {
      return { label: 'COD — Collected', classes: `${base} bg-green-50 text-green-700 border-green-200` };
    }
    return { label: 'Cash on Delivery', classes: `${base} bg-orange-50 text-orange-700 border-orange-200` };
  }

  if (method === 'BANK_TRANSFER') {
    if (status === 'PENDING') {
      return { label: 'UTR — Pending Verification', classes: `${base} bg-amber-50 text-amber-800 border-amber-200` };
    }
    if (status === 'SUCCESS') {
      return { label: 'Bank Transfer — Verified', classes: `${base} bg-green-50 text-green-700 border-green-200` };
    }
    if (status === 'REJECTED') {
      return { label: 'UTR — Rejected', classes: `${base} bg-red-50 text-red-700 border-red-200` };
    }
    return { label: 'Bank Transfer', classes: `${base} bg-blue-50 text-blue-700 border-blue-200` };
  }

  if (method === 'ONLINE') {
    if (status === 'SUCCESS') {
      return { label: 'Paid via UPI / Card', classes: `${base} bg-green-50 text-green-700 border-green-200` };
    }
    return { label: 'UPI / Card', classes: `${base} bg-blue-50 text-blue-700 border-blue-200` };
  }

  return { label: method.replace(/_/g, ' '), classes: `${base} bg-gray-50 text-gray-700 border-gray-200` };
}