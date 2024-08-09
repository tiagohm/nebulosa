import type { Severity } from './angular.types'
import type { MessageEvent } from './api.types'

export type InternalEventType = (typeof INTERNAL_EVENT_TYPES)[number]

export type SaveJson<T = unknown> = OpenFile & JsonFile<T>

export interface NotificationEvent extends MessageEvent {
	target?: string
	severity: Severity
	title?: string
	body: string
}

export interface ConfirmationEvent extends MessageEvent {
	message: string
	idempotencyKey: string
}

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

export interface OpenWindow extends WindowCommand {
	id: string
	path: string
	preference: WindowPreference
	data?: unknown
}

export interface CloseWindow extends WindowCommand {
	data?: unknown
}

export interface FullscreenWindow extends WindowCommand {
	enabled?: boolean
}

export interface ResizeWindow extends WindowCommand {
	height: number
}

export interface OpenDirectory extends WindowCommand {
	defaultPath?: string
}

export interface OpenFile extends OpenDirectory {
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
