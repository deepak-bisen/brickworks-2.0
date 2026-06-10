import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  authService = inject(AuthService);
  private router = inject(Router);

  loginForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  onSubmit() {
    if (this.loginForm.invalid) return;

    const credentials = {
      username: this.loginForm.value.username!.trim(),
      password: this.loginForm.value.password!,
    };

    this.authService.login(credentials).subscribe({
      next: () => {
        // FIX: authService.login() now saves the token and refreshes signals
        // before the tap completes, so getRole() is accurate here.
        const role = this.authService.getRole();

        if (role === 'ADMIN') {
          this.router.navigate(['/admin-dashboard']);
        } else if (role === 'CUSTOMER') {
          this.router.navigate(['/customer/dashboard']);
        } else if (role === 'STAFF') {
          this.router.navigate(['/staff/dashboard']);
        } else {
          console.warn('Unknown role after login:', role);
          this.router.navigate(['/']);
        }

        
      },
      error: () => {
        // Error toast handled by error interceptor (login 401 is excluded from auto-logout)
      },
    });
  }
}
