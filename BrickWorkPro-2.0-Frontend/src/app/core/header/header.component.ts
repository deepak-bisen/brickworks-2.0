import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  // Injecting the service we created earlier
  authService = inject(AuthService);
  private router = inject(Router);

  // We use the signal from our service to drive the UI
  isLoggedIn = this.authService.isAuthenticated;

 onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
