import { PanzoomObject } from '@panzoom/panzoom'
import { Base64 } from 'js-base64'
import { AutoSubFolderMode } from '../shared/enums/auto-subfolder.enum'
import { ExposureType } from '../shared/enums/exposure-type.enum'
import { FrameType } from '../shared/enums/frame-type.enum'
import { Camera } from '../shared/models/camera.model'
import { FrameFormat } from '../shared/models/frame-format.model'
import { API_URL } from '../shared/services/api.service'
import { StorageService } from '../shared/services/storage.service'

export class CameraFrame {
  // Camera.
  camera?: Camera
  closeable = true
  path?: string
  image?: HTMLImageElement
  panzoom?: PanzoomObject
  // Capture.
  autoSaveAllExposures = false
  imageSavePath = ''
  autoSubFolder = true
  autoSubFolderMode: AutoSubFolderMode = 'NOON'
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
  // Frame.
  format = 'JPEG'
  midtone = 32767
  shadow = 0
  highlight = 65535
  flipH = false
  flipV = false
  invert = false

  static readonly EMPTY = new CameraFrame()

  get label() {
    return this.camera?.name ?? this.path.substring(this.path.lastIndexOf('/') + 1)
  }

  get src() {
    if (!this.path) return null
    return `${API_URL}/fits/${Base64.encode(this.path, true)}` +
      `?format=${this.format}&midtone=${this.midtone}&shadow=${this.shadow}` +
      `&highlight=${this.highlight}&flipH=${this.flipH}&flipV=${this.flipV}` +
      `&invert=${this.invert}` +
      `&ts=${this.camera?.latestCaptureDate}`
  }

  get exposureInMicroseconds() {
    if (this.camera) {
      switch (this.exposureUnitType) {
        case 's': return this.exposure * 1000000
        case 'ms': return this.exposure * 1000
        default: return this.exposure
      }
    }

    return 0
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
    if (this.camera) {
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

  loadOptions(storage: StorageService) {
    if (this.camera) {
      this.autoSaveAllExposures = storage.bool(`${this.camera.name}.autoSaveAllExposures`) ?? false
      this.autoSubFolderMode = (storage.string(`${this.camera.name}.autoSubFolderMode`) as AutoSubFolderMode) ?? 'NOON'
      this.imageSavePath = storage.string(`${this.camera.name}.imageSavePath`) ?? ''

      this.exposure = storage.number(`${this.camera.name}.exposure`) ?? 0
      this.exposureUnitType = storage.string(`${this.camera.name}.exposureUnitType`) ?? 'µs'
      this.exposureType = (storage.string(`${this.camera.name}.exposureType`) as ExposureType) ?? 'SINGLE'
      this.subFrameOn = storage.bool(`${this.camera.name}.subFrameOn`) ?? false
      this.x = storage.number(`${this.camera.name}.x`) ?? 0
      this.y = storage.number(`${this.camera.name}.y`) ?? 0
      this.width = storage.number(`${this.camera.name}.width`) ?? this.camera.maxWidth
      this.height = storage.number(`${this.camera.name}.height`) ?? this.camera.maxHeight
      this.frameType = (storage.string(`${this.camera.name}.frameType`) as FrameType) ?? 'DARK'
      this.binX = storage.number(`${this.camera.name}.binX`) ?? 1
      this.binY = storage.number(`${this.camera.name}.binY`) ?? 1
      this.frameFormat = this.camera.frameFormats.find((e) => e.name === storage.string(`${this.camera.name}.frameFormat`))

      this.updateExposureUnitType()
      this.updateROI()
    }
  }

  saveOptions(storage: StorageService) {
    if (this.camera) {
      storage.bool(`${this.camera.name}.autoSaveAllExposures`, this.autoSaveAllExposures)
      storage.string(`${this.camera.name}.autoSubFolderMode`, this.autoSubFolderMode)
      storage.string(`${this.camera.name}.imageSavePath`, this.imageSavePath)

      storage.number(`${this.camera.name}.exposure`, this.exposure)
      storage.string(`${this.camera.name}.exposureUnitType`, this.exposureUnitType)
      storage.string(`${this.camera.name}.exposureType`, this.exposureType)
      storage.bool(`${this.camera.name}.subFrameOn`, this.subFrameOn)
      storage.number(`${this.camera.name}.x`, this.x)
      storage.number(`${this.camera.name}.y`, this.y)
      storage.number(`${this.camera.name}.width`, this.width)
      storage.number(`${this.camera.name}.height`, this.height)
      storage.string(`${this.camera.name}.frameType`, this.frameType)
      storage.number(`${this.camera.name}.binX`, this.binX)
      storage.number(`${this.camera.name}.binY`, this.binY)
      storage.string(`${this.camera.name}.frameFormat`, this.frameFormat?.name)
    }
  }
}
