import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http'
import { inject, Injectable } from '@angular/core'
import { catchError, Observable, throwError } from 'rxjs'
import { AngularService } from '../services/angular.service'
import { Severity } from '../types/angular.types'

export interface ErrorResponse {
	message: string
	type: Severity
}

@Injectable({ providedIn: 'root' })
export class ErrorInterceptor implements HttpInterceptor {
	private readonly angularService = inject(AngularService)

	intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
		return next.handle(req).pipe(
			catchError((e: HttpErrorResponse) => {
				if (e.status === 400) {
					const error = e.error as ErrorResponse
					this.angularService.message(error.message, error.type)
				}

				return throwError(() => e)
			}),
		)
	}
}
