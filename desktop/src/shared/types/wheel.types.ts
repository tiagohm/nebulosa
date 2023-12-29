import { Device } from './device.types'

export interface FilterWheel extends Device {
    count: number
    position: number
    moving: boolean
}

export const EMPTY_WHEEL: FilterWheel = {
    count: 0,
    position: 0,
    moving: false,
    name: '',
    connected: false
}
