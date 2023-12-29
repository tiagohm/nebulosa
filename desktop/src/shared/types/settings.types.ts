export type PlateSolverType = 'ASTROMETRY_NET' | 'ASTAP'

export interface PlateSolverSettings {
    type: PlateSolverType
    executablePath: string
    downsampleFactor: number
}
