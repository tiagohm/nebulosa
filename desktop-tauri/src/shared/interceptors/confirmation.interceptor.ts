import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { Observable } from 'rxjs'
import { ConfirmationService } from '../services/confirmation.service'
import { IdempotencyKeyInterceptor } from './idempotency-key.interceptor'

@Injectable({ providedIn: 'root' })
export class ConfirmationInterceptor implements HttpInterceptor {
	constructor(private readonly confirmationService: ConfirmationService) {}

	intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
		const hasConfirmation = req.urlWithParams.includes('hasConfirmation')

		if (hasConfirmation) {
			const idempotencyKey = req.headers.get(IdempotencyKeyInterceptor.HEADER_KEY)

			if (idempotencyKey) {
				this.confirmationService.register(idempotencyKey)
			}
		}

		return next.handle(req)
	}
}
