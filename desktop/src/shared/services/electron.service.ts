import { Injectable } from '@angular/core'

// If you import a module but never use any of the imported values
// other than as TypeScript types, the resulting javascript file will
// look as if you never imported the module at all.

import type * as childProcess from 'child_process'
import type { ipcRenderer, webFrame } from 'electron'
import type * as fs from 'fs'
import { DARVEvent, TPPAEvent } from '../types/alignment.types'
import { DeviceMessageEvent } from '../types/api.types'
import { CloseWindow, ConfirmationEvent, FullscreenWindow, JsonFile, NotificationEvent, OpenDirectory, OpenFile, ResizeWindow, SaveJson, WindowCommand } from '../types/app.types'
import { Location } from '../types/atlas.types'
import { AutoFocusEvent } from '../types/autofocus.type'
import { Camera, CameraCaptureEvent } from '../types/camera.types'
import { INDIMessageEvent } from '../types/device.types'
import { DustCap } from '../types/dustcap.types'
import { FlatWizardEvent } from '../types/flat-wizard.types'
import { Focuser } from '../types/focuser.types'
import { GuideOutput, Guider, GuiderHistoryStep, GuiderMessageEvent } from '../types/guider.types'
import { ConnectionClosed } from '../types/home.types'
import { ROISelected } from '../types/image.types'
import { LightBox } from '../types/lightbox.types'
import { Mount } from '../types/mount.types'
import { Rotator } from '../types/rotator.types'
import { SequencerEvent } from '../types/sequencer.types'
import { StackerEvent } from '../types/stacker.types'
import { Wheel, WheelRenamed } from '../types/wheel.types'

export const OPEN_IMAGE_FILE_FILTER: Electron.FileFilter[] = [
	{ name: 'All', extensions: ['fits', 'fit', 'xisf'] },
	{ name: 'FITS', extensions: ['fits', 'fit'] },
	{ name: 'XISF', extensions: ['xisf'] },
]

export const SAVE_IMAGE_FILE_FILTER: Electron.FileFilter[] = [
	{ name: 'All', extensions: ['fits', 'fit', 'xisf', 'png', 'jpg', 'jpeg'] },
	{ name: 'FITS', extensions: ['fits', 'fit'] },
	{ name: 'XISF', extensions: ['xisf'] },
	{ name: 'Image', extensions: ['png', 'jpg', 'jpeg'] },
]

