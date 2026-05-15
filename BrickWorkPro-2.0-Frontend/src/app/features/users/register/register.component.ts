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

  regForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(4)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    fullName: ['', Validators.required],
    phoneNumber: ['', Validators.required],
    // Customer specific
    address: [''],
    customerType: ['INDIVIDUAL'],
    // Employee specific
    employeeCode: [''],
    designation: ['']
  });

  toggleMode(isEmployee: boolean) {
    this.isEmployeeMode.set(isEmployee);
    this.regForm.reset({ customerType: 'INDIVIDUAL' });
  }

  onSubmit() {
    if (this.regForm.valid) {
      const data = this.regForm.value as any;
      const request = this.isEmployeeMode()
        ? this.authService.registerEmployee(data)
        : this.authService.registerCustomer(data);

      request.subscribe({
        next: () => {
          alert('Registration successful! Please login.');
          this.router.navigate(['/login']);
        },
        error: (err) => console.error('Registration failed', err)
      });
    }
  }
}
