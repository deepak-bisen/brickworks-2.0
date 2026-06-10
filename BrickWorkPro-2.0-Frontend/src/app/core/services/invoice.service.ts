import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, switchMap, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface InvoiceErrorDetails {
  message: string;
  notFound: boolean;
}

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private http = inject(HttpClient);
  private invoiceUrl = `${environment.apiUrl}/api/finance/invoice`;

  downloadInvoice(orderId: string): Observable<Blob> {
    return this.http.get(`${this.invoiceUrl}/download/${orderId}`, {
      responseType: 'blob',
    });
  }

  generateInvoice(orderId: string): Observable<string> {
    return this.http.post(
      `${this.invoiceUrl}/generate/${orderId}`,
      {},
      { responseType: 'text' },
    );
  }

  saveBlobAsPdf(blob: Blob, orderId: string, filenamePrefix = 'BrickWork_Invoice'): void {
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `${filenamePrefix}_${orderId}.pdf`;
    anchor.click();
    window.URL.revokeObjectURL(url);
  }

  resolveDownloadError(err: unknown): Promise<InvoiceErrorDetails> {
    const httpError = err as { status?: number; error?: unknown };
    const maybeBlob = httpError?.error;

    if (maybeBlob instanceof Blob && maybeBlob.type === 'application/json') {
      return maybeBlob.text().then((text) => {
        try {
          const json = JSON.parse(text) as { error?: string };
          const notFound = httpError.status === 404;
          return {
            message: notFound
              ? 'Invoice not generated yet. You can generate it now.'
              : json.error || 'Invoice download failed.',
            notFound,
          };
        } catch {
          return { message: 'Invoice download failed.', notFound: false };
        }
      });
    }

    return Promise.resolve({
      message: 'Could not download invoice. Please try again.',
      notFound: httpError?.status === 404,
    });
  }

  downloadAndSave(
    orderId: string,
    filenamePrefix = 'BrickWork_Invoice',
  ): Observable<void> {
    return this.downloadInvoice(orderId).pipe(
      tap((blob) => this.saveBlobAsPdf(blob, orderId, filenamePrefix)),
      map(() => undefined),
    );
  }

  generateAndDownload(
    orderId: string,
    filenamePrefix = 'BrickWork_Invoice',
  ): Observable<void> {
    return this.generateInvoice(orderId).pipe(
      switchMap(() => this.downloadAndSave(orderId, filenamePrefix)),
    );
  }
}