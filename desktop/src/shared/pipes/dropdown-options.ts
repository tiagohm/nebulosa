import { Pipe, PipeTransform } from '@angular/core'
import { AutoFocusFittingMode, BacklashCompensationMode } from '../types/autofocus.type'
import { LiveStackerType } from '../types/camera.types'
import { Bitpix, ImageChannel, ImageFormat, SCNRProtectionMethod } from '../types/image.types'
import { MountRemoteControlType } from '../types/mount.types'
import { PlateSolverType, StarDetectorType } from '../types/settings.types'

export type DropdownOptions = {
	STAR_DETECTOR: StarDetectorType[]
	PLATE_SOLVER: PlateSolverType[]
	LIVE_STACKER: LiveStackerType[]
	AUTO_FOCUS_FITTING_MODE: AutoFocusFittingMode[]
	AUTO_FOCUS_BACKLASH_COMPENSATION_MODE: BacklashCompensationMode[]
	SCNR_PROTECTION_METHOD: SCNRProtectionMethod[]
	IMAGE_FORMAT: ImageFormat[]
	IMAGE_BITPIX: Bitpix[]
	IMAGE_CHANNEL: ImageChannel[]
	MOUNT_REMOTE_CONTROL_TYPE: MountRemoteControlType[]
}

@Pipe({ name: 'dropdownOptions' })
export class DropdownOptionsPipe implements PipeTransform {
	transform<K extends keyof DropdownOptions>(type: K): DropdownOptions[K] {
		switch (type) {
			case 'STAR_DETECTOR':
				return ['ASTAP', 'PIXINSIGHT', 'SIRIL'] as DropdownOptions[K]
			case 'PLATE_SOLVER':
				return ['ASTAP', 'ASTROMETRY_NET_ONLINE', 'SIRIL'] as DropdownOptions[K]
			case 'AUTO_FOCUS_FITTING_MODE':
				return ['TRENDLINES', 'PARABOLIC', 'TREND_PARABOLIC', 'HYPERBOLIC', 'TREND_HYPERBOLIC'] as DropdownOptions[K]
			case 'AUTO_FOCUS_BACKLASH_COMPENSATION_MODE':
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
		}

		return []
	}
}
