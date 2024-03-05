import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { Observable } from 'rxjs'
import { PreferenceService } from '../services/preference.service'

@Injectable({ providedIn: 'root' })
export class LocationInterceptor implements HttpInterceptor {

    constructor(private preference: PreferenceService) { }

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const selectedLocation = this.preference.selectedLocation.get()

        req = req.clone({
            headers: req.headers.set('X-Location', JSON.stringify(selectedLocation))
        })

        return next.handle(req)
    }
}