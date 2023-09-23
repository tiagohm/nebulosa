import { Injectable } from '@angular/core'

// If you import a module but never use any of the imported values
// other than as TypeScript types, the resulting javascript file will
// look as if you never imported the module at all.
import * as childProcess from 'child_process'
import { ipcRenderer, webFrame } from 'electron'
import * as fs from 'fs'
import { INDIEventType, InternalEventType, Mount, OpenDirectory, WindowEventType } from '../types'
import { ApiService } from './api.service'

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

    send(channel: INDIEventType | InternalEventType | WindowEventType, ...data: any[]) {
        this.ipcRenderer.send(channel, ...data)
    }

    sendSync(channel: INDIEventType | InternalEventType | WindowEventType, ...data: any[]) {
        return this.ipcRenderer.sendSync(channel, ...data)
    }

    on(channel: INDIEventType | InternalEventType | 'PARAMS_CHANGED',
        listener: (event: Electron.IpcRendererEvent, ...args: any[]) => void) {
        return this.ipcRenderer.on(channel, listener)
    }

    openDirectory(data?: OpenDirectory): string | false {
        return this.sendSync('OPEN_DIRECTORY', data)
    }

    async selectedMount(): Promise<Mount | undefined> {
        const mount: Mount | undefined = this.sendSync('SELECTED_MOUNT')
        if (!mount) return undefined
        return this.api.mount(mount.name)
    }
}
