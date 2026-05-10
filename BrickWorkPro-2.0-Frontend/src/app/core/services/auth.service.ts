import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // Signals are perfect for BrickWorks Pro's interactive dashboard 
  // as they instantly notify the UI when the login status changes.
  isLoggedIn = signal<boolean>(!!localStorage.getItem('adminToken'));

  constructor(private http: HttpClient) {}

  // This matches your backend AuthController login endpoint
  login(credentials: any): Observable<any> {
    return this.http.post<any>(`${environment.apiUrl}/api/auth/login`, credentials).pipe(
      tap(response => {
        if (response.jwt) {
          localStorage.setItem('adminToken', response.jwt);
          this.isLoggedIn.set(true);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('adminToken');
    this.isLoggedIn.set(false);
  }
}