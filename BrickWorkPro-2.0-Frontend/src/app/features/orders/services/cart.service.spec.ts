import { TestBed } from '@angular/core/testing';
import { CartService } from './cart.service';
import { Product } from '../../products/models/product.model';

const sampleProduct = (id: string, price = 10, threshold = 1000): Product => ({
  productId: id,
  name: `Brick ${id}`,
  description: 'Test brick',
  category: 'Standard',
  unitPrice: price,
  stockQuantity: 5000,
  brickType: 'Clay',
  dimensions: '9x4x3',
  estimatedCost: price * 0.7,
  bulkDiscountThreshold: threshold,
});

describe('CartService', () => {
  let service: CartService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(CartService);
    service.clearCart();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should add a product with default quantity of 500', () => {
    const product = sampleProduct('p1');
    service.addToCart(product);

    expect(service.items()).toEqual([{ product, quantity: 500 }]);
    expect(service.totalItems()).toBe(500);
  });

  it('should increment quantity when adding the same product again', () => {
    const product = sampleProduct('p1');
    service.addToCart(product, 500);
    service.addToCart(product, 500);

    expect(service.items()[0].quantity).toBe(1000);
  });

  it('should remove items from the cart', () => {
    const product = sampleProduct('p1');
    service.addToCart(product);
    service.removeFromCart('p1');

    expect(service.items()).toEqual([]);
    expect(service.totalItems()).toBe(0);
  });

  it('should enforce a minimum quantity of 500 when decreasing', () => {
    const product = sampleProduct('p1');
    service.addToCart(product, 600);
    service.decreaseQuantity('p1', 200);

    expect(service.items()[0].quantity).toBe(500);
  });

  it('should enforce a minimum quantity of 500 when updating quantity', () => {
    const product = sampleProduct('p1');
    service.addToCart(product);
    service.updateQuantity('p1', 200);

    expect(service.items()[0].quantity).toBe(500);
  });

  it('should apply bulk discount when threshold is met', () => {
    const product = sampleProduct('p1', 100, 500);
    service.addToCart(product, 500);

    expect(service.grossTotal()).toBe(50000);
    expect(service.totalDiscount()).toBe(1000);
    expect(service.cartTotal()).toBe(49000);
  });

  it('should persist cart state to localStorage', () => {
    const product = sampleProduct('p1');
    service.addToCart(product, 500);

    const saved = JSON.parse(localStorage.getItem('brickworks_user_cart') ?? '[]');
    expect(saved).toHaveLength(1);
    expect(saved[0].quantity).toBe(500);
  });
});