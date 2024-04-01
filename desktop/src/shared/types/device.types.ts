import { DeviceMessageEvent } from './api.types'

export type PropertyState = 'IDLE' | 'OK' | 'BUSY' | 'ALERT'

export type PropertyPermission = 'RO' | 'RW' | 'WO'

export type INDIPropertyType = 'NUMBER' | 'SWITCH' | 'TEXT'

export type SwitchRule = 'ONE_OF_MANY' | 'AT_MOST_ONE' | 'ANY_OF_MANY'

export interface Device {
    readonly sender: string
    readonly id: string
    readonly name: string
    connected: boolean
}

export interface CompanionDevice<D = Device> extends Device {
    main: D
}

export interface INDIMessageEvent extends DeviceMessageEvent<Device> {
    property?: INDIProperty<any>
    message?: string
}

export interface INDIProperty<T> {
    name: string
    label: string
    type: INDIPropertyType
    group: string
    perm: PropertyPermission
    state: PropertyState
    rule?: SwitchRule
    items: INDIPropertyItem<T>[]
}

export interface INDIPropertyItem<T> {
    name: string
    label: string
    value: T
    valueToSend?: string
}

export interface INDISendProperty {
    name: string
    type: INDIPropertyType
    items: INDISendPropertyItem[]
}

export interface INDISendPropertyItem {
    name: string
    value: any
}

export interface INDIDeviceMessage {
    device?: Device
    message: string
}
