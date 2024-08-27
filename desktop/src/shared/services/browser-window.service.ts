import { Injectable } from '@angular/core'
import { OpenWindow, WindowPreference } from '../types/app.types'
import { SkyAtlasInput } from '../types/atlas.types'
import { Camera, CameraDialogInput, CameraStartCapture } from '../types/camera.types'
import { Device } from '../types/device.types'
import { Focuser } from '../types/focuser.types'
import { LoadFraming } from '../types/framing.types'
import { ImageSource, OpenImage } from '../types/image.types'
import { LightBox } from '../types/lightbox.types'
import { Mount } from '../types/mount.types'
import { Rotator } from '../types/rotator.types'
import { Wheel, WheelDialogInput } from '../types/wheel.types'
import { Undefinable } from '../utils/types'
import { ElectronService } from './electron.service'

@Injectable({ providedIn: 'root' })
export class BrowserWindowService {
	constructor(private readonly electronService: ElectronService) {}

	openWindow(open: OpenWindow): Promise<boolean> {
		open.preference.modal = false
		return this.electronService.ipcRenderer.invoke('WINDOW.OPEN', { ...open, windowId: window.id })
	}

	openModal<R = unknown>(open: OpenWindow): Promise<Undefinable<R>> {
		open.preference.modal = true
		return this.electronService.ipcRenderer.invoke('WINDOW.OPEN', { ...open, windowId: window.id })
	}

	openMount(data: Mount, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'telescope', width: 400, height: 477 })
		return this.openWindow({ preference, data, id: `mount.${data.name}`, path: 'mount' })
	}

	openCamera(data: Camera, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'camera', width: 400, height: 477 })
		return this.openWindow({ preference, data, id: `camera.${data.name}`, path: 'camera' })
	}

	openCameraDialog(data: CameraDialogInput, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'camera', width: 400, height: 424 })
		return this.openModal<CameraStartCapture>({
			preference,
			data,
			id: `camera.${data.camera.name}.modal`,
			path: 'camera',
		})
	}

	openFocuser(data: Focuser, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'focus', width: 252, height: 252 })
		return this.openWindow({ preference, data, id: `focuser.${data.name}`, path: 'focuser' })
	}

	openWheel(data: Wheel, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'filter-wheel', width: 280, height: 195 })
		return this.openWindow({ preference, data, id: `wheel.${data.name}`, path: 'wheel' })
	}

	openRotator(data: Rotator, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'rotate', width: 280, height: 210 })
		return this.openWindow({ preference, data, id: `rotator.${data.name}`, path: 'rotator' })
	}

	openLightBox(data: LightBox, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'light', width: 290, height: 216 })
		return this.openWindow({ preference, data, id: `lightbox.${data.name}`, path: 'light-box' })
	}

	openWheelDialog(data: WheelDialogInput, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'filter-wheel', width: 300, height: 217 })
		return this.openModal<CameraStartCapture>({
			preference,
			data,
			id: `wheel.${data.wheel.name}.modal`,
			path: 'wheel',
		})
	}

	openGuider(preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'guider', width: 380, height: 444 })
		return this.openWindow({ preference, id: 'guider', path: 'guider' })
	}

	async openCameraImage(camera: Camera, source: ImageSource = 'CAMERA', capture?: CameraStartCapture) {
		const factor = camera.height / camera.width
		const id = `image.${camera.name}`
		const preference: WindowPreference = { icon: 'image', width: '50%', height: `${factor}w`, resizable: true }
		const data: OpenImage = { camera, source, capture, path: '' }
		await this.openWindow({ preference, data, id, path: 'image' })
		return id
	}

	async openImage(data: OpenImage) {
		const hash = data.id || `${btoa(data.path)}!`
		const id = `image.${hash}`
		const preference: WindowPreference = { icon: 'image', width: '50%', height: `0.9w`, resizable: true, autoResizable: false }
		await this.openWindow({ preference, data, id, path: 'image' })
		return id
	}

	openINDI(data?: Device, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'indi', width: 760, height: 420, resizable: true })
		return this.openWindow({ preference, data, id: 'indi', path: 'indi' })
	}

	openSkyAtlas(data?: SkyAtlasInput, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'atlas', width: 450, height: 530, autoResizable: false })
		return this.openWindow({ preference, data, id: 'atlas', path: 'atlas' })
	}

	openFraming(data?: LoadFraming, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'framing', width: 280, height: 303 })
		return this.openWindow({ preference, data, id: 'framing', path: 'framing' })
	}

	openAlignment(preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'star', width: 425, height: 396 })
		return this.openWindow({ preference, id: 'alignment', path: 'alignment' })
	}

	openSequencer(camera: Camera, preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'workflow', width: 628, height: 467, resizable: true, minWidth: 628, minHeight: 328 })
		return this.openWindow({ preference, data: camera, id: `sequencer.${camera.name}`, path: 'sequencer' })
	}

	openAutoFocus(preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'auto-focus', width: 425, height: 453 })
		return this.openWindow({ preference, id: 'auto-focus', path: 'auto-focus' })
	}

	openFlatWizard(preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'star', width: 385, height: 402 })
		return this.openWindow({ preference, id: 'flat-wizard', path: 'flat-wizard' })
	}

	openSettings(preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'settings', width: 320, height: 397, minHeight: 350 })
		return this.openWindow({ preference, id: 'settings', path: 'settings' })
	}

	openCalculator(preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'calculator', width: 300, height: 335 })
		return this.openWindow({ preference, id: 'calculator', path: 'calculator' })
	}

	openCalibration(preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'calibration', width: 370, height: 442, minHeight: 400 })
		return this.openWindow({ preference, id: 'calibration', path: 'calibration' })
	}

	openStacker(preference: WindowPreference = {}) {
		Object.assign(preference, { icon: 'stack', width: 370, height: 460 })
		return this.openWindow({ preference, id: 'stacker', path: 'stacker' })
	}

	openAbout() {
		const preference: WindowPreference = { icon: 'about', width: 430, height: 340, bringToFront: true }
		return this.openWindow({ preference, id: 'about', path: 'about' })
	}
}
