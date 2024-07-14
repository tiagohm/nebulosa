import type { FrameType } from './camera.types'

export interface CameraCaptureNamingFormat {
	light?: string
	dark?: string
	flat?: string
	bias?: string
}

export type SettingsTabKey = 'LOCATION' | 'PLATE_SOLVER' | 'STAR_DETECTOR' | 'LIVE_STACKER' | 'STACKER' | 'CAPTURE_NAMING_FORMAT'

export interface SettingsTab {
	id: SettingsTabKey
	name: string
}

export const DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT: CameraCaptureNamingFormat = {
	light: '[camera]_[type]_[year:2][month][day][hour][min][sec][ms]_[filter]_[width]_[height]_[exp]_[bin]_[gain]',
	dark: '[camera]_[type]_[width]_[height]_[exp]_[bin]_[gain]',
	flat: '[camera]_[type]_[filter]_[width]_[height]_[bin]',
	bias: '[camera]_[type]_[width]_[height]_[bin]_[gain]',
}

export function resetCameraCaptureNamingFormat(type: FrameType, format: CameraCaptureNamingFormat, defaultValue?: CameraCaptureNamingFormat) {
	switch (type) {
		case 'LIGHT':
			format.light = defaultValue?.light ?? DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.light
			break
		case 'DARK':
			format.dark = defaultValue?.dark ?? DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.dark
			break
		case 'FLAT':
			format.flat = defaultValue?.flat ?? DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.flat
			break
		case 'BIAS':
			format.bias = defaultValue?.bias ?? DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.bias
			break
	}
}
