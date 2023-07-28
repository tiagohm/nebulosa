import { Component, HostListener, NgZone, OnDestroy, OnInit } from '@angular/core'
import { MessageService } from 'primeng/api'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Camera, Device, FilterWheel, Focuser, HomeWindowType } from '../../shared/types'

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit, OnDestroy {

    host = ''
    port = 7624
    connected = false

    cameras: Camera[] = []
    mounts: Camera[] = []
    focusers: Focuser[] = []
    filterWheels: FilterWheel[] = []
    domes: Camera[] = []
    rotators: Camera[] = []
    switches: Camera[] = []

    get hasCamera() {
        return this.cameras.length > 0
    }

    get hasMount() {
        return this.mounts.length > 0
    }

    get hasFocuser() {
        return this.focusers.length > 0
    }

    get hasFilterWheel() {
        return this.filterWheels.length > 0
    }

    get hasDome() {
        return this.domes.length > 0
    }

    get hasSwitch() {
        return this.switches.length > 0
    }

    get hasRotator() {
        return this.rotators.length > 0
    }

    get hasGuider() {
        return this.hasCamera && this.hasMount
    }

    get hasAlignment() {
        return this.hasCamera && this.hasMount
    }

    get hasSequencer() {
        return this.hasCamera
    }

    get hasINDI() {
        return this.hasCamera || this.hasMount || this.hasFocuser
            || this.hasFilterWheel || this.hasDome || this.hasRotator || this.hasSwitch
    }

    private startListening<T extends Device>(
        type: 'CAMERA' | 'FOCUSER' | 'FILTER_WHEEL',
        devices: T[],
    ) {
        this.api.indiStartListening(`${type}_ATTACHED`)
        this.api.indiStartListening(`${type}_DETACHED`)

        this.electron.ipcRenderer.on(`${type}_ATTACHED`, (_, device: T) => {
            this.ngZone.run(() => {
                if (devices.length === 0) {
                    this.electron.send(`${type}_CHANGED`, device)
                }

                devices.push(device)
            })
        })

        this.electron.ipcRenderer.on(`${type}_DETACHED`, (_, device: T) => {
            this.ngZone.run(() => {
                devices.splice(devices.findIndex(e => e.name === device.name), 1)

                if (devices.length === 0) {
                    this.electron.send(`${type}_CHANGED`, undefined)
                }
            })
        })
    }

    constructor(
        private electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private api: ApiService,
        private message: MessageService,
        private preference: PreferenceService,
        private ngZone: NgZone,
    ) {
        this.startListening('CAMERA', this.cameras)
        this.startListening('FOCUSER', this.focusers)
        this.startListening('FILTER_WHEEL', this.filterWheels)
    }

    async ngOnInit() {
        this.updateConnection()

        this.host = this.preference.get('home.host', 'localhost')
        this.port = this.preference.get('home.port', 7624)

        this.cameras = await this.api.attachedCameras()
        this.focusers = await this.api.attachedFocusers()
        this.filterWheels = await this.api.attachedFilterWheels()

        if (this.cameras.length > 0) {
            this.electron.send('CAMERA_CHANGED', this.cameras[0])
        }

        if (this.focusers.length > 0) {
            this.electron.send('FOCUSER_CHANGED', this.focusers[0])
        }

        if (this.filterWheels.length > 0) {
            this.electron.send('FILTER_WHEEL_CHANGED', this.filterWheels[0])
        }
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.api.indiStopListening('CAMERA_ATTACHED')
        this.api.indiStopListening('CAMERA_DETACHED')

        this.api.indiStopListening('FOCUSER_ATTACHED')
        this.api.indiStopListening('FOCUSER_DETACHED')

        this.api.indiStopListening('FILTER_WHEEL_ATTACHED')
        this.api.indiStopListening('FILTER_WHEEL_DETACHED')
    }

    async connect() {
        try {
            if (this.connected) {
                await this.api.disconnect()
            } else {
                await this.api.connect(this.host || 'localhost', this.port)

                this.preference.set('home.host', this.host)
                this.preference.set('home.port', this.port)
            }
        } catch (e) {
            console.error(e)

            this.message.add({ severity: 'error', detail: 'Connection failed' })
        } finally {
            this.updateConnection()
        }
    }

    async open(type: HomeWindowType) {
        switch (type) {
            case 'CAMERA':
                this.browserWindow.openCamera({ bringToFront: true })
                break
            case 'FOCUSER':
                this.browserWindow.openFocuser({ bringToFront: true })
                break
            case 'FILTER_WHEEL':
                this.browserWindow.openFilterWheel({ bringToFront: true })
                break
            case 'ATLAS':
                this.browserWindow.openSkyAtlas({ bringToFront: true })
                break
            case 'FRAMING':
                this.browserWindow.openFraming(undefined, { bringToFront: true })
                break
            case 'INDI':
                this.browserWindow.openINDI(undefined, { bringToFront: true })
                break
            case 'IMAGE':
                const path = await this.electron.sendSync('OPEN_FITS')
                if (path) this.browserWindow.openImage(path, undefined, 'PATH')
                break
            case 'ABOUT':
                this.browserWindow.openAbout()
                break
        }
    }

    private async updateConnection() {
        this.connected = await this.api.connectionStatus()
    }
}
