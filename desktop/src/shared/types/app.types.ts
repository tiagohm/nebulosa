import { MenuItem } from 'primeng/api'
import { CheckboxChangeEvent } from 'primeng/checkbox'
import { MessageEvent } from './api.types'

export interface CheckableMenuItem extends MenuItem {
    checked: boolean
}

export interface ToggleableMenuItem extends MenuItem {
    toggleable: boolean
    toggled: boolean

    toggle: (event: CheckboxChangeEvent) => void
}

export interface NotificationEvent extends MessageEvent {
    type: string
    body: string
    title?: string
    silent: boolean
}

export const INTERNAL_EVENT_TYPES = [
    'DIRECTORY.OPEN', 'FILE.OPEN', 'FILE.SAVE', 'WINDOW.OPEN', 'WINDOW.CLOSE',
    'WINDOW.PIN', 'WINDOW.UNPIN', 'WINDOW.MINIMIZE', 'WINDOW.MAXIMIZE', 'WINDOW.RESIZE',
    'WHEEL.RENAMED', 'LOCATION.CHANGED', 'JSON.WRITE', 'JSON.READ',
    'CALIBRATION.CHANGED', 'WINDOW.FULLSCREEN'
] as const

export type InternalEventType = (typeof INTERNAL_EVENT_TYPES)[number]

export interface OpenWindowOptions {
    icon?: string
    resizable?: boolean
    width?: number | string
    height?: number | string
    bringToFront?: boolean
    requestFocus?: boolean
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

export interface CloseWindow<T = undefined> {
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
