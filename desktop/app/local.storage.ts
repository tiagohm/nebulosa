import * as fs from 'fs'
import { dirname } from 'path'

export class LocalStorage<T extends {}> {
	private readonly data: T = Object.create(null)

	constructor(private path: string) {
		try {
			console.info(`loading config file at ${path}`)

			const parsedData = JSON.parse(fs.readFileSync(path, 'utf8'))

			if (typeof parsedData === 'object' && !Array.isArray(parsedData) && parsedData) {
				Object.assign(this.data, parsedData)
			}
		} catch (e) {
			console.error(e)

			this.ensureDirectory()

			fs.writeFileSync(path, '{}', { mode: 0o666 })
		}
	}

	get<K extends keyof T>(key: K): T[K] | undefined {
		return this.data[key]
	}

	set<K extends keyof T>(key: K, value: T[K]) {
		this.data[key] = value
	}

	delete<K extends keyof T>(key: K) {
		if (this.has(key)) {
			delete this.data[key]
		}
	}

	has<K extends keyof T>(key: K | string) {
		return key in this.data
	}

	save() {
		try {
			this.ensureDirectory()
			fs.writeFileSync(this.path, JSON.stringify(this.data))
		} catch (e) {
			console.error(e)
		}
	}

	private ensureDirectory(): void {
		const dir = dirname(this.path)

		if (!fs.existsSync(dir)) {
			// Ensure the directory exists as it could have been deleted in the meantime.
			fs.mkdirSync(dir, { recursive: true })
		}
	}
}
