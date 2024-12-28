import { Pipe, PipeTransform } from '@angular/core'
import { Hemisphere } from '../types/alignment.types'
import { Constellation, CONSTELLATIONS, SATELLITE_GROUPS, SatelliteGroupType, SKY_OBJECT_TYPES, SkyObjectType } from '../types/atlas.types'
import { AutoFocusFittingMode, BacklashCompensationMode } from '../types/autofocus.type'
import { ExposureMode, FrameType, LiveStackerType } from '../types/camera.types'
import { GuideDirection, GuiderPlotMode, GuiderYAxisUnit } from '../types/guider.types'
import { ConnectionType } from '../types/home.types'
import { Bitpix, IMAGE_STATISTICS_BIT_OPTIONS, ImageChannel, ImageFilterType, ImageFormat, ImageStatisticsBitOption, SCNRProtectionMethod } from '../types/image.types'
import { MountRemoteControlProtocol } from '../types/mount.types'
import { PlateSolverType } from '../types/platesolver.types'
import { SequencerCaptureMode } from '../types/sequencer.types'
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
	MOUNT_REMOTE_CONTROL_PROTOCOL: MountRemoteControlProtocol[]
	FRAME_TYPE: FrameType[]
	EXPOSURE_MODE: ExposureMode[]
	GUIDE_DIRECTION: GuideDirection[]
	GUIDE_DIRECTION_NS: GuideDirection[]
	GUIDE_DIRECTION_WE: GuideDirection[]
	HEMISPHERE: Hemisphere[]
	GUIDER_PLOT_MODE: GuiderPlotMode[]
	GUIDER_Y_AXIS_UNIT: GuiderYAxisUnit[]
	SEQUENCE_CAPTURE_MODE: SequencerCaptureMode[]
	IMAGE_FILTER_TYPE: ImageFilterType[]
	CONNECTION_TYPE: ConnectionType[]
	IMAGE_STATISTICS_BIT_OPTION: ImageStatisticsBitOption[]
	SATELLITE_GROUP_TYPE: SatelliteGroupType[]
	CONSTELLATION: Constellation[]
	SKY_OBJECT_TYPE: SkyObjectType[]
	SEQUENCER_CAPTURE_MODE: SequencerCaptureMode[]
}

@Pipe({ standalone: false, name: 'dropdownOptions' })
export class DropdownOptionsPipe implements PipeTransform {
	transform<K extends keyof DropdownOptions>(type: K): DropdownOptions[K] {
		switch (type) {
			case 'STAR_DETECTOR':
				return ['ASTAP', 'PIXINSIGHT', 'SIRIL'] as DropdownOptions[K]
			case 'PLATE_SOLVER':
				return ['ASTAP', 'ASTROMETRY_NET_ONLINE', 'ASTROMETRY_NET', 'SIRIL', 'PIXINSIGHT'] as DropdownOptions[K]
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
			case 'MOUNT_REMOTE_CONTROL_PROTOCOL':
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
			case 'IMAGE_FILTER_TYPE':
				return ['LUMINANCE', 'RED', 'GREEN', 'BLUE', 'MONO', 'RGB', 'NONE'] as DropdownOptions[K]
			case 'CONNECTION_TYPE':
				return ['INDI', 'ALPACA'] as DropdownOptions[K]
			case 'IMAGE_STATISTICS_BIT_OPTION':
				return IMAGE_STATISTICS_BIT_OPTIONS as DropdownOptions[K]
			case 'SATELLITE_GROUP_TYPE':
				return SATELLITE_GROUPS as unknown as DropdownOptions[K]
			case 'CONSTELLATION':
				return CONSTELLATIONS as unknown as DropdownOptions[K]
			case 'SKY_OBJECT_TYPE':
				return SKY_OBJECT_TYPES as unknown as DropdownOptions[K]
			case 'SEQUENCER_CAPTURE_MODE':
				return ['FULLY', 'INTERLEAVED'] as DropdownOptions[K]
		}

		return []
	}
}
