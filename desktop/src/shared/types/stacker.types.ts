import type { FrameType } from './camera.types'

export type StackerType = 'PIXINSIGHT'

export type StackerGroupType = 'LUMINANCE' | 'RED' | 'GREEN' | 'BLUE' | 'MONO' | 'RGB'

export interface StackingRequest {
	outputDirectory: string
	type: StackerType
	executablePath: string
	darkPath?: string
	darkEnabled: boolean
	flatPath?: string
	flatEnabled: boolean
	biasPath?: string
	biasEnabled: boolean
	use32Bits: boolean
	slot: number
	referencePath: string
	targets: StackingTarget[]
}

export const EMPTY_STACKING_REQUEST: StackingRequest = {
	outputDirectory: '',
	type: 'PIXINSIGHT',
	executablePath: '',
	use32Bits: false,
	slot: 1,
	referencePath: '',
	targets: [],
	darkEnabled: false,
	flatEnabled: false,
	biasEnabled: false,
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
	type?: StackerType
	outputDirectory?: string
	defaultPath?: string
	darkPath?: string
	darkEnabled?: boolean
	flatPath?: string
	flatEnabled?: boolean
	biasPath?: string
	biasEnabled?: boolean
}

export const EMPTY_STACKER_PREFERENCE: StackerPreference = {
	darkEnabled: false,
	flatEnabled: false,
	biasEnabled: false,
}
