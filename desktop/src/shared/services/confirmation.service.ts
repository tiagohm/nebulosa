import { Injectable } from '@angular/core'
import { ConfirmEventType } from 'primeng/api'
import { ConfirmationEvent } from '../types/app.types'
import { AngularService } from './angular.service'
import { ApiService } from './api.service'

@Injectable({ providedIn: 'root' })
export class ConfirmationService {
	private readonly keys = new Map<string, string>()

	constructor(
		private readonly angularService: AngularService,
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
		const response = await this.angularService.confirm(event.message)
		await this.api.confirm(event.idempotencyKey, response === ConfirmEventType.ACCEPT)
		this.unregister(event.idempotencyKey)
	}
}
