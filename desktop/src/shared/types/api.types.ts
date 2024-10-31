import type { Severity } from './angular.types'
import type { Device } from './device.types'

export type ApiEventType = (typeof API_EVENT_TYPES)[number]

export interface MessageEvent {
	eventName: string
}

export interface OpenImageEvent extends MessageEvent {
	path: string
}

export interface DeviceMessageEvent<T extends Device> extends MessageEvent {
	device: T
}

export interface NotificationEvent extends MessageEvent {
	target?: string
	severity: Severity
	title?: string
	body: string
}

export interface ConfirmationEvent extends MessageEvent {
	message: string
	idempotencyKey: string
}

export const API_EVENT_TYPES = [
	// Device.
	'DEVICE.PROPERTY_CHANGED',
	'DEVICE.PROPERTY_DELETED',
	'DEVICE.MESSAGE_RECEIVED',
	// Camera.
	'CAMERA.UPDATED',
	'CAMERA.ATTACHED',
	'CAMERA.DETACHED',
	'CAMERA.CAPTURE_STARTED',
	'CAMERA.CAPTURE_FINISHED',
	'CAMERA.EXPOSURE_UPDATED',
	'CAMERA.EXPOSURE_STARTED',
	'CAMERA.EXPOSURE_FINISHED',
	// Mount.
	'MOUNT.UPDATED',
	'MOUNT.ATTACHED',
	'MOUNT.DETACHED',
	// Focuser.
	'FOCUSER.UPDATED',
	'FOCUSER.ATTACHED',
	'FOCUSER.DETACHED',
	// Filter Wheel.
	'WHEEL.UPDATED',
	'WHEEL.ATTACHED',
	'WHEEL.DETACHED',
	// Rotator.
	'ROTATOR.UPDATED',
	'ROTATOR.ATTACHED',
	'ROTATOR.DETACHED',
	// Guide Output.
	'GUIDE_OUTPUT.ATTACHED',
	'GUIDE_OUTPUT.DETACHED',
	'GUIDE_OUTPUT.UPDATED',
	// Guider.
	'GUIDER.CONNECTED',
	'GUIDER.DISCONNECTED',
	'GUIDER.UPDATED',
	'GUIDER.STEPPED',
	'GUIDER.MESSAGE_RECEIVED',
	// Polar Alignment.
	'DARV_ALIGNMENT.ELAPSED',
	// Auto Focus.
	'AUTO_FOCUS.ELAPSED',
] as const

export function isNotificationEvent(event: MessageEvent): event is NotificationEvent {
	return event.eventName === 'NOTIFICATION'
}

export function isConfirmationEvent(event: MessageEvent): event is ConfirmationEvent {
	return event.eventName === 'CONFIRMATION'
}
