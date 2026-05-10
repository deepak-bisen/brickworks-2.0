import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
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

  // We use the signal from our service to drive the UI
  isLoggedIn = this.authService.isLoggedIn;

  logout() {
    this.authService.logout();
  }
}