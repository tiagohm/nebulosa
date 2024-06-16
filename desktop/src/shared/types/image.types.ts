import { Point, Size } from 'electron'
import { Angle, AstronomicalObject, DeepSkyObject, EquatorialCoordinateJ2000, Star } from './atlas.types'
import { Camera, CameraStartCapture } from './camera.types'
import { PlateSolverRequest, StarDetectionRequest } from './settings.types'

export type ImageChannel = 'RED' | 'GREEN' | 'BLUE' | 'GRAY'

export type SCNRProtectionMethod = 'MAXIMUM_MASK' | 'ADDITIVE_MASK' | 'AVERAGE_NEUTRAL' | 'MAXIMUM_NEUTRAL' | 'MINIMUM_NEUTRAL'

export type ImageSource = 'FRAMING' | 'PATH' | 'CAMERA' | 'FLAT_WIZARD' | 'SEQUENCER' | 'ALIGNMENT' | 'AUTO_FOCUS'

export type ImageFormat = 'FITS' | 'XISF' | 'PNG' | 'JPG'

export type Bitpix = 'BYTE' | 'SHORT' | 'INTEGER' | 'LONG' | 'FLOAT' | 'DOUBLE'

export type LiveStackingMode = 'NONE' | 'RAW' | 'STACKED'

export interface FITSHeaderItem {
	name: string
	value: string
}

export interface ImageInfo {
	camera?: Camera
	path: string
	width: number
	height: number
	mono: boolean
	stretchShadow: number
	stretchHighlight: number
	stretchMidtone: number
	rightAscension?: Angle
	declination?: Angle
	solved?: ImageSolved
	headers: FITSHeaderItem[]
	bitpix: Bitpix
	statistics: ImageStatistics
}

export interface ImageAnnotation {
	x: number
	y: number
	star?: Star
	dso?: DeepSkyObject
	minorPlanet?: AstronomicalObject
}

export interface ImageSolved extends EquatorialCoordinateJ2000 {
	solved: boolean
	orientation: number
	scale: number
	width: number
	height: number
	radius: number
}

export const EMPTY_IMAGE_SOLVED: ImageSolved = {
	solved: false,
	orientation: 0,
	scale: 0,
	width: 0,
	height: 0,
	radius: 0,
	rightAscensionJ2000: '00h00m00s',
	declinationJ2000: '+000°00\'00"',
}

export interface CoordinateInterpolation {
	ma: number[]
	md: number[]
	x0: number
	y0: number
	x1: number
	y1: number
	delta: number
	date?: string
}

export interface DetectedStar {
	x: number
	y: number
	snr: number
	hfd: number
	flux: number
}

export interface ComputedDetectedStars {
	hfd: number
	stdDev: number
	snr: number
	fluxMin: number
	fluxMax: number
}

export interface ImageStatisticsBitOption {
	name: string
	rangeMax: number
	bitLength: number
}

export const IMAGE_STATISTICS_BIT_OPTIONS: ImageStatisticsBitOption[] = [
	{ name: 'Normalized: [0, 1]', rangeMax: 1, bitLength: 16 },
	{ name: '8-bit: [0, 255]', rangeMax: 255, bitLength: 8 },
	{ name: '9-bit: [0, 511]', rangeMax: 511, bitLength: 9 },
	{ name: '10-bit: [0, 1023]', rangeMax: 1023, bitLength: 10 },
	{ name: '12-bit: [0, 4095]', rangeMax: 4095, bitLength: 12 },
	{ name: '14-bit: [0, 16383]', rangeMax: 16383, bitLength: 14 },
	{ name: '16-bit: [0, 65535]', rangeMax: 65535, bitLength: 16 },
] as const

export interface ImageStatistics {
	count: number
	maxCount: number
	mean: number
	sumOfSquares: number
	median: number
	variance: number
	stdDev: number
	avgDev: number
	minimum: number
	maximum: number
}

export interface StarDetectionImagePreference extends Pick<StarDetectionRequest, 'type' | 'minSNR' | 'maxStars'> {}

export interface PlateSolverImagePreference extends Pick<PlateSolverRequest, 'type'> {
	radius: number
	focalLength: number
	pixelSize: number
}

