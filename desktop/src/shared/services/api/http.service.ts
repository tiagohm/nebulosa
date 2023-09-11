import { HttpClient } from '@angular/common/http'
import { Injectable } from '@angular/core'
import { firstValueFrom } from 'rxjs'

@Injectable({ providedIn: 'root' })
export class HttpService {

    constructor(private http: HttpClient) { }

    get baseUrl() {
        return `http://localhost:${window.apiPort}`
    }

    get<T>(path: string) {
        return firstValueFrom(this.http.get<T>(`${this.baseUrl}/${path}`))
    }

    getBlob(path: string) {
        return firstValueFrom(this.http.get(`${this.baseUrl}/${path}`, { observe: 'response', responseType: 'blob' }))
    }

    post<T>(path: string, body?: any) {
        return firstValueFrom(this.http.post<T>(`${this.baseUrl}/${path}`, body))
    }

    postAsQueryParams<T>(path: string, body: Record<string, any>) {
        const query = this.mountQueryParamsFromRecord(body)
        return firstValueFrom(this.http.post<T>(`${this.baseUrl}/${path}?${query}`, null))
    }

    patch<T>(path: string, body?: any) {
        return firstValueFrom(this.http.patch<T>(`${this.baseUrl}/${path}`, body))
    }

    put<T>(path: string, body?: any) {
        return firstValueFrom(this.http.put<T>(`${this.baseUrl}/${path}`, body))
    }

    putAsQueryParams<T>(path: string, body: Record<string, any>) {
        const query = this.mountQueryParamsFromRecord(body)
        return firstValueFrom(this.http.put<T>(`${this.baseUrl}/${path}?${query}`, null))
    }

    delete<T>(path: string) {
        return firstValueFrom(this.http.delete<T>(`${this.baseUrl}/${path}`))
    }

    mountQueryParamsFromRecord(data: Record<string, any>) {
        const query = []

        for (const key in data) {
            const value = data[key]

            if (value !== undefined && value !== null) {
                query.push(`${key}=${encodeURIComponent(value)}`)
            }
        }

        return query.join('&')
    }
}