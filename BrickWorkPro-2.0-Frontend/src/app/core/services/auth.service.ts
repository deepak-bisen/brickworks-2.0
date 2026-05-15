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

  private hasToken(): boolean {
    // Check if the token exists in local storage
    return !!localStorage.getItem('adminToken'); // Ensure this key matches what your auth.interceptor.ts expects
  }

  login(credentials: { username: string; password: string }) {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        // Assuming your backend JwtResponseDTO returns the token in a 'token' field
        if (response && response.token) {
          localStorage.setItem('adminToken', response.token);
          this.isAuthenticated.set(true);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('adminToken');
    this.isAuthenticated.set(false);
  }


//register customer and employee


  registerCustomer(data: CustomerRegistration) {
    return this.http.post(`${this.apiUrl}/register/customer`, data);
  }

  registerEmployee(data: EmployeeRegistration) {
    return this.http.post(`${this.apiUrl}/register/employee`, data);
  }

}
