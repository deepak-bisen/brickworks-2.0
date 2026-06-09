import { Routes } from '@angular/router';
import { LoginComponent } from './features/users/login/login.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { DashboardComponent } from './features/admin/dashboard/dashboard.component';
import { HomeComponent } from './features/home/home.component';
import { QuoteRequestComponent } from './features/orders/quote-request/quote-request.component';
import { RegisterComponent } from './features/users/register/register.component';
import { ProductManagerComponent } from './features/admin/product-manager/product-manager.component';
import { ContactComponent } from './features/home/contact/contact.component';
import { CheckoutComponent } from './features/orders/checkout/checkout.component';
import { CustomerDashboardComponent } from './features/customer/dashboard/dashboard.component';
import { CustomerOrdersComponent } from './features/customer/orders/orders.component';
import { StaffDashboardComponent } from './features/staff/dashboard/dashboard.component';
import { ProductionLogComponent } from './features/staff/production-log/production-log.component';
import { RawMaterialsComponent } from './features/staff/raw-materials/raw-materials.component';
// FIX: Import and APPLY the guards that were declared but never used
import { authGuard, adminGuard, customerGuard, staffGuard } from './core/gaurds/auth.guard';
import { ProductionStageManagementComponent } from './features/staff/production-stage-management/production-stage-management.component';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },

  // Public routes
  { path: 'home', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'products', component: ProductListComponent },
  { path: 'get-quote', component: QuoteRequestComponent },
  { path: 'contact', component: ContactComponent },

  {
    path: 'calculator',
    loadComponent: () =>
      import('./features/orders/brick-calculator/brick-calculator.component').then(
        (m) => m.BrickCalculatorComponent
      ),
  },

  // Checkout is public so customers can order without registering.
  // Login/register remains optional for a better guest-shopping experience.
  {
    path: 'checkout',
    component: CheckoutComponent,
  },

  // FIX: Admin routes now protected with adminGuard
  {
    path: 'admin-dashboard',
    component: DashboardComponent,
    canActivate: [adminGuard],
  },
  {
    path: 'admin/products',
    component: ProductManagerComponent,
    canActivate: [adminGuard],
  },
  {
    path: 'admin/products/edit/:id',
    component: ProductManagerComponent,
    canActivate: [adminGuard],
  },

  // FIX: Customer routes now protected with customerGuard
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

  // FIX: Staff routes now protected with staffGuard
  {
    path: 'staff/dashboard',
    component: StaffDashboardComponent,
    canActivate: [staffGuard],
  },
  {
    path: 'staff/production',
    component: ProductionLogComponent,
    canActivate: [staffGuard],
  },
  {
    path: 'staff/raw-materials',
    component: RawMaterialsComponent,
    canActivate: [staffGuard],
  },
{
  path: 'production-stage-management',
  component: ProductionStageManagementComponent,
  canActivate: [staffGuard],
},

{ path: 'track-order', loadComponent: () => import('./features/orders/track-order/track-order.component').then(m => m.TrackOrderComponent) },
  { path: '**', redirectTo: 'home' },
];
