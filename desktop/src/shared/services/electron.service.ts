import { Injectable } from '@angular/core'

// If you import a module but never use any of the imported values
// other than as TypeScript types, the resulting javascript file will
// look as if you never imported the module at all.

import * as childProcess from 'child_process'
import { ipcRenderer, webFrame } from 'electron'
import * as fs from 'fs'
import { DARVEvent, TPPAEvent } from '../types/alignment.types'
import { DeviceMessageEvent } from '../types/api.types'
import { CloseWindow, JsonFile, OpenDirectory, OpenFile, SaveJson } from '../types/app.types'
import { Location, SkyAtlasUpdated } from '../types/atlas.types'
import { Camera, CameraCaptureEvent } from '../types/camera.types'
import { INDIMessageEvent } from '../types/device.types'
import { FlatWizardEvent } from '../types/flat-wizard.types'
import { Focuser } from '../types/focuser.types'
import { GuideOutput, Guider, GuiderHistoryStep, GuiderMessageEvent } from '../types/guider.types'
import { ConnectionClosed } from '../types/home.types'
import { ROISelected } from '../types/image.types'
import { Mount } from '../types/mount.types'
import { Rotator } from '../types/rotator.types'
import { SequencerEvent } from '../types/sequencer.types'
import { FilterWheel, WheelRenamed } from '../types/wheel.types'
import { AutoFocusEvent } from '../types/autofocus.type'

type EventMappedType = {
    'DEVICE.PROPERTY_CHANGED': INDIMessageEvent
    'DEVICE.PROPERTY_DELETED': INDIMessageEvent
    'DEVICE.MESSAGE_RECEIVED': INDIMessageEvent
    'CAMERA.UPDATED': DeviceMessageEvent<Camera>
    'CAMERA.ATTACHED': DeviceMessageEvent<Camera>
    'CAMERA.DETACHED': DeviceMessageEvent<Camera>
    'CAMERA.CAPTURE_ELAPSED': CameraCaptureEvent
    'MOUNT.UPDATED': DeviceMessageEvent<Mount>
    'MOUNT.ATTACHED': DeviceMessageEvent<Mount>
    'MOUNT.DETACHED': DeviceMessageEvent<Mount>
    'FOCUSER.UPDATED': DeviceMessageEvent<Focuser>
    'FOCUSER.ATTACHED': DeviceMessageEvent<Focuser>
    'FOCUSER.DETACHED': DeviceMessageEvent<Focuser>
    'ROTATOR.UPDATED': DeviceMessageEvent<Rotator>
    'ROTATOR.ATTACHED': DeviceMessageEvent<Rotator>
    'ROTATOR.DETACHED': DeviceMessageEvent<Rotator>
    'WHEEL.UPDATED': DeviceMessageEvent<FilterWheel>
    'WHEEL.ATTACHED': DeviceMessageEvent<FilterWheel>
    'WHEEL.DETACHED': DeviceMessageEvent<FilterWheel>
    'GUIDE_OUTPUT.UPDATED': DeviceMessageEvent<GuideOutput>
    'GUIDE_OUTPUT.ATTACHED': DeviceMessageEvent<GuideOutput>
    'GUIDE_OUTPUT.DETACHED': DeviceMessageEvent<GuideOutput>
    'GUIDER.CONNECTED': GuiderMessageEvent<undefined>
    'GUIDER.DISCONNECTED': GuiderMessageEvent<undefined>
    'GUIDER.UPDATED': GuiderMessageEvent<Guider>
    'GUIDER.STEPPED': GuiderMessageEvent<GuiderHistoryStep>
    'GUIDER.MESSAGE_RECEIVED': GuiderMessageEvent<string>
    'DARV.ELAPSED': DARVEvent
    'TPPA.ELAPSED': TPPAEvent
    'DATA.CHANGED': any
    'LOCATION.CHANGED': Location
    'SEQUENCER.ELAPSED': SequencerEvent
    'FLAT_WIZARD.ELAPSED': FlatWizardEvent
    'CONNECTION.CLOSED': ConnectionClosed
    'SKY_ATLAS.PROGRESS_CHANGED': SkyAtlasUpdated
    'CALIBRATION.CHANGED': unknown
    'FILE.OPEN': OpenFile
    'FILE.SAVE': OpenFile
    'DIRECTORY.OPEN': OpenDirectory
    'JSON.WRITE': JsonFile
    'JSON.READ': string
    'WINDOW.RESIZE': number
    'WINDOW.PIN': unknown
    'WINDOW.UNPIN': unknown
    'WINDOW.MINIMIZE': unknown
    'WINDOW.MAXIMIZE': unknown
    'WINDOW.FULLSCREEN': boolean
    'WINDOW.CLOSE': CloseWindow
    'WHEEL.RENAMED': WheelRenamed
    'ROI.SELECTED': ROISelected
    'AUTO_FOCUS.ELAPSED': AutoFocusEvent
}

