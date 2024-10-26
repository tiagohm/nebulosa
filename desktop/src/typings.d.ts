declare const nodeModule: NodeModule

interface NodeModule {
	id: string
}

interface Window {
	readonly apiHost: string
	readonly apiPort: number
	readonly id: string
	readonly context: {
		readonly data?: unknown
		readonly modal?: boolean
		readonly autoResizable?: boolean
		readonly icon?: string
		readonly resizable?: boolean
		readonly width?: number | string
		readonly height?: number | string
		readonly bringToFront?: boolean
		readonly requestFocus?: boolean
		readonly minWidth?: number
		readonly minHeight?: number
	}
	readonly path: {
		readonly basename: (path: string) => string
		readonly dirname: (path: string) => string
		readonly extname: (path: string) => string
		readonly join: (...paths: string[]) => string
	}
	readonly electron: {
		readonly invoke: <T>(channel: string, data?: unknown) => Promise<T>
		readonly on: (channel: string, listener: (arg: never) => void) => void
	}
}
