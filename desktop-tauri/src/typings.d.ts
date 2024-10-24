declare const nodeModule: NodeModule

interface NodeModule {
	id: string
}

interface Window {
	process: unknown
	require: (string) => never
	apiHost: string
	apiPort: number
	id: string
	data?: unknown
	preference: {
		modal?: boolean
		autoResizable?: boolean
		icon?: string
		resizable?: boolean
		width?: number | string
		height?: number | string
		bringToFront?: boolean
		requestFocus?: boolean
		minWidth?: number
		minHeight?: number
	}
}
