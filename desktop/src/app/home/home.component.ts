import { AfterContentInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import path from 'path'
import { MenuItem, MessageService } from 'primeng/api'
import { AutoCompleteCompleteEvent } from 'primeng/autocomplete'
import { DeviceListMenuComponent } from '../../shared/components/device-list-menu/device-list-menu.component'
import { DialogMenuComponent } from '../../shared/components/dialog-menu/dialog-menu.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Camera } from '../../shared/types/camera.types'
import { Device } from '../../shared/types/device.types'
import { Focuser } from '../../shared/types/focuser.types'
import { ConnectionDetails, HomeWindowType } from '../../shared/types/home.types'
import { Mount } from '../../shared/types/mount.types'
import { FilterWheel } from '../../shared/types/wheel.types'
import { compareDevice } from '../../shared/utils/comparators'
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

    @ViewChild('deviceMenu')
    private readonly deviceMenu!: DialogMenuComponent

    @ViewChild('imageMenu')
    private readonly imageMenu!: DeviceListMenuComponent

    connected = false
    lastConnectedHosts: ConnectionDetails[] = []
    connection: ConnectionDetails

    readonly connectionTypeModel: MenuItem[] = [
        {
            label: 'INDI',
            command: () => {
                this.connection.type = 'INDI'
            },
        },
        {
            label: 'ASCOM Alpaca',
            command: () => {
                this.connection.type = 'ALPACA'
            },
        }
    ]

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

    get hasFlatWizard() {
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
        this.electron.on(`${type}.ATTACHED`, event => {
            this.ngZone.run(() => {
                onAdd(event.device as any)
            })
        })

        this.electron.on(`${type}.DETACHED`, event => {
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

        this.lastConnectedHosts = this.preference.lastConnectedHosts.get()
        this.connection = Object.assign({}, this.lastConnectedHosts[0])
        this.connection.type ??= 'INDI'
    }

    async ngAfterContentInit() {
        this.updateConnection()

        this.cameras = await this.api.cameras()
        this.mounts = await this.api.mounts()
        this.focusers = await this.api.focusers()
        this.wheels = await this.api.wheels()
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    hostChanged(event: string | ConnectionDetails) {
        if (typeof event === 'string') {
            this.connection.host = event
        } else {
            Object.assign(this.connection, event)
        }
    }

    removeConnection(connection: ConnectionDetails, event: MouseEvent) {
        const { host, port } = connection
        const index = this.lastConnectedHosts.findIndex(e => e.host === host && e.port === port)

        if (index >= 0) {
            this.lastConnectedHosts.splice(index, 1)
            this.preference.lastConnectedHosts.set(this.lastConnectedHosts)
        }

        event.stopImmediatePropagation()
    }

    async connect() {
        try {
            if (this.connected) {
                await this.api.disconnect()
            } else {
                let { host, port, type } = this.connection

                host ||= 'localhost'
                port ||= 7624
                type || 'INDI'

                await this.api.connect(host, port, type)

                const index = this.lastConnectedHosts.findIndex(e => e.host === host && e.port === port)

                if (index >= 0) {
                    this.lastConnectedHosts.splice(index, 1)
                }

                this.lastConnectedHosts.splice(0, 0, this.connection)
                this.lastConnectedHosts[0].connectedAt = Date.now()

                this.preference.lastConnectedHosts.set(this.lastConnectedHosts)
            }
        } catch (e) {
            console.error(e)

            this.message.add({ severity: 'error', detail: 'Connection failed' })
        } finally {
            this.updateConnection()
        }
    }

    filterLastConnected(event: AutoCompleteCompleteEvent) {

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
            const defaultPath = this.preference.homeImageDefaultDirectory.get()
            const filePath = await this.electron.openFits({ defaultPath })

            if (filePath) {
                this.preference.homeImageDefaultDirectory.set(path.dirname(filePath))
                this.browserWindow.openImage({ path: filePath, source: 'PATH' })
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
                this.browserWindow.openSkyAtlas({ bringToFront: true, data: undefined })
                break
            case 'FRAMING':
                this.browserWindow.openFraming({ bringToFront: true, data: undefined })
                break
            case 'ALIGNMENT':
                this.browserWindow.openAlignment({ bringToFront: true })
                break
            case 'SEQUENCER':
                this.browserWindow.openSequencer({ bringToFront: true })
                break
            case 'FLAT_WIZARD':
                this.browserWindow.openFlatWizard({ bringToFront: true })
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
