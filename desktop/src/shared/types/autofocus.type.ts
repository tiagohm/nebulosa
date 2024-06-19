import { Point } from 'electron'
import { CameraCaptureEvent, CameraStartCapture } from './camera.types'
import { EMPTY_STAR_DETECTION_REQUEST, StarDetectionRequest } from './settings.types'

export type AutoFocusState = 'IDLE' | 'MOVING' | 'EXPOSURING' | 'EXPOSURED' | 'ANALYSING' | 'ANALYSED' | 'CURVE_FITTED' | 'FAILED' | 'FINISHED'

export type AutoFocusFittingMode = 'TRENDLINES' | 'PARABOLIC' | 'TREND_PARABOLIC' | 'HYPERBOLIC' | 'TREND_HYPERBOLIC'

export type BacklashCompensationMode = 'NONE' | 'ABSOLUTE' | 'OVERSHOOT'

export interface BacklashCompensation {
	mode: BacklashCompensationMode
	backlashIn: number
	backlashOut: number
}

export interface AutoFocusRequest {
	fittingMode: AutoFocusFittingMode
	capture: CameraStartCapture
	rSquaredThreshold: number
	backlashCompensation: BacklashCompensation
	initialOffsetSteps: number
	stepSize: number
	totalNumberOfAttempts: number
	starDetector: StarDetectionRequest
}

export interface AutoFocusPreference extends Omit<AutoFocusRequest, 'capture'> {}

export const EMPTY_AUTO_FOCUS_PREFERENCE: AutoFocusPreference = {
	fittingMode: 'HYPERBOLIC',
	rSquaredThreshold: 0.5,
	initialOffsetSteps: 4,
	stepSize: 100,
	totalNumberOfAttempts: 1,
	backlashCompensation: {
		mode: 'NONE',
		backlashIn: 0,
		backlashOut: 0,
	},
	starDetector: EMPTY_STAR_DETECTION_REQUEST,
}

export interface Curve {
	minimum: Point
	rSquared: number
}

export interface Plottable {
	points: Point[]
}

export interface HyperbolicCurve extends Curve, Plottable {
	a: number
	b: number
	p: number
}

export interface ParabolicCurve extends Curve, Plottable {}

export interface Line extends Plottable {
	slope: number
	intercept: number
	rSquared: number
}

export interface TrendLineCurve extends Curve {
	left: Line
	right: Line
	intersection: Point
}

export interface CurveChart {
	predictedFocusPoint?: Point
	minX: number
	maxX: number
	minY: number
	maxY: number
	trendLine?: TrendLineCurve
	parabolic?: ParabolicCurve
	hyperbolic?: HyperbolicCurve
}

export interface AutoFocusEvent {
	state: AutoFocusState
	focusPoint?: Point
	determinedFocusPoint?: Point
	starCount: number
	starHFD: number
	chart?: CurveChart
	capture?: CameraCaptureEvent
}
