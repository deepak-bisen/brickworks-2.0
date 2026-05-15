import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, tap } from 'rxjs';
import { CustomerRegistration, EmployeeRegistration } from '../../features/users/models/registration.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  // Use the signal to drive the state
  isLoggedIn = signal<boolean>(!!localStorage.getItem('adminToken'));

  constructor(private http: HttpClient) {}

  login(credentials: any): Observable<any> {
    return this.http.post<any>(`${environment.apiUrl}/api/auth/login`, credentials).pipe(
      tap(response => {
        if (response.jwt) {
          localStorage.setItem('adminToken', response.jwt);
          // Manually update the signal so the UI and Interceptor react instantly
          this.isLoggedIn.set(true);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('adminToken');
    this.isLoggedIn.set(false);
  }


//register customer
private apiUrl = `${environment.apiUrl}/api/auth`;

  registerCustomer(data: CustomerRegistration) {
    return this.http.post(`${this.apiUrl}/register/customer`, data);
  }

  registerEmployee(data: EmployeeRegistration) {
    return this.http.post(`${this.apiUrl}/register/employee`, data);
  }

}
