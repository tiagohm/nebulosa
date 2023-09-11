import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import { MenuItem } from 'primeng/api'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Device, INDIDeviceMessage, INDIProperty, INDIPropertyItem, INDISendProperty } from '../../shared/types'

export interface INDIParams {
    device?: Device
}

@Component({
    selector: 'app-indi',
    templateUrl: './indi.component.html',
    styleUrls: ['./indi.component.scss'],
})
export class INDIComponent implements AfterViewInit, OnDestroy {

    devices: Device[] = []
    properties: INDIProperty<any>[] = []
    groups: MenuItem[] = []

    device?: Device
    group = ''
    showLog = false
    messages: string[] = []

    constructor(
        title: Title,
        private route: ActivatedRoute,
        private api: ApiService,
        electron: ElectronService,
        ngZone: NgZone,
    ) {
        title.setTitle('INDI')

        this.api.startListening('INDI')

        electron.on('DEVICE_PROPERTY_CHANGED', (_, data: INDIProperty<any>) => {
            ngZone.run(() => {
                this.addOrUpdateProperty(data)
                this.updateGroups()
            })
        })

        electron.on('DEVICE_PROPERTY_DELETED', (_, data: INDIProperty<any>) => {
            const index = this.properties.findIndex((e) => e.name === data.name)

            if (index >= 0) {
                ngZone.run(() => {
                    this.properties.splice(index, 1)
                    this.updateGroups()
                })
            }
        })

        electron.on('DEVICE_MESSAGE_RECEIVED', (_, data: INDIDeviceMessage) => {
            if (this.device && data.device?.name === this.device.name) {
                ngZone.run(() => {
                    this.messages.splice(0, 0, data.message)
                })
            }
        })
    }

    async ngAfterViewInit() {
        this.route.queryParams.subscribe(e => {
            const params = JSON.parse(decodeURIComponent(e.params)) as INDIParams
            this.device = params.device
        })

        this.devices = [
            ...await this.api.cameras(),
            ...await this.api.mounts(),
            ...await this.api.attachedFocusers(),
            ...await this.api.attachedWheels(),
        ]
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.api.stopListening('INDI')
    }

    async deviceChanged() {
        this.updateProperties()
        this.messages = await this.api.indiLog(this.device!)
    }

    changeGroup(group: string) {
        this.showLog = false
        this.group = group
    }

    send(property: INDISendProperty) {
        this.api.sendIndiProperty(this.device!, property)
    }

    private updateGroups() {
        const groups = new Set<string>()

        for (const property of this.properties) {
            groups.add(property.group)
        }

        let groupsChanged = false

        if (this.groups.length === groups.size) {
            let index = 0

            for (const item of groups) {
                if (this.groups[index++].label !== item) {
                    groupsChanged = true
                    break
                }
            }
        } else {
            groupsChanged = true
        }

        if (this.groups.length === 0 || groupsChanged) {
            this.groups = Array.from(groups)
                .sort((a, b) => a.localeCompare(b))
                .map(e => <MenuItem>{
                    icon: 'mdi mdi-sitemap',
                    label: e,
                    command: () => this.changeGroup(e),
                })
        }

        if (!this.group || !this.groups.find(e => e.label === this.group)) {
            this.group = this.groups[0].label!
        }
    }

    async updateProperties() {
        if (this.device) {
            const properties = await this.api.indiProperties(this.device)

            for (const property of properties) {
                this.addOrUpdateProperty(property)
            }

            for (let i = 0; i < this.properties.length; i++) {
                if (properties.findIndex(e => e.name === this.properties[i].name) < 0) {
                    this.properties.splice(i--, 1)
                }
            }

            this.updateGroups()
        }
    }

    private addOrUpdateProperty<T>(property: INDIProperty<T>) {
        const index = this.properties.findIndex(e => e.name === property.name)

        if (index >= 0) {
            this.updateProperty(this.properties[index], property)
        } else {
            this.properties.push(property)
        }
    }

    private updateProperty<T>(current: INDIProperty<T>, update: INDIProperty<T>) {
        current.state = update.state
        current.perm = update.perm

        for (const item of update.items) {
            const index = current.items.findIndex(e => e.name == item.name)

            if (index >= 0) {
                this.updatePropertyItem(current.items[index], item)
            } else {
                current.items.push(item)
            }
        }

        for (let i = 0; i < current.items.length; i++) {
            if (update.items.findIndex(e => e.name === current.items[i].name) < 0) {
                current.items.splice(i--, 1)
            }
        }
    }

    private updatePropertyItem<T>(current: INDIPropertyItem<T>, update: INDIPropertyItem<T>) {
        current.value = update.value
    }
}
