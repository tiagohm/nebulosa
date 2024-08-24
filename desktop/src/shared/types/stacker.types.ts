import type { FrameType } from './camera.types'

export type StackerType = 'PIXINSIGHT'

export type StackerGroupType = 'LUMINANCE' | 'RED' | 'GREEN' | 'BLUE' | 'MONO' | 'RGB' | 'NONE'

export type StackerState = 'IDLE' | 'CALIBRATING' | 'ALIGNING' | 'INTEGRATING'

export interface StackerEvent {
	state: StackerState
	type: StackerGroupType
	stackCount: number
	numberOfTargets: number
}

export interface StackerSettings {
	executablePath: string
	slot: number
}

export interface StackingRequest extends StackerSettings {
	outputDirectory: string
	type: StackerType
	darkPath?: string
	darkEnabled: boolean
	flatPath?: string
	flatEnabled: boolean
	biasPath?: string
	biasEnabled: boolean
	use32Bits: boolean
	referencePath: string
	targets: StackingTarget[]
}

export interface StackingTarget {
	enabled: boolean
	path: string
	type: FrameType
	group: StackerGroupType
	reference: boolean
	analyzed?: AnalyzedTarget
}

export interface AnalyzedTarget {
	width: number
	height: number
	binX: number
	binY: number
	gain: number
	exposureTime: number
	type: FrameType
	group: StackerGroupType
}

export interface StackerPreference {
	request: StackingRequest
	defaultPath?: string
}

export const DEFAULT_STACKER_EVENT: StackerEvent = {
	state: 'IDLE',
	type: 'LUMINANCE',
	stackCount: 0,
	numberOfTargets: 0,
}

export const DEFAULT_STACKER_SETTINGS: StackerSettings = {
	executablePath: '',
	slot: 0,
}

export const DEFAULT_STACKING_REQUEST: StackingRequest = {
	...DEFAULT_STACKER_SETTINGS,
	outputDirectory: '',
	type: 'PIXINSIGHT',
	use32Bits: false,
	referencePath: '',
	targets: [],
	darkEnabled: false,
	flatEnabled: false,
	biasEnabled: false,
}

export const DEFAULT_STACKER_PREFERENCE: StackerPreference = {
	request: DEFAULT_STACKING_REQUEST,
}

export function stackerSettingsWithDefault(preference?: Partial<StackerSettings>, source: StackerSettings = DEFAULT_STACKER_SETTINGS) {
	if (!preference) return structuredClone(source)
	preference.executablePath ||= source.executablePath
	preference.slot ??= source.slot
	return preference as StackerSettings
}

export function stackingRequestWithDefault(request?: Partial<StackingRequest>, source: StackingRequest = DEFAULT_STACKING_REQUEST) {
	if (!request) return structuredClone(source)
	stackerSettingsWithDefault(request, source)
	request.outputDirectory ||= source.outputDirectory
	request.type ||= source.type
	request.darkPath ||= source.darkPath
	request.darkEnabled ??= source.darkEnabled
	request.flatPath ||= source.flatPath
	request.flatEnabled ??= source.flatEnabled
	request.biasPath ||= source.biasPath
	request.biasEnabled ??= source.biasEnabled
	request.use32Bits ??= source.use32Bits
	request.referencePath ||= source.referencePath
	request.targets ??= source.targets
	return request as StackingRequest
}

export function stackerPreferenceWithDefault(preference?: Partial<StackerPreference>, source: StackerPreference = DEFAULT_STACKER_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.request = stackingRequestWithDefault(preference.request, source.request)
	preference.defaultPath ??= source.defaultPath
	return preference as StackerPreference
}