export interface EventMap {
	NOTIFICATION: NotificationEvent
	CONFIRMATION: ConfirmationEvent
	'DEVICE.PROPERTY_CHANGED': INDIMessageEvent
	'DEVICE.PROPERTY_DELETED': INDIMessageEvent
	'DEVICE.MESSAGE_RECEIVED': INDIMessageEvent
	'CAMERA.UPDATED': DeviceMessageEvent<Camera>
	'CAMERA.ATTACHED': DeviceMessageEvent<Camera>
	'CAMERA.DETACHED': DeviceMessageEvent<Camera>
	'CAMERA.CAPTURE_ELAPSED': CameraCaptureEvent
	'MOUNT.UPDATED': DeviceMessageEvent<Mount>
	'MOUNT.ATTACHED': DeviceMessageEvent<Mount>
	'MOUNT.DETACHED': DeviceMessageEvent<Mount>
	'FOCUSER.UPDATED': DeviceMessageEvent<Focuser>
	'FOCUSER.ATTACHED': DeviceMessageEvent<Focuser>
	'FOCUSER.DETACHED': DeviceMessageEvent<Focuser>
	'ROTATOR.UPDATED': DeviceMessageEvent<Rotator>
	'ROTATOR.ATTACHED': DeviceMessageEvent<Rotator>
	'ROTATOR.DETACHED': DeviceMessageEvent<Rotator>
	'WHEEL.UPDATED': DeviceMessageEvent<Wheel>
	'WHEEL.ATTACHED': DeviceMessageEvent<Wheel>
	'WHEEL.DETACHED': DeviceMessageEvent<Wheel>
	'GUIDE_OUTPUT.UPDATED': DeviceMessageEvent<GuideOutput>
	'GUIDE_OUTPUT.ATTACHED': DeviceMessageEvent<GuideOutput>
	'GUIDE_OUTPUT.DETACHED': DeviceMessageEvent<GuideOutput>
	'LIGHT_BOX.UPDATED': DeviceMessageEvent<LightBox>
	'LIGHT_BOX.ATTACHED': DeviceMessageEvent<LightBox>
	'LIGHT_BOX.DETACHED': DeviceMessageEvent<LightBox>
	'DUST_CAP.UPDATED': DeviceMessageEvent<DustCap>
	'DUST_CAP.ATTACHED': DeviceMessageEvent<DustCap>
	'DUST_CAP.DETACHED': DeviceMessageEvent<DustCap>
	'GUIDER.CONNECTED': GuiderMessageEvent<undefined>
	'GUIDER.DISCONNECTED': GuiderMessageEvent<undefined>
	'GUIDER.UPDATED': GuiderMessageEvent<Guider>
	'GUIDER.STEPPED': GuiderMessageEvent<GuiderHistoryStep>
	'GUIDER.MESSAGE_RECEIVED': GuiderMessageEvent<string>
	'DARV.ELAPSED': DARVEvent
	'TPPA.ELAPSED': TPPAEvent
	'DATA.CHANGED': never
	'LOCATION.CHANGED': Location
	'SEQUENCER.ELAPSED': SequencerEvent
	'FLAT_WIZARD.ELAPSED': FlatWizardEvent
	'CONNECTION.CLOSED': ConnectionClosed
	'CALIBRATION.CHANGED': unknown
	'FILE.OPEN': OpenFile
	'FILE.SAVE': OpenFile
	'DIRECTORY.OPEN': OpenDirectory
	'JSON.WRITE': JsonFile
	'JSON.READ': string
	'WINDOW.RESIZE': ResizeWindow
	'WINDOW.PIN': WindowCommand
	'WINDOW.UNPIN': WindowCommand
	'WINDOW.MINIMIZE': WindowCommand
	'WINDOW.MAXIMIZE': WindowCommand
	'WINDOW.FULLSCREEN': FullscreenWindow
	'WINDOW.CLOSE': CloseWindow
	'WHEEL.RENAMED': WheelRenamed
	'ROI.SELECTED': ROISelected
	'AUTO_FOCUS.ELAPSED': AutoFocusEvent
	'STACKER.ELAPSED': StackerEvent
}

@Injectable({ providedIn: 'root' })
export class ElectronService {
	readonly ipcRenderer!: typeof ipcRenderer
	private readonly webFrame!: typeof webFrame
	private readonly childProcess!: typeof childProcess
	private readonly fs!: typeof fs

	constructor() {
		if (this.isElectron) {
			this.ipcRenderer = window.require('electron').ipcRenderer
			this.webFrame = window.require('electron').webFrame

			this.fs = window.require('fs')

			this.childProcess = window.require('child_process')
			this.childProcess.exec('node -v')

			// Notes :
			// * A NodeJS's dependency imported with 'window.require' MUST BE present in `dependencies` of both `app/package.json`
			// and `package.json (root folder)` in order to make it work here in Electron's Renderer process (src folder)
			// because it will loaded at runtime by Electron.
			// * A NodeJS's dependency imported with TS module import (ex: import { Dropbox } from 'dropbox') CAN only be present
			// in `dependencies` of `package.json (root folder)` because it is loaded during build phase and does not need to be
			// in the final bundle. Reminder : only if not used in Electron's Main process (app folder)

			// If you want to use a NodeJS 3rd party deps in Renderer process,
			// ipcRenderer.invoke can serve many common use cases.
			// https://www.electronjs.org/docs/latest/api/ipc-renderer#ipcrendererinvokechannel-args
		}
	}

