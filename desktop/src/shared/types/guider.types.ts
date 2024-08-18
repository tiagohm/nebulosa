import type { Device } from './device.types'

export type GuideDirection =
	| 'NORTH' // DEC+
	| 'SOUTH' // DEC-
	| 'WEST' // RA+
	| 'EAST' // RA-

export type GuideState = 'STOPPED' | 'SELECTED' | 'CALIBRATING' | 'GUIDING' | 'LOST_LOCK' | 'PAUSED' | 'LOOPING'

export type GuiderType = 'PHD2'

export type GuiderPlotMode = 'RA/DEC' | 'DX/DY'

export type GuiderYAxisUnit = 'ARCSEC' | 'PIXEL'

export type GuidePulseDurations = Record<Lowercase<GuideDirection>, number>

export interface GuidePoint {
	x: number
	y: number
}

export interface GuideStep {
	frame: number
	starMass: number
	snr: number
	hfd: number
	dx: number
	dy: number
	raDistance: number
	decDistance: number
	raDistanceGuide: number
	decDistanceGuide: number
	raDuration: number
	raDirection: GuideDirection
	decDuration: number
	decDirection: GuideDirection
	averageDistance: number
}

export interface GuideStar {
	lockPosition: GuidePoint
	starPosition: GuidePoint
	image: string
	guideStep: GuideStep
}

export interface GuiderHistoryStep {
	id: number
	rmsRA: number
	rmsDEC: number
	rmsTotal: number
	guideStep?: GuideStep
	ditherX: number
	ditherY: number
}

export interface GuideOutput extends Device {
	canPulseGuide: boolean
	pulseGuiding: boolean
}

export interface Guider {
	connected: boolean
	state: GuideState
	settling: boolean
	pixelScale: number
}

export interface SettleInfo {
	amount: number
	time: number
	timeout: number
}

export interface GuiderMessageEvent<T> extends MessageEvent {
	data: T
}

export interface GuiderPreference {
	host: string
	port: number
	plotMode: GuiderPlotMode
	yAxisUnit: GuiderYAxisUnit
	settle: SettleInfo
	pulseDuration: GuidePulseDurations
}

export interface GuiderPHD2 {
	connected: boolean
	state: GuideState
	step?: GuideStep
	message: string
}

export interface GuiderChartInfo {
	pixelScale: number
	rmsRA: number
	rmsDEC: number
	rmsTotal: number
	durationScale: number
}

export const DEFAULT_GUIDE_OUTPUT: GuideOutput = {
	type: 'CAMERA',
	sender: '',
	id: '',
	canPulseGuide: false,
	pulseGuiding: false,
	name: '',
	connected: false,
}

export const DEFAULT_SETTLE: SettleInfo = {
	amount: 1.5,
	time: 10,
	timeout: 30,
}

export const DEFAULT_GUIDE_PULSE_DURATIONS: GuidePulseDurations = {
	north: 1000,
	south: 1000,
	east: 1000,
	west: 1000,
}

export const DEFAULT_GUIDER_PHD2: GuiderPHD2 = {
	connected: false,
	state: 'STOPPED',
	message: '',
}

export const DEFAULT_GUIDER_PREFERENCE: GuiderPreference = {
	host: 'localhost',
	port: 4400,
	plotMode: 'RA/DEC',
	yAxisUnit: 'ARCSEC',
	settle: DEFAULT_SETTLE,
	pulseDuration: DEFAULT_GUIDE_PULSE_DURATIONS,
}

export const DEFAULT_GUIDER_CHART_INFO: GuiderChartInfo = {
	pixelScale: 1.0,
	rmsRA: 0.0,
	rmsDEC: 0.0,
	rmsTotal: 0.0,
	durationScale: 1.0,
}

export function reverseGuideDirection(direction: GuideDirection): GuideDirection {
	switch (direction) {
		case 'NORTH':
			return 'SOUTH'
		case 'SOUTH':
			return 'NORTH'
		case 'WEST':
			return 'EAST'
		case 'EAST':
			return 'WEST'
		default:
			return direction
	}
}

export function settleWithDefault(settle?: Partial<SettleInfo>, source: SettleInfo = DEFAULT_SETTLE) {
	if (!settle) return structuredClone(source)
	settle.amount ??= source.amount
	settle.time ??= source.time
	settle.timeout ??= source.timeout
	return settle as SettleInfo
}

export function pulseDurationWithDefault(duration?: Partial<GuidePulseDurations>, source: GuidePulseDurations = DEFAULT_GUIDE_PULSE_DURATIONS) {
	if (!duration) return structuredClone(source)
	duration.north ??= source.north
	duration.south ??= source.south
	duration.east ??= source.east
	duration.west ??= source.west
	return duration as GuidePulseDurations
}

export function guiderPreferenceWithDefault(preference?: Partial<GuiderPreference>, source: GuiderPreference = DEFAULT_GUIDER_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.host ||= source.host
	preference.port ??= source.port
	preference.plotMode ||= source.plotMode
	preference.yAxisUnit ||= source.yAxisUnit
	preference.settle = settleWithDefault(preference.settle, source.settle)
	preference.pulseDuration = pulseDurationWithDefault(preference.pulseDuration, source.pulseDuration)
	return preference as GuiderPreference
}

export function isGuideOuptut(device?: Device): device is GuideOutput {
	return !!device && device.type === 'GUIDE_OUTPUT'
}
