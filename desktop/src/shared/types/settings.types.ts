
export type PlateSolverType = 'ASTROMETRY_NET' | 'ASTROMETRY_NET_ONLINE' | 'ASTAP'

export interface PlateSolverPreference {
    type: PlateSolverType
    executablePath: string
    downsampleFactor: number
    apiUrl: string
    apiKey: string
    timeout: number
}

export const EMPTY_PLATE_SOLVER_PREFERENCE: PlateSolverPreference = {
    type: 'ASTAP',
    executablePath: '',
    downsampleFactor: 0,
    apiUrl: 'https://nova.astrometry.net/',
    apiKey: '',
    timeout: 600,
}
