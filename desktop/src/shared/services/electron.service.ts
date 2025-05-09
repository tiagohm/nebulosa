import { Injectable } from '@angular/core'

// If you import a module but never use any of the imported values
// other than as TypeScript types, the resulting javascript file will
// look as if you never imported the module at all.

import type { DARVEvent, TPPAEvent } from '../types/alignment.types'
import type { ConfirmationEvent, DeviceMessageEvent, NotificationEvent, OpenImageEvent } from '../types/api.types'
import type { CloseWindowCommand, FullscreenWindowCommand, JsonFile, OpenDirectoryCommand, OpenFileCommand, ResizeWindowCommand, SaveJsonCommand, WindowCommand } from '../types/app.types'
import type { Location } from '../types/atlas.types'
import type { AutoFocusEvent } from '../types/autofocus.type'
import type { Camera, CameraCaptureEvent } from '../types/camera.types'
import type { INDIMessageEvent } from '../types/device.types'
import type { DustCap } from '../types/dustcap.types'
import type { FlatWizardEvent } from '../types/flat-wizard.types'
import type { Focuser } from '../types/focuser.types'
import type { GuideOutput, Guider, GuiderHistoryStep, GuiderMessageEvent } from '../types/guider.types'
import type { ConnectionClosed } from '../types/home.types'
import type { ROISelected } from '../types/image.types'
import type { LightBox } from '../types/lightbox.types'
import type { Mount } from '../types/mount.types'
import type { Rotator } from '../types/rotator.types'
import type { SequencerEvent } from '../types/sequencer.types'
import type { Wheel, WheelRenamed } from '../types/wheel.types'

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
	'FILE.OPEN': OpenFileCommand
	'FILE.SAVE': OpenFileCommand
	'DIRECTORY.OPEN': OpenDirectoryCommand
	'JSON.WRITE': JsonFile
	'JSON.READ': string
	'WINDOW.RESIZE': ResizeWindowCommand
	'WINDOW.PIN': WindowCommand
	'WINDOW.UNPIN': WindowCommand
	'WINDOW.MINIMIZE': WindowCommand
	'WINDOW.MAXIMIZE': WindowCommand
	'WINDOW.FULLSCREEN': FullscreenWindowCommand
	'WINDOW.CLOSE': CloseWindowCommand
	'WINDOW.OPEN_DEV_TOOLS': WindowCommand
	'WHEEL.RENAMED': WheelRenamed
	'ROI.SELECTED': ROISelected
	'AUTO_FOCUS.ELAPSED': AutoFocusEvent
	'IMAGE.OPEN': OpenImageEvent
}

@Injectable({ providedIn: 'root' })
export class ElectronService {
	send<R, K extends keyof EventMap>(channel: K, data?: EventMap[K]) {
		return window.electron.invoke<R>(channel, data)
	}

	on<K extends keyof EventMap>(channel: K, listener: (arg: EventMap[K]) => void) {
		window.electron.on(channel, listener)
	}

	openFile(data?: OpenFileCommand): Promise<string | false> {
		return this.send('FILE.OPEN', { ...data, windowId: data?.windowId ?? window.id, multiple: false })
	}

	openFiles(data?: OpenFileCommand): Promise<string[] | false> {
		return this.send('FILE.OPEN', { ...data, windowId: data?.windowId ?? window.id, multiple: true })
	}

	saveFile(data?: OpenFileCommand): Promise<string | false> {
		return this.send('FILE.SAVE', { ...data, windowId: data?.windowId ?? window.id })
	}

	openImage(data?: OpenFileCommand) {
		return this.openFile({
			...data,
			windowId: data?.windowId ?? window.id,
			filters: OPEN_IMAGE_FILE_FILTER,
		})
	}

	openImages(data?: OpenFileCommand) {
		return this.openFiles({
			...data,
			windowId: data?.windowId ?? window.id,
			filters: OPEN_IMAGE_FILE_FILTER,
		})
	}

	saveImage(data?: OpenFileCommand) {
		return this.saveFile({
			...data,
			windowId: data?.windowId ?? window.id,
			filters: SAVE_IMAGE_FILE_FILTER,
		})
	}

	openDirectory(data?: OpenDirectoryCommand): Promise<string | false> {
		return this.send('DIRECTORY.OPEN', { ...data, windowId: data?.windowId ?? window.id })
	}

	async saveJson<T>(data: SaveJsonCommand<T>): Promise<JsonFile<T> | false> {
		data.path = data.path || (await this.saveFile({ ...data, windowId: data.windowId ?? window.id, filters: [{ name: 'JSON files', extensions: ['json'] }] })) || undefined

		if (data.path) {
			if (await this.writeJson(data)) {
				return data
			}
		}

		return false
	}

	async openJson<T>(data?: OpenFileCommand): Promise<JsonFile<T> | false> {
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

	openDevTools() {
		return this.send('WINDOW.OPEN_DEV_TOOLS', { windowId: window.id })
	}

	calibrationChanged() {
		return this.send('CALIBRATION.CHANGED')
	}

	locationChanged(location: Location) {
		return this.send('LOCATION.CHANGED', location)
	}
}
