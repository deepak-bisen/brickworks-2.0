import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CartService } from '../../features/orders/services/cart.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
})
export class HeaderComponent {
  authService = inject(AuthService);
  cartService = inject(CartService);
  private router = inject(Router);

  isMobileMenuOpen = signal(false);
  isStaffMenuOpen = signal(false);

  toggleMobileMenu() {
    this.isMobileMenuOpen.update((open) => !open);
  }

  closeMobileMenu() {
    this.isMobileMenuOpen.set(false);
    this.isStaffMenuOpen.set(false);
  }

  toggleStaffMenu() {
    this.isStaffMenuOpen.update((open) => !open);
  }

  logout() {
    this.closeMobileMenu();
    this.authService.logout();
    this.router.navigate(['/home']);
  }
}