declare const nodeModule: NodeModule

interface NodeModule {
	id: string
}

interface Window {
	process: any
	require: any
	apiHost: string
	apiPort: number
	id: string
	data?: any
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
