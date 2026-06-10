/** Canonical ordering copy — use everywhere for consistent UX. */
export const ORDER_POLICY = {
  onlineMinUnits: 500,
  shortOnline: 'Order online: 500+ units per product',
  shortQuote: 'Custom bulk quote: any volume inquiry',
  full: 'Order online: 500+ units per product · Custom bulk quote: any volume inquiry',
  cartHint: 'Minimum 500 units per product for direct orders.',
  quoteCta: 'Get a bulk quote',
  bulkDiscountAd: '2% bulk discount on qualifying order volumes',
} as const;