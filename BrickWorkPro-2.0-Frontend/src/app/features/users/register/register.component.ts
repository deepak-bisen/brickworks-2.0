import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  isEmployeeMode = signal(false); // Toggle Signal

// Updated form controls to match exact payload
  regForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(4)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    fullName: ['', Validators.required],
    phoneNumber: ['', Validators.required],
    // Customer specific
    customerType: ['INDIVIDUAL'],
    billingAddress: [''],
    // Employee specific
    shiftTiming: ['MORNING']
  });

 toggleMode(isEmployee: boolean) {
    this.isEmployeeMode.set(isEmployee);
    // Reset specific fields when toggling
    this.regForm.patchValue({ customerType: 'INDIVIDUAL', shiftTiming: 'MORNING' });
  }

  onSubmit() {
    if (this.regForm.valid) {
      const formValue = this.regForm.value;

      // 1. Extract the shared base data
      const baseData = {
        username: formValue.username!,
        email: formValue.email!,
        password: formValue.password!,
        fullName: formValue.fullName!,
        phoneNumber: formValue.phoneNumber!
      };

      // 2. Build the EXACT payload based on the mode
      let request$;

      if (this.isEmployeeMode()) {
        const employeePayload = { ...baseData, shiftTiming: formValue.shiftTiming! };
        console.log('Sending Employee Payload:', employeePayload);
        request$ = this.authService.registerEmployee(employeePayload as any);
      } else {
        const customerPayload = {
          ...baseData,
          customerType: formValue.customerType!,
          billingAddress: formValue.billingAddress!
        };
        console.log('Sending Customer Payload:', customerPayload);
        request$ = this.authService.registerCustomer(customerPayload as any);
      }

      // 3. Send the request
      request$.subscribe({
        next: () => {
          alert('Registration successful! Please login.');
          this.router.navigate(['/login']);
        },
        error: (err) => {
          console.error('Registration failed', err);
          alert('Registration failed. Check the console for details.');
        }
      });
    }
  }
}
