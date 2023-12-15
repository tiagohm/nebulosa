import { AfterContentInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import path from 'path'
import { MenuItem, MessageService } from 'primeng/api'
import { DeviceMenuComponent } from '../../shared/components/devicemenu/devicemenu.component'
import { DialogMenuComponent } from '../../shared/components/dialogmenu/dialogmenu.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { Camera, Device, FilterWheel, Focuser, HomeWindowType, Mount } from '../../shared/types'
import { compareDevice } from '../../shared/utils/comparators'
import { AppComponent } from '../app.component'

type MappedDevice = {
    'CAMERA': Camera
    'MOUNT': Mount
    'FOCUSER': Focuser
    'WHEEL': FilterWheel
}

export interface HomePreference {
    host?: string
    port?: number
}

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements AfterContentInit, OnDestroy {

    @ViewChild('deviceMenu')
    private readonly deviceMenu!: DialogMenuComponent

    @ViewChild('imageMenu')
    private readonly imageMenu!: DeviceMenuComponent

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

    readonly deviceModel: MenuItem[] = []

    readonly imageModel: MenuItem[] = [
        {
            icon: 'mdi mdi-image-plus',
            label: 'Open new image',
            command: () => {
                this.openImage(true)
            }
        }
    ]

    private startListening<K extends keyof MappedDevice>(
        type: K,
        onAdd: (device: MappedDevice[K]) => number,
        onRemove: (device: MappedDevice[K]) => number,
    ) {
        this.electron.on(`${type}_ATTACHED`, event => {
            this.ngZone.run(() => {
                onAdd(event.device as any)
            })
        })

        this.electron.on(`${type}_DETACHED`, event => {
            this.ngZone.run(() => {
                onRemove(event.device as any)
            })
        })
    }

    constructor(
        app: AppComponent,
        private electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private api: ApiService,
        private message: MessageService,
        private storage: LocalStorageService,
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

        const preference = this.storage.get<HomePreference>('home', {})

        this.host = preference.host ?? 'localhost'
        this.port = preference.port ?? 7624

        this.cameras = await this.api.cameras()
        this.mounts = await this.api.mounts()
        this.focusers = await this.api.focusers()
        this.wheels = await this.api.wheels()
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    async connect() {
        try {
            if (this.connected) {
                await this.api.disconnect()
            } else {
                await this.api.connect(this.host || 'localhost', this.port)

                const preference: HomePreference = {
                    host: this.host,
                    port: this.port,
                }

                this.storage.set('home', preference)
            }
        } catch (e) {
            console.error(e)

            this.message.add({ severity: 'error', detail: 'Connection failed' })
        } finally {
            this.updateConnection()
        }
    }

    private openDevice<K extends keyof MappedDevice>(type: K) {
        this.deviceModel.length = 0

        const devices: Device[] = type === 'CAMERA' ? this.cameras
            : type === 'MOUNT' ? this.mounts
                : type === 'FOCUSER' ? this.focusers
                    : type === 'WHEEL' ? this.wheels
                        : []

        if (devices.length === 0) return
        if (devices.length === 1) return this.openDeviceWindow(type, devices[0] as any)

        for (const device of [...devices].sort(compareDevice)) {
            this.deviceModel.push({
                icon: 'mdi mdi-connection',
                label: device.name,
                command: () => {
                    this.openDeviceWindow(type, device as any)
                }
            })
        }

        this.deviceMenu.show()
    }

    private openDeviceWindow<K extends keyof MappedDevice>(type: K, device: MappedDevice[K]) {
        switch (type) {
            case 'MOUNT':
                this.browserWindow.openMount({ bringToFront: true, data: device as Mount })
                break
            case 'CAMERA':
                this.browserWindow.openCamera({ bringToFront: true, data: device as Camera })
                break
            case 'FOCUSER':
                this.browserWindow.openFocuser({ bringToFront: true, data: device as Focuser })
                break
            case 'WHEEL':
                this.browserWindow.openWheel({ bringToFront: true, data: device as FilterWheel })
                break
        }
    }

    private async openImage(force: boolean = false) {
        if (force || this.cameras.length === 0) {
            const defaultPath = this.storage.get('home.image.directory', '')
            const fitsPath = await this.electron.openFITS({ defaultPath })

            if (fitsPath) {
                this.storage.set('home.image.directory', path.dirname(fitsPath))
                this.browserWindow.openImage({ path: fitsPath, source: 'PATH' })
            }
        } else {
            const camera = await this.imageMenu.show(this.cameras)

            if (camera) {
                this.browserWindow.openCameraImage(camera)
            }
        }
    }

    open(type: HomeWindowType) {
        switch (type) {
            case 'MOUNT':
            case 'CAMERA':
            case 'FOCUSER':
            case 'WHEEL':
                this.openDevice(type)
                break
            case 'GUIDER':
                this.browserWindow.openGuider({ bringToFront: true })
                break
            case 'SKY_ATLAS':
                this.browserWindow.openSkyAtlas({ bringToFront: true })
                break
            case 'FRAMING':
                this.browserWindow.openFraming({ bringToFront: true, data: undefined })
                break
            case 'ALIGNMENT':
                this.browserWindow.openAlignment({ bringToFront: true })
                break
            case 'INDI':
                this.browserWindow.openINDI({ data: undefined, bringToFront: true })
                break
            case 'IMAGE':
                this.openImage()
                break
            case 'SETTINGS':
                this.browserWindow.openSettings()
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
        }

        if (!this.connected) {
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
