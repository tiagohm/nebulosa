import { Injectable } from '@angular/core'
import { ApiService } from './api.service'

@Injectable({ providedIn: 'root' })
export class PreferenceService {

    constructor(private api: ApiService) { }

    clear() {
        return this.api.preferenceClear()
    }

    delete(key: string) {
        return this.api.preferenceDelete(key)
    }

    async get<T>(key: string, defaultValue: T) {
        return await this.api.preferenceGet<T>(key) ?? defaultValue
    }

    has(key: string) {
        return this.api.preferenceExists(key)
    }

    set(key: string, value: any) {
        if (value === null || value === undefined) return this.delete(key)
        else return this.api.preferencePut(key, value)
    }
}