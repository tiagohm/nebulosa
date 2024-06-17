import { MessageEvent } from './api.types'

export type Severity = 'success' | 'info' | 'warning' | 'danger'

export type TooltipPosition = 'right' | 'left' | 'top' | 'bottom'

export interface NotificationEvent extends MessageEvent {
	target?: string
	severity: Severity
	title?: string
	body: string
}

export const INTERNAL_EVENT_TYPES = [
	'DIRECTORY.OPEN',
	'FILE.OPEN',
	'FILE.SAVE',
	'WINDOW.OPEN',
	'WINDOW.CLOSE',
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

export type InternalEventType = (typeof INTERNAL_EVENT_TYPES)[number]

export interface OpenWindowOptions {
	icon?: string
	resizable?: boolean
	width?: number | string
	height?: number | string
	bringToFront?: boolean
	requestFocus?: boolean
	minWidth?: number
	minHeight?: number
}

export interface OpenWindowOptionsWithData<T> extends OpenWindowOptions {
	data: T
}

export interface OpenWindow<T> extends OpenWindowOptionsWithData<T> {
	id: string
	path: string
	modal?: boolean
	autoResizable?: boolean
}

export interface CloseWindow<T = any> {
	id?: string
	data?: T
}

export interface OpenDirectory {
	defaultPath?: string
}

export interface OpenFile extends OpenDirectory {
	filters?: Electron.FileFilter[]
}

export interface JsonFile<T = any> {
	path?: string
	json: T
}

export type SaveJson<T = any> = OpenFile & JsonFile<T>
