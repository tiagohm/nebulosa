import type { Point, Size } from 'electron'
import type { PanZoom } from 'panzoom'
import type { CoordinateInterpolator, InterpolatedCoordinate } from '../utils/coordinate-interpolation'
import type { Angle, AstronomicalObject, DeepSkyObject, EquatorialCoordinateJ2000, Star } from './atlas.types'
import type { Camera, CameraStartCapture, FrameType } from './camera.types'
import { DEFAULT_PLATE_SOLVER_REQUEST, plateSolverRequestWithDefault, type PlateSolverRequest } from './platesolver.types'
import { DEFAULT_STAR_DETECTION_REQUEST, starDetectionRequestWithDefault, type StarDetectionRequest } from './stardetector.types'

export type ImageChannel = 'RED' | 'GREEN' | 'BLUE' | 'GRAY'

export type SCNRProtectionMethod = 'MAXIMUM_MASK' | 'ADDITIVE_MASK' | 'AVERAGE_NEUTRAL' | 'MAXIMUM_NEUTRAL' | 'MINIMUM_NEUTRAL'

export type ImageSource = 'FRAMING' | 'PATH' | 'CAMERA' | 'FLAT_WIZARD' | 'SEQUENCER' | 'ALIGNMENT' | 'AUTO_FOCUS' | 'STACKER'

export type ImageFormat = 'FITS' | 'XISF' | 'PNG' | 'JPG'

export type Bitpix = 'BYTE' | 'SHORT' | 'INTEGER' | 'LONG' | 'FLOAT' | 'DOUBLE'

export type LiveStackingMode = 'NONE' | 'RAW' | 'STACKED'

export type ImageCalibrationSource = 'CAMERA' | 'MENU'

export type ImageMousePosition = Point

export interface Image {
	type: FrameType
	width: number
	height: number
	binX: number
	binY: number
	exposureTime: number
	temperature?: number
	gain: number
	filter?: string
}

export interface ImagePreference {
	savePath?: string
	crossHair: boolean
	transformation: ImageTransformation
	solver: PlateSolverRequest
	starDetector: StarDetectionRequest
	annotation: AnnotateImageRequest
	fovs: FOV[]
	pixelated: boolean
}

export interface ImageHeaderItem {
	name: string
	value: string
}

