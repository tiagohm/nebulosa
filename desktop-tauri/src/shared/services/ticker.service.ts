import { Injectable } from '@angular/core'

export interface Tickable {
	tick(): void
}

@Injectable({ providedIn: 'root' })
export class Ticker {
	private readonly tickables = new Map<Tickable, number>()

	isRegistered(tickable: Tickable) {
		return this.tickables.has(tickable)
	}

	register(tickable: Tickable, interval: number, initialDelay: number = 1000) {
		this.unregister(tickable)

		if (interval > 0) {
			if (initialDelay > 0 && initialDelay < interval - 1000) {
				setTimeout(() => {
					tickable.tick()
				}, initialDelay)
			}

			const ping = setInterval(() => {
				tickable.tick()
			}, interval) as unknown as number

			this.tickables.set(tickable, ping)
		}
	}

	unregister(tickable: Tickable) {
		clearInterval(this.tickables.get(tickable))
		this.tickables.delete(tickable)
	}
}
