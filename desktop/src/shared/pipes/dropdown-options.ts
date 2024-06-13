import { Pipe, PipeTransform } from '@angular/core'
import { AutoFocusFittingMode, BacklashCompensationMode } from '../types/autofocus.type'
import { LiveStackerType } from '../types/camera.types'
import { Bitpix, ImageChannel, ImageFormat, SCNRProtectionMethod } from '../types/image.types'
import { PlateSolverType, StarDetectorType } from '../types/settings.types'
import { MountRemoteControlType } from '../types/mount.types'

export type DropdownOptionType = 'STAR_DETECTOR' | 'PLATE_SOLVER' | 'LIVE_STACKER'
    | 'AUTO_FOCUS_FITTING_MODE' | 'AUTO_FOCUS_BACKLASH_COMPENSATION_MODE' | 'SCNR_PROTECTION_METHOD'
    | 'IMAGE_FORMAT' | 'IMAGE_BITPIX' | 'IMAGE_CHANNEL' | 'MOUNT_REMOTE_CONTROL_TYPE'

export type DropdownOptionReturnType = StarDetectorType[] | PlateSolverType[] | LiveStackerType[]
    | AutoFocusFittingMode[] | BacklashCompensationMode[] | SCNRProtectionMethod[]
    | ImageFormat[] | Bitpix[] | ImageChannel[] | MountRemoteControlType[]

@Pipe({ name: 'dropdownOptions' })
export class DropdownOptionsPipe implements PipeTransform {

    transform(type: DropdownOptionType): DropdownOptionReturnType | undefined {
        switch (type) {
            case 'STAR_DETECTOR': return ['ASTAP', 'PIXINSIGHT']
            case 'PLATE_SOLVER': return ['ASTAP', 'ASTROMETRY_NET_ONLINE', 'SIRIL']
            case 'AUTO_FOCUS_FITTING_MODE': return ['TRENDLINES', 'PARABOLIC', 'TREND_PARABOLIC', 'HYPERBOLIC', 'TREND_HYPERBOLIC']
            case 'AUTO_FOCUS_BACKLASH_COMPENSATION_MODE': return ['NONE', 'ABSOLUTE', 'OVERSHOOT']
            case 'LIVE_STACKER': return ['SIRIL', 'PIXINSIGHT']
            case 'SCNR_PROTECTION_METHOD': return ['MAXIMUM_MASK', 'ADDITIVE_MASK', 'AVERAGE_NEUTRAL', 'MAXIMUM_NEUTRAL', 'MINIMUM_NEUTRAL']
            case 'IMAGE_FORMAT': return ['FITS', 'XISF', 'PNG', 'JPG']
            case 'IMAGE_BITPIX': return ['BYTE', 'SHORT', 'INTEGER', 'FLOAT', 'DOUBLE']
            case 'IMAGE_CHANNEL': return ['RED', 'GREEN', 'BLUE', 'GRAY']
            case 'MOUNT_REMOTE_CONTROL_TYPE': return ['LX200', 'STELLARIUM']
        }
    }
}