export interface ImagePreference {
	savePath?: string
	solver?: PlateSolverImagePreference
	starDetection?: StarDetectionImagePreference
}

export const EMPTY_IMAGE_PREFERENCE: ImagePreference = {
	solver: {
		type: 'ASTAP',
		radius: 4,
		focalLength: 0,
		pixelSize: 0,
	},
	starDetection: {
		type: 'ASTAP',
		minSNR: 0,
		maxStars: 0,
	},
}

export interface ImageData {
	camera?: Camera
	path?: string
	liveStackedPath?: string
	source?: ImageSource
	title?: string
	capture?: CameraStartCapture
	exposureCount?: number
}

export interface FOV {
	enabled: boolean
	focalLength: number
	aperture: number
	cameraSize: Size
	pixelSize: Size
	barlowReducer: number
	bin: number
	rotation: number
	color: string
	computed?: {
		cameraResolution: Size
		focalRatio: number
		fieldSize: Size
		svg: Size & Point
	}
}

export const DEFAULT_FOV: FOV = {
	enabled: true,
	focalLength: 600,
	aperture: 80,
	cameraSize: {
		width: 1392,
		height: 1040,
	},
	pixelSize: {
		width: 6.45,
		height: 6.45,
	},
	barlowReducer: 1,
	bin: 1,
	rotation: 0,
	color: '#FFFF00',
}

export interface FOVEquipment {
	id: number
	name: string
}

export interface FOVCamera extends FOVEquipment {
	sensor: string
	width: number
	height: number
	pixelSize: number
}

export interface FOVTelescope extends FOVEquipment {
	aperture: number
	focalLength: number
}

export interface ImageSCNRDialog {
	showDialog: boolean
	channel?: ImageChannel
	amount: number
	method: SCNRProtectionMethod
}

export interface ImageFITSHeadersDialog {
	showDialog: boolean
	headers: FITSHeaderItem[]
}

export interface ImageStretchDialog {
	showDialog: boolean
	auto: boolean
	shadow: number
	highlight: number
	midtone: number
}

export interface ImageSolverDialog extends PlateSolverImagePreference {
	showDialog: boolean
	running: boolean
	blind: boolean
	centerRA: Angle
	centerDEC: Angle
	readonly solved: ImageSolved
}

export interface ImageFOVDialog extends FOV {
	showDialog: boolean
	fovs: FOV[]
	edited?: FOV
	showCameraDialog: boolean
	cameras: FOVCamera[]
	camera?: FOVCamera
	showTelescopeDialog: boolean
	telescopes: FOVTelescope[]
	telescope?: FOVTelescope
}

export interface ImageROI {
	x: number
	y: number
	width: number
	height: number
}

export interface ImageSaveDialog {
	showDialog: boolean
	format: ImageFormat
	bitpix: Bitpix
	shouldBeTransformed: boolean
	transformation: ImageTransformation
	path: string
}

export interface ImageTransformation {
	force: boolean
	calibrationGroup?: string
	debayer: boolean
	stretch: Omit<ImageStretchDialog, 'showDialog'>
	mirrorHorizontal: boolean
	mirrorVertical: boolean
	invert: boolean
	scnr: Pick<ImageSCNRDialog, 'channel' | 'amount' | 'method'>
}

export interface ImageAnnotationDialog {
	showDialog: boolean
	running: boolean
	visible: boolean
	useStarsAndDSOs: boolean
	useMinorPlanets: boolean
	minorPlanetsMagLimit: number
	includeMinorPlanetsWithoutMagnitude: boolean
	useSimbad: boolean
	data: ImageAnnotation[]
}

export interface ROISelected {
	camera: Camera
	x: number
	y: number
	width: number
	height: number
}

export interface StarDetectionDialog extends StarDetectionImagePreference {
	showDialog: boolean
	running: boolean
	visible: boolean
	stars: DetectedStar[]
	computed: ComputedDetectedStars
	selected: DetectedStar
}

export interface AnnotationInfoDialog {
	showDialog: boolean
	info?: AstronomicalObject & Partial<Star & DeepSkyObject>
}
