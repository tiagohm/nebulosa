import * as fs from 'fs'
import { dirname } from 'path'
import type { Undefinable } from '../src/shared/utils/types'

export class LocalStorage<T extends NonNullable<unknown>> {
	private readonly data = Object.create(null) as T

	constructor(private readonly path: string) {
		try {
			console.info(`loading config file at ${path}`)

			const parsedData = JSON.parse(fs.readFileSync(path, 'utf8')) as unknown

			if (typeof parsedData === 'object' && !Array.isArray(parsedData) && parsedData) {
				Object.assign(this.data, parsedData)
			}
		} catch (e) {
			console.error(e)

			this.ensureDirectory()

			fs.writeFileSync(path, '{}', { mode: 0o666 })
		}
	}

	get<K extends keyof T>(key: K): Undefinable<T[K]> {
		return this.data[key]
	}

	set<K extends keyof T>(key: K, value: T[K]) {
		this.data[key] = value
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
