import { Component, OnDestroy, OnInit } from '@angular/core'
import { FormControl } from '@angular/forms'
import { Subscription } from 'rxjs'
import { Camera } from '../shared/models/camera.model'
import { ApiService } from '../shared/services/api.service'
import { ConnectedEvent, DisconnectedEvent, Event, EventService } from '../shared/services/event.service'

@Component({
  selector: 'camera',
  templateUrl: 'camera.component.html',
  styleUrls: ['camera.component.scss'],
})
export class CameraTab implements OnInit, OnDestroy {

  connected = false
  camera?: Camera = undefined
  exposureUnitType = 'µs'
  exposureType = 'SINGLE'
  exposureMin = 0
  exposureMax = 0
  exposureDelay = 100

  readonly temperatureSetpoint = new FormControl<number>(0)
  readonly exposure = new FormControl<number>(0)
  readonly cameras = new FormControl<Camera[]>([])

  private eventSubscription: Subscription
  private updateCameraListTimer = null

  constructor(
    private apiService: ApiService,
    private eventService: EventService,
  ) {
    this.eventSubscription = eventService.subscribe((e) => this.eventReceived(e))
  }

  ngOnInit() {
    this.updateCameraList()
    this.updateCameraListTimer = setInterval(() => this.updateCameraList(), 30000)
  }

  ngOnDestroy() {
    clearTimeout(this.updateCameraListTimer)
    this.eventSubscription.unsubscribe()
  }

  private eventReceived(event: Event) {
    if (event instanceof ConnectedEvent) {
      this.connected = true
      this.updateCameraList()
    } else if (event instanceof DisconnectedEvent) {
      this.connected = false
      this.cameras.setValue([])
      this.camera = undefined
    }
  }

  private async updateCameraList() {
    const cameras = await this.apiService.cameras()

    this.cameras.setValue(cameras)

    if (this.camera) {
      for (const camera of cameras) {
        if (camera.name === this.camera.name) {
          this.camera = camera
          return
        }
      }
    }

    this.camera = undefined
    this.updateExposureUnitType()
  }

  cameraSelecionada() {
    this.updateExposureUnitType()
  }

  async connectCamera() {
    this.apiService.connectCamera(this.camera)
  }

  async disconnectCamera() {
    this.apiService.disconnectCamera(this.camera)
  }

  updateExposureUnitType(type: string = this.exposureUnitType) {
    if (!this.camera) {
      this.exposureMin = 0
      this.exposureMax = 0
      this.exposure.setValue(0)
      return
    }

    switch (type) {
      case 's':
        {
          const factor = this.exposureUnitType === 'µs' ? 1000000 : this.exposureUnitType === 'ms' ? 1000 : 1
          const value = this.exposure.value / factor
          this.exposureMin = Math.max(1, this.camera.exposureMin / 1000000)
          this.exposureMax = this.camera.exposureMax / 1000000
          this.exposure.setValue(Math.max(this.exposureMin, Math.min(value, this.exposureMax)))
          break
        }
      case 'ms':
        {
          const factor = this.exposureUnitType === 'µs' ? 1000 : this.exposureUnitType === 's' ? 1e-3 : 1
          const value = this.exposure.value / factor
          this.exposureMin = Math.max(1, this.camera.exposureMin / 1000)
          this.exposureMax = this.camera.exposureMax / 1000
          this.exposure.setValue(Math.max(this.exposureMin, Math.min(value, this.exposureMax)))
          break
        }
      case 'µs':
        {
          const factor = this.exposureUnitType === 'ms' ? 1000 : this.exposureUnitType === 's' ? 1000000 : 1e-3
          const value = this.exposure.value * factor
          this.exposureMin = Math.max(1, this.camera.exposureMin)
          this.exposureMax = this.camera.exposureMax
          this.exposure.setValue(Math.max(this.exposureMin, Math.min(value, this.exposureMax)))
          break
        }
    }

    this.exposureUnitType = type
  }
}