@Injectable({ providedIn: 'root' })
export class ElectronService {

    ipcRenderer!: typeof ipcRenderer
    webFrame!: typeof webFrame
    childProcess!: typeof childProcess
    fs!: typeof fs

    constructor() {
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

    send<K extends keyof EventMappedType>(channel: K, data?: EventMappedType[K]) {
        return this.ipcRenderer.invoke(channel, data)
    }

    on<K extends keyof EventMappedType>(channel: K, listener: (arg: EventMappedType[K]) => void) {
        console.info('listening to channel: %s', channel)
        this.ipcRenderer.on(channel, (_, arg) => listener(arg))
    }

    openFile(data?: OpenFile): Promise<string | undefined> {
        return this.send('FILE.OPEN', data)
    }

    saveFile(data?: OpenFile): Promise<string | undefined> {
        return this.send('FILE.SAVE', data)
    }

    openImage(data?: OpenFile): Promise<string | undefined> {
        return this.openFile({
            ...data, filters: [
                { name: 'All', extensions: ['fits', 'fit', 'xisf'] },
                { name: 'FITS', extensions: ['fits', 'fit'] },
                { name: 'XISF', extensions: ['xisf'] },
            ]
        })
    }

    saveImage(data?: OpenFile) {
        return this.saveFile({
            ...data,
            filters: [
                { name: 'All', extensions: ['fits', 'fit', 'xisf', 'png', 'jpg', 'jpeg'] },
                { name: 'FITS', extensions: ['fits', 'fit'] },
                { name: 'XISF', extensions: ['xisf'] },
                { name: 'Image', extensions: ['png', 'jpg', 'jpeg'] },
            ]
        })
    }

    openDirectory(data?: OpenDirectory): Promise<string | false> {
        return this.send('DIRECTORY.OPEN', data)
    }

    async saveJson<T>(data: SaveJson<T>): Promise<JsonFile<T> | false> {
        data.path = data.path || await this.saveFile({ ...data, filters: [{ name: 'JSON files', extensions: ['json'] }] })

        if (data.path) {
            if (await this.writeJson(data)) {
                return data
            }
        }

        return false
    }

    async openJson<T>(data?: OpenFile): Promise<JsonFile<T> | false> {
        const path = await this.openFile({ ...data, filters: [{ name: 'JSON files', extensions: ['json'] }] })

        if (path) {
            return await this.readJson<T>(path)
        }

        return false
    }

    writeJson<T>(json: JsonFile<T>): Promise<boolean> {
        return this.send('JSON.WRITE', json)
    }

    readJson<T>(path: string): Promise<JsonFile<T> | false> {
        return this.send('JSON.READ', path)
    }

    resizeWindow(size: number) {
        this.send('WINDOW.RESIZE', Math.floor(size))
    }

    autoResizeWindow(timeout: number = 500): any {
        if (timeout <= 0) {
            const size = document.getElementsByTagName('app-root')[0]?.getBoundingClientRect()?.height

            if (size > 0) {
                this.resizeWindow(size)
            }
        } else {
            return setTimeout(() => this.autoResizeWindow(0), timeout)
        }
    }

    pinWindow() {
        this.send('WINDOW.PIN')
    }

    unpinWindow() {
        this.send('WINDOW.UNPIN')
    }

    minimizeWindow() {
        this.send('WINDOW.MINIMIZE')
    }

    maximizeWindow() {
        this.send('WINDOW.MAXIMIZE')
    }

    fullscreenWindow(enabled?: boolean): Promise<boolean> {
        return this.send('WINDOW.FULLSCREEN', enabled)
    }

    closeWindow<T>(data: CloseWindow<T>) {
        return this.send('WINDOW.CLOSE', data)
    }

    calibrationChanged() {
        this.send('CALIBRATION.CHANGED')
    }
}
