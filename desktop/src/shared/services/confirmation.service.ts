import { Injectable } from '@angular/core'
import { ConfirmEventType } from 'primeng/api'
import { ConfirmationEvent } from '../types/app.types'
import { ApiService } from './api.service'
import { PrimeService } from './prime.service'

@Injectable({ providedIn: 'root' })
export class ConfirmationService {
	private readonly keys = new Map<string, string>()

	constructor(
		private readonly prime: PrimeService,
		private readonly api: ApiService,
	) {}

	register(key: string) {
		this.keys.set(key, '')
	}

	unregister(key: string) {
		this.keys.delete(key)
	}

	has(key: string) {
		return this.keys.has(key)
	}

	async processConfirmationEvent(event: ConfirmationEvent) {
		const response = await this.prime.confirm(event.message)
		await this.api.confirm(event.idempotencyKey, response === ConfirmEventType.ACCEPT)
		this.unregister(event.idempotencyKey)
	}
}
