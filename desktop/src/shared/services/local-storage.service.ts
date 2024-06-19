import { Injectable } from '@angular/core'
import { StorageService } from './storage.service'

@Injectable({ providedIn: 'root' })
export class LocalStorageService implements StorageService {
	clear() {
		return localStorage.clear()
	}

	delete(key: string) {
		return localStorage.removeItem(key)
	}

	get<T>(key: string, defaultValue: T | (() => T)): T {
		const value = localStorage.getItem(key)

		if (value === undefined || value === null) {
			return defaultValue instanceof Function ? defaultValue() : defaultValue
		}

		return JSON.parse(value) as T
	}

	has(key: string) {
		const length = localStorage.length

		for (let i = 0; i < length; i++) {
			if (localStorage.key(i) === key) return true
		}

		return false
	}

	set(key: string, value: any) {
		if (value === null || value === undefined) return this.delete(key)
		else return localStorage.setItem(key, JSON.stringify(value))
	}
}
