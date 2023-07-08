import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import Hex from 'hex-encoding'
import { ApiService } from '../../shared/services/api.service'
import { Device, INDIProperty, INDIPropertyItem, INDISendProperty } from '../../shared/types'

export interface INDIParams {
    device?: Device
}

@Component({
    selector: 'app-indi',
    templateUrl: './indi.component.html',
    styleUrls: ['./indi.component.scss'],
})
export class INDIComponent implements OnInit, AfterViewInit, OnDestroy {

    devices: Device[] = []
    properties: INDIProperty<any>[] = []
    groups: string[] = []

    device?: Device
    group = ""

    private devicesTimer: any
    private propertiesTimer: any

    constructor(
        title: Title,
        private route: ActivatedRoute,
        private api: ApiService,
    ) {
        title.setTitle('INDI')

        this.devicesTimer = setInterval(async () => this.updateDevices(), 60000)
        this.propertiesTimer = setInterval(async () => this.updateProperties(), 5000)
    }

    ngOnInit() { }

    async ngAfterViewInit() {
        this.route.queryParams.subscribe(e => {
            const params = JSON.parse(Hex.decodeStr(e.params)) as INDIParams
            this.device = params.device
        })

        await this.updateDevices()
    }

    ngOnDestroy() {
        clearInterval(this.devicesTimer)
        clearInterval(this.propertiesTimer)
    }

    changeGroup(group: string) {
        this.group = group
    }

    send(property: INDISendProperty) {
        this.api.sendIndiProperty(this.device!, property)
    }

    async updateProperties() {
        if (this.device) {
            const properties = await this.api.indiProperties(this.device)
            const groups = new Set<string>()

            for (const property of properties) {
                groups.add(property.group)
            }

            this.groups = Array.from(groups)
                .sort((a, b) => a.localeCompare(b))

            if (!this.group && this.groups.length > 0) {
                this.group = this.groups[0]
            }

            let position = 0

            for (const property of properties) {
                const index = this.properties.findIndex(e => e.name === property.name)

                if (index >= 0) {
                    this.updateProperty(this.properties[index], property)
                } else {
                    this.properties.splice(position, 0, property)
                }

                position++
            }

            for (let i = 0; i < this.properties.length; i++) {
                if (properties.findIndex(e => e.name === this.properties[i].name) < 0) {
                    this.properties.splice(i--, 1)
                }
            }
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

    private async updateDevices() {
        const cameras = await this.api.attachedCameras()

        this.devices = [...cameras]

        if (this.devices.findIndex(e => e.name === this.device?.name) < 0) {
            this.device = this.devices[0]
        }
    }
}
