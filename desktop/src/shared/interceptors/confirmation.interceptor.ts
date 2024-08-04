import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { Observable, finalize } from 'rxjs'
import { ConfirmationService } from '../services/confirmation.service'
import { IdempotencyKeyInterceptor } from './idempotency-key.interceptor'

@Injectable({ providedIn: 'root' })
export class ConfirmationInterceptor implements HttpInterceptor {
	constructor(private readonly confirmation: ConfirmationService) {}

	intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
		const hasConfirmation = req.urlWithParams.includes('hasConfirmation')

		if (hasConfirmation) {
			const idempotencyKey = req.headers.get(IdempotencyKeyInterceptor.HEADER_KEY)

			if (idempotencyKey) {
				this.confirmation.register(idempotencyKey)
			}

			const res = next.handle(req)

			if (idempotencyKey) {
				return res.pipe(
					finalize(() => {
						this.confirmation.unregister(idempotencyKey)
					}),
				)
			}

			return res
		} else {
			return next.handle(req)
		}
	}
}
