export type InternalEventType = (typeof INTERNAL_EVENT_TYPES)[number]

export type SaveJsonCommand<T = unknown> = OpenFileCommand & JsonFile<T>

export interface WindowPreference {
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

export interface WindowCommand {
	windowId?: string
}

export interface OpenWindowCommand extends WindowCommand {
	id: string
	path: string
	preference: WindowPreference
	data?: unknown
}

export interface CloseWindowCommand extends WindowCommand {
	data?: unknown
}

export interface FullscreenWindowCommand extends WindowCommand {
	enabled?: boolean
}

export interface ResizeWindowCommand extends WindowCommand {
	height: number
}

export interface OpenDirectoryCommand extends WindowCommand {
	defaultPath?: string
}

export interface OpenFileCommand extends OpenDirectoryCommand {
	filters?: Electron.FileFilter[]
	multiple?: boolean
}

export interface JsonFile<T = unknown> {
	path?: string
	json: T
}

export const INTERNAL_EVENT_TYPES = [
	'DIRECTORY.OPEN',
	'FILE.OPEN',
	'FILE.SAVE',
	'WINDOW.OPEN',
	'WINDOW.CLOSE',
	'WINDOW.OPEN_DEV_TOOLS',
	'WINDOW.PIN',
	'WINDOW.UNPIN',
	'WINDOW.MINIMIZE',
	'WINDOW.MAXIMIZE',
	'WINDOW.RESIZE',
	'WHEEL.RENAMED',
	'LOCATION.CHANGED',
	'JSON.WRITE',
	'JSON.READ',
	'CALIBRATION.CHANGED',
	'WINDOW.FULLSCREEN',
	'ROI.SELECTED',
] as const
