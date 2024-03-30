import { KeyValue } from '@angular/common'

export type PlateSolverType = 'ASTROMETRY_NET' | 'ASTROMETRY_NET_ONLINE' | 'ASTAP'

export const DEFAULT_SOLVER_TYPES: PlateSolverType[] = ['ASTROMETRY_NET_ONLINE', 'ASTAP']

export interface PlateSolverOptions {
    type: PlateSolverType
    executablePath: string
    downsampleFactor: number
    apiUrl: string
    apiKey: string
    timeout: number
}

export const EMPTY_PLATE_SOLVER_OPTIONS: PlateSolverOptions = {
    type: 'ASTROMETRY_NET_ONLINE',
    executablePath: '',
    downsampleFactor: 0,
    apiUrl: 'https://nova.astrometry.net/',
    apiKey: '',
    timeout: 600,
}

export type DatabaseEntry = KeyValue<string, any>
