import { Injectable } from '@angular/core'
import { Undefinable } from '../utils/types'
import { ApiService } from './api.service'
import { StorageService } from './storage.service'

@Injectable({ providedIn: 'root' })
export class RemoteStorageService implements StorageService {
	constructor(private readonly api: ApiService) {}

	clear() {
		return this.api.clearPreferences()
	}

	delete(key: string) {
		return this.api.deletePreference(key)
	}

	async get<T>(key: string, defaultValue: T) {
		return (await this.api.getPreference<Undefinable<T>>(key)) ?? defaultValue
	}

	has(key: string) {
		return this.api.hasPreference(key)
	}

	set(key: string, value: unknown) {
		if (value === null || value === undefined) return this.delete(key)
		else return this.api.setPreference(key, value)
	}
}
