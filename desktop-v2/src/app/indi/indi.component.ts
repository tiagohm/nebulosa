import { AfterViewInit, Component, OnInit } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import Hex from 'hex-encoding'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Device, INDIProperty, INDIPropertyItem, INDISendProperty } from '../../shared/types'

export interface INDIParams {
    device?: Device
}

@Component({
    selector: 'app-indi',
    templateUrl: './indi.component.html',
    styleUrls: ['./indi.component.scss'],
})
export class INDIComponent implements OnInit, AfterViewInit {

    devices: Device[] = []
    properties: INDIProperty<any>[] = []
    groups: string[] = []

    device?: Device
    group = ""

    constructor(
        title: Title,
        private route: ActivatedRoute,
        private api: ApiService,
        electron: ElectronService,
    ) {
        title.setTitle('INDI')

        electron.ipcRenderer.on('DEVICE_PROPERTY_CHANGED', (_, data: INDIProperty<any>) => {
            this.addOrUpdateProperty(data)
            this.updateGroups()
        })

        electron.ipcRenderer.on('DEVICE_PROPERTY_DELETED', (_, data: INDIProperty<any>) => {
            const index = this.properties.findIndex((e) => e.name === data.name)

            if (index >= 0) {
                this.properties.splice(index, 1)
                this.updateGroups()
            }
        })
    }

    ngOnInit() { }

    async ngAfterViewInit() {
        this.route.queryParams.subscribe(e => {
            const params = JSON.parse(Hex.decodeStr(e.params)) as INDIParams
            this.device = params.device
        })

        this.devices = await this.api.attachedCameras()
    }

    deviceChanged() {
        this.updateProperties()
    }

    changeGroup(group: string) {
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

        this.groups = Array.from(groups)
            .sort((a, b) => a.localeCompare(b))

        if (!this.group || !this.groups.includes(this.group)) {
            this.group = this.groups[0]
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
