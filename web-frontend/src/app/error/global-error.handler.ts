import { ErrorHandler, Injectable } from '@angular/core';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  handleError(error: any): void {
    const message = error?.message || String(error);
    console.error('[GLOBAL_ERROR]', message, error);
    (window as any).__LAST_GLOBAL_ERROR__ = { message, error, ts: Date.now() };
  }
}
