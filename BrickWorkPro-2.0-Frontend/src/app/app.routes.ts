import { Routes } from '@angular/router';
import { LoginComponent } from './features/users/login/login.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { HomeComponent } from './features/home/home.component';
import { QuoteRequestComponent } from './features/orders/quote-request/quote-request.component';
import { RegisterComponent } from './features/users/register/register.component';
import { ForgotPasswordComponent } from './features/users/forgot-password/forgot-password.component';
import { ContactComponent } from './features/home/contact/contact.component';
import { CheckoutComponent } from './features/orders/checkout/checkout.component';
import { CartComponent } from './features/orders/cart/cart.component';
import { OrderConfirmationComponent } from './features/orders/order-confirmation/order-confirmation.component';
import { OrderDetailComponent } from './features/customer/orders/order-detail/order-detail.component';
import { CustomerDashboardComponent } from './features/customer/dashboard/dashboard.component';
import { CustomerOrdersComponent } from './features/customer/orders/orders.component';
import { CustomerProfileComponent } from './features/customer/profile/profile.component';
import { adminGuard, customerGuard, staffGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },

  // Public routes
  { path: 'home', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'products', component: ProductListComponent },
  { path: 'get-quote', component: QuoteRequestComponent },
  { path: 'contact', component: ContactComponent },

  {
    path: 'calculator',
    loadComponent: () =>
      import('./features/orders/brick-calculator/brick-calculator.component').then(
        (m) => m.BrickCalculatorComponent,
      ),
  },

  {
    path: 'cart',
    component: CartComponent,
  },
  {
    path: 'checkout',
    component: CheckoutComponent,
  },
  {
    path: 'order-confirmation',
    component: OrderConfirmationComponent,
  },

  {
    path: 'admin-dashboard',
    loadComponent: () =>
      import('./features/admin/dashboard/dashboard.component').then(
        (m) => m.DashboardComponent,
      ),
    canActivate: [adminGuard],
  },
  {
    path: 'admin/products',
    loadComponent: () =>
      import('./features/admin/product-manager/product-manager.component').then(
        (m) => m.ProductManagerComponent,
      ),
    canActivate: [adminGuard],
  },
  {
    path: 'admin/products/edit/:id',
    loadComponent: () =>
      import('./features/admin/product-manager/product-manager.component').then(
        (m) => m.ProductManagerComponent,
      ),
    canActivate: [adminGuard],
  },

  {
    path: 'customer/dashboard',
    component: CustomerDashboardComponent,
    canActivate: [customerGuard],
  },
  {
    path: 'customer/orders',
    component: CustomerOrdersComponent,
    canActivate: [customerGuard],
  },
  {
    path: 'customer/orders/:orderId',
    component: OrderDetailComponent,
    canActivate: [customerGuard],
  },
  {
    path: 'customer/profile',
    component: CustomerProfileComponent,
    canActivate: [customerGuard],
  },

  {
    path: 'staff/dashboard',
    loadComponent: () =>
      import('./features/staff/dashboard/dashboard.component').then(
        (m) => m.StaffDashboardComponent,
      ),
    canActivate: [staffGuard],
  },
  {
    path: 'staff/production',
    loadComponent: () =>
      import('./features/staff/production-log/production-log.component').then(
        (m) => m.ProductionLogComponent,
      ),
    canActivate: [staffGuard],
  },
  {
    path: 'staff/raw-materials',
    loadComponent: () =>
      import('./features/staff/raw-materials/raw-materials.component').then(
        (m) => m.RawMaterialsComponent,
      ),
    canActivate: [staffGuard],
  },
  {
    path: 'staff/production-stage-management',
    loadComponent: () =>
      import('./features/staff/production-stage-management/production-stage-management.component').then(
        (m) => m.ProductionStageManagementComponent,
      ),
    canActivate: [staffGuard],
  },

  {
    path: 'track-order',
    loadComponent: () =>
      import('./features/orders/track-order/track-order.component').then(
        (m) => m.TrackOrderComponent,
      ),
  },
  { path: '**', redirectTo: 'home' },
];
