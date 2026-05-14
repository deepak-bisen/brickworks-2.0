import { Routes } from '@angular/router';
import { LoginComponent } from './features/users/login/login.component';
import { ProductListComponent } from './features/products/product-list/product-list.component';
import { DashboardComponent } from './features/admin/dashboard/dashboard.component';
// Import your Home component here
 import { HomeComponent } from './features/home/home.component';

export const routes: Routes = [
  // 1. Default Route: Points to 'home' when the URL is empty
  { path: '', redirectTo: 'home', pathMatch: 'full' },

  // 2. Feature Routes
  { path: 'home', component: HomeComponent }, // Ensure this component exists
  { path: 'login', component: LoginComponent },
  { path: 'products', component: ProductListComponent },
  { path: 'admin-dashboard', component: DashboardComponent },
  // { path: 'contact', component: ContactComponent },

  // 3. Wildcard Route: Catches broken URLs and sends them to 'products'
  // This MUST be the last item in the array
  { path: '**', redirectTo: 'products' }
];
