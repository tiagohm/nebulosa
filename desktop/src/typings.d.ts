declare const nodeModule: NodeModule

interface NodeModule {
	id: string
}

interface Window {
	process: any
	require: any
	apiHost: string
	apiPort: number
	options: {
		icon?: string
		resizable?: boolean
		width?: number | string
		height?: number | string
		bringToFront?: boolean
		requestFocus?: boolean
		id: string
		path: string
		modal?: boolean
		autoResizable?: boolean
		data: any
	}
}
