import { AfterContentInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { MessageService } from 'primeng/api'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Camera, FilterWheel, Focuser, HomeWindowType, Mount } from '../../shared/types'
import { AppComponent } from '../app.component'

type MappedDevice = {
    'CAMERA': Camera
    'MOUNT': Mount
    'FOCUSER': Focuser
    'WHEEL': FilterWheel
}

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements AfterContentInit, OnDestroy {

    host = ''
    port = 7624
    connected = false

    cameras: Camera[] = []
    mounts: Mount[] = []
    focusers: Focuser[] = []
    wheels: FilterWheel[] = []
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

    get hasWheel() {
        return this.wheels.length > 0
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
            || this.hasWheel || this.hasDome || this.hasRotator || this.hasSwitch
    }

    private startListening<K extends keyof MappedDevice>(
        type: K,
        onAdd: (device: MappedDevice[K]) => number,
        onRemove: (device: MappedDevice[K]) => number,
    ) {
        this.electron.on(`${type}_ATTACHED`, event => {
            this.ngZone.run(() => {
                if (onAdd(event.device as any) === 1) {
                    this.electron.send(`${type}_CHANGED`, event.device)
                }
            })
        })

        this.electron.on(`${type}_DETACHED`, event => {
            this.ngZone.run(() => {
                if (onRemove(event.device as any) === 0) {
                    this.electron.send(`${type}_CHANGED`, undefined)
                }
            })
        })
    }

    constructor(
        app: AppComponent,
        private electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private api: ApiService,
        private message: MessageService,
        private preference: PreferenceService,
        private ngZone: NgZone,
    ) {
        app.title = 'Nebulosa'

        this.startListening('CAMERA',
            (device) => {
                return this.cameras.push(device)
            },
            (device) => {
                this.cameras.splice(this.cameras.findIndex(e => e.name === device.name), 1)
                return this.cameras.length
            },
        )

        this.startListening('MOUNT',
            (device) => {
                return this.mounts.push(device)
            },
            (device) => {
                this.mounts.splice(this.mounts.findIndex(e => e.name === device.name), 1)
                return this.mounts.length
            },
        )

        this.startListening('FOCUSER',
            (device) => {
                return this.focusers.push(device)
            },
            (device) => {
                this.focusers.splice(this.focusers.findIndex(e => e.name === device.name), 1)
                return this.focusers.length
            },
        )

        this.startListening('WHEEL',
            (device) => {
                return this.wheels.push(device)
            },
            (device) => {
                this.wheels.splice(this.wheels.findIndex(e => e.name === device.name), 1)
                return this.wheels.length
            },
        )

        electron.on('SKY_ATLAS_UPDATE_FINISHED', () => this.open('SKY_ATLAS'))
    }

    async ngAfterContentInit() {
        this.updateConnection()

        this.host = this.preference.get('home.host', 'localhost')
        this.port = this.preference.get('home.port', 7624)

        this.cameras = await this.api.cameras()
        this.mounts = await this.api.mounts()
        this.focusers = await this.api.focusers()
        this.wheels = await this.api.wheels()

        if (this.cameras.length > 0) {
            this.electron.send('CAMERA_CHANGED', this.cameras[0])
        }

        if (this.mounts.length > 0) {
            this.electron.send('MOUNT_CHANGED', this.mounts[0])
        }

        if (this.focusers.length > 0) {
            this.electron.send('FOCUSER_CHANGED', this.focusers[0])
        }

        if (this.wheels.length > 0) {
            this.electron.send('WHEEL_CHANGED', this.wheels[0])
        }
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

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
            case 'MOUNT':
                this.browserWindow.openMount({ bringToFront: true })
                break
            case 'CAMERA':
                this.browserWindow.openCamera({ bringToFront: true })
                break
            case 'FOCUSER':
                this.browserWindow.openFocuser({ bringToFront: true })
                break
            case 'WHEEL':
                this.browserWindow.openWheel({ bringToFront: true })
                break
            case 'GUIDER':
                this.browserWindow.openGuider({ bringToFront: true })
                break
            case 'SKY_ATLAS':
                this.browserWindow.openSkyAtlas({ bringToFront: true })
                break
            case 'FRAMING':
                this.browserWindow.openFraming(undefined, { bringToFront: true })
                break
            case 'ALIGNMENT':
                this.browserWindow.openAlignment({ bringToFront: true })
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
        try {
            this.connected = await this.api.connectionStatus()
        } catch {
            this.connected = false

            this.cameras = []
            this.mounts = []
            this.focusers = []
            this.wheels = []
            this.domes = []
            this.rotators = []
            this.switches = []
        }
    }
}
