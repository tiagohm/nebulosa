export type Severity = 'success' | 'info' | 'warning' | 'danger'

export type TooltipPosition = 'right' | 'left' | 'top' | 'bottom'

export interface DropdownItem<T> {
	label: string
	value: T
}

export function extractDateTime(date: Date) {
	return [extractDate(date), extractTime(date)]
}

function padNumber(value: number) {
	return value <= 9 ? `0${value}` : `${value}`
}

export function extractDate(date: Date) {
	return `${date.getFullYear()}-${padNumber(date.getMonth())}-${padNumber(date.getDay())}`
}

export function extractTime(date: Date, hasSeconds: boolean = true) {
	const time = `${padNumber(date.getHours())}:${padNumber(date.getMinutes())}`

	if (hasSeconds) {
		return `${time}:${padNumber(date.getSeconds())}`
	} else {
		return time
	}
}
