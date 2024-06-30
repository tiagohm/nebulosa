import { Injectable } from '@angular/core'

export interface Pingable {
	ping(): void
}

@Injectable({ providedIn: 'root' })
export class Pinger {
	private readonly pingables = new Map<Pingable, number>()

	isRegistered(pingable: Pingable) {
		return this.pingables.has(pingable)
	}

	register(pingable: Pingable, interval: number, initialDelay: number = 1000) {
		this.unregister(pingable)

		if (interval > 0) {
			if (initialDelay > 0 && initialDelay < interval - 1000) {
				setTimeout(() => {
					pingable.ping()
				}, initialDelay)
			}

			const ping = setInterval(() => {
				pingable.ping()
			}, interval) as unknown as number

			this.pingables.set(pingable, ping)
		}
	}

	unregister(pingable: Pingable) {
		clearInterval(this.pingables.get(pingable))
		this.pingables.delete(pingable)
	}
}
