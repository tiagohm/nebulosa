import { HttpClient } from '@angular/common/http'
import { Injectable, inject } from '@angular/core'
import { firstValueFrom } from 'rxjs'
import { Nullable } from '../utils/types'

export type QueryParamType = Nullable<string | number | boolean> | QueryParamType[]

@Injectable({ providedIn: 'root' })
export class HttpService {
	private readonly http = inject(HttpClient)

	readonly baseUrl = `http://${window.apiHost}:${window.apiPort}`

	get<T>(path: string) {
		return firstValueFrom(this.http.get<T>(`${this.baseUrl}/${path}`))
	}

	getBlob(path: string) {
		return firstValueFrom(this.http.get(`${this.baseUrl}/${path}`, { observe: 'response', responseType: 'blob' }))
	}

	post<T>(path: string, body?: unknown) {
		return firstValueFrom(this.http.post<T>(`${this.baseUrl}/${path}`, body))
	}

	postBodyAsQueryParams<T>(path: string, body: Record<string, QueryParamType>) {
		const query = this.query(body)
		return firstValueFrom(this.http.post<T>(`${this.baseUrl}/${path}?${query}`, null))
	}

	postBlob(path: string, body?: unknown) {
		return firstValueFrom(this.http.post(`${this.baseUrl}/${path}`, body, { observe: 'response', responseType: 'blob' }))
	}

	patch<T>(path: string, body?: unknown) {
		return firstValueFrom(this.http.patch<T>(`${this.baseUrl}/${path}`, body))
	}

	put<T>(path: string, body?: unknown) {
		return firstValueFrom(this.http.put<T>(`${this.baseUrl}/${path}`, body))
	}

	putBodyAsQueryParams<T>(path: string, body: Record<string, QueryParamType>) {
		const query = this.query(body)
		return firstValueFrom(this.http.put<T>(`${this.baseUrl}/${path}?${query}`, null))
	}

	putBlob(path: string, body?: unknown) {
		return firstValueFrom(this.http.put(`${this.baseUrl}/${path}`, body, { observe: 'response', responseType: 'blob' }))
	}

	delete<T>(path: string) {
		return firstValueFrom(this.http.delete<T>(`${this.baseUrl}/${path}`))
	}

	query(data: Record<string, QueryParamType>) {
		const query: string[] = []

		for (const key in data) {
			this.addQuery(key, data[key], query)
		}

		return query.join('&')
	}

	private addQuery(key: string, value: QueryParamType, output: string[]) {
		if (Array.isArray(value)) {
			for (const item of value) {
				this.addQuery(key, item, output)
			}
		} else if (value !== undefined && value !== null) {
			output.push(`${key}=${encodeURIComponent(value)}`)
		}
	}
}
