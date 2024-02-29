import { Device } from '../types/device.types'

export type Comparator<T = any> = (a: T, b: T) => number

export const textComparator: Comparator<string> = (a: string, b: string) => a.localeCompare(b)
export const deviceComparator: Comparator<Device> = (a: Device, b: Device) => textComparator(a.name, b.name)
