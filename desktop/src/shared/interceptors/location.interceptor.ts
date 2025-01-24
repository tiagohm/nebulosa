import type { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http'
import { Injectable, inject } from '@angular/core'
import type { Observable } from 'rxjs'
import { PreferenceService } from '../services/preference.service'

@Injectable({ providedIn: 'root' })
export class LocationInterceptor implements HttpInterceptor {
	private readonly preferenceService = inject(PreferenceService)

	static readonly HEADER_KEY = 'X-Location'

	intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
		if (req.urlWithParams.includes('hasLocation')) {
			const params = new URLSearchParams(req.urlWithParams)
			const hasLocation = params.get('hasLocation')

			if (!hasLocation || hasLocation === 'true') {
				const location = this.preferenceService.settings.get().location

				req = req.clone({
					headers: req.headers.set(LocationInterceptor.HEADER_KEY, JSON.stringify(location)),
				})
			} else {
				const id = parseInt(hasLocation)

				if (id) {
					const locations = this.preferenceService.settings.get().locations
					const location = locations.find((e) => e.id === id)

					if (location) {
						req = req.clone({
							headers: req.headers.set(LocationInterceptor.HEADER_KEY, JSON.stringify(location)),
						})
					}
				}
			}
		}

		return next.handle(req)
	}
}
