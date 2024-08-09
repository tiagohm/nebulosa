import type { MessageEvent } from './api.types'
import type { Thermometer } from './auxiliary.types'
import type { CompanionDevice, Device, PropertyState } from './device.types'
import { isCompanionDevice } from './device.types'
import type { Focuser } from './focuser.types'
import type { GuideOutput } from './guider.types'
import type { Mount } from './mount.types'
import type { Rotator } from './rotator.types'
import type { Wheel } from './wheel.types'

export type CameraMode = 'CAPTURE' | 'SEQUENCER' | 'FLAT_WIZARD' | 'TPPA' | 'DARV' | 'AUTO_FOCUS'

export type FrameType = 'LIGHT' | 'DARK' | 'FLAT' | 'BIAS'

export type CfaPattern = 'RGGB' | 'BGGR' | 'GBRG' | 'GRBG' | 'GRGB' | 'GBGR' | 'RGBG' | 'BGRG'

export type AutoSubFolderMode = 'OFF' | 'NOON' | 'MIDNIGHT'

export type ExposureMode = 'SINGLE' | 'FIXED' | 'LOOP'

export type LiveStackerType = 'SIRIL' | 'PIXINSIGHT'

export type CameraCaptureState = 'IDLE' | 'CAPTURE_STARTED' | 'EXPOSURE_STARTED' | 'EXPOSURING' | 'WAITING' | 'SETTLING' | 'DITHERING' | 'STACKING' | 'PAUSING' | 'PAUSED' | 'EXPOSURE_FINISHED' | 'CAPTURE_FINISHED'

export type ExposureTimeUnit = 'MINUTE' | 'SECOND' | 'MILLISECOND' | 'MICROSECOND'

export interface Camera extends GuideOutput, Thermometer {
	readonly exposuring: boolean
	readonly hasCoolerControl: boolean
	readonly coolerPower: number
	readonly cooler: boolean
	readonly hasDewHeater: boolean
	readonly dewHeater: boolean
	readonly frameFormats: string[]
	readonly canAbort: boolean
	readonly cfaOffsetX: number
	readonly cfaOffsetY: number
	readonly cfaType: CfaPattern
	readonly exposureMin: number
	readonly exposureMax: number
	readonly exposureState: PropertyState
	readonly exposureTime: number
	readonly hasCooler: boolean
	readonly canSetTemperature: boolean
	readonly canSubFrame: boolean
	readonly x: number
	readonly minX: number
	readonly maxX: number
	readonly y: number
	readonly minY: number
	readonly maxY: number
	readonly width: number
	readonly minWidth: number
	readonly maxWidth: number
	readonly height: number
	readonly minHeight: number
	readonly maxHeight: number
	readonly canBin: boolean
	readonly maxBinX: number
	readonly maxBinY: number
	readonly binX: number
	readonly binY: number
	readonly gain: number
	readonly gainMin: number
	readonly gainMax: number
	readonly offset: number
	readonly offsetMin: number
	readonly offsetMax: number
	readonly hasGuideHead: boolean
	readonly pixelSizeX: number
	readonly pixelSizeY: number
	readonly capturesPath: string
	readonly guideHead?: Device
}

export interface GuideHead extends Camera, CompanionDevice<Camera> {}

export interface Dither {
	enabled: boolean
	amount: number
	raOnly: boolean
	afterExposures: number
}

export interface CameraCaptureNamingFormat {
	light?: string
	dark?: string
	flat?: string
	bias?: string
}

export interface CameraStartCapture {
	enabled: boolean
	exposureTime: number
	exposureAmount: number
	exposureDelay: number
	x: number
	y: number
	width: number
	height: number
	frameFormat?: string
	frameType: FrameType
	binX: number
	binY: number
	gain: number
	offset: number
	autoSave: boolean
	savePath?: string
	autoSubFolderMode: AutoSubFolderMode
	dither: Dither
	filterPosition: number
	shutterPosition: number
	focusOffset: number
	calibrationGroup?: string
	liveStacking: LiveStackingRequest
	namingFormat: CameraCaptureNamingFormat
}

