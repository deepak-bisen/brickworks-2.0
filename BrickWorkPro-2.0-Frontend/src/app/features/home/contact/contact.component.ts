import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './contact.component.html'
})
export class ContactComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  // Route to the new Users Service endpoint
  private apiUrl = `${environment.apiUrl}/api/users/contact`;

  isSubmitting = signal(false);
  successMessage = signal('');
  errorMessage = signal('');

  // FIX: Added mobileNumber with 10-digit validation
  contactForm = this.fb.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    mobileNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
    message: ['', [Validators.required, Validators.minLength(10)]]
  });

  onSubmit() {
    if (this.contactForm.invalid) {
      this.contactForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    this.http.post(this.apiUrl, this.contactForm.value).subscribe({
      next: () => {
        this.successMessage.set('Message sent successfully! We will get back to you soon.');
        this.contactForm.reset();
        this.isSubmitting.set(false);
      },
      error: (err) => {
        console.error('Contact error:', err);
        this.errorMessage.set('Failed to send message. Please try again later.');
        this.isSubmitting.set(false);
      }
    });
  }
}
