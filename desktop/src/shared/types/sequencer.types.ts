import type { AutoSubFolderMode, CameraCaptureEvent, CameraStartCapture, Dither } from './camera.types'
import type { CameraCaptureNamingFormat } from './settings.types'

export type SequenceCaptureMode = 'FULLY' | 'INTERLEAVED'

export const SEQUENCE_ENTRY_PROPERTIES = ['EXPOSURE_TIME', 'EXPOSURE_AMOUNT', 'EXPOSURE_DELAY', 'FRAME_TYPE', 'X', 'Y', 'WIDTH', 'HEIGHT', 'BIN', 'FRAME_FORMAT', 'GAIN', 'OFFSET'] as const

export type SequenceEntryProperty = (typeof SEQUENCE_ENTRY_PROPERTIES)[number]

export interface AutoFocusAfterConditions {
	enabled: boolean
	onStart: boolean
	onFilterChange: boolean
	afterElapsedTime: number
	afterElapsedTimeEnabled: boolean
	afterExposures: number
	afterExposuresEnabled: boolean
	afterTemperatureChange: number
	afterTemperatureChangeEnabled: boolean
	afterHFDIncrease: number
	afterHFDIncreaseEnabled: boolean
}

export interface SequencePlan {
	initialDelay: number
	captureMode: SequenceCaptureMode
	autoSubFolderMode: AutoSubFolderMode
	savePath?: string
	entries: CameraStartCapture[]
	dither: Dither
	autoFocus: AutoFocusAfterConditions
	namingFormat: CameraCaptureNamingFormat
	camera?: string
	mount?: string
	wheel?: string
	focuser?: string
	rotator?: string
}

export const EMPTY_SEQUENCE_PLAN: SequencePlan = {
	initialDelay: 0,
	captureMode: 'FULLY',
	autoSubFolderMode: 'OFF',
	entries: [],
	dither: {
		enabled: false,
		amount: 1.5,
		raOnly: false,
		afterExposures: 1,
	},
	autoFocus: {
		enabled: false,
		onStart: false,
		onFilterChange: false,
		afterElapsedTime: 1800, // 30 min
		afterExposures: 10,
		afterTemperatureChange: 5,
		afterHFDIncrease: 10,
		afterElapsedTimeEnabled: false,
		afterExposuresEnabled: false,
		afterTemperatureChangeEnabled: false,
		afterHFDIncreaseEnabled: false,
	},
	namingFormat: {},
}

export interface SequencerEvent extends MessageEvent {
	id: number
	elapsedTime: number
	remainingTime: number
	progress: number
	capture?: CameraCaptureEvent
}

export interface SequencerPreference {
	savedPath?: string
	plan: SequencePlan
}

export const EMPTY_SEQUENCER_PREFERENCE: SequencerPreference = {
	plan: structuredClone(EMPTY_SEQUENCE_PLAN),
}
