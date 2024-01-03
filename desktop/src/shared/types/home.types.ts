export type HomeWindowType = 'CAMERA' | 'MOUNT' | 'GUIDER' | 'WHEEL' | 'FOCUSER' | 'DOME' | 'ROTATOR' | 'SWITCH' |
    'SKY_ATLAS' | 'ALIGNMENT' | 'SEQUENCER' | 'IMAGE' | 'FRAMING' | 'INDI' | 'SETTINGS' | 'ABOUT' | 'FLAT_WIZARD'

export interface ConnectionDetails {
    host: string
    port: number
    connectedAt?: number
}

export const EMPTY_CONNECTION_DETAILS: ConnectionDetails = {
    host: 'localhost',
    port: 7624
}
