import { parseArgs } from 'util'

export type ApplicationMode = 'UI' | 'API' | 'FULL'

export class ParsedArgument {
	constructor(
		readonly serve: boolean,
		readonly mode: ApplicationMode,
		readonly host: string,
		readonly port: number,
	) {}

	get uiMode() {
		return !this.serve && this.mode === 'UI'
	}

	get apiMode() {
		return !this.serve && this.mode === 'API'
	}

	get fullMode() {
		return this.serve || this.mode === 'FULL'
	}

	get uri() {
		return `${this.host}:${this.port}`
	}
}

export class ArgumentParser {
	static parse(args?: string[]): ParsedArgument {
		const parsed = parseArgs({
			args: args ?? process.argv.slice(1),
			allowPositionals: true,
			options: {
				serve: {
					type: 'boolean',
				},
				mode: {
					type: 'string',
				},
				host: {
					type: 'string',
				},
				port: {
					type: 'string',
				},
			},
		})

		const serve = parsed.values.serve ?? false
		const mode: ApplicationMode =
			parsed.values.mode === 'ui' ? 'UI'
			: parsed.values.mode === 'api' ? 'API'
			: 'FULL'
		const host = parsed.values.host || 'localhost'
		const port = parseInt(parsed.values.port || '0') || (serve ? 7000 : 0)

		return new ParsedArgument(serve, mode, host, port)
	}
}
