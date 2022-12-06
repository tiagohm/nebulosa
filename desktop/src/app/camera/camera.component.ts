import { Component, OnDestroy, OnInit } from '@angular/core'
import { MatCheckboxChange } from '@angular/material/checkbox'
import { MatTabChangeEvent } from '@angular/material/tabs'
import { Base64 } from 'js-base64'
import createPanZoom, { PanZoom } from 'panzoom'
import { Subscription } from 'rxjs'
import { ElectronService } from '../core/services/electron/electron.service'
import { AutoSubFolderMode } from '../shared/enums/auto-subfolder.enum'
import { ExposureType } from '../shared/enums/exposure-type.enum'
import { FrameType } from '../shared/enums/frame-type.enum'
import { CameraCaptureHistory } from '../shared/models/camera-capture-history.model'
import { Camera } from '../shared/models/camera.model'
import { FrameFormat } from '../shared/models/frame-format.model'
import { ApiService, API_URL } from '../shared/services/api.service'
import { ConnectedEvent, DisconnectedEvent, EventService, INDIEvent } from '../shared/services/event.service'
import { StorageService } from '../shared/services/storage.service'

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
    if (!this.path) return null
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
  exposureType: ExposureType = 'SINGLE'
  exposureDelay = 100
  x = 0
  y = 0
  width = 0
  height = 0
  subFrameOn = false
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

  // Options.
  autoSaveAllExposures = false
  imageSavePath = ''
  autoSubFolder = true
  autoSubFolderMode: AutoSubFolderMode = 'NOON'

  // History.
  cameraHistory: CameraCaptureHistory[] = []

  private eventSubscription: Subscription
  private refreshCamerasTimer = null
  private cameraHistoryTimer = null

  constructor(
    private apiService: ApiService,
    private eventService: EventService,
    private electronService: ElectronService,
    private storageService: StorageService,
  ) {
    this.eventSubscription = eventService.subscribe((e) => this.eventReceived(e))
  }

  ngOnInit() {
    this.refreshCameras()
    this.refreshCamerasTimer = setInterval(() => this.refreshCameras(), 5000)
    this.cameraHistoryTimer = setInterval(() => this.refreshCameraHistory(), 1000)
  }

  ngOnDestroy() {
    clearTimeout(this.refreshCamerasTimer)
    clearTimeout(this.cameraHistoryTimer)
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

      for (let i = 0; i < this.frames.length; i++) {
        if (this.frames[i].camera) {
          this.frames.splice(i, 1)
          i--
        }
      }
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

    this.cameraUpdated()
  }

  private async refreshCameraHistory() {
    if (this.selectedFrame?.camera) {
      const history = await this.apiService.cameraHistory(this.selectedFrame.camera)

      if (this.cameraHistory.length === 0 ||
        (history.length > 0 && this.cameraHistory[0].id != history[0].id)) {
        this.cameraHistory = history
      }
    } else {
      this.cameraHistory = []
    }
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

  async openFits() {
    const data = await this.electronService.ipcRenderer?.invoke('open-fits')
    this.newFrameFromPath(data)
  }

  private newFrameFromPath(path: string) {
    const frame = new CameraFrame()
    frame.label = path.substring(path.lastIndexOf('/') + 1)
    frame.path = path
    this.frames.push(frame)
  }

  closeFrame(frame: CameraFrame) {
    const idx = this.frames.indexOf(frame)

    if (idx >= 0) {
      this.frames.splice(idx, 1)
      frame.panZoom?.dispose()
      frame.panZoom = undefined
    }

    if (this.frames.length === 0) {
      this.selectedFrame = undefined
    }
  }

  frameSelected(event: MatTabChangeEvent) {
    if (event.index >= 0 &&
      event.index < this.frames.length) {
      this.selectedFrame = this.frames[event.index]
      this.format = this.selectedFrame.format
      this.midtone = Math.trunc(this.selectedFrame.midtone * 65535)
      this.shadow = Math.trunc(this.selectedFrame.shadow * 65535)
      this.highlight = Math.trunc(this.selectedFrame.highlight * 65535)
    }
  }

  applyImageProcessing() {
    this.selectedFrame.format = this.format
    this.selectedFrame.midtone = this.midtone / 65535
    this.selectedFrame.shadow = this.shadow / 65535
    this.selectedFrame.highlight = this.highlight / 65535
  }

  cameraUpdated() {
    this.frameFormat = this.camera?.frameFormats?.[0]

    this.loadCameraOptions()
  }

  async connectCamera() {
    this.apiService.connectCamera(this.camera)
  }

  async disconnectCamera() {
    this.apiService.disconnectCamera(this.camera)
  }

  updateROI(fullsize: boolean = false) {
    if (this.camera) {
      if (fullsize) {
        this.x = this.camera.minX
        this.y = this.camera.minY
        this.width = this.camera.maxWidth
        this.height = this.camera.maxHeight
      } else {
        this.x = Math.max(this.x, this.camera.minX)
        this.y = Math.max(this.y, this.camera.minY)
        this.width = this.width === 0 ? this.camera.maxWidth : Math.min(this.width, this.camera.maxWidth)
        this.height = this.height === 0 ? this.camera.maxHeight : Math.min(this.height, this.camera.maxHeight)
      }
    }
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

    this.saveCameraOptions()
  }

  autoSaveAllExposuresChanged(event: MatCheckboxChange) {
    this.autoSaveAllExposures = event.checked
    this.saveCameraOptions()
  }

  async openDirectoryForImageSavePath() {
    const data = await this.electronService.ipcRenderer.invoke('open-directory')
    this.imageSavePath = data
    this.saveCameraOptions()
  }

  autoSubFolderChanged(event: MatCheckboxChange) {
    this.autoSubFolder = event.checked
    this.saveCameraOptions()
  }

  autoSubFolderModeChanged(mode: AutoSubFolderMode) {
    this.autoSubFolderMode = mode
    this.saveCameraOptions()
  }

  loadCameraOptions() {
    if (this.camera) {
      this.autoSaveAllExposures = this.storageService.bool(`${this.camera.name}.autoSaveAllExposures`) ?? false
      this.autoSubFolderMode = (this.storageService.string(`${this.camera.name}.autoSubFolderMode`) as AutoSubFolderMode) ?? 'NOON'
      this.imageSavePath = this.storageService.string(`${this.camera.name}.imageSavePath`) ?? ''

      this.exposure = this.storageService.number(`${this.camera.name}.exposure`) ?? 0
      this.exposureUnitType = this.storageService.string(`${this.camera.name}.exposureUnitType`) ?? 'µs'
      this.exposureType = (this.storageService.string(`${this.camera.name}.exposureType`) as ExposureType) ?? 'SINGLE'
      this.subFrameOn = this.storageService.bool(`${this.camera.name}.subFrameOn`) ?? false
      this.x = this.storageService.number(`${this.camera.name}.x`) ?? 0
      this.y = this.storageService.number(`${this.camera.name}.y`) ?? 0
      this.width = this.storageService.number(`${this.camera.name}.width`) ?? this.camera.maxWidth
      this.height = this.storageService.number(`${this.camera.name}.height`) ?? this.camera.maxHeight
      this.frameType = (this.storageService.string(`${this.camera.name}.frameType`) as FrameType) ?? 'DARK'
      this.binX = this.storageService.number(`${this.camera.name}.binX`) ?? 1
      this.binY = this.storageService.number(`${this.camera.name}.binY`) ?? 1

      this.updateExposureUnitType()
      this.updateROI()
    }
  }

  saveCameraOptions() {
    if (this.camera) {
      this.storageService.bool(`${this.camera.name}.autoSaveAllExposures`, this.autoSaveAllExposures)
      this.storageService.string(`${this.camera.name}.autoSubFolderMode`, this.autoSubFolderMode)
      this.storageService.string(`${this.camera.name}.imageSavePath`, this.imageSavePath)

      this.storageService.number(`${this.camera.name}.exposure`, this.exposure)
      this.storageService.string(`${this.camera.name}.exposureUnitType`, this.exposureUnitType)
      this.storageService.string(`${this.camera.name}.exposureType`, this.exposureType)
      this.storageService.bool(`${this.camera.name}.subFrameOn`, this.subFrameOn)
      this.storageService.number(`${this.camera.name}.x`, this.x)
      this.storageService.number(`${this.camera.name}.y`, this.y)
      this.storageService.number(`${this.camera.name}.width`, this.width)
      this.storageService.number(`${this.camera.name}.height`, this.height)
      this.storageService.string(`${this.camera.name}.frameType`, this.frameType)
      this.storageService.number(`${this.camera.name}.binX`, this.binX)
      this.storageService.number(`${this.camera.name}.binY`, this.binY)
    }
  }

  loadHistory(history: CameraCaptureHistory) {
    if (this.selectedFrame && this.selectedFrame.camera?.name === history.name) {
      this.selectedFrame.path = history.path
    }
  }

  async startCapture() {
    const amount = this.exposureType === 'SINGLE' ? 1 : 2147483648

    this.saveCameraOptions()

    this.camera!.isCapturing = true

    await this.apiService.cameraStartCapture(this.camera, this.exposure, amount, this.exposureDelay,
      this.x, this.y, this.width, this.height, this.frameFormat.name, this.frameType,
      this.binX, this.binY, this.autoSaveAllExposures, this.imageSavePath,
      this.autoSubFolder ? this.autoSubFolderMode : 'OFF')
  }
}
