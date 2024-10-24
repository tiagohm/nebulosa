import type { Image } from './image.types'

export interface CalibrationFrame extends Image {
	id: number
	group: string
	path: string
	enabled: boolean
}

export interface CalibrationGroupDialog {
	showDialog: boolean
	group: string
	save?: () => Promise<void> | void
}

export interface CalibrationPreference {
	filePath?: string
	directoryPath?: string
}

export const DEFAULT_CALIBRATION_GROUP_DIALOG: CalibrationGroupDialog = {
	showDialog: false,
	group: '',
}

export const DEFAULT_CALIBRATION_PREFERENCE: CalibrationPreference = {}

export function calibrationPreferenceWithDefault(preference?: Partial<CalibrationPreference>, source: CalibrationPreference = DEFAULT_CALIBRATION_PREFERENCE) {
	if (!preference) return structuredClone(source)
	preference.filePath ||= source.filePath
	preference.directoryPath ||= source.directoryPath
	return preference as CalibrationPreference
}
