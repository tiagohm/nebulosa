import { Pipe, PipeTransform } from '@angular/core'
import { Hemisphere } from '../types/alignment.types'
import { AutoFocusFittingMode, BacklashCompensationMode } from '../types/autofocus.type'
import { ExposureMode, FrameType, LiveStackerType } from '../types/camera.types'
import { GuideDirection, GuiderPlotMode, GuiderYAxisUnit } from '../types/guider.types'
import { Bitpix, ImageChannel, ImageFormat, SCNRProtectionMethod } from '../types/image.types'
import { MountRemoteControlType } from '../types/mount.types'
import { PlateSolverType } from '../types/platesolver.types'
import { SequenceCaptureMode } from '../types/sequencer.types'
import { SettingsTabKey } from '../types/settings.types'
import { StackerGroupType, StackerType } from '../types/stacker.types'
import { StarDetectorType } from '../types/stardetector.types'

export interface DropdownOptions {
	STAR_DETECTOR: StarDetectorType[]
	PLATE_SOLVER: PlateSolverType[]
	LIVE_STACKER: LiveStackerType[]
	CURVE_FITTING_MODE: AutoFocusFittingMode[]
	BACKLASH_COMPENSATION_MODE: BacklashCompensationMode[]
	SCNR_PROTECTION_METHOD: SCNRProtectionMethod[]
	IMAGE_FORMAT: ImageFormat[]
	IMAGE_BITPIX: Bitpix[]
	IMAGE_CHANNEL: ImageChannel[]
	MOUNT_REMOTE_CONTROL_TYPE: MountRemoteControlType[]
	FRAME_TYPE: FrameType[]
	EXPOSURE_MODE: ExposureMode[]
	GUIDE_DIRECTION: GuideDirection[]
	GUIDE_DIRECTION_NS: GuideDirection[]
	GUIDE_DIRECTION_WE: GuideDirection[]
	HEMISPHERE: Hemisphere[]
	GUIDER_PLOT_MODE: GuiderPlotMode[]
	GUIDER_Y_AXIS_UNIT: GuiderYAxisUnit[]
	SEQUENCE_CAPTURE_MODE: SequenceCaptureMode[]
	STACKER: StackerType[]
	SETTINGS_TAB: SettingsTabKey[]
	STACKER_GROUP_TYPE: StackerGroupType[]
}

@Pipe({ name: 'dropdownOptions' })
export class DropdownOptionsPipe implements PipeTransform {
	transform<K extends keyof DropdownOptions>(type: K): DropdownOptions[K] {
		switch (type) {
			case 'STAR_DETECTOR':
				return ['ASTAP', 'PIXINSIGHT', 'SIRIL'] as DropdownOptions[K]
			case 'PLATE_SOLVER':
				return ['ASTAP', 'ASTROMETRY_NET_ONLINE', 'SIRIL', 'PIXINSIGHT'] as DropdownOptions[K]
			case 'CURVE_FITTING_MODE':
				return ['TRENDLINES', 'PARABOLIC', 'TREND_PARABOLIC', 'HYPERBOLIC', 'TREND_HYPERBOLIC'] as DropdownOptions[K]
			case 'BACKLASH_COMPENSATION_MODE':
				return ['NONE', 'ABSOLUTE', 'OVERSHOOT'] as DropdownOptions[K]
			case 'LIVE_STACKER':
				return ['SIRIL', 'PIXINSIGHT'] as DropdownOptions[K]
			case 'SCNR_PROTECTION_METHOD':
				return ['MAXIMUM_MASK', 'ADDITIVE_MASK', 'AVERAGE_NEUTRAL', 'MAXIMUM_NEUTRAL', 'MINIMUM_NEUTRAL'] as DropdownOptions[K]
			case 'IMAGE_FORMAT':
				return ['FITS', 'XISF', 'PNG', 'JPG'] as DropdownOptions[K]
			case 'IMAGE_BITPIX':
				return ['BYTE', 'SHORT', 'INTEGER', 'FLOAT', 'DOUBLE'] as DropdownOptions[K]
			case 'IMAGE_CHANNEL':
				return ['RED', 'GREEN', 'BLUE', 'GRAY'] as DropdownOptions[K]
			case 'MOUNT_REMOTE_CONTROL_TYPE':
				return ['LX200', 'STELLARIUM'] as DropdownOptions[K]
			case 'FRAME_TYPE':
				return ['LIGHT', 'DARK', 'FLAT', 'BIAS'] as DropdownOptions[K]
			case 'EXPOSURE_MODE':
				return ['SINGLE', 'FIXED', 'LOOP'] as DropdownOptions[K]
			case 'GUIDE_DIRECTION':
				return ['NORTH', 'SOUTH', 'WEST', 'EAST'] as DropdownOptions[K]
			case 'GUIDE_DIRECTION_NS':
				return ['NORTH', 'SOUTH'] as DropdownOptions[K]
			case 'GUIDE_DIRECTION_WE':
				return ['WEST', 'EAST'] as DropdownOptions[K]
			case 'HEMISPHERE':
				return ['NORTHERN', 'SOUTHERN'] as DropdownOptions[K]
			case 'GUIDER_PLOT_MODE':
				return ['RA/DEC', 'DX/DY'] as DropdownOptions[K]
			case 'GUIDER_Y_AXIS_UNIT':
				return ['ARCSEC', 'PIXEL'] as DropdownOptions[K]
			case 'SEQUENCE_CAPTURE_MODE':
				return ['FULLY', 'INTERLEAVED'] as DropdownOptions[K]
			case 'STACKER':
				return ['PIXINSIGHT'] as DropdownOptions[K]
			case 'SETTINGS_TAB':
				return ['LOCATION', 'PLATE_SOLVER', 'STAR_DETECTOR', 'LIVE_STACKER', 'STACKER', 'CAPTURE_NAMING_FORMAT'] as DropdownOptions[K]
			case 'STACKER_GROUP_TYPE':
				return ['LUMINANCE', 'RED', 'GREEN', 'BLUE', 'MONO', 'RGB'] as DropdownOptions[K]
		}

		return []
	}
}
