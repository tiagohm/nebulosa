import { Injectable } from '@angular/core'

// If you import a module but never use any of the imported values
// other than as TypeScript types, the resulting javascript file will
// look as if you never imported the module at all.
import * as childProcess from 'child_process'
import { ipcRenderer, webFrame } from 'electron'
import * as fs from 'fs'
import {
    ApiEventType, Camera, CameraCaptureElapsed, CameraCaptureFinished, CameraCaptureIsWaiting, CameraCaptureStarted,
    CameraExposureElapsed, CameraExposureFinished, CameraExposureStarted, DARVPolarAlignmentEvent, DARVPolarAlignmentGuidePulseElapsed,
    DARVPolarAlignmentInitialPauseElapsed, DeviceMessageEvent, FilterWheel, Focuser, GuideOutput, Guider,
    GuiderMessageEvent, HistoryStep, INDIMessageEvent, InternalEventType, Location, Mount, NotificationEvent, NotificationEventType, OpenDirectory
} from '../types'
import { ApiService } from './api.service'

type EventMappedType = {
    'DEVICE_PROPERTY_CHANGED': INDIMessageEvent
    'DEVICE_PROPERTY_DELETED': INDIMessageEvent
    'DEVICE_MESSAGE_RECEIVED': INDIMessageEvent
    'CAMERA_UPDATED': DeviceMessageEvent<Camera>
    'CAMERA_ATTACHED': DeviceMessageEvent<Camera>
    'CAMERA_DETACHED': DeviceMessageEvent<Camera>
    'CAMERA_CAPTURE_STARTED': CameraCaptureStarted
    'CAMERA_CAPTURE_FINISHED': CameraCaptureFinished
    'CAMERA_CAPTURE_ELAPSED': CameraCaptureElapsed
    'CAMERA_CAPTURE_WAITING': CameraCaptureIsWaiting
    'CAMERA_EXPOSURE_ELAPSED': CameraExposureElapsed
    'CAMERA_EXPOSURE_STARTED': CameraExposureStarted
    'CAMERA_EXPOSURE_FINISHED': CameraExposureFinished
    'MOUNT_UPDATED': DeviceMessageEvent<Mount>
    'MOUNT_ATTACHED': DeviceMessageEvent<Mount>
    'MOUNT_DETACHED': DeviceMessageEvent<Mount>
    'FOCUSER_UPDATED': DeviceMessageEvent<Focuser>
    'FOCUSER_ATTACHED': DeviceMessageEvent<Focuser>
    'FOCUSER_DETACHED': DeviceMessageEvent<Focuser>
    'WHEEL_UPDATED': DeviceMessageEvent<FilterWheel>
    'WHEEL_ATTACHED': DeviceMessageEvent<FilterWheel>
    'WHEEL_DETACHED': DeviceMessageEvent<FilterWheel>
    'GUIDE_OUTPUT_UPDATED': DeviceMessageEvent<GuideOutput>
    'GUIDE_OUTPUT_ATTACHED': DeviceMessageEvent<GuideOutput>
    'GUIDE_OUTPUT_DETACHED': DeviceMessageEvent<GuideOutput>
    'GUIDER_CONNECTED': GuiderMessageEvent<undefined>
    'GUIDER_DISCONNECTED': GuiderMessageEvent<undefined>
    'GUIDER_UPDATED': GuiderMessageEvent<Guider>
    'GUIDER_STEPPED': GuiderMessageEvent<HistoryStep>
    'GUIDER_MESSAGE_RECEIVED': GuiderMessageEvent<string>
    'DARV_POLAR_ALIGNMENT_STARTED': DARVPolarAlignmentEvent
    'DARV_POLAR_ALIGNMENT_FINISHED': DARVPolarAlignmentEvent
    'DARV_POLAR_ALIGNMENT_UPDATED': DARVPolarAlignmentInitialPauseElapsed | DARVPolarAlignmentGuidePulseElapsed
    'DATA_CHANGED': any
    'LOCATION_CHANGED': Location
    'SKY_ATLAS_UPDATE_FINISHED': NotificationEvent
}

@Injectable({ providedIn: 'root' })
export class ElectronService {

    ipcRenderer!: typeof ipcRenderer
    webFrame!: typeof webFrame
    childProcess!: typeof childProcess
    fs!: typeof fs

    constructor(private api: ApiService) {
        if (this.isElectron) {
            this.ipcRenderer = (window as any).require('electron').ipcRenderer
            this.webFrame = (window as any).require('electron').webFrame

            this.fs = (window as any).require('fs')

            this.childProcess = (window as any).require('child_process')
            this.childProcess.exec('node -v')

            // Notes :
            // * A NodeJS's dependency imported with 'window.require' MUST BE present in `dependencies` of both `app/package.json`
            // and `package.json (root folder)` in order to make it work here in Electron's Renderer process (src folder)
            // because it will loaded at runtime by Electron.
            // * A NodeJS's dependency imported with TS module import (ex: import { Dropbox } from 'dropbox') CAN only be present
            // in `dependencies` of `package.json (root folder)` because it is loaded during build phase and does not need to be
            // in the final bundle. Reminder : only if not used in Electron's Main process (app folder)

            // If you want to use a NodeJS 3rd party deps in Renderer process,
            // ipcRenderer.invoke can serve many common use cases.
            // https://www.electronjs.org/docs/latest/api/ipc-renderer#ipcrendererinvokechannel-args
        }
    }

    get isElectron() {
        return !!(window && window.process && window.process.type)
    }

    send(channel: ApiEventType | InternalEventType | NotificationEventType, ...data: any[]) {
        return this.ipcRenderer.invoke(channel, ...data)
    }

    on<K extends keyof EventMappedType>(channel: K, listener: (arg: EventMappedType[K]) => void) {
        console.info('listening to channel: %s', channel)
        this.ipcRenderer.on(channel, (_, arg) => listener(arg))
    }

    openDirectory(data?: OpenDirectory): Promise<string | false> {
        return this.send('OPEN_DIRECTORY', data)
    }
}
