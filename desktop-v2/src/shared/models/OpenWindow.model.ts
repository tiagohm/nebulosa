export interface OpenWindow {
    id: string
    path: string
    icon?: string
    resizable?: boolean
    width?: number | string
    height?: number | string
    bringToFront?: boolean
    requestFocus?: boolean
    params?: Record<string, any>
}
