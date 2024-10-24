import type { Device } from '../types/device.types'

export type Comparator<T = unknown> = (a: T, b: T) => number

export const textComparator: Comparator<string> = (a: string, b: string) => a.localeCompare(b, undefined, { sensitivity: 'base' })
export const numberComparator: Comparator<number> = (a: number, b: number) => a - b
export const deviceComparator: Comparator<Device> = (a: Device, b: Device) => textComparator(a.name, b.name)
export const numericTextComparator: Comparator<string> = (a: string, b: string) => a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' })

export function negateComparator<T>(comparator: Comparator<T>): Comparator<T> {
	return (a, b) => -comparator(a, b)
}

export function compareBy<T>(name: keyof T, comparator: (a: T[typeof name], b: T[typeof name]) => number): Comparator<T> {
	return (a, b) => comparator(a[name], b[name])
}
