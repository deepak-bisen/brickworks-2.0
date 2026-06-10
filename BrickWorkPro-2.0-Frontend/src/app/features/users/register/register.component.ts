import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private notification = inject(NotificationService);

  isEmployeeMode = signal(false);

  regForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(4)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    fullName: ['', Validators.required],
    phoneNumber: ['', Validators.required],
    // Customer-specific
    customerType: ['INDIVIDUAL'],
    billingAddress: [''],
    // FIX: Added companyName and gstNumber — required by backend for BUSINESS customers
    companyName: [''],
    gstNumber: [''],
    // Employee-specific
    shiftTiming: ['MORNING'],
    employeeCode: [''],
  });

  toggleMode(isEmployee: boolean) {
    this.isEmployeeMode.set(isEmployee);
  }

  onSubmit() {
    if (this.regForm.invalid) return;
    const formValue = this.regForm.value;

    const baseData = {
      username: formValue.username!,
      email: formValue.email!,
      password: formValue.password!,
      fullName: formValue.fullName!,
      phoneNumber: formValue.phoneNumber!,
    };

    let request$;

    if (this.isEmployeeMode()) {
      const employeePayload = {
        ...baseData,
        shiftTiming: formValue.shiftTiming || 'MORNING',
        employeeCode: formValue.employeeCode || undefined,
      };
      request$ = this.authService.registerEmployee(employeePayload);
    } else {
      const customerType = (formValue.customerType || 'INDIVIDUAL') as 'INDIVIDUAL' | 'BUSINESS';
      const customerPayload: any = {
        ...baseData,
        customerType,
        billingAddress: formValue.billingAddress || '',
      };
      // FIX: Only send BUSINESS-specific fields when they have values
      if (customerType === 'BUSINESS') {
        if (!formValue.companyName?.trim()) {
          this.notification.warning('Business customers must provide a Company Name.');
          return;
        }
        customerPayload.companyName = formValue.companyName.trim();
        customerPayload.gstNumber = formValue.gstNumber || '';
      }
      request$ = this.authService.registerCustomer(customerPayload);
    }

    request$.subscribe({
      next: () => {
        this.notification.success('Registration successful! Please log in.');
        this.router.navigate(['/login']);
      },
      error: () => {},
    });
  }
}
