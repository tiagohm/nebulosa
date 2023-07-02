import { HttpClient } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { firstValueFrom } from 'rxjs'
import { Camera } from '../models/Camera.model'

@Injectable({ providedIn: 'root' })
export class ApiService {

    constructor(private http: HttpClient) { }

    private get<T>(url: string) {
        return firstValueFrom(this.http.get<T>(`http://localhost:32907/${url}`))
    }

    private post<T>(url: string, body: any = null) {
        return firstValueFrom(this.http.post<T>(`http://localhost:32907/${url}`, body))
    }

    private patch<T>(url: string, body: any = null) {
        return firstValueFrom(this.http.patch<T>(`http://localhost:32907/${url}`, body))
    }

    private put<T>(url: string, body: any = null) {
        return firstValueFrom(this.http.put<T>(`http://localhost:32907/${url}`, body))
    }

    private delete<T>(url: string) {
        return firstValueFrom(this.http.delete<T>(`http://localhost:32907/${url}`))
    }

    async cameras() {
        return [<Camera>{
            name: 'CCD Simulator'
        }]
    }
}
