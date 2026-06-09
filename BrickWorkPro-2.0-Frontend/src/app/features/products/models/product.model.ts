export interface Product {
  productId: string; // Matches backend exact key
  name: string;
  description: string;
  category: string;
  unitPrice: number; // Replaced 'price'
  stockQuantity: number;
  brickType: string;
  dimensions: string;
  estimatedCost: number;
  bulkDiscountThreshold: number;
  imageName?: string;
  imageType?: string;
  imageData?: string; // Note: imageData (byte[]) from backend is typically converted to a base64 string or an image URL by your service layer.
  // Note: imageData (byte[]) from backend is typically converted to a base64 string or an image URL by your service layer.
}
