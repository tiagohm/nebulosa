import { Device } from './device.types'

export interface Thermometer extends Device {
    hasThermometer: boolean
    temperature: number
}
