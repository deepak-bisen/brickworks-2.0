import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { SkeletonBlockComponent } from '../../../shared/components/skeleton-block/skeleton-block.component';

@Component({
  selector: 'app-customer-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, LoadingSpinnerComponent, SkeletonBlockComponent],
  templateUrl: './profile.component.html',
})
export class CustomerProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private notification = inject(NotificationService);

  isLoading = signal(true);
  isSaving = signal(false);
  loadError = signal('');

  profileForm = this.fb.group({
    fullName: ['', Validators.required],
    email: [{ value: '', disabled: true }],
    phone: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
    address: ['', Validators.required],
  });

  private username = '';

  ngOnInit() {
    const user = this.authService.getCurrentUser();
    this.username = user?.username ?? '';

    if (!this.username) {
      this.isLoading.set(false);
      this.loadError.set('Could not determine your account. Please log in again.');
      return;
    }

    this.authService.getProfile(this.username).subscribe({
      next: (profile) => {
        const phone = this.normalizePhoneForDisplay(profile.phoneNumber ?? '');
        this.profileForm.patchValue({
          fullName: profile.fullName ?? '',
          email: profile.email ?? '',
          phone,
          address: profile.address ?? '',
        });
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.loadError.set('Failed to load your profile. Please try again.');
      },
    });
  }

  onSubmit() {
    if (this.profileForm.invalid || !this.username) {
      this.profileForm.markAllAsTouched();
      return;
    }

    const { fullName, phone, address } = this.profileForm.getRawValue();
    this.isSaving.set(true);

    this.authService
      .updateProfile(this.username, {
        name: fullName,
        phone,
        address,
      })
      .subscribe({
        next: () => {
          this.isSaving.set(false);
          this.notification.success('Profile updated successfully.');
        },
        error: (err) => {
          this.isSaving.set(false);
          const msg =
            typeof err?.message === 'string'
              ? err.message
              : 'Failed to update profile. Please try again.';
          this.notification.error(msg);
        },
      });
  }

  private normalizePhoneForDisplay(phone: string): string {
    const digits = phone.replace(/\D/g, '');
    if (digits.length >= 10) {
      return digits.slice(-10);
    }
    return digits;
  }
}