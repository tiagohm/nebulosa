import { Thermometer } from './auxiliary.types'
import { Device } from './device.types'

export interface Focuser extends Device, Thermometer {
    moving: boolean
    position: number
    canAbsoluteMove: boolean
    canRelativeMove: boolean
    canAbort: boolean
    canReverse: boolean
    reverse: boolean
    canSync: boolean
    hasBacklash: boolean
    maxPosition: number
}

export const EMPTY_FOCUSER: Focuser = {
    moving: false,
    position: 0,
    canAbsoluteMove: false,
    canRelativeMove: false,
    canAbort: false,
    canReverse: false,
    reverse: false,
    canSync: false,
    hasBacklash: false,
    maxPosition: 0,
    name: '',
    connected: false,
    hasThermometer: false,
    temperature: 0
}
