import { AfterContentInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { dirname } from 'path'
import { DeviceChooserComponent } from '../../shared/components/device-chooser/device-chooser.component'
import { DeviceConnectionCommandEvent, DeviceListMenuComponent } from '../../shared/components/device-list-menu/device-list-menu.component'
import { SlideMenuItem } from '../../shared/components/slide-menu/slide-menu.component'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { Camera } from '../../shared/types/camera.types'
import { Device } from '../../shared/types/device.types'
import { Focuser } from '../../shared/types/focuser.types'
import { CONNECTION_TYPES, ConnectionDetails, EMPTY_CONNECTION_DETAILS, HomeWindowType } from '../../shared/types/home.types'
import { Mount } from '../../shared/types/mount.types'
import { Rotator } from '../../shared/types/rotator.types'
import { FilterWheel } from '../../shared/types/wheel.types'
import { AppComponent } from '../app.component'

type MappedDevice = {
    'CAMERA': Camera
    'MOUNT': Mount
    'FOCUSER': Focuser
    'WHEEL': FilterWheel
    'ROTATOR': Rotator
}

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements AfterContentInit, OnDestroy {

    @ViewChild('deviceMenu')
    private readonly deviceMenu!: DeviceListMenuComponent

    @ViewChild('imageMenu')
    private readonly imageMenu!: DeviceListMenuComponent

    readonly connectionTypes = Array.from(CONNECTION_TYPES)
    showConnectionDialog = false
    connections: ConnectionDetails[] = []
    connection?: ConnectionDetails
    newConnection?: [ConnectionDetails, ConnectionDetails | undefined]
    skyAtlasProgress?: number = undefined

    cameras: Camera[] = []
    mounts: Mount[] = []
    focusers: Focuser[] = []
    wheels: FilterWheel[] = []
    rotators: Rotator[] = []
    domes: Camera[] = []
    switches: Camera[] = []

    currentPage = 0

    get connected() {
        return !!this.connection && this.connection.connected
    }

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

    get hasAutoFocus() {
        return this.hasCamera && this.hasFocuser
    }

    get hasFlatWizard() {
        return this.hasCamera
    }

    get hasDevices() {
        return this.hasCamera || this.hasMount || this.hasFocuser
            || this.hasWheel || this.hasDome || this.hasRotator || this.hasSwitch
    }

    get hasINDI() {
        return this.connection?.type === 'INDI' && this.hasDevices
    }

    get hasAlpaca() {
        return this.connection?.type === 'ALPACA' && this.hasDevices
    }

    readonly deviceModel: SlideMenuItem[] = []

    readonly imageModel: SlideMenuItem[] = [
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
        onUpdate: (device: MappedDevice[K]) => void,
    ) {
        this.electron.on(`${type}.ATTACHED`, event => {
            this.ngZone.run(() => {
                onAdd(event.device as never)
            })
        })

        this.electron.on(`${type}.DETACHED`, event => {
            this.ngZone.run(() => {
                onRemove(event.device as never)
            })
        })

        this.electron.on(`${type}.UPDATED`, event => {
            this.ngZone.run(() => {
                onUpdate(event.device as never)
            })
        })
    }

    constructor(
        app: AppComponent,
        private electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private api: ApiService,
        private prime: PrimeService,
        private preference: PreferenceService,
        private ngZone: NgZone,
    ) {
        app.title = 'Nebulosa'

        this.startListening('CAMERA',
            device => {
                return this.cameras.push(device)
            },
            device => {
                this.cameras.splice(this.cameras.findIndex(e => e.id === device.id), 1)
                return this.cameras.length
            },
            device => {
                const found = this.cameras.find(e => e.id === device.id)
                if (!found) return
                Object.assign(found, device)
            }
        )

        this.startListening('MOUNT',
            device => {
                return this.mounts.push(device)
            },
            device => {
                this.mounts.splice(this.mounts.findIndex(e => e.id === device.id), 1)
                return this.mounts.length
            },
            device => {
                const found = this.mounts.find(e => e.id === device.id)
                if (!found) return
                Object.assign(found, device)
            }
        )

        this.startListening('FOCUSER',
            device => {
                return this.focusers.push(device)
            },
            device => {
                this.focusers.splice(this.focusers.findIndex(e => e.id === device.id), 1)
                return this.focusers.length
            },
            device => {
                const found = this.focusers.find(e => e.id === device.id)
                if (!found) return
                Object.assign(found, device)
            }
        )

        this.startListening('WHEEL',
            device => {
                return this.wheels.push(device)
            },
            device => {
                this.wheels.splice(this.wheels.findIndex(e => e.id === device.id), 1)
                return this.wheels.length
            },
            device => {
                const found = this.wheels.find(e => e.id === device.id)
                if (!found) return
                Object.assign(found, device)
            }
        )

        this.startListening('ROTATOR',
            device => {
                return this.rotators.push(device)
            },
            device => {
                this.rotators.splice(this.rotators.findIndex(e => e.id === device.id), 1)
                return this.rotators.length
            },
            device => {
                const found = this.rotators.find(e => e.id === device.id)
                if (!found) return
                Object.assign(found, device)
            }
        )

        electron.on('CONNECTION.CLOSED', event => {
            if (this.connection?.id === event.id) {
                ngZone.run(() => {
                    this.updateConnection()
                })
            }
        })

        electron.on('SKY_ATLAS.PROGRESS_CHANGED', event => {
            ngZone.run(() => {
                if (event.progress >= 100) {
                    this.skyAtlasProgress = undefined
                } else {
                    this.skyAtlasProgress = event.progress
                }
            })
        })

        this.connections = preference.connections.get().sort((a, b) => (b.connectedAt ?? 0) - (a.connectedAt ?? 0))
        this.connections.forEach(e => { e.id = undefined; e.connected = false })
        this.connection = this.connections[0]
    }

    async ngAfterContentInit() {
        await this.updateConnection()

        if (this.connected) {
            this.cameras = await this.api.cameras()
            this.mounts = await this.api.mounts()
            this.focusers = await this.api.focusers()
            this.wheels = await this.api.wheels()
            this.rotators = await this.api.rotators()
        }
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    addConnection() {
        this.newConnection = [structuredClone(EMPTY_CONNECTION_DETAILS), undefined]
        this.showConnectionDialog = true
    }

    editConnection(connection: ConnectionDetails, event: MouseEvent) {
        this.newConnection = [structuredClone(connection), connection]
        this.showConnectionDialog = true
        event.stopImmediatePropagation()
    }

    deleteConnection(connection: ConnectionDetails, event: MouseEvent) {
        const index = this.connections.findIndex(e => e === connection)

        if (index >= 0 && !connection.connected) {
            this.connections.splice(index, 1)

            if (connection === this.connection) {
                this.connection = this.connections[0]
            }

            this.preference.connections.set(this.connections)
        }

        event.stopImmediatePropagation()
    }

    saveConnection() {
        if (this.newConnection) {
            // Edit.
            if (this.newConnection[1]) {
                Object.assign(this.newConnection[1], this.newConnection[0])
            }
            // New.
            else {
                const newConnection = structuredClone(this.newConnection[0])
                this.connections = [...this.connections, newConnection]
                this.connection = newConnection
            }
        }

        this.preference.connections.set(this.connections)

        this.newConnection = undefined
        this.showConnectionDialog = false
    }

    async connect() {
        try {
            if (this.connection && !this.connection.connected) {
                this.connection.id = await this.api.connect(this.connection.host, this.connection.port, this.connection.type)
            }
        } catch (e) {
            console.error(e)

            this.prime.message('Connection failed', 'error')
        } finally {
            await this.updateConnection()
        }
    }

    async disconnect() {
        try {
            if (this.connection && this.connection.connected) {
                await this.api.disconnect(this.connection.id!)
            }
        } catch (e) {
            console.error(e)
        } finally {
            this.updateConnection()
        }
    }

    protected findDeviceById(id: string) {
        return this.cameras.find(e => e.id === id) ||
            this.mounts.find(e => e.id === id) ||
            this.wheels.find(e => e.id === id) ||
            this.focusers.find(e => e.id === id) ||
            this.rotators.find(e => e.id === id)
    }

    protected async deviceConnected(event: DeviceConnectionCommandEvent) {
        DeviceChooserComponent.handleConnectDevice(this.api, event.device, event.item)
    }

    protected async deviceDisconnected(event: DeviceConnectionCommandEvent) {
        DeviceChooserComponent.handleDisconnectDevice(this.api, event.device, event.item)
    }

    private async openDevice<K extends keyof MappedDevice>(type: K) {
        this.deviceModel.length = 0

        const devices: Device[] = type === 'CAMERA' ? this.cameras
            : type === 'MOUNT' ? this.mounts
                : type === 'FOCUSER' ? this.focusers
                    : type === 'WHEEL' ? this.wheels
                        : type === 'ROTATOR' ? this.rotators
                            : []

        if (devices.length === 0) return

        this.deviceMenu.header = type
        const device = await this.deviceMenu.show(devices)

        if (device && device !== 'NONE') {
            this.openDeviceWindow(type, device as any)
        }
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
            case 'ROTATOR':
                this.browserWindow.openRotator({ bringToFront: true, data: device as Rotator })
                break
        }
    }

    private async openImage(force: boolean = false) {
        if (force || this.cameras.length === 0) {
            const preference = this.preference.homePreference.get()
            const path = await this.electron.openImage({ defaultPath: preference.imagePath })

            if (path) {
                preference.imagePath = dirname(path)
                this.preference.homePreference.set(preference)
                this.browserWindow.openImage({ path, source: 'PATH' })
            }
        } else {
            const camera = await this.imageMenu.show(this.cameras)

            if (camera && camera !== 'NONE') {
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
            case 'ROTATOR':
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
            case 'AUTO_FOCUS':
                this.browserWindow.openAutoFocus({ bringToFront: true })
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
            case 'CALCULATOR':
                this.browserWindow.openCalculator()
                break
            case 'ABOUT':
                this.browserWindow.openAbout()
                break
        }
    }

    private async updateConnection() {
        if (this.connection && this.connection.id) {
            try {
                const status = await this.api.connectionStatus(this.connection.id!)

                if (status && !this.connection.connected) {
                    this.connection.connectedAt = Date.now()
                    this.preference.connections.set(this.connections)
                    this.connection.connected = true
                } else if (!status) {
                    this.connection.connected = false
                }
            } catch {
                this.connection.connected = false
            }
        } else {
            const statuses = await this.api.connectionStatuses()

            for (const status of statuses) {
                for (const connection of this.connections) {
                    if (!connection.connected &&
                        (status.host === connection.host || status.ip === connection.host) &&
                        status.port === connection.port) {
                        connection.id = status.id
                        connection.type = status.type
                        connection.connected = true
                        this.connection = connection
                        break
                    }
                }

                if (this.connection?.connected) {
                    break
                }
            }
        }

        if (!this.connection?.connected) {
            this.cameras = []
            this.mounts = []
            this.focusers = []
            this.wheels = []
            this.domes = []
            this.rotators = []
            this.switches = []
        }
    }

    private scrollPageOf(element: Element) {
        return parseInt(element.getAttribute('scroll-page') || '0')
    }

    scrolled(event: Event) {
        function isVisible(element: Element) {
            const bound = element.getBoundingClientRect()

            return bound.top >= 0 &&
                bound.left >= 0 &&
                bound.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
                bound.right <= (window.innerWidth || document.documentElement.clientWidth)
        }

        let page = 0
        const scrollChidren = document.getElementsByClassName('scroll-child')

        for (let i = 0; i < scrollChidren.length; i++) {
            const child = scrollChidren[i]

            if (isVisible(child)) {
                page = Math.max(page, this.scrollPageOf(child))
            }
        }

        this.currentPage = page
    }

    scrollTo(event: Event, page: number) {
        this.currentPage = page
        this.scrollToPage(page)
        event.stopImmediatePropagation()
    }

    scrollToPage(page: number) {
        const scrollChidren = document.getElementsByClassName('scroll-child')

        for (let i = 0; i < scrollChidren.length; i++) {
            const child = scrollChidren[i]

            if (this.scrollPageOf(child) === page) {
                child.scrollIntoView({ behavior: 'smooth' })
                break
            }
        }
    }
}
