import type { MessageEvent } from './api.types'

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

export interface OpenWindow {
	id: string
	path: string
	preference: WindowPreference
	data?: unknown
}

export interface CloseWindow {
	id?: string
	data?: unknown
}

export interface OpenDirectory {
	defaultPath?: string
}

export interface OpenFile extends OpenDirectory {
	filters?: Electron.FileFilter[]
}

export interface JsonFile<T = unknown> {
	path?: string
	json: T
}

export interface SaveJson<T = unknown> extends OpenFile, JsonFile<T> {}

export type StoredWindowDataKey = `window.${string}`

export type StoredWindowDataValue = {
	x: number
	y: number
	width: number
	height: number
}

export type StoredWindowData = Record<StoredWindowDataKey, StoredWindowDataValue>
