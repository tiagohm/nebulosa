export type HomeWindowType = 'CAMERA' | 'MOUNT' | 'GUIDER' | 'WHEEL' | 'FOCUSER' | 'DOME' | 'ROTATOR' | 'SWITCH' |
    'SKY_ATLAS' | 'ALIGNMENT' | 'SEQUENCER' | 'IMAGE' | 'FRAMING' | 'INDI' | 'SETTINGS' | 'ABOUT' | 'FLAT_WIZARD'

export const CONNECTION_TYPES = ['INDI', 'ALPACA'] as const

export type ConnectionType = (typeof CONNECTION_TYPES)[number]

export interface ConnectionDetails {
    host: string
    port: number
    type: ConnectionType
    connectedAt?: number
}

export const EMPTY_CONNECTION_DETAILS: ConnectionDetails = {
    host: 'localhost',
    port: 7624,
    type: 'INDI'
}
