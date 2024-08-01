import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { Observable } from 'rxjs'
import { PreferenceService } from '../services/preference.service'

@Injectable({ providedIn: 'root' })
export class LocationInterceptor implements HttpInterceptor {
	static readonly HEADER_KEY = 'X-Location'

	constructor(private readonly preference: PreferenceService) {}

	intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
		if (req.urlWithParams.includes('hasLocation')) {
			const { location, locations } = this.preference.settings.get()

			req = req.clone({
				headers: req.headers.set(LocationInterceptor.HEADER_KEY, JSON.stringify(locations[location])),
			})
		}

		return next.handle(req)
	}
}
