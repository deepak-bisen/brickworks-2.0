import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, tap } from 'rxjs';
import { CustomerRegistration, EmployeeRegistration } from '../../features/users/models/registration.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/auth`;

  // Signal to track if the user is currently logged in
 isAuthenticated = signal<boolean>(this.hasToken());

 // NEW: Track Admin Role
  isAdmin = signal<boolean>(this.checkAdmin());

  private hasToken(): boolean {
    // Check if the token exists in local storage
    return !!localStorage.getItem('adminToken'); // Ensure this key matches what your auth.interceptor.ts expects
  }

  // NEW: Decode token to check for ADMIN role
  private checkAdmin(): boolean {
    const token = localStorage.getItem('adminToken');
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const roles = payload.role || payload.roles || payload.authorities || '';
      return roles.includes('ADMIN') || roles.includes('ROLE_ADMIN');
    } catch (e) {
      return false;
    }
  }

  login(credentials: { username: string; password: string }) {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (response && response.token) {
          localStorage.setItem('adminToken', response.token);
          this.isAuthenticated.set(true);
          this.isAdmin.set(this.checkAdmin()); // Update admin status on login
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('adminToken');
    this.isAuthenticated.set(false);
    this.isAdmin.set(false); // Reset admin status
  }


//register customer and employee

  registerCustomer(data: CustomerRegistration) {
    return this.http.post(`${this.apiUrl}/register/customer`, data);
  }

  registerEmployee(data: EmployeeRegistration) {
    return this.http.post(`${this.apiUrl}/register/employee`, data);
  }

}
