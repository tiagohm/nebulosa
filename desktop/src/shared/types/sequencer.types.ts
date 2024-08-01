import { DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT, DEFAULT_DITHER, type AutoSubFolderMode, type CameraCaptureEvent, type CameraCaptureNamingFormat, type CameraStartCapture, type Dither } from './camera.types'

export type SequenceCaptureMode = 'FULLY' | 'INTERLEAVED'

export type SequencerState = 'IDLE' | 'PAUSING' | 'PAUSED' | 'RUNNING'

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

export interface SequencerEvent extends MessageEvent {
	id: number
	elapsedTime: number
	remainingTime: number
	progress: number
	capture?: CameraCaptureEvent
	state: SequencerState
}

export interface SequencerPreference {
	savedPath?: string
	plan: SequencePlan
}

export const DEFAULT_AUTO_FOCUS_AFTER_CONDITIONS: AutoFocusAfterConditions = {
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
}

export const DEFAULT_SEQUENCE_PLAN: SequencePlan = {
	initialDelay: 0,
	captureMode: 'FULLY',
	autoSubFolderMode: 'OFF',
	entries: [],
	dither: DEFAULT_DITHER,
	autoFocus: DEFAULT_AUTO_FOCUS_AFTER_CONDITIONS,
	namingFormat: DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT,
}

export const DEFAULT_SEQUENCER_PREFERENCE: SequencerPreference = {
	plan: structuredClone(DEFAULT_SEQUENCE_PLAN),
}
