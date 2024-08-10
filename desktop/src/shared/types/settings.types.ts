import type { Location } from './atlas.types'
import { DEFAULT_LOCATION, locationWithDefault } from './atlas.types'
import type { LiveStackerSettings, LiveStackerType } from './camera.types'
import { cameraCaptureNamingFormatWithDefault, DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT, DEFAULT_LIVE_STACKER_SETTINGS, liveStackerSettingsWithDefault, type CameraCaptureNamingFormat, type FrameType } from './camera.types'
import { DEFAULT_PLATE_SOLVER_SETTINGS, plateSolverSettingsWithDefault, type PlateSolverSettings, type PlateSolverType } from './platesolver.types'
import { DEFAULT_STACKER_SETTINGS, stackerSettingsWithDefault, type StackerSettings, type StackerType } from './stacker.types'
import { DEFAULT_STAR_DETECTOR_SETTINGS, starDetectorSettingsWithDefault, type StarDetectorSettings, type StarDetectorType } from './stardetector.types'

export type SettingsTabKey = 'LOCATION' | 'PLATE_SOLVER' | 'STAR_DETECTOR' | 'LIVE_STACKER' | 'STACKER' | 'CAPTURE_NAMING_FORMAT'

export interface SettingsPreference {
	plateSolver: Record<PlateSolverType, PlateSolverSettings>
	starDetector: Record<StarDetectorType, StarDetectorSettings>
	liveStacker: Record<LiveStackerType, LiveStackerSettings>
	stacker: Record<StackerType, StackerSettings>
	namingFormat: CameraCaptureNamingFormat
	locations: Location[]
	location: Location
}

export const DEFAULT_SETTINGS_PREFERENCE: SettingsPreference = {
	plateSolver: {
		ASTROMETRY_NET: structuredClone(DEFAULT_PLATE_SOLVER_SETTINGS),
		ASTROMETRY_NET_ONLINE: structuredClone(DEFAULT_PLATE_SOLVER_SETTINGS),
		ASTAP: structuredClone(DEFAULT_PLATE_SOLVER_SETTINGS),
		SIRIL: structuredClone(DEFAULT_PLATE_SOLVER_SETTINGS),
		PIXINSIGHT: structuredClone(DEFAULT_PLATE_SOLVER_SETTINGS),
	},
	starDetector: {
		ASTAP: structuredClone(DEFAULT_STAR_DETECTOR_SETTINGS),
		SIRIL: structuredClone(DEFAULT_STAR_DETECTOR_SETTINGS),
		PIXINSIGHT: structuredClone(DEFAULT_STAR_DETECTOR_SETTINGS),
	},
	liveStacker: {
		SIRIL: structuredClone(DEFAULT_LIVE_STACKER_SETTINGS),
		PIXINSIGHT: structuredClone(DEFAULT_LIVE_STACKER_SETTINGS),
	},
	stacker: {
		PIXINSIGHT: structuredClone(DEFAULT_STACKER_SETTINGS),
	},
	namingFormat: DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT,
	locations: [DEFAULT_LOCATION],
	location: DEFAULT_LOCATION,
}

export function settingsPreferenceWithDefault(preference?: Partial<SettingsPreference>, source: SettingsPreference = DEFAULT_SETTINGS_PREFERENCE) {
	if (!preference) return structuredClone(source)

	preference.plateSolver ??= structuredClone(source.plateSolver)
	preference.starDetector ??= structuredClone(source.starDetector)
	preference.liveStacker ??= structuredClone(source.liveStacker)
	preference.stacker ??= structuredClone(source.stacker)

	for (const [key, value] of Object.entries(preference.plateSolver)) {
		plateSolverSettingsWithDefault(value, source.plateSolver[key as never])
	}
	for (const [key, value] of Object.entries(preference.starDetector)) {
		starDetectorSettingsWithDefault(value, source.starDetector[key as never])
	}
	for (const [key, value] of Object.entries(preference.liveStacker)) {
		liveStackerSettingsWithDefault(value, source.liveStacker[key as never])
	}
	for (const [key, value] of Object.entries(preference.stacker)) {
		stackerSettingsWithDefault(value, source.stacker[key as never])
	}

	preference.namingFormat = cameraCaptureNamingFormatWithDefault(preference.namingFormat, source.namingFormat)
	preference.location = locationWithDefault(preference.location, source.location)

	if (!preference.locations?.length) {
		preference.locations = structuredClone(source.locations)
	}

	return preference as SettingsPreference
}

export function resetCameraCaptureNamingFormat(type: FrameType, format: CameraCaptureNamingFormat, defaultValue?: CameraCaptureNamingFormat) {
	switch (type) {
		case 'LIGHT':
			format.light = defaultValue?.light || DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.light
			break
		case 'DARK':
			format.dark = defaultValue?.dark || DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.dark
			break
		case 'FLAT':
			format.flat = defaultValue?.flat || DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.flat
			break
		case 'BIAS':
			format.bias = defaultValue?.bias || DEFAULT_CAMERA_CAPTURE_NAMING_FORMAT.bias
			break
	}
}
