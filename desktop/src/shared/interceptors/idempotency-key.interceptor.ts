import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { Observable } from 'rxjs'
import { uid } from '../utils/random'

@Injectable({ providedIn: 'root' })
export class IdempotencyKeyInterceptor implements HttpInterceptor {
	static readonly HEADER_KEY = 'X-Idempotency-Key'

	intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
		const idempotencyKey = uid()

		req = req.clone({
			headers: req.headers.set(IdempotencyKeyInterceptor.HEADER_KEY, idempotencyKey),
		})

		return next.handle(req)
	}
}
