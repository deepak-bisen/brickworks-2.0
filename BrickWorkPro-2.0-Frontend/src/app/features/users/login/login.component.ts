import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  username = '';
  password = '';

  private authService = inject(AuthService);
  private router = inject(Router);

 onSubmit() {
  this.authService.login({ username: this.username, password: this.password }).subscribe({
    next: (response) => {
      console.log('Login Successful', response);
      this.router.navigate(['/admin-dashboard']); // Only navigate on SUCCESS
    },
    error: (err) => {
      console.error('Login Failed', err);
      alert('Invalid Credentials. Please try again.');
    }
  });
}
}
