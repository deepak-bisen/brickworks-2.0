import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  onSubmit() {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value as any).subscribe({
        next: (res) => {
          alert('Login successful!');

          try {
            const token = localStorage.getItem('adminToken');
            if (token) {
              // Decode the JWT payload to find the user's role
              const payload = JSON.parse(atob(token.split('.')[1]));
              const roles = payload.role || payload.roles || payload.authorities || '';

              // If user is admin, send them to the dashboard
              if (roles.includes('ADMIN') || roles.includes('ROLE_ADMIN')) {
                this.router.navigate(['/admin-dashboard']);
                return;
              }
            }
          } catch (e) {
            console.error('Error parsing token', e);
          }

          // Default redirect for Customers/Employees
          this.router.navigate(['/home']);
        },
        error: (err) => {
          console.error('Login failed', err);
          alert('Invalid username or password. Please try again.');
        }
      });
    }
  }
}