export interface CameraCaptureEvent extends MessageEvent {
	camera: Camera
	exposureAmount: number
	exposureCount: number
	captureElapsedTime: number
	captureProgress: number
	captureRemainingTime: number
	stepElapsedTime: number
	stepProgress: number
	stepRemainingTime: number
	savedPath?: string
	liveStackedPath?: string
	state: CameraCaptureState
	capture?: CameraStartCapture
}

export interface CameraDialogInput {
	mode: CameraMode
	camera: Camera
	request: CameraStartCapture
}

export interface CameraPreference {
	request: CameraStartCapture
	setpointTemperature: number
	exposureTimeUnit: ExposureTimeUnit
	exposureMode: ExposureMode
	subFrame: boolean
	mount?: Mount
	focuser?: Focuser
	wheel?: Wheel
	rotator?: Rotator
}

export interface CameraStepInfo {
	remainingTime: number
	progress: number
	elapsedTime: number
}

export interface CameraCaptureInfo {
	looping: boolean
	amount: number
	remainingTime: number
	elapsedTime: number
	progress: number
	count: number
}

export interface LiveStackerSettings {
	executablePath: string
	slot: number
}

export interface LiveStackingRequest extends LiveStackerSettings {
	enabled: boolean
	type: LiveStackerType
	darkPath?: string
	flatPath?: string
	biasPath?: string
	use32Bits: boolean
}

export interface CameraDitherDialog {
	showDialog: boolean
	request: Dither
}

export interface CameraLiveStackingDialog {
	showDialog: boolean
	request: LiveStackingRequest
}

export interface CameraNamingFormatDialog {
	showDialog: boolean
	format: CameraCaptureNamingFormat
}

export const DEFAULT_CAMERA: Camera = {
	type: 'CAMERA',
	sender: '',
	id: '',
	exposuring: false,
	hasCoolerControl: false,
	coolerPower: 0,
	cooler: false,
	hasDewHeater: false,
	dewHeater: false,
	frameFormats: [],
	canAbort: false,
	cfaOffsetX: 0,
	cfaOffsetY: 0,
	cfaType: 'RGGB',
	exposureMin: 0,
	exposureMax: 1,
	exposureState: 'IDLE',
	exposureTime: 1,
	hasCooler: false,
	canSetTemperature: false,
	canSubFrame: false,
	x: 0,
	minX: 0,
	maxX: 0,
	y: 0,
	minY: 0,
	maxY: 0,
	width: 1023,
	minWidth: 1023,
	maxWidth: 1023,
	height: 1280,
	minHeight: 1280,
	maxHeight: 1280,
	canBin: false,
	maxBinX: 1,
	maxBinY: 1,
	binX: 1,
	binY: 1,
	gain: 0,
	gainMin: 0,
	gainMax: 0,
	offset: 0,
	offsetMin: 0,
	offsetMax: 0,
	hasGuideHead: false,
	pixelSizeX: 0,
	pixelSizeY: 0,
	capturesPath: '',
	canPulseGuide: false,
	pulseGuiding: false,
	name: '',
	connected: false,
	hasThermometer: false,
	temperature: 0,
}

export const DEFAULT_CAMERA_CAPTURE_INFO: CameraCaptureInfo = {
	looping: false,
	amount: 0,
	remainingTime: 0,
	elapsedTime: 0,
	progress: 0,
	count: 0,
}

export const DEFAULT_LIVE_STACKER_SETTINGS: LiveStackerSettings = {
	executablePath: '',
	slot: 0,
}

export const DEFAULT_LIVE_STACKING_REQUEST: LiveStackingRequest = {
	...DEFAULT_LIVE_STACKER_SETTINGS,
	enabled: false,
	type: 'SIRIL',
	use32Bits: false,
}

