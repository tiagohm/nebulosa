import type { FrameType } from './camera.types'

export type StackerType = 'PIXINSIGHT'

export type StackerGroupType = 'LUMINANCE' | 'RED' | 'GREEN' | 'BLUE' | 'MONO' | 'RGB'

export interface StackingRequest {
	outputDirectory: string
	type: StackerType
	executablePath: string
	darkPath?: string
	flatPath?: string
	biasPath?: string
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
}

export interface StackingTarget {
	enabled: boolean
	path: string
	type: FrameType
	group: StackerGroupType
	debayer: boolean
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
	outputDirectory?: string
	defaultPath?: string
}

export const EMPTY_STACKER_PREFERENCE: StackerPreference = {}
