import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { AuthService } from './auth.service';

const CUSTOMER_TOKEN =
  'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.' +
  'eyJ1c2VySWQiOiJjdXN0b21lci0xIiwicm9sZSI6IlJPTEVfQ1VTVE9NRVIiLCJzdWIiOiJjdXN0b21lckBleGFtcGxlLmNvbSJ9.' +
  'signature';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should store token and user on login', () => {
    service.login({ username: 'customer', password: 'secret' }).subscribe();

    const req = httpMock.expectOne('http://localhost:9191/api/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({
      token: CUSTOMER_TOKEN,
      username: 'customer',
      role: 'ROLE_CUSTOMER',
    });

    expect(localStorage.getItem('brickworks_token')).toBe(CUSTOMER_TOKEN);
    expect(localStorage.getItem('user')).toContain('customer');
    expect(service.isAuthenticated()).toBe(true);
    expect(service.getRole()).toBe('CUSTOMER');
    expect(service.getUserId()).toBe('customer-1');
  });

  it('should clear token and user on logout', () => {
    localStorage.setItem('brickworks_token', CUSTOMER_TOKEN);
    localStorage.setItem('user', JSON.stringify({ username: 'customer', role: 'ROLE_CUSTOMER' }));

    service.logout();

    expect(localStorage.getItem('brickworks_token')).toBeNull();
    expect(localStorage.getItem('user')).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(service.isCustomer()).toBe(false);
  });

  it('should strip ROLE_ prefix from JWT role claim', () => {
    localStorage.setItem('brickworks_token', CUSTOMER_TOKEN);

    expect(service.getRole()).toBe('CUSTOMER');
    expect(service.getUserId()).toBe('customer-1');
  });

  it('should return current user from localStorage', () => {
    localStorage.setItem(
      'user',
      JSON.stringify({ username: 'customer', role: 'ROLE_CUSTOMER' }),
    );

    expect(service.getCurrentUser()).toEqual({
      username: 'customer',
      role: 'ROLE_CUSTOMER',
    });
  });
});