import { backlashCompensationWithDefault, DEFAULT_BACKLASH_COMPENSATION, type BacklashCompensation } from './autofocus.type'
import type { Camera, LiveStackingRequest } from './camera.types'
import {
	cameraCaptureNamingFormatWithDefault,
	DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT,
	DEFAULT_CAMERA_START_CAPTURE,
	DEFAULT_DITHER,
	DEFAULT_LIVE_STACKING_REQUEST,
	ditherWithDefault,
	EMPTY_CAMERA_CAPTURE_NAMING_FORMAT,
	liveStackingRequestWithDefault,
	type AutoSubFolderMode,
	type CameraCaptureEvent,
	type CameraCaptureNamingFormat,
	type CameraStartCapture,
	type Dither,
} from './camera.types'
import type { Focuser } from './focuser.types'
import type { Mount } from './mount.types'
import type { Rotator } from './rotator.types'
import type { Wheel } from './wheel.types'

export type Sequence = CameraStartCapture

export type SequencerCaptureMode = 'FULLY' | 'INTERLEAVED'

export type SequencerState = 'IDLE' | 'WAITING' | 'PAUSING' | 'PAUSED' | 'RUNNING'

export type SequenceProperty = 'EXPOSURE_TIME' | 'EXPOSURE_AMOUNT' | 'EXPOSURE_DELAY' | 'FRAME_TYPE' | 'X' | 'Y' | 'WIDTH' | 'HEIGHT' | 'BIN' | 'FRAME_FORMAT' | 'GAIN' | 'OFFSET' | 'STACKING_GROUP' | 'CALIBRATION_GROUP'

export type SequenceProperties = Record<SequenceProperty, boolean>

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

export interface SequencerPlan {
	initialDelay: number
	captureMode: SequencerCaptureMode
	autoSubFolderMode: AutoSubFolderMode
	savePath?: string
	sequences: Sequence[]
	dither: Dither
	autoFocus: AutoFocusAfterConditions
	liveStacking: LiveStackingRequest
	namingFormat: CameraCaptureNamingFormat
	backlashCompensation: BacklashCompensation
	camera?: Camera
	mount?: Mount
	wheel?: Wheel
	focuser?: Focuser
	rotator?: Rotator
}

export interface SequencerEvent extends MessageEvent {
	camera: Camera
	id: number
	elapsedTime: number
	remainingTime: number
	progress: number
	capture?: Omit<CameraCaptureEvent, 'camera'>
	state: SequencerState
}

export interface SequencePropertyDialog {
	showDialog: boolean
	sequence?: Sequence
	count: [number, number]
	properties: SequenceProperties
}

export interface SequencerPreference {
	loadPath?: string
	plan: SequencerPlan
	properties: SequenceProperties
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

export const DEFAULT_SEQUENCE: Sequence = {
	...DEFAULT_CAMERA_START_CAPTURE,
	autoSave: true,
	autoSubFolderMode: 'OFF',
	filterPosition: 0,
	shutterPosition: 0,
	focusOffset: 0,
	namingFormat: EMPTY_CAMERA_CAPTURE_NAMING_FORMAT,
}

export const DEFAULT_SEQUENCER_PLAN: SequencerPlan = {
	initialDelay: 0,
	captureMode: 'FULLY',
	autoSubFolderMode: 'OFF',
	dither: DEFAULT_DITHER,
	autoFocus: DEFAULT_AUTO_FOCUS_AFTER_CONDITIONS,
	liveStacking: DEFAULT_LIVE_STACKING_REQUEST,
	namingFormat: DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT,
	backlashCompensation: DEFAULT_BACKLASH_COMPENSATION,
	sequences: [],
}

export const DEFAULT_SEQUENCE_PROPERTIES: SequenceProperties = {
	EXPOSURE_TIME: true,
	EXPOSURE_AMOUNT: true,
	EXPOSURE_DELAY: true,
	FRAME_TYPE: true,
	X: true,
	Y: true,
	WIDTH: true,
	HEIGHT: true,
	BIN: true,
	FRAME_FORMAT: true,
	GAIN: true,
	OFFSET: true,
	STACKING_GROUP: false,
	CALIBRATION_GROUP: false,
}

export const DEFAULT_SEQUENCE_PROPERTY_DIALOG: SequencePropertyDialog = {
	showDialog: false,
	count: [0, 0],
	properties: DEFAULT_SEQUENCE_PROPERTIES,
}

export const DEFAULT_SEQUENCER_PREFERENCE: SequencerPreference = {
	plan: DEFAULT_SEQUENCER_PLAN,
	properties: DEFAULT_SEQUENCE_PROPERTY_DIALOG.properties,
}

export function autoFocusAfterConditionsWithDefault(conditions?: Partial<AutoFocusAfterConditions>, source: AutoFocusAfterConditions = DEFAULT_AUTO_FOCUS_AFTER_CONDITIONS) {
	if (!conditions) return structuredClone(source)
	conditions.enabled ??= source.enabled
	conditions.onStart ??= source.onStart
	conditions.onFilterChange ??= source.onFilterChange
	conditions.afterElapsedTime ??= source.afterElapsedTime
	conditions.afterElapsedTimeEnabled ??= source.afterElapsedTimeEnabled
	conditions.afterExposures ??= source.afterExposures
	conditions.afterExposuresEnabled ??= source.afterExposuresEnabled
	conditions.afterTemperatureChange ??= source.afterTemperatureChange
	conditions.afterTemperatureChangeEnabled ??= source.afterTemperatureChangeEnabled
	conditions.afterHFDIncrease ??= source.afterHFDIncrease
	conditions.afterHFDIncreaseEnabled ??= source.afterHFDIncreaseEnabled
	return conditions as AutoFocusAfterConditions
}

export function sequencePropertiesWithDefault(properties?: Partial<SequenceProperties>, source: SequenceProperties = DEFAULT_SEQUENCE_PROPERTIES) {
	if (!properties) return structuredClone(source)

	for (const entry of Object.entries(source)) {
		const key = entry[0] as SequenceProperty
		properties[key] ??= source[key]
	}

	return properties as SequenceProperties
}

export function sequencerPlanWithDefault(plan?: Partial<SequencerPlan>, source: SequencerPlan = DEFAULT_SEQUENCER_PLAN) {
	if (!plan) return structuredClone(source)
	plan.initialDelay ??= source.initialDelay
	plan.captureMode ||= source.captureMode
	plan.autoSubFolderMode ||= source.autoSubFolderMode
	plan.savePath ||= source.savePath
	plan.sequences ??= source.sequences
	plan.dither = ditherWithDefault(plan.dither, source.dither)
	plan.autoFocus = autoFocusAfterConditionsWithDefault(plan.autoFocus, source.autoFocus)
	plan.liveStacking = liveStackingRequestWithDefault(plan.liveStacking, source.liveStacking)
	plan.namingFormat = cameraCaptureNamingFormatWithDefault(plan.namingFormat, source.namingFormat)
	plan.backlashCompensation = backlashCompensationWithDefault(plan.backlashCompensation, source.backlashCompensation)
	return plan as SequencerPlan
}

export function sequencerPreferenceWithDefault(preference?: Partial<SequencerPreference>, source: SequencerPreference = DEFAULT_SEQUENCER_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.loadPath ||= source.loadPath
	preference.plan = sequencerPlanWithDefault(preference.plan, source.plan)
	preference.properties = sequencePropertiesWithDefault(preference.properties, source.properties)
	return preference as SequencerPreference
}
