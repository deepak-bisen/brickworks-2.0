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

  it('should request a password reset for the provided email', () => {
    service.forgotPassword('customer@example.com').subscribe();

    const req = httpMock.expectOne('http://localhost:9191/api/auth/forgot-password');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'customer@example.com' });
    req.flush({ message: 'Password reset request received' });
  });

  it('should verify an otp for the provided email', () => {
    service.verifyOtp('customer@example.com', '123456').subscribe();

    const req = httpMock.expectOne('http://localhost:9191/api/auth/verify-otp');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'customer@example.com', otp: '123456' });
    req.flush({ message: 'OTP verified successfully' });
  });

  it('should reset the password for the provided email', () => {
    service
      .resetPassword({
        email: 'customer@example.com',
        newPassword: 'newPassword123',
        confirmPassword: 'newPassword123',
      })
      .subscribe();

    const req = httpMock.expectOne('http://localhost:9191/api/auth/reset-password');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      email: 'customer@example.com',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123',
    });
    req.flush({ message: 'Password reset successfully' });
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
