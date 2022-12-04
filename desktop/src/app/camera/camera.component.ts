import { Component, NgZone, OnDestroy, OnInit } from '@angular/core'
import { MatTabChangeEvent } from '@angular/material/tabs'
import { Base64 } from 'js-base64'
import createPanZoom, { PanZoom } from 'panzoom'
import { Subscription } from 'rxjs'
import { ElectronService } from '../core/services/electron/electron.service'
import { FrameType } from '../shared/enums/frame-type.enum'
import { Camera } from '../shared/models/camera.model'
import { FrameFormat } from '../shared/models/frame-format.model'
import { ApiService, API_URL } from '../shared/services/api.service'
import { ConnectedEvent, DisconnectedEvent, EventService, INDIEvent } from '../shared/services/event.service'

export class CameraFrame {
  label: string
  camera?: Camera
  panZoom?: PanZoom
  closeable = true
  path?: string
  format = 'JPEG'
  midtone = 0.5
  shadow = 0.0
  highlight = 1.0

  get src() {
    return `${API_URL}/fits/${Base64.encode(this.path, true)}` +
      `?format=${this.format}&midtone=${this.midtone}&shadow=${this.shadow}` +
      `&highlight=${this.highlight}`
  }
}

@Component({
  selector: 'camera',
  templateUrl: 'camera.component.html',
  styleUrls: ['camera.component.scss'],
})
export class CameraTab implements OnInit, OnDestroy {

  connected = false
  cameras: Camera[] = []
  camera?: Camera = undefined
  temperatureSetpoint = 0
  exposure = 0
  exposureMin = 0
  exposureMax = 0
  exposureUnitType = 'µs'
  exposureType = 'SINGLE'
  exposureDelay = 100
  x = 0
  y = 0
  width = 0
  height = 0
  subframeOn = false
  frameType: FrameType = 'LIGHT'
  frameFormat?: FrameFormat = undefined
  binX = 1
  binY = 1

  frames: CameraFrame[] = []
  selectedFrame?: CameraFrame
  format = 'JPEG'
  midtone = 32767
  shadow = 0
  highlight = 65535

  private eventSubscription: Subscription
  private updateCameraListTimer = null

  constructor(
    private apiService: ApiService,
    private eventService: EventService,
    private electronService: ElectronService,
    private ngZone: NgZone,
  ) {
    this.eventSubscription = eventService.subscribe((e) => this.eventReceived(e))

    electronService.ipcRenderer.on('fits-open', (event, data) => this.newFrameFromPath(data))
  }

  ngOnInit() {
    this.refreshCameras()
    this.updateCameraListTimer = setInterval(() => this.refreshCameras(), 30000)
  }

  ngOnDestroy() {
    clearTimeout(this.updateCameraListTimer)
    this.eventSubscription.unsubscribe()
  }

  private eventReceived(event: INDIEvent) {
    if (event instanceof ConnectedEvent) {
      this.connected = true
      this.refreshCameras()
    } else if (event instanceof DisconnectedEvent) {
      this.connected = false
      this.cameras = []
      this.camera = undefined
    }
  }

  private async refreshCameras() {
    const cameras = await this.apiService.cameras()

    for (let i = 0; i < cameras.length; i++) {
      const camera = cameras[i]
      const idx = this.cameras.findIndex((e) => e.name === camera.name)

      if (idx >= 0) {
        Object.assign(this.cameras[idx], camera)
      } else {
        this.cameras.push(camera)
        const frame = new CameraFrame()
        frame.label = camera.name
        frame.camera = camera
        this.frames.push(frame)
      }
    }

    this.updateCamera()
  }

  frameLoaded(image: Event, frame: CameraFrame) {
    if (!frame.panZoom) {
      const e = image.target as HTMLElement
      const panZoom = createPanZoom(e, {
        minZoom: 0.1,
        maxZoom: 500,
      })
      frame.panZoom = panZoom
    }
  }

  openFile() {
    this.electronService.ipcRenderer.send('open-fits')
  }

  private newFrameFromPath(path: string) {
    this.ngZone.run(() => {
      const frame = new CameraFrame()
      frame.label = path.substring(path.lastIndexOf('/') + 1)
      frame.path = path
      this.frames.push(frame)
    })
  }

  closeFrame(frame: CameraFrame) {
    const idx = this.frames.indexOf(frame)

    if (idx >= 0) {
      this.frames.splice(idx, 1)
      frame.panZoom?.dispose()
    }

    if (this.frames.length === 0) {
      this.selectedFrame = undefined
    }
  }

  frameSelected(event: MatTabChangeEvent) {
    if (event.index >= 0 && event.index < this.frames.length) {
      this.selectedFrame = this.frames[event.index]
      this.format = this.selectedFrame.format
      this.midtone = Math.trunc(this.selectedFrame.midtone * 65535)
      this.shadow = Math.trunc(this.selectedFrame.shadow * 65535)
      this.highlight = Math.trunc(this.selectedFrame.highlight * 65535)
    }
  }

  applySTF() {
    this.selectedFrame.format = this.format
    this.selectedFrame.midtone = this.midtone / 65535
    this.selectedFrame.shadow = this.shadow / 65535
    this.selectedFrame.highlight = this.highlight / 65535
  }

  updateCamera() {
    this.updateExposureUnitType()

    this.frameFormat = this.camera?.frameFormats?.[0]
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
      this.exposure = 0
      return
    }

    switch (type) {
      case 's':
        {
          const factor = this.exposureUnitType === 'µs' ? 1000000 : this.exposureUnitType === 'ms' ? 1000 : 1
          const value = this.exposure / factor
          this.exposureMin = Math.max(1, this.camera.exposureMin / 1000000)
          this.exposureMax = this.camera.exposureMax / 1000000
          this.exposure = Math.max(this.exposureMin, Math.min(value, this.exposureMax))
          break
        }
      case 'ms':
        {
          const factor = this.exposureUnitType === 'µs' ? 1000 : this.exposureUnitType === 's' ? 1e-3 : 1
          const value = this.exposure / factor
          this.exposureMin = Math.max(1, this.camera.exposureMin / 1000)
          this.exposureMax = this.camera.exposureMax / 1000
          this.exposure = Math.max(this.exposureMin, Math.min(value, this.exposureMax))
          break
        }
      case 'µs':
        {
          const factor = this.exposureUnitType === 'ms' ? 1000 : this.exposureUnitType === 's' ? 1000000 : 1e-3
          const value = this.exposure * factor
          this.exposureMin = Math.max(1, this.camera.exposureMin)
          this.exposureMax = this.camera.exposureMax
          this.exposure = Math.max(this.exposureMin, Math.min(value, this.exposureMax))
          break
        }
    }

    this.exposureUnitType = type
  }
}
