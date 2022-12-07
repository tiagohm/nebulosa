import { Component, OnDestroy, OnInit } from '@angular/core'
import { MatCheckboxChange } from '@angular/material/checkbox'
import { MatTabChangeEvent } from '@angular/material/tabs'
import panzoom from '@panzoom/panzoom'
import { Subscription } from 'rxjs'
import { ElectronService } from '../core/services/electron/electron.service'
import { AutoSubFolderMode } from '../shared/enums/auto-subfolder.enum'
import { CameraCaptureHistory } from '../shared/models/camera-capture-history.model'
import { ApiService } from '../shared/services/api.service'
import { ConnectedEvent, DisconnectedEvent, EventService, INDIEvent } from '../shared/services/event.service'
import { StorageService } from '../shared/services/storage.service'
import { CameraFrame } from './camera-frame.model'

@Component({
  selector: 'camera',
  templateUrl: 'camera.component.html',
  styleUrls: ['camera.component.scss'],
})
export class CameraTab implements OnInit, OnDestroy {

  connected = false

  frames: CameraFrame[] = []
  frame = CameraFrame.EMPTY

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

      for (let i = 0; i < this.frames.length; i++) {
        if (this.frames[i].camera) {
          this.frames.splice(i, 1)

          if (this.frame === this.frames[i]) {
            this.frame = CameraFrame.EMPTY
          }

          i--
        }
      }
    }
  }

  private async refreshCameras() {
    const cameras = await this.apiService.cameras()

    if (cameras.length === 0) {
      for (let i = 0; i < this.frames.length; i++) {
        if (this.frames[i].camera) {
          this.closeFrame(this.frames[i])
          i--
        }
      }

      return
    }

    for (let i = 0; i < cameras.length; i++) {
      const idx = this.frames.findIndex((e) => e.camera?.name === cameras[i].name)

      if (idx >= 0) {
        const frameFormat = cameras[i].frameFormats.find((e) => e.name === this.frames[idx].frameFormat?.name)
        const connectionChanged = this.frames[idx].camera.isConnected != cameras[i].isConnected
        Object.assign(this.frames[idx].camera, cameras[i])
        if (connectionChanged) this.frames[idx].updateROI()
        this.frames[idx].frameFormat = frameFormat
        if (cameras[i].latestCapturePath) this.frames[idx].path = cameras[i].latestCapturePath
      } else {
        const frame = new CameraFrame()
        frame.closeable = false
        frame.camera = cameras[i]
        frame.loadOptions(this.storageService)
        this.frames.push(frame)
      }
    }
  }

  private async refreshCameraHistory() {
    if (this.frame.camera) {
      const history = await this.apiService.cameraHistory(this.frame.camera)

      if (this.cameraHistory.length === 0 ||
        (history.length > 0 && this.cameraHistory[0].id != history[0].id)) {
        this.cameraHistory = history
      }
    } else {
      this.cameraHistory = []
    }
  }

  frameLoaded(image: Event, frame: CameraFrame) {
    if (!frame.panzoom) {
      const e = image.target as HTMLImageElement
      frame.image = e
      frame.panzoom = panzoom(e, {
        animate: true,
        pinchAndPan: true,
        minScale: 0.1,
        maxScale: 500,
        step: 0.5,
        cursor: 'default',
        contain: 'outside',
      })

      e.addEventListener('wheel', frame.panzoom.zoomWithWheel)
    }
  }

  async openFits() {
    const data = await this.electronService.ipcRenderer?.invoke('open-fits')
    this.newFrameFromPath(data)
  }

  private newFrameFromPath(path: string) {
    const frame = new CameraFrame()
    frame.path = path
    this.frames.push(frame)
  }

  closeFrame(frame: CameraFrame) {
    const idx = this.frames.indexOf(frame)

    if (idx >= 0) {
      this.frames.splice(idx, 1)
      frame.image?.removeEventListener('wheel', frame.panzoom?.zoomWithWheel)
      frame.panzoom?.destroy()
      frame.panzoom = undefined
    }

    if (this.frames.length === 0) {
      this.frame = CameraFrame.EMPTY
    }
  }

  frameSelected(event: MatTabChangeEvent) {
    if (event.index >= 0 && event.index < this.frames.length) {
      this.frame = this.frames[event.index]
      this.frame.loadOptions(this.storageService)
    } else {
      this.frame = CameraFrame.EMPTY
    }
  }

  connectCamera() {
    if (this.frame.camera) {
      this.apiService.connectCamera(this.frame.camera)
    }
  }

  disconnectCamera() {
    if (this.frame.camera) {
      this.apiService.disconnectCamera(this.frame.camera)
    }
  }

  autoSaveAllExposuresChanged(event: MatCheckboxChange) {
    if (this.frame.camera) {
      const frame = this.frame
      frame.autoSaveAllExposures = event.checked
      this.saveFrameOptions()
    }
  }

  async openDirectoryForImageSavePath() {
    if (this.frame.camera) {
      const frame = this.frame
      const data = await this.electronService.ipcRenderer.invoke('open-directory')
      frame.imageSavePath = data
      this.saveFrameOptions()
    }
  }

  autoSubFolderChanged(event: MatCheckboxChange) {
    if (this.frame.camera) {
      const frame = this.frame
      frame.autoSubFolder = event.checked
      this.saveFrameOptions()
    }
  }

  autoSubFolderModeChanged(mode: AutoSubFolderMode) {
    if (this.frame.camera) {
      const frame = this.frame
      frame.autoSubFolderMode = mode
      this.saveFrameOptions()
    }
  }

  private saveFrameOptions() {
    this.frame.saveOptions(this.storageService)
  }

  loadHistory(history: CameraCaptureHistory) {
    if (this.frame && this.frame.camera?.name === history.name) {
      this.frame.path = history.path
    }
  }

  async startCapture() {
    if (this.frame.camera) {
      const frame = this.frame
      const amount = frame.exposureType === 'SINGLE' ? 1 : 1999999999

      this.saveFrameOptions()

      frame.camera.isCapturing = true

      await this.apiService.cameraStartCapture(frame.camera, frame.exposureInMicroseconds, amount, frame.exposureDelay,
        frame.x, frame.y, frame.width, frame.height, frame.frameFormat.name, frame.frameType,
        frame.binX, frame.binY, frame.autoSaveAllExposures, frame.imageSavePath,
        frame.autoSubFolder ? frame.autoSubFolderMode : 'OFF')
    }
  }

  async stopCapture() {
    if (this.frame.camera) {
      this.apiService.cameraStopCapture(this.frame.camera)
    }
  }
}