export interface ImageInfo {
	camera?: Camera
	path: string
	width: number
	height: number
	mono: boolean
	stretch: ImageStretch
	rightAscension?: Angle
	declination?: Angle
	solved?: ImageSolved
	headers: ImageHeaderItem[]
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

export interface OpenImage {
	camera?: Camera
	path: string
	source: ImageSource
	id?: string
	title?: string
	capture?: CameraStartCapture
}

export interface ImageData {
	camera?: Camera
	path?: string
	source: ImageSource
	title?: string
	capture?: CameraStartCapture
	exposureCount: number
	filter?: string
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

export interface ImageSCNR {
	channel?: ImageChannel
	amount: number
	method: SCNRProtectionMethod
}

export interface ImageSCNRDialog {
	showDialog: boolean
	transformation: ImageSCNR
}

export interface ImageHeadersDialog {
	showDialog: boolean
	headers: ImageHeaderItem[]
}

export interface ImageStretch {
	auto: boolean
	shadow: number
	highlight: number
	midtone: number
}

export interface ImageStretchDialog {
	showDialog: boolean
	transformation: ImageStretch
}

export interface ImageSolverDialog {
	showDialog: boolean
	running: boolean
	request: PlateSolverRequest
	readonly solved: ImageSolved
}

export interface ImageFOVDialog {
	showDialog: boolean
	selected: FOV
	fovs: FOV[]
	showCameraDialog: boolean
	cameras: FOVCamera[]
	camera?: FOVCamera
	showTelescopeDialog: boolean
	telescopes: FOVTelescope[]
	telescope?: FOVTelescope
}

export interface ImageROI extends Size, Point {
	show: boolean
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
	stretch: ImageStretch
	mirrorHorizontal: boolean
	mirrorVertical: boolean
	invert: boolean
	scnr: ImageSCNR
	useJPEG: boolean
}

export interface AnnotateImageRequest {
	starsAndDSOs: boolean
	minorPlanets: boolean
	minorPlanetsMagLimit: number
	includeMinorPlanetsWithoutMagnitude: boolean
	useSimbad: boolean
}

export interface ImageAnnotationDialog {
	showDialog: boolean
	running: boolean
	visible: boolean
	request: AnnotateImageRequest
	data: ImageAnnotation[]
}

export interface ROISelected {
	camera: Camera
	x: number
	y: number
	width: number
	height: number
}

export interface StarDetectorDialog {
	showDialog: boolean
	running: boolean
	visible: boolean
	stars: DetectedStar[]
	computed: ComputedDetectedStars
	selected: DetectedStar
	request: StarDetectionRequest
}

export interface AstronomicalObjectDialog {
	showDialog: boolean
	info?: AstronomicalObject & Partial<Star & DeepSkyObject>
}

export interface ImageStatisticsDialog {
	showDialog: boolean
	statistics: ImageStatistics
	bitOption: ImageStatisticsBitOption
}

export interface ImageMouseCoordinates extends InterpolatedCoordinate<Angle>, ImageMousePosition {
	show: boolean
	interpolator?: CoordinateInterpolator
}

export interface ImageCalibration {
	source: ImageCalibrationSource
}

export interface ImageLiveStacking {
	mode: LiveStackingMode
	path?: string
}

export interface ImageZoom {
	scale: number
	panZoom?: PanZoom
}

export interface ImageSettingsDialog {
	showDialog: boolean
	preference: ImagePreference
}

export const DEFAULT_IMAGE_SOLVED: ImageSolved = {
	solved: false,
	orientation: 0,
	scale: 0,
	width: 0,
	height: 0,
	radius: 0,
	rightAscensionJ2000: '00h00m00s',
	declinationJ2000: '+000°00\'00"',
}

export const DEFAULT_IMAGE_STRETCH: ImageStretch = {
	auto: true,
	shadow: 0,
	highlight: 1,
	midtone: 0.5,
}

export const DEFAULT_IMAGE_STRETCH_DIALOG: ImageStretchDialog = {
	showDialog: false,
	transformation: DEFAULT_IMAGE_STRETCH,
}

export const DEFAULT_IMAGE_SCNR: ImageSCNR = {
	amount: 0.5,
	method: 'AVERAGE_NEUTRAL',
}

export const DEFAULT_IMAGE_SCNR_DIALOG: ImageSCNRDialog = {
	showDialog: false,
	transformation: DEFAULT_IMAGE_SCNR,
}

export const DEFAULT_IMAGE_TRANSFORMATION: ImageTransformation = {
	force: false,
	debayer: true,
	stretch: DEFAULT_IMAGE_STRETCH,
	mirrorHorizontal: false,
	mirrorVertical: false,
	invert: false,
	scnr: DEFAULT_IMAGE_SCNR,
	useJPEG: true,
}

export const DEFAULT_IMAGE_SOLVER_DIALOG: ImageSolverDialog = {
	showDialog: false,
	running: false,
	request: DEFAULT_PLATE_SOLVER_REQUEST,
	solved: DEFAULT_IMAGE_SOLVED,
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

export const DEFAULT_IMAGE_FOV_DIALOG: ImageFOVDialog = {
	selected: DEFAULT_FOV,
	showDialog: false,
	fovs: [],
	showCameraDialog: false,
	cameras: [],
	showTelescopeDialog: false,
	telescopes: [],
}

export const DEFAULT_COMPUTED_DETECTED_STARS: ComputedDetectedStars = {
	hfd: 0,
	snr: 0,
	stdDev: 0,
	fluxMax: 0,
	fluxMin: 0,
}

export const DEFAULT_DETECTED_STAR: DetectedStar = {
	x: 0,
	y: 0,
	snr: 0,
	hfd: 0,
	flux: 0,
}

export const DEFAULT_STAR_DETECTOR_DIALOG: StarDetectorDialog = {
	showDialog: false,
	running: false,
	visible: false,
	stars: [],
	computed: DEFAULT_COMPUTED_DETECTED_STARS,
	selected: DEFAULT_DETECTED_STAR,
	request: DEFAULT_STAR_DETECTION_REQUEST,
}

export const DEFAULT_ANNOTATE_IMAGE_REQUEST: AnnotateImageRequest = {
	starsAndDSOs: true,
	minorPlanets: false,
	minorPlanetsMagLimit: 15.0,
	includeMinorPlanetsWithoutMagnitude: false,
	useSimbad: false,
}

export const DEFAULT_IMAGE_ANNOTATION_DIALOG: ImageAnnotationDialog = {
	showDialog: false,
	running: false,
	visible: false,
	data: [],
	request: DEFAULT_ANNOTATE_IMAGE_REQUEST,
}

export const DEFAULT_IMAGE_ROI: ImageROI = {
	show: false,
	x: 0,
	y: 0,
	width: 0,
	height: 0,
}

export const DEFAULT_IMAGE_SAVE_DIALOG: ImageSaveDialog = {
	showDialog: false,
	format: 'FITS',
	bitpix: 'BYTE',
	path: '',
	shouldBeTransformed: true,
	transformation: DEFAULT_IMAGE_TRANSFORMATION,
}

export const DEFAULT_IMAGE_STATISTICS: ImageStatistics = {
	count: 0,
	maxCount: 0,
	mean: 0,
	sumOfSquares: 0,
	median: 0,
	variance: 0,
	stdDev: 0,
	avgDev: 0,
	minimum: 0,
	maximum: 0,
}

export const DEFAULT_IMAGE_STATISTICS_DIALOG: ImageStatisticsDialog = {
	showDialog: false,
	statistics: DEFAULT_IMAGE_STATISTICS,
	bitOption: IMAGE_STATISTICS_BIT_OPTIONS[0],
}

export const DEFAULT_IMAGE_DATA: ImageData = {
	source: 'PATH',
	exposureCount: 0,
}

export const DEFAULT_IMAGE_MOUSE_POSITION: ImageMousePosition = {
	x: 0,
	y: 0,
}

export const DEFAULT_IMAGE_MOUSE_COORDINATES: ImageMouseCoordinates = {
	show: false,
	...DEFAULT_IMAGE_MOUSE_POSITION,
	alpha: '',
	delta: '',
	rightAscensionJ2000: '',
	declinationJ2000: '',
}

export const DEFAULT_IMAGE_CALIBRATION: ImageCalibration = {
	source: 'CAMERA',
}

export const DEFAULT_IMAGE_LIVE_STACKING: ImageLiveStacking = {
	mode: 'NONE',
}

export const DEFAULT_IMAGE_ZOOM: ImageZoom = {
	scale: 1,
}

export const DEFAULT_IMAGE_PREFERENCE: ImagePreference = {
	crossHair: false,
	transformation: DEFAULT_IMAGE_TRANSFORMATION,
	solver: DEFAULT_PLATE_SOLVER_REQUEST,
	starDetector: DEFAULT_STAR_DETECTION_REQUEST,
	annotation: DEFAULT_ANNOTATE_IMAGE_REQUEST,
	fovs: [],
	pixelated: true,
}

export const DEFAULT_IMAGE_SETTINGS_DIALOG: ImageSettingsDialog = {
	showDialog: false,
	preference: DEFAULT_IMAGE_PREFERENCE,
}

export function imageFormatFromExtension(extension: string): ImageFormat {
	return (
		extension === '.xisf' ? 'XISF'
		: extension === '.png' ? 'PNG'
		: extension === '.jpg' ? 'JPG'
		: 'FITS'
	)
}

export function imageStretchWithDefault(stretch?: Partial<ImageStretch>, source: ImageStretch = DEFAULT_IMAGE_STRETCH) {
	if (!stretch) return structuredClone(source)
	stretch.auto ??= source.auto
	stretch.shadow ??= source.shadow
	stretch.highlight ??= source.highlight
	stretch.midtone ??= source.midtone
	return stretch as ImageStretch
}

export function annotateImageRequestWithDefault(request?: Partial<AnnotateImageRequest>, source: AnnotateImageRequest = DEFAULT_ANNOTATE_IMAGE_REQUEST) {
	if (!request) return structuredClone(source)
	request.starsAndDSOs ??= source.starsAndDSOs
	request.minorPlanets ??= source.minorPlanets
	request.minorPlanetsMagLimit ??= source.minorPlanetsMagLimit
	request.includeMinorPlanetsWithoutMagnitude ??= source.includeMinorPlanetsWithoutMagnitude
	request.useSimbad ??= source.useSimbad
	return request as AnnotateImageRequest
}

export function imageTransformationWithDefault(transformation?: Partial<ImageTransformation>, source: ImageTransformation = DEFAULT_IMAGE_TRANSFORMATION) {
	if (!transformation) return structuredClone(source)
	transformation.force ??= source.force
	transformation.calibrationGroup ||= source.calibrationGroup
	transformation.debayer ??= source.debayer
	transformation.stretch = imageStretchWithDefault(transformation.stretch, source.stretch)
	transformation.mirrorHorizontal ??= source.mirrorHorizontal
	transformation.mirrorVertical ??= source.mirrorVertical
	transformation.invert ??= source.invert
	transformation.scnr ??= source.scnr
	transformation.useJPEG ??= source.useJPEG
	return transformation as ImageTransformation
}

export function imagePreferenceWithDefault(preference?: Partial<ImagePreference>, source: ImagePreference = DEFAULT_IMAGE_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.savePath ||= source.savePath
	preference.crossHair ??= source.crossHair
	preference.transformation = imageTransformationWithDefault(preference.transformation, source.transformation)
	preference.solver = plateSolverRequestWithDefault(preference.solver, source.solver)
	preference.starDetector = starDetectionRequestWithDefault(preference.starDetector, source.starDetector)
	preference.annotation = annotateImageRequestWithDefault(preference.annotation, source.annotation)
	preference.fovs ??= structuredClone(source.fovs)
	preference.pixelated ??= source.pixelated
	preference.fovs.forEach((e) => (e.enabled = false))
	preference.fovs.forEach((e) => (e.computed = undefined))
	return preference as ImagePreference
}
