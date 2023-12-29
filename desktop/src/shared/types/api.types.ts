import { Device } from './device.types'

export interface MessageEvent {
    eventName: string
}

export interface DeviceMessageEvent<T extends Device> {
    device: T
}

export const API_EVENT_TYPES = [
    // Device.
    'DEVICE.PROPERTY_CHANGED', 'DEVICE.PROPERTY_DELETED', 'DEVICE.MESSAGE_RECEIVED',
    // Camera.
    'CAMERA.UPDATED', 'CAMERA.ATTACHED', 'CAMERA.DETACHED',
    'CAMERA.CAPTURE_STARTED', 'CAMERA.CAPTURE_FINISHED',
    'CAMERA.EXPOSURE_UPDATED', 'CAMERA.EXPOSURE_STARTED', 'CAMERA.EXPOSURE_FINISHED',
    // Mount.
    'MOUNT.UPDATED', 'MOUNT.ATTACHED', 'MOUNT.DETACHED',
    // Focuser.
    'FOCUSER.UPDATED', 'FOCUSER.ATTACHED', 'FOCUSER.DETACHED',
    // Filter Wheel.
    'WHEEL.UPDATED', 'WHEEL.ATTACHED', 'WHEEL.DETACHED',
    // Guide Output.
    'GUIDE_OUTPUT.ATTACHED', 'GUIDE_OUTPUT.DETACHED', 'GUIDE_OUTPUT.UPDATED',
    // Guider.
    'GUIDER.CONNECTED', 'GUIDER.DISCONNECTED', 'GUIDER.UPDATED', 'GUIDER.STEPPED', 'GUIDER.MESSAGE_RECEIVED',
    // Polar Alignment.
    'DARV_ALIGNMENT.ELAPSED',
] as const

export type ApiEventType = (typeof API_EVENT_TYPES)[number]
