import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { nuid } from 'nuid'
import { Observable } from 'rxjs'

@Injectable({ providedIn: 'root' })
export class IdempotencyKeyInterceptor implements HttpInterceptor {
	static readonly HEADER_KEY = 'X-Idempotency-Key'

	intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
		const idempotencyKey = nuid.next()

		req = req.clone({
			headers: req.headers.set(IdempotencyKeyInterceptor.HEADER_KEY, idempotencyKey),
		})

		return next.handle(req)
	}
}
