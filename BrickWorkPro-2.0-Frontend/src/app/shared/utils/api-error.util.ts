import { HttpErrorResponse } from '@angular/common/http';

/** Extract a user-facing message from a backend error response. */
export function extractApiErrorMessage(error: unknown): string | null {
  if (!error || typeof error !== 'object') {
    return null;
  }

  const httpError = error as HttpErrorResponse;
  const body = httpError.error ?? (error as { message?: string }).message;

  if (!body) {
    return null;
  }

  if (typeof body === 'string') {
    return stripUnexpectedPrefix(body);
  }

  if (typeof body === 'object') {
    const record = body as Record<string, unknown>;
    if (typeof record['error'] === 'string') {
      return stripUnexpectedPrefix(record['error']);
    }
    if (typeof record['message'] === 'string') {
      return stripUnexpectedPrefix(record['message']);
    }

    const fieldMessages = Object.values(record).filter((v) => typeof v === 'string') as string[];
    if (fieldMessages.length > 0) {
      return fieldMessages.join(' | ');
    }
  }

  return null;
}

function stripUnexpectedPrefix(message: string): string {
  return message.replace(/^An unexpected error:\s*/i, '').trim();
}