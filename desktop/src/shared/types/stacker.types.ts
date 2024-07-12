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

export interface StackingTarget {
	enabled: boolean
	path: string
	group: StackerGroupType
	debayer: boolean
}
