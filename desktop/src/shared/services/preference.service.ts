import { Injectable } from '@angular/core'

@Injectable({ providedIn: 'root' })
export class PreferenceService {

    clear() {
        localStorage.clear()
    }

    delete(key: string) {
        localStorage.removeItem(key)
        return true
    }

    get<T>(key: string, defaultValue: T) {
        const value = localStorage.getItem(key) ?? undefined
        return value !== undefined ? JSON.parse(value) as T : defaultValue
    }

    has(key: string): boolean {
        return localStorage.getItem(key) !== null
    }

    set(key: string, value: any) {
        localStorage.setItem(key, JSON.stringify(value))
        return this
    }

    get size() {
        return localStorage.length
    }

    get isNightMode() {
        return this.get('settings.nightMode', false)
    }

    set isNightMode(enabled: boolean) {
        this.set('settings.nightMode', enabled)
    }
}