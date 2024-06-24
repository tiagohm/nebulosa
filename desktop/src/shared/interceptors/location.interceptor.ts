import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { Observable } from 'rxjs'
import { PreferenceService } from '../services/preference.service'

@Injectable({ providedIn: 'root' })
export class LocationInterceptor implements HttpInterceptor {
	constructor(private readonly preference: PreferenceService) {}

	intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
		if (req.urlWithParams.includes('hasLocation')) {
			const selectedLocation = this.preference.selectedLocation.get()

			req = req.clone({
				headers: req.headers.set('X-Location', JSON.stringify(selectedLocation)),
			})
		}

		return next.handle(req)
	}
}