	get isElectron() {
		// eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
		return !!(window && window.process?.type)
	}

	send<K extends keyof EventMap>(channel: K, data?: EventMap[K]) {
		return this.ipcRenderer.invoke(channel, data)
	}

	on<K extends keyof EventMap>(channel: K, listener: (arg: EventMap[K]) => void) {
		this.ipcRenderer.on(channel, (_, arg) => {
			listener(arg)
		})
	}

	openFile(data?: OpenFile): Promise<string | false> {
		return this.send('FILE.OPEN', { ...data, windowId: data?.windowId ?? window.id, multiple: false })
	}

	openFiles(data?: OpenFile): Promise<string[] | false> {
		return this.send('FILE.OPEN', { ...data, windowId: data?.windowId ?? window.id, multiple: true })
	}

	saveFile(data?: OpenFile): Promise<string | false> {
		return this.send('FILE.SAVE', { ...data, windowId: data?.windowId ?? window.id })
	}

	openImage(data?: OpenFile) {
		return this.openFile({
			...data,
			windowId: data?.windowId ?? window.id,
			filters: OPEN_IMAGE_FILE_FILTER,
		})
	}

	openImages(data?: OpenFile) {
		return this.openFiles({
			...data,
			windowId: data?.windowId ?? window.id,
			filters: OPEN_IMAGE_FILE_FILTER,
		})
	}

	saveImage(data?: OpenFile) {
		return this.saveFile({
			...data,
			windowId: data?.windowId ?? window.id,
			filters: SAVE_IMAGE_FILE_FILTER,
		})
	}

	openDirectory(data?: OpenDirectory): Promise<string | false> {
		return this.send('DIRECTORY.OPEN', { ...data, windowId: data?.windowId ?? window.id })
	}

	async saveJson<T>(data: SaveJson<T>): Promise<JsonFile<T> | false> {
		data.path = data.path || (await this.saveFile({ ...data, windowId: data.windowId ?? window.id, filters: [{ name: 'JSON files', extensions: ['json'] }] })) || undefined

		if (data.path) {
			if (await this.writeJson(data)) {
				return data
			}
		}

		return false
	}

	async openJson<T>(data?: OpenFile): Promise<JsonFile<T> | false> {
		const path = await this.openFile({ ...data, windowId: data?.windowId ?? window.id, filters: [{ name: 'JSON files', extensions: ['json'] }] })

		if (path) {
			return await this.readJson<T>(path)
		}

		return false
	}

	writeJson<T>(json: JsonFile<T>): Promise<boolean> {
		return this.send('JSON.WRITE', json)
	}

	readJson<T>(path: string): Promise<JsonFile<T> | false> {
		return this.send('JSON.READ', path)
	}

	resizeWindow(size: number) {
		return this.send('WINDOW.RESIZE', { height: Math.floor(size), windowId: window.id })
	}

	pinWindow() {
		return this.send('WINDOW.PIN', { windowId: window.id })
	}

	unpinWindow() {
		return this.send('WINDOW.UNPIN', { windowId: window.id })
	}

	minimizeWindow() {
		return this.send('WINDOW.MINIMIZE', { windowId: window.id })
	}

	maximizeWindow() {
		return this.send('WINDOW.MAXIMIZE', { windowId: window.id })
	}

	fullscreenWindow(enabled?: boolean): Promise<boolean> {
		return this.send('WINDOW.FULLSCREEN', { enabled, windowId: window.id })
	}

	closeWindow<T = unknown>(data?: unknown, id?: string): Promise<T> {
		return this.send('WINDOW.CLOSE', { data, windowId: id ?? window.id })
	}

	calibrationChanged() {
		return this.send('CALIBRATION.CHANGED')
	}

	locationChanged(location: Location) {
		return this.send('LOCATION.CHANGED', location)
	}
}
