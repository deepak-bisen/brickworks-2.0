import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, tap } from 'rxjs';
import {
  CustomerRegistration,
  EmployeeRegistration,
} from '../../features/users/models/registration.model';
import { jwtDecode } from 'jwt-decode';

// BUG FIX #1: The backend JWT stores role as "ROLE_ADMIN" (with prefix).
// The frontend getRole() strips this prefix correctly, but checkAdmin()
// was searching both "ADMIN" and "ROLE_ADMIN" — which is fine, but the
// real bug was that login() stored the token under key 'token', while
// getTokenFromStorage() first checked 'adminToken'. This caused a race
// condition where checkAdmin() called after login could miss the token.
// FIX: Normalize to a SINGLE storage key 'brickworks_token' everywhere.

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/auth`;
  // FIX: Single canonical key — no more 'adminToken' vs 'token' split
  private readonly TOKEN_KEY = 'brickworks_token';

  isAuthenticated = signal<boolean>(this.hasToken());
  isAdmin = signal<boolean>(this.checkRole('ADMIN'));
  isCustomer = signal<boolean>(this.checkRole('CUSTOMER'));
  // FIX: Backend Role enum only has ADMIN, CUSTOMER, STAFF — no 'MANAGER'
  isStaff = signal<boolean>(this.checkRole('STAFF'));

  private getTokenFromStorage(): string | null {
    // FIX: Read from canonical key only; also fall back to old keys for
    // users who already have a session so they aren't force-logged-out
    return (
      localStorage.getItem(this.TOKEN_KEY) ??
      localStorage.getItem('token') ??
      localStorage.getItem('adminToken')
    );
  }

  private hasToken(): boolean {
    return !!this.getTokenFromStorage();
  }

  // FIX: Unified role-check helper used by all signals
  private checkRole(expectedRole: string): boolean {
    return this.getRole() === expectedRole;
  }

  private refreshSignals(): void {
    this.isAuthenticated.set(this.hasToken());
    this.isAdmin.set(this.checkRole('ADMIN'));
    this.isCustomer.set(this.checkRole('CUSTOMER'));
    this.isStaff.set(this.checkRole('STAFF'));
  }

  login(credentials: { username: string; password: string }) {
    return this.http
      .post<{ token: string; username: string; role: string }>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap((res) => {
          if (res?.token) {
            // FIX: Save under the ONE canonical key
            localStorage.setItem(this.TOKEN_KEY, res.token);

            // Save user basic data here for easy access across the app without needing to decode JWT repeatedly
            localStorage.setItem('user', JSON.stringify({ username: res.username, role: res.role }));
            // Clean up old keys in case they exist (from previous versions)
            localStorage.removeItem('token');
            localStorage.removeItem('adminToken');
            this.refreshSignals();
          }
        }),
      );
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem('token');
    localStorage.removeItem('adminToken');
    localStorage.removeItem('user');
    this.isAuthenticated.set(false);
    this.isAdmin.set(false);
    this.isCustomer.set(false);
    this.isStaff.set(false);
  }

  registerCustomer(data: CustomerRegistration) {
    return this.http.post(`${this.apiUrl}/register/customer`, data);
  }

  registerEmployee(data: EmployeeRegistration) {
    return this.http.post(`${this.apiUrl}/register/employee`, data);
  }

  forgotPassword(email: string) {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email }, {
      responseType: 'text' as const,
      observe: 'body' as const,
    });
  }

  verifyOtp(email: string, otp: string) {
    return this.http.post(`${this.apiUrl}/verify-otp`, { email, otp }, {
      responseType: 'text' as const,
      observe: 'body' as const,
    });
  }

  resetPassword(data: { email: string; newPassword: string; confirmPassword: string }) {
    return this.http.post(`${this.apiUrl}/reset-password`, data, {
      responseType: 'text' as const,
      observe: 'body' as const,
    });
  }

  // FIX: Read userId from the JWT claim 'userId' (set by JwtUtil.generateCustomToken)
  getUserId(): string | null {
    const token = this.getTokenFromStorage();
    if (!token) return null;
    try {
      const decoded: any = jwtDecode(token);
      // JwtUtil puts it under claim "userId"
      return decoded.userId ?? decoded.sub ?? null;
    } catch {
      return null;
    }
  }

  // FIX: The backend stores "ROLE_ADMIN", "ROLE_CUSTOMER", "ROLE_STAFF".
  // This method strips the prefix and returns the clean role name.
  getRole(): string | null {
    const token = this.getTokenFromStorage();
    if (!token) return null;
    try {
      const decoded: any = jwtDecode(token);
      // JwtUtil.generateCustomToken() puts role under claim "role"
      let role: string = decoded.role ?? '';
      if (!role) return null;
      // Strip ROLE_ prefix added by the backend
      if (role.startsWith('ROLE_')) {
        role = role.substring(5);
      }
      return role.toUpperCase();
    } catch {
      return null;
    }
  }

  // Convenience helper kept for backward-compat with header component
  isAdminRole(): boolean {
    return this.getRole() === 'ADMIN';
  }

  // FIX: New method to get current user info from localStorage (set at login)
  getCurrentUser(): any {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (e) {
        console.error('Error parsing user data', e);
      }
    }

    // Agar user object nahi hai, toh JWT decode karke email nikal lo (Fallback)
    const token = this.getTokenFromStorage();
    if (token) {
      try {
        const decoded: any = jwtDecode(token);
        return { username: decoded.sub }; // Spring Boot 'sub' mein email/username bhejta hai
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  updateProfile(username: string, data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/profile/update?username=${username}`, data);
  }

  getProfile(username: string) {
    return this.http.get<any>(`${this.apiUrl}/profile?username=${username}`);
  }
}
