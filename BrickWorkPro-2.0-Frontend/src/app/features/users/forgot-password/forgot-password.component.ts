import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ForgotPasswordComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);

  step: 'email' | 'otp' | 'reset' | 'done' = 'email';
  isSubmitting = false;
  successMessage = '';
  errorMessage = '';
  email = '';

  emailForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
  });

  otpForm = this.fb.group({
    otp: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
  });

  resetForm = this.fb.group(
    {
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: this.passwordsMatchValidator },
  );

  onSubmitEmail() {
    if (this.emailForm.invalid) return;

    this.isSubmitting = true;
    this.clearMessages();

    const email = this.emailForm.value.email!.trim();

    this.authService
      .forgotPassword(email)
      .pipe(finalize(() => {
        this.isSubmitting = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.email = email;
          this.step = 'otp';
          this.setSuccess('An OTP has been sent to your email. Please enter it below.');
        },
        error: (error) => this.setError(this.getErrorMessage(error)),
      });
  }

  onSubmitOtp() {
    if (this.otpForm.invalid) return;

    this.isSubmitting = true;
    this.clearMessages();

    const otp = this.otpForm.value.otp!.trim();

    this.authService
      .verifyOtp(this.email, otp)
      .pipe(finalize(() => {
        this.isSubmitting = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.step = 'reset';
          this.setSuccess('OTP verified successfully. Please create a new password.');
        },
        error: (error) => this.setError(this.getErrorMessage(error)),
      });
  }

  onSubmitReset() {
    if (this.resetForm.invalid) return;

    this.isSubmitting = true;
    this.clearMessages();

    const values = this.resetForm.value;

    this.authService
      .resetPassword({
        email: this.email,
        newPassword: values.newPassword!,
        confirmPassword: values.confirmPassword!,
      })
      .pipe(finalize(() => {
        this.isSubmitting = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => {
          this.step = 'done';
          this.setSuccess('Your password has been reset successfully. You can sign in now.');
        },
        error: (error) => this.setError(this.getErrorMessage(error)),
      });
  }

  resendOtp() {
    this.isSubmitting = true;
    this.clearMessages();

    this.authService
      .forgotPassword(this.email)
      .pipe(finalize(() => {
        this.isSubmitting = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: () => this.setSuccess('A new OTP has been sent to your email.'),
        error: (error) => this.setError(this.getErrorMessage(error)),
      });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }

  private passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
    const newPassword = control.get('newPassword')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;

    if (newPassword && confirmPassword && newPassword !== confirmPassword) {
      return { mismatch: true };
    }

    return null;
  }

  private setSuccess(message: string) {
    this.successMessage = message;
    this.errorMessage = '';
    this.cdr.markForCheck();
  }

  private setError(message: string) {
    this.errorMessage = message;
    this.successMessage = '';
    this.cdr.markForCheck();
  }

  private clearMessages() {
    this.successMessage = '';
    this.errorMessage = '';
    this.cdr.markForCheck();
  }

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { message?: string; error?: string } | string | null;

      // If server returned a JSON string (common when responseType is 'text'),
      // attempt to parse it and extract a friendly message.
      if (typeof body === 'string') {
        try {
          const parsed = JSON.parse(body);
          if (parsed?.message) return String(parsed.message);
          if (parsed?.error) return String(parsed.error);
          // Fallback: if parsed has nested 'errors' or 'detail', try those
          if (parsed?.errors) return String(parsed.errors);
          if (parsed?.detail) return String(parsed.detail);
        } catch {
          // not JSON, fall through to return raw string
        }
        return body || 'We could not process your request right now.';
      }

      if (body?.message) {
        return body.message;
      }
      if (body?.error) {
        return body.error;
      }

      // Fallback to generic HttpErrorResponse message
      return error.message || 'We could not process your request right now.';
    }

    return 'We could not process your request right now.';
  }
}
