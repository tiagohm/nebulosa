import { HttpErrorResponse, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { catchError, throwError } from 'rxjs'

export class HttpError extends Error {

  constructor(public code: number, message: string, error: Error) {
    super(message)

    this.name = 'HttpError'
    this.message = message
    this.stack = error.stack
  }

  get system() {
    return this.code === 0
  }

  get client() {
    return this.code >= 400 && this.code < 500
  }

  get server() {
    return this.code >= 500
  }
}

@Injectable({ providedIn: 'root' })
export class HttpErrorInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    return next.handle(req)
      .pipe(catchError((e) => {
        if (e instanceof HttpErrorResponse) {
          e = new HttpError(e.status, 'Server error', e)
        } else {
          e = new HttpError(500, 'Server error', e)
        }

        return throwError(() => e)
      }))
  }
}