export const CAMERA_CAPTURE_NAMING_FORMAT_LIGHT = '[camera]_[type]_[year:2][month][day][hour][min][sec][ms]_[filter]_[width]_[height]_[exp]_[bin]_[gain]'
export const CAMERA_CAPTURE_NAMING_FORMAT_DARK = '[camera]_[type]_[width]_[height]_[exp]_[bin]_[gain]'
export const CAMERA_CAPTURE_NAMING_FORMAT_FLAT = '[camera]_[type]_[filter]_[width]_[height]_[bin]'
export const CAMERA_CAPTURE_NAMING_FORMAT_BIAS = '[camera]_[type]_[width]_[height]_[bin]_[gain]'

export const EMPTY_CAMERA_CAPTURE_NAMING_FORMAT: CameraCaptureNamingFormat = {}

export const DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT: CameraCaptureNamingFormat = {
	light: CAMERA_CAPTURE_NAMING_FORMAT_LIGHT,
	dark: CAMERA_CAPTURE_NAMING_FORMAT_DARK,
	flat: CAMERA_CAPTURE_NAMING_FORMAT_FLAT,
	bias: CAMERA_CAPTURE_NAMING_FORMAT_BIAS,
}

export const DEFAULT_DITHER: Dither = {
	enabled: false,
	amount: 1.5,
	raOnly: false,
	afterExposures: 5,
}

export const DEFAULT_CAMERA_START_CAPTURE: CameraStartCapture = {
	enabled: true,
	exposureTime: 1,
	exposureAmount: 1,
	exposureDelay: 0,
	x: 0,
	y: 0,
	width: 0,
	height: 0,
	frameType: 'LIGHT',
	binX: 1,
	binY: 1,
	gain: 0,
	offset: 0,
	autoSave: false,
	autoSubFolderMode: 'OFF',
	filterPosition: 0,
	shutterPosition: 0,
	focusOffset: 0,
	dither: DEFAULT_DITHER,
	liveStacking: DEFAULT_LIVE_STACKING_REQUEST,
	namingFormat: {},
}

export const DEFAULT_CAMERA_PREFERENCE: CameraPreference = {
	request: DEFAULT_CAMERA_START_CAPTURE,
	setpointTemperature: 0,
	exposureTimeUnit: 'MICROSECOND',
	exposureMode: 'SINGLE',
	subFrame: false,
}

export const DEFAULT_CAMERA_STEP_INFO: CameraStepInfo = {
	remainingTime: 0,
	progress: 0,
	elapsedTime: 0,
}

export function cameraStartCaptureWithDefault(request?: Partial<CameraStartCapture>, source: CameraStartCapture = DEFAULT_CAMERA_START_CAPTURE) {
	if (!request) return structuredClone(source)
	request.enabled ??= source.enabled
	request.exposureTime ??= source.exposureTime
	request.exposureAmount ??= source.exposureAmount
	request.exposureDelay ??= source.exposureDelay
	request.x ??= source.x
	request.y ??= source.y
	request.width ??= source.width
	request.height ??= source.height
	request.frameFormat ||= source.frameFormat
	request.frameType ||= source.frameType
	request.binX ??= source.binX
	request.binY ??= source.binY
	request.gain ??= source.gain
	request.offset ??= source.offset
	request.autoSave ??= source.autoSave
	request.savePath ||= source.savePath
	request.autoSubFolderMode ||= source.autoSubFolderMode
	request.filterPosition ??= source.filterPosition
	request.shutterPosition ??= source.shutterPosition
	request.focusOffset ??= source.focusOffset
	request.calibrationGroup ||= source.calibrationGroup
	request.dither = ditherWithDefault(request.dither, source.dither)
	request.liveStacking = liveStackingRequestWithDefault(request.liveStacking, source.liveStacking)
	request.namingFormat = cameraCaptureNamingFormatWithDefault(request.namingFormat, source.namingFormat)
	return request as CameraStartCapture
}

