import { Device } from '../types'

export function compareText(a: string, b: string) {
    return a.localeCompare(b)
}

export function compareDevice(a: Device, b: Device) {
    return compareText(a.name, b.name)
}