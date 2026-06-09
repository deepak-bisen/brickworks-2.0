import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CartService } from '../../features/orders/services/cart.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.component.html'
})
export class HeaderComponent {
  authService = inject(AuthService);
  cartService = inject(CartService);
  router = inject(Router);

  constructor() {
  // DEBUG: Open your browser console (F12).
  // If this prints 'null', your JWT decoding is failing.
  console.log('Current User Role:', this.authService.getRole());
}

  logout() {
    this.authService.logout();
    this.router.navigate(['/home']);
  }
}
