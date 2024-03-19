import { Camera } from './camera.types'
import { Focuser } from './focuser.types'
import { Mount } from './mount.types'
import { FilterWheel } from './wheel.types'

export type HomeWindowType = 'CAMERA' | 'MOUNT' | 'GUIDER' | 'WHEEL' | 'FOCUSER' | 'DOME' | 'ROTATOR' | 'SWITCH' |
    'SKY_ATLAS' | 'ALIGNMENT' | 'SEQUENCER' | 'IMAGE' | 'FRAMING' | 'INDI' | 'SETTINGS' | 'CALCULATOR' | 'ABOUT' | 'FLAT_WIZARD'

export const CONNECTION_TYPES = ['INDI', 'ALPACA'] as const

export type ConnectionType = (typeof CONNECTION_TYPES)[number]

export interface ConnectionDetails {
    name: string
    host: string
    port: number
    ip?: string
    type: ConnectionType
    connected: boolean
    connectedAt?: number
    id?: string
}

export type ConnectionStatus = Omit<Required<ConnectionDetails>, 'connected' | 'name' | 'connectedAt'>

export const EMPTY_CONNECTION_DETAILS: ConnectionDetails = {
    name: '',
    host: 'localhost',
    port: 7624,
    type: 'INDI',
    connected: false
}

export interface ConnectionClosed {
    id: string
}

export interface Equipment {
    camera?: Camera
    guider?: Camera
    mount?: Mount
    focuser?: Focuser
    wheel?: FilterWheel
}