export function updateCameraStartCaptureFromCamera(request: CameraStartCapture, camera: Camera) {
	if (camera.maxX > 1) request.x = Math.max(camera.minX, Math.min(request.x, camera.maxX))
	if (camera.maxY > 1) request.y = Math.max(camera.minY, Math.min(request.y, camera.maxY))

	if (camera.maxWidth > 1 && (request.width <= 1 || request.width > camera.maxWidth)) request.width = camera.maxWidth
	if (camera.maxHeight > 1 && (request.height <= 1 || request.height > camera.maxHeight)) request.height = camera.maxHeight
	if (camera.minWidth > 1 && request.width < camera.minWidth) request.width = camera.minWidth
	if (camera.minHeight > 1 && request.height < camera.minHeight) request.height = camera.minHeight

	if (camera.maxBinX > 1) request.binX = Math.max(1, Math.min(request.binX, camera.maxBinX))
	if (camera.maxBinY > 1) request.binY = Math.max(1, Math.min(request.binY, camera.maxBinY))
	if (camera.gainMax) request.gain = Math.max(camera.gainMin, Math.min(request.gain, camera.gainMax))
	if (camera.offsetMax) request.offset = Math.max(camera.offsetMin, Math.min(request.offset, camera.offsetMax))
	if (camera.frameFormats.length && (!request.frameFormat || !camera.frameFormats.includes(request.frameFormat))) request.frameFormat = camera.frameFormats[0]
	if (camera.exposureMin > 1 && camera.exposureMax > camera.exposureMin) request.exposureTime = Math.max(camera.exposureMin, Math.min(request.exposureTime, camera.exposureMax))
}

export function cameraPreferenceWithDefault(preference?: Partial<CameraPreference>, source: CameraPreference = DEFAULT_CAMERA_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.request = cameraStartCaptureWithDefault(preference.request, source.request)
	preference.setpointTemperature ??= source.setpointTemperature
	preference.exposureTimeUnit ??= source.exposureTimeUnit
	preference.exposureMode ||= source.exposureMode
	preference.subFrame ??= source.subFrame
	return preference as CameraPreference
}

export function liveStackerSettingsWithDefault(settings?: Partial<LiveStackerSettings>, source: LiveStackerSettings = DEFAULT_LIVE_STACKER_SETTINGS) {
	if (!settings) return structuredClone(source)
	settings.executablePath ||= source.executablePath
	settings.slot ??= source.slot
	return settings as LiveStackerSettings
}

export function liveStackingRequestWithDefault(request?: Partial<LiveStackingRequest>, source: LiveStackingRequest = DEFAULT_LIVE_STACKING_REQUEST) {
	if (!request) return structuredClone(source)
	liveStackerSettingsWithDefault(request, source)
	request.enabled ??= source.enabled
	request.type ??= source.type
	request.use32Bits ??= source.use32Bits
	return request as LiveStackingRequest
}

export function cameraCaptureNamingFormatWithDefault(format?: Partial<CameraCaptureNamingFormat>, source: CameraCaptureNamingFormat = DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT) {
	if (!format) return structuredClone(source)
	format.light ||= source.light
	format.dark ||= source.dark
	format.flat ||= source.flat
	format.bias ||= source.bias
	return format as CameraCaptureNamingFormat
}

export function ditherWithDefault(dither?: Partial<Dither>, source: Dither = DEFAULT_DITHER) {
	if (!dither) return structuredClone(source)
	dither.enabled ??= source.enabled
	dither.amount ??= source.amount
	dither.raOnly ??= source.raOnly
	dither.afterExposures ??= source.afterExposures
	return dither as Dither
}

export function isCamera(device?: Device): device is Camera {
	return !!device && device.type === 'CAMERA'
}

export function isGuideHead(device?: Device): device is GuideHead {
	return isCamera(device) && isCompanionDevice(device) && !!device.main
}
