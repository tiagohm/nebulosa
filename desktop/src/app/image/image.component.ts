import { AfterViewInit, Component, ElementRef, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import hotkeys from 'hotkeys-js'
import { NgxLegacyMoveableComponent, OnDrag, OnResize, OnRotate } from 'ngx-moveable'
import createPanZoom from 'panzoom'
import { basename, dirname, extname } from 'path'
import { ContextMenu } from 'primeng/contextmenu'
import { DeviceListMenuComponent } from '../../shared/components/device-list-menu/device-list-menu.component'
import { HistogramComponent } from '../../shared/components/histogram/histogram.component'
import { MenuItem } from '../../shared/components/menu-item/menu-item.component'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { AngularService } from '../../shared/services/angular.service'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { EquatorialCoordinateJ2000, filterAstronomicalObject } from '../../shared/types/atlas.types'
import { Camera } from '../../shared/types/camera.types'
import {
	AstronomicalObjectDialog,
	DEFAULT_FOV,
	DEFAULT_IMAGE_ANNOTATION_DIALOG,
	DEFAULT_IMAGE_CALIBRATION,
	DEFAULT_IMAGE_DATA,
	DEFAULT_IMAGE_FOV_DIALOG,
	DEFAULT_IMAGE_LIVE_STACKING,
	DEFAULT_IMAGE_MOUSE_COORDINATES,
	DEFAULT_IMAGE_MOUSE_POSITION,
	DEFAULT_IMAGE_PREFERENCE,
	DEFAULT_IMAGE_ROI,
	DEFAULT_IMAGE_SAVE_DIALOG,
	DEFAULT_IMAGE_SETTINGS_DIALOG,
	DEFAULT_IMAGE_SOLVED,
	DEFAULT_IMAGE_SOLVER_DIALOG,
	DEFAULT_IMAGE_STATISTICS_DIALOG,
	DEFAULT_IMAGE_ZOOM,
	DEFAULT_STAR_DETECTOR_DIALOG,
	DetectedStar,
	FOV,
	ImageAnnotation,
	ImageHeaderItem,
	ImageHeadersDialog,
	ImageInfo,
	ImageSCNRDialog,
	ImageSolved,
	ImageStretchDialog,
	LiveStackingMode,
	OpenImage,
	imageFormatFromExtension,
} from '../../shared/types/image.types'
import { Mount } from '../../shared/types/mount.types'
import { PlateSolverRequest } from '../../shared/types/platesolver.types'
import { StarDetectionRequest } from '../../shared/types/stardetector.types'
import { CoordinateInterpolator } from '../../shared/utils/coordinate-interpolation'
import { AppComponent } from '../app.component'

@Component({
	selector: 'neb-image',
	templateUrl: './image.component.html',
	styleUrls: ['./image.component.scss'],
})
export class ImageComponent implements AfterViewInit, OnDestroy {
	protected readonly preference = structuredClone(DEFAULT_IMAGE_PREFERENCE)
	protected readonly solver = structuredClone(DEFAULT_IMAGE_SOLVER_DIALOG)
	protected readonly starDetector = structuredClone(DEFAULT_STAR_DETECTOR_DIALOG)
	protected transformation = this.preference.transformation
	protected readonly fov = structuredClone(DEFAULT_IMAGE_FOV_DIALOG)
	protected readonly annotation = structuredClone(DEFAULT_IMAGE_ANNOTATION_DIALOG)
	protected readonly imageROI = structuredClone(DEFAULT_IMAGE_ROI)
	protected readonly saveAs = structuredClone(DEFAULT_IMAGE_SAVE_DIALOG)
	protected readonly statistics = structuredClone(DEFAULT_IMAGE_STATISTICS_DIALOG)
	protected readonly mouseCoordinate = structuredClone(DEFAULT_IMAGE_MOUSE_COORDINATES)
	protected readonly liveStacking = structuredClone(DEFAULT_IMAGE_LIVE_STACKING)
	protected readonly zoom = structuredClone(DEFAULT_IMAGE_ZOOM)
	protected readonly settings = structuredClone(DEFAULT_IMAGE_SETTINGS_DIALOG)
	private readonly calibration = structuredClone(DEFAULT_IMAGE_CALIBRATION)
	private readonly mouseMountCoordinate = structuredClone(DEFAULT_IMAGE_MOUSE_POSITION)
	private readonly imageData = structuredClone(DEFAULT_IMAGE_DATA)

	protected readonly stretch: ImageStretchDialog = {
		showDialog: false,
		transformation: this.transformation.stretch,
	}

	protected readonly scnr: ImageSCNRDialog = {
		showDialog: false,
		transformation: this.transformation.scnr,
	}

	protected readonly astronomicalObject: AstronomicalObjectDialog = {
		showDialog: false,
	}

	protected readonly headers: ImageHeadersDialog = {
		showDialog: false,
		headers: [],
	}

	protected imageInfo?: ImageInfo

	private readonly saveAsMenuItem: MenuItem = {
		label: 'Save as...',
		icon: 'mdi mdi-content-save',
		command: () => {
			this.saveAs.subFrame.width ||= this.imageInfo?.width ?? 0
			this.saveAs.subFrame.height ||= this.imageInfo?.height ?? 0
			this.saveAs.showDialog = true
		},
	}

	private readonly plateSolveMenuItem: MenuItem = {
		label: 'Plate Solve',
		icon: 'mdi mdi-sigma',
		command: () => {
			this.solver.showDialog = true
		},
	}

	private readonly stretchMenuItem: MenuItem = {
		label: 'Stretch',
		icon: 'mdi mdi-chart-histogram',
		command: () => {
			this.stretch.showDialog = true
		},
	}

	private readonly autoStretchMenuItem: MenuItem = {
		label: 'Auto stretch',
		icon: 'mdi mdi-auto-fix',
		selected: true,
		command: () => {
			return this.toggleStretch()
		},
	}

	private readonly scnrMenuItem: MenuItem = {
		label: 'SCNR',
		icon: 'mdi mdi-palette',
		disabled: true,
		command: () => {
			this.scnr.showDialog = true
		},
	}

	private readonly horizontalMirrorMenuItem: MenuItem = {
		label: 'Horizontal mirror',
		icon: 'mdi mdi-flip-horizontal',
		selected: false,
		command: () => {
			this.transformation.mirrorHorizontal = !this.transformation.mirrorHorizontal
			this.horizontalMirrorMenuItem.selected = this.transformation.mirrorHorizontal
			this.savePreference()
			return this.loadImage()
		},
	}

	private readonly verticalMirrorMenuItem: MenuItem = {
		label: 'Vertical mirror',
		icon: 'mdi mdi-flip-vertical',
		selected: false,
		command: () => {
			this.transformation.mirrorVertical = !this.transformation.mirrorVertical
			this.verticalMirrorMenuItem.selected = this.transformation.mirrorVertical
			this.savePreference()
			return this.loadImage()
		},
	}

	private readonly invertMenuItem: MenuItem = {
		label: 'Invert',
		icon: 'mdi mdi-invert-colors',
		selected: false,
		command: () => {
			return this.invertImage()
		},
	}

	private readonly calibrationMenuItem: MenuItem = {
		label: 'Calibration',
		icon: 'mdi mdi-wrench',
		items: [],
	}

	private readonly statisticsMenuItem: MenuItem = {
		icon: 'mdi mdi-chart-histogram',
		label: 'Statistics',
		command: () => {
			this.statistics.showDialog = true
			return this.computeHistogram()
		},
	}

	private readonly fitsHeaderMenuItem: MenuItem = {
		icon: 'mdi mdi-list-box',
		label: 'FITS Header',
		command: () => {
			this.headers.showDialog = true
		},
	}

	private readonly pointMountHereMenuItem: MenuItem = {
		label: 'Point mount here',
		icon: 'mdi mdi-telescope',
		disabled: true,
		command: () => {
			const path = this.imagePath

			if (path) {
				void this.executeMount((mount) => {
					return this.api.pointMountHere(mount, path, this.mouseMountCoordinate)
				})
			}
		},
	}

	private readonly frameAtThisCoordinateMenuItem: MenuItem = {
		label: 'Frame at this coordinate',
		icon: 'mdi mdi-image',
		disabled: true,
		command: () => {
			const coordinate = this.mouseCoordinate.interpolator?.interpolate(this.mouseMountCoordinate.x, this.mouseMountCoordinate.y, false, false)

			if (coordinate) {
				void this.frame(coordinate)
			}
		},
	}

	private readonly crosshairMenuItem: MenuItem = {
		label: 'Crosshair',
		icon: 'mdi mdi-bullseye',
		selected: false,
		command: () => {
			this.toggleCrosshair()
		},
	}

	private readonly annotationMenuItem: MenuItem = {
		label: 'Annotate',
		icon: 'mdi mdi-marker',
		disabled: true,
		checkable: true,
		toggled: false,
		command: () => {
			this.annotation.showDialog = true
		},
		check: (event) => {
			this.annotation.visible = !!event.checked
		},
	}

	private readonly detectStarsMenuItem: MenuItem = {
		label: 'Detect stars',
		icon: 'mdi mdi-creation',
		disabled: false,
		checkable: false,
		selected: false,
		command: () => {
			this.starDetector.showDialog = true
		},
		check: (event) => {
			this.starDetector.visible = !!event.checked
		},
	}

	private readonly roiMenuItem: MenuItem = {
		label: 'ROI',
		icon: 'mdi mdi-select',
		selected: false,
		command: () => {
			this.imageROI.show = !this.imageROI.show
			this.roiMenuItem.selected = this.imageROI.show
		},
	}

	private readonly fovMenuItem: MenuItem = {
		label: 'Field of View',
		icon: 'mdi mdi-camera-metering-spot',
		command: () => {
			this.fov.showDialog = !this.fov.showDialog

			if (this.fov.showDialog) {
				this.fov.fovs.forEach((e) => this.computeFOV(e))
			}
		},
	}

	private readonly overlayMenuItem: MenuItem = {
		label: 'Overlay',
		icon: 'mdi mdi-layers',
		items: [this.crosshairMenuItem, this.annotationMenuItem, this.detectStarsMenuItem, this.roiMenuItem, this.fovMenuItem],
	}

	protected readonly contextMenuModel = [
		this.saveAsMenuItem,
		SEPARATOR_MENU_ITEM,
		this.plateSolveMenuItem,
		SEPARATOR_MENU_ITEM,
		this.stretchMenuItem,
		this.autoStretchMenuItem,
		this.scnrMenuItem,
		this.horizontalMirrorMenuItem,
		this.verticalMirrorMenuItem,
		this.invertMenuItem,
		this.calibrationMenuItem,
		SEPARATOR_MENU_ITEM,
		this.overlayMenuItem,
		this.statisticsMenuItem,
		this.fitsHeaderMenuItem,
		SEPARATOR_MENU_ITEM,
		this.pointMountHereMenuItem,
		this.frameAtThisCoordinateMenuItem,
	]

	private readonly liveStackingMenuItem: MenuItem = {
		label: 'RAW',
		icon: 'mdi mdi-image-multiple',
		tooltip: 'Live Stacking',
		visible: false,
		splitButtonMenu: [
			{
				label: 'RAW',
				command: () => {
					return this.changeLiveStackingMode('RAW')
				},
			},
			{
				label: 'STACKED',
				command: () => {
					return this.changeLiveStackingMode('STACKED')
				},
			},
		],
	}

	@ViewChild('image')
	private readonly image!: ElementRef<HTMLImageElement>

	@ViewChild('roi')
	private readonly roi!: ElementRef<HTMLDivElement>

	@ViewChild('menu')
	private readonly menu!: ContextMenu

	@ViewChild('deviceMenu')
	private readonly deviceMenu!: DeviceListMenuComponent

	@ViewChild('histogram')
	private readonly histogram?: HistogramComponent

	@ViewChild('detectedStarCanvas')
	private readonly detectedStarCanvas!: ElementRef<HTMLCanvasElement>

	@ViewChild('moveable')
	private readonly moveable!: NgxLegacyMoveableComponent

	get isMouseCoordinateVisible() {
		return this.mouseCoordinate.show && !!this.mouseCoordinate.interpolator && !this.transformation.mirrorHorizontal && !this.transformation.mirrorVertical
	}

	get imagePath() {
		if (this.liveStacking.mode === 'NONE' || this.liveStacking.mode === 'RAW' || !this.liveStacking.path) {
			return this.imageData.path
		} else {
			return this.liveStacking.path
		}
	}

	get canPlateSolve() {
		return (this.solver.request.type !== 'SIRIL' && this.solver.request.type !== 'PIXINSIGHT') || (this.solver.request.focalLength > 0 && this.solver.request.pixelSize > 0)
	}

	get canAddFOV() {
		const fov = this.fov.selected
		return fov.aperture && fov.focalLength && fov.cameraSize.width && fov.cameraSize.height && fov.pixelSize.width && fov.pixelSize.height && fov.bin
	}

	constructor(
		private readonly app: AppComponent,
		private readonly route: ActivatedRoute,
		private readonly api: ApiService,
		private readonly electronService: ElectronService,
		private readonly browserWindowService: BrowserWindowService,
		private readonly preferenceService: PreferenceService,
		private readonly angularService: AngularService,
		ngZone: NgZone,
	) {
		app.title = 'Image'

		app.topMenu.push(this.liveStackingMenuItem)

		app.topMenu.push({
			icon: 'mdi mdi-fullscreen',
			label: 'Fullscreen',
			command: () => this.enterFullscreen(),
		})

		app.topMenu.push({
			icon: 'mdi mdi-minus',
			label: 'Zoom Out',
			command: () => {
				this.zoomOut()
			},
		})

		app.topMenu.push({
			icon: 'mdi mdi-plus',
			label: 'Zoom In',
			command: () => {
				this.zoomIn()
			},
		})

		app.topMenu.push({
			icon: 'mdi mdi-numeric-0',
			label: 'Reset Zoom',
			command: () => {
				this.resetZoom(false)
			},
		})

		app.topMenu.push({
			icon: 'mdi mdi-fit-to-screen',
			label: 'Fit to Screen',
			command: () => {
				this.resetZoom(true)
			},
		})

		app.topMenu.push({
			icon: 'mdi mdi-cog',
			label: 'Settings',
			command: () => {
				this.settings.showDialog = true
			},
		})

		electronService.on('CAMERA.CAPTURE_ELAPSED', async (event) => {
			if (event.state === 'EXPOSURE_FINISHED' && event.camera.id === this.imageData.camera?.id) {
				await ngZone.run(async () => {
					if (this.liveStacking.mode === 'NONE') {
						if (event.liveStackedPath) {
							await this.changeLiveStackingMode('STACKED')
						}
					} else if (!event.liveStackedPath) {
						await this.changeLiveStackingMode('NONE')
					}

					await this.closeImage()

					this.imageData.path = event.savedPath
					this.imageData.exposureCount = event.exposureCount
					this.liveStacking.path = event.liveStackedPath

					this.clearOverlay()

					await this.loadImage()
				})
			}
		})

		electronService.on('DATA.CHANGED', (event: OpenImage) => {
			return ngZone.run(() => {
				return this.loadImageFromOpenImage(event)
			})
		})

		electronService.on('CALIBRATION.CHANGED', async () => {
			return ngZone.run(() => {
				return this.loadCalibrationGroups()
			})
		})

		hotkeys('ctrl+a', (event) => {
			event.preventDefault()
			void this.toggleStretch()
		})
		hotkeys('ctrl+i', (event) => {
			event.preventDefault()
			void this.invertImage()
		})
		hotkeys('ctrl+x', (event) => {
			event.preventDefault()
			this.toggleCrosshair()
		})
		hotkeys('ctrl+-', (event) => {
			event.preventDefault()
			this.zoomOut()
		})
		hotkeys('ctrl+=', (event) => {
			event.preventDefault()
			this.zoomIn()
		})
		hotkeys('ctrl+0', (event) => {
			event.preventDefault()
			this.resetZoom()
		})
		hotkeys('ctrl+alt+0', (event) => {
			event.preventDefault()
			this.resetZoom(true)
		})
		hotkeys('f12', (event) => {
			event.preventDefault()

			if (this.app.showTopBar) {
				void this.enterFullscreen()
			} else {
				void this.exitFullscreen()
			}
		})
		hotkeys('escape', (event) => {
			if (!this.app.showTopBar) {
				event.preventDefault()
				void this.exitFullscreen()
			}
		})

		this.loadPreference()
	}

	async ngAfterViewInit() {
		await this.loadCalibrationGroups()

		this.route.queryParams.subscribe((e) => {
			const data = JSON.parse(decodeURIComponent(e['data'] as string)) as OpenImage
			return this.loadImageFromOpenImage(data)
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.zoom.panZoom?.dispose()
		void this.closeImage()
	}

	private markCalibrationGroupItem(name: string | undefined = this.transformation.calibrationGroup) {
		const items = this.calibrationMenuItem.items
		const calibrationViaCamera = this.calibration.source === 'CAMERA'

		if (items) {
			items[2].disabled = !this.imageInfo?.camera?.id
			items[2].selected = calibrationViaCamera

			for (let i = 3; i < items.length; i++) {
				const item = items[i]
				item.selected = !calibrationViaCamera && item.data === name
			}
		}
	}

	private async loadCalibrationGroups() {
		const groups = await this.api.calibrationGroups()
		const found = !!groups.find((e) => this.transformation.calibrationGroup === e)
		let reloadImage = false

		if (!found) {
			reloadImage = !!this.transformation.calibrationGroup
			this.transformation.calibrationGroup = undefined
			this.savePreference()
			this.calibration.source = 'CAMERA'
		}

		const makeItem = (name?: string) => {
			const label = name ?? 'None'
			const icon = name ? 'mdi mdi-wrench' : 'mdi mdi-close'

			return {
				label,
				icon,
				selected: this.calibration.source === 'MENU' && this.transformation.calibrationGroup === name,
				data: name,
				command: () => {
					this.calibration.source = 'MENU'
					this.transformation.calibrationGroup = name
					this.savePreference()
					this.markCalibrationGroupItem(name)
					void this.loadImage()
				},
			} as MenuItem
		}

		const menu: MenuItem[] = []

		menu.push({
			label: 'Open',
			icon: 'mdi mdi-wrench',
			command: () => {
				return this.browserWindowService.openCalibration()
			},
		})

		menu.push(SEPARATOR_MENU_ITEM)

		menu.push({
			label: 'Camera',
			icon: 'mdi mdi-camera-iris',
			selected: this.calibration.source === 'CAMERA',
			disabled: !this.imageInfo?.camera?.id,
			data: 0,
			command: () => {
				if (this.imageInfo?.camera?.id) {
					this.calibration.source = this.calibration.source === 'CAMERA' ? 'MENU' : 'CAMERA'
					this.markCalibrationGroupItem()
					void this.loadImage()
				}
			},
		})

		menu.push(makeItem())

		for (const group of groups) {
			menu.push(makeItem(group))
		}

		this.calibrationMenuItem.items = menu
		this.menu.model = this.contextMenuModel
		this.menu.cd.markForCheck()

		if (reloadImage) {
			await this.loadImage()
		}
	}

	private async closeImage() {
		if (this.imageData.path) {
			await this.api.closeImage(this.imageData.path)
		}
		if (this.liveStacking.path) {
			await this.api.closeImage(this.liveStacking.path)
		}
	}

	private async changeLiveStackingMode(mode: LiveStackingMode) {
		this.liveStacking.mode = mode

		if (this.liveStacking.mode !== 'NONE') {
			this.disableCalibration(true)
		}

		this.liveStackingMenuItem.visible = this.liveStacking.mode !== 'NONE'
		this.liveStackingMenuItem.label = mode

		await this.closeImage()
		await this.loadImage()
	}

	protected roiDrag(event: OnDrag) {
		const { target, transform } = event
		target.style.transform = transform

		const rect = this.moveable.getRect()
		this.imageROI.area.x = Math.trunc(rect.left)
		this.imageROI.area.y = Math.trunc(rect.top)
	}

	protected roiResize(event: OnResize) {
		const { target, width, height, transform } = event
		target.style.transform = transform

		const rect = this.moveable.getRect()

		target.style.width = `${width}px`
		this.imageROI.area.x = Math.trunc(rect.left)
		this.imageROI.area.width = Math.trunc(width)

		target.style.height = `${height}px`
		this.imageROI.area.y = Math.trunc(rect.top)
		this.imageROI.area.height = Math.trunc(height)
	}

	protected roiRotate(event: OnRotate) {
		const { target, transform } = event
		target.style.transform = transform
	}

	protected roiForCamera() {
		return this.executeCamera((camera) => {
			const x = Math.max(0, Math.min(camera.x + this.imageROI.area.x, camera.maxX))
			const y = Math.max(0, Math.min(camera.y + this.imageROI.area.y, camera.maxY))
			const width = Math.max(0, Math.min(camera.binX * this.imageROI.area.width, camera.maxWidth))
			const height = Math.max(0, Math.min(camera.binY * this.imageROI.area.height, camera.maxHeight))

			return this.electronService.send('ROI.SELECTED', { camera, x, y, width, height })
		}, false)
	}

	private async loadImageFromOpenImage(data: OpenImage) {
		if (this.imageInfo) {
			await this.closeImage()
		}

		if (data.camera) this.imageData.camera = data.camera
		if (data.path) this.imageData.path = data.path
		this.imageData.source = data.source
		if (data.title) this.imageData.title = data.title
		if (data.capture) this.imageData.capture = data.capture

		// Not clicked on menu item.
		if (this.calibration.source === 'CAMERA' && this.transformation.calibrationGroup !== data.capture?.calibrationGroup) {
			this.transformation.calibrationGroup = data.capture?.calibrationGroup
			this.savePreference()
			this.markCalibrationGroupItem()
		}

		if (data.source === 'FRAMING') {
			this.disableAutoStretch()
			await this.resetStretch(false)
		} else if (data.source === 'FLAT_WIZARD') {
			this.disableCalibration(false)
		}

		if (data.path) {
			this.clearOverlay()
			await this.loadImage()
		}
	}

	private clearOverlay() {
		this.annotation.data = []
		this.annotation.filtered = []
		this.annotation.visible = false
		this.annotationMenuItem.checkable = false

		this.starDetector.stars = []
		this.starDetector.visible = false
		this.detectStarsMenuItem.checkable = false

		Object.assign(this.solver.solved, DEFAULT_IMAGE_SOLVED)

		this.histogram?.update([])
	}

	protected async computeHistogram() {
		const path = this.imagePath

		if (path) {
			const data = await this.api.imageHistogram(path, this.statistics.bitOption.bitLength)
			this.histogram?.update(data)
		}
	}

	protected async detectStars() {
		const path = this.imagePath

		if (path) {
			const request: StarDetectionRequest = {
				...this.starDetector.request,
				...this.preferenceService.settings.get().starDetector[this.starDetector.request.type],
			}

			try {
				this.starDetector.running = true
				this.starDetector.stars = await this.api.detectStars(path, request)
			} finally {
				this.starDetector.running = false
			}

			let hfd = 0
			let stdDev = 0
			let snr = 0
			let fluxMin = 0
			let fluxMax = 0

			const starCount = this.starDetector.stars.length

			if (starCount) {
				fluxMax = this.starDetector.stars[0].flux

				for (const star of this.starDetector.stars) {
					hfd += star.hfd
					snr += star.snr
					fluxMax = Math.min(fluxMax, star.flux)
					fluxMin = Math.max(fluxMin, star.flux)
				}

				hfd = hfd / starCount
				snr = snr / starCount

				let squared = 0

				for (const star of this.starDetector.stars) {
					squared += Math.pow(star.hfd - hfd, 2)
				}

				stdDev = Math.sqrt(squared / starCount)
			}

			this.starDetector.computed.hfd = hfd
			this.starDetector.computed.stdDev = stdDev
			this.starDetector.computed.snr = snr
			this.starDetector.computed.fluxMax = fluxMin
			this.starDetector.computed.fluxMin = fluxMax

			this.savePreference()

			this.starDetector.visible = this.starDetector.stars.length > 0
			this.detectStarsMenuItem.checkable = this.starDetector.visible
			this.detectStarsMenuItem.checked = this.starDetector.visible
		}
	}

	protected drawDetectedStar(star: DetectedStar) {
		Object.assign(this.starDetector.selected, star)

		const canvas = this.detectedStarCanvas.nativeElement
		const ctx = canvas.getContext('2d')
		ctx?.drawImage(this.image.nativeElement, star.x - 8, star.y - 8, 16, 16, 0, 0, canvas.width, canvas.height)
	}

	private async loadImage() {
		const path = this.imagePath

		if (path) {
			await this.loadImageFromPath(path)
		}

		this.updateSubTitle()
	}

	private updateSubTitle() {
		let text = ''

		if (this.imageData.title) {
			text = this.imageData.title
		} else if (this.imageData.camera) {
			text = this.imageData.camera.name
		} else if (this.imageData.path) {
			text = basename(this.imageData.path)
		} else {
			return
		}

		if (this.imageData.exposureCount) {
			text += ` · ${this.imageData.exposureCount}`
		}

		if (this.imageData.filter) {
			text += ` · ${this.imageData.filter}`
		}

		this.app.subTitle = text
	}

	private async loadImageFromPath(path: string) {
		const image = this.image.nativeElement

		const transformation = structuredClone(this.transformation)
		if (this.calibration.source === 'CAMERA' && this.liveStacking.mode !== 'NONE') transformation.calibrationGroup = this.imageData.capture?.calibrationGroup
		const { info, blob } = await this.api.openImage(path, transformation, this.imageData.camera)

		if (!blob || !info) return

		this.imageInfo = info
		this.scnrMenuItem.disabled = info.mono

		if (info.rightAscension) this.solver.request.centerRA = info.rightAscension
		if (info.declination) this.solver.request.centerDEC = info.declination
		this.solver.request.blind = !this.solver.request.centerRA || !this.solver.request.centerDEC

		if (this.stretch.transformation.auto) {
			Object.assign(this.stretch.transformation, info.stretch)
		}

		this.updateImageSolved(info.solved)

		this.headers.headers = info.headers
		this.statistics.statistics = info.statistics

		this.retrieveInfoFromImageHeaders(info.headers)

		image.src = URL.createObjectURL(blob)

		if (!info.camera?.id) {
			this.calibration.source = 'MENU'
			this.markCalibrationGroupItem()
		} else if (this.calibrationMenuItem.items) {
			this.calibrationMenuItem.items[2].disabled = false
		}

		return this.retrieveCoordinateInterpolation()
	}

	private retrieveInfoFromImageHeaders(headers: ImageHeaderItem[]) {
		this.imageData.filter = undefined

		for (const item of headers) {
			if (item.name === 'FOCALLEN') {
				this.solver.request.focalLength = parseFloat(item.value)
			} else if (item.name === 'XPIXSZ') {
				this.solver.request.pixelSize = parseFloat(item.value)
			} else if (item.name === 'FILTER') {
				this.imageData.filter = item.value
			}
		}
	}

	protected imageClicked(event: MouseEvent, contextMenu: boolean) {
		this.mouseMountCoordinate.x = event.offsetX
		this.mouseMountCoordinate.y = event.offsetY

		if (contextMenu) {
			this.menu.show(event)
		}
	}

	protected imageMouseMoved(event: MouseEvent) {
		this.imageMouseMovedWithCoordinates(event.offsetX, event.offsetY)
	}

	private imageMouseMovedWithCoordinates(x: number, y: number) {
		if (!this.menu.visible() && this.mouseCoordinate.interpolator) {
			Object.assign(this.mouseCoordinate, this.mouseCoordinate.interpolator.interpolateAsText(x, y, true, true, false))
			this.mouseCoordinate.x = x
			this.mouseCoordinate.y = y
		}
	}

	protected pathChangedForSaveAs() {
		if (this.saveAs.path) {
			const extension = extname(this.saveAs.path).toLowerCase()
			this.saveAs.format = imageFormatFromExtension(extension)
			this.saveAs.bitpix = this.imageInfo?.bitpix ?? 'BYTE'

			this.preference.savePath = dirname(this.saveAs.path)
			this.savePreference()
		}
	}

	protected useROIAreaForSaveAs() {
		Object.assign(this.saveAs.subFrame, this.imageROI.area)
	}

	protected useImageAreaForSaveAs() {
		this.saveAs.subFrame.x = 0
		this.saveAs.subFrame.y = 0
		this.saveAs.subFrame.width = this.imageInfo?.width ?? 0
		this.saveAs.subFrame.height = this.imageInfo?.height ?? 0
	}

	protected async saveImageAs() {
		const path = this.imagePath

		if (path) {
			await this.api.saveImageAs(path, this.saveAs, this.imageData.camera)
			this.saveAs.showDialog = false
		}
	}

	protected async annotateImage() {
		const path = this.imagePath

		if (path) {
			try {
				this.annotation.running = true
				this.annotation.data = await this.api.annotationsOfImage(path, this.annotation.request)
				this.annotation.filtered = this.annotation.data
				this.annotation.visible = this.annotation.data.length > 0
				this.annotationMenuItem.checkable = this.annotation.visible
				this.annotationMenuItem.checked = this.annotation.visible
				// this.annotation.showDialog = false
			} finally {
				this.annotation.running = false
			}
		}
	}

	protected showAnnotationInfo(annotation: ImageAnnotation) {
		this.astronomicalObject.info = annotation.star ?? annotation.dso ?? annotation.minorPlanet
		this.astronomicalObject.showDialog = true
	}

	protected searchAnnotations() {
		const search = this.annotation.search.toUpperCase()

		if (search) {
			this.annotation.filtered = this.annotation.data.filter((e) => filterAstronomicalObject((e.star ?? e.dso ?? e.minorPlanet)!, search))
		} else {
			this.annotation.filtered = this.annotation.data
		}
	}

	protected annotationSelected(selected?: ImageAnnotation) {
		this.annotation.selected = selected

		if (selected && this.zoom.panZoom) {
			const { scale } = this.zoom.panZoom.getTransform()
			const { clientWidth: pw, clientHeight: ph } = this.image.nativeElement.parentElement!.parentElement!
			this.zoom.panZoom.smoothMoveTo(pw / 2 - selected.x * scale, (ph + 42) / 2 - selected.y * scale)
		}
	}

	private disableAutoStretch() {
		this.stretch.transformation.auto = false
		this.savePreference()
		this.autoStretchMenuItem.selected = false
	}

	private disableCalibration(canEnable: boolean = true) {
		this.transformation.calibrationGroup = undefined
		this.savePreference()
		this.markCalibrationGroupItem(undefined)
		this.calibrationMenuItem.disabled = !canEnable
	}

	protected autoStretch() {
		this.stretch.transformation.auto = true
		this.savePreference()
		this.autoStretchMenuItem.selected = true

		return this.loadImage()
	}

	protected async resetStretch(load: boolean = true) {
		this.stretch.transformation.shadow = 0
		this.stretch.transformation.highlight = 65536
		this.stretch.transformation.midtone = 32768
		this.savePreference()

		if (load) {
			await this.stretchImage()
		}
	}

	private async toggleStretch() {
		this.stretch.transformation.auto = !this.stretch.transformation.auto
		this.savePreference()
		this.autoStretchMenuItem.selected = this.stretch.transformation.auto

		if (this.stretch.transformation.auto) {
			return this.loadImage()
		} else {
			return this.resetStretch()
		}
	}

	protected stretchImage() {
		this.disableAutoStretch()
		return this.loadImage()
	}

	private invertImage() {
		this.transformation.invert = !this.transformation.invert
		this.invertMenuItem.selected = this.transformation.invert
		this.savePreference()
		return this.loadImage()
	}

	protected scnrImage() {
		return this.loadImage()
	}

	private toggleCrosshair() {
		this.preference.crossHair = !this.preference.crossHair
		this.savePreference()
		this.crosshairMenuItem.selected = this.preference.crossHair
	}

	private zoomIn() {
		if (!this.zoom.panZoom) return
		const { scale } = this.zoom.panZoom.getTransform()
		this.zoom.panZoom.smoothZoomAbs(window.innerWidth / 2, window.innerHeight / 2, scale * 1.1)
	}

	private zoomOut() {
		if (!this.zoom.panZoom) return
		const { scale } = this.zoom.panZoom.getTransform()
		this.zoom.panZoom.smoothZoomAbs(window.innerWidth / 2, window.innerHeight / 2, scale * 0.9)
	}

	private center() {
		const { width, height } = this.image.nativeElement.getBoundingClientRect()
		this.zoom.panZoom?.moveTo(window.innerWidth / 2 - width / 2, (window.innerHeight - 42) / 2 - height / 2)
	}

	private resetZoom(fitToScreen: boolean = false, center: boolean = true) {
		if (fitToScreen) {
			const { width, height } = this.image.nativeElement
			const factor = Math.min(window.innerWidth, window.innerHeight - 42) / Math.min(width, height)
			this.zoom.panZoom?.smoothZoomAbs(window.innerWidth / 2, window.innerHeight / 2, factor)
		} else {
			this.zoom.panZoom?.smoothZoomAbs(window.innerWidth / 2, window.innerHeight / 2, 1.0)
		}

		if (center) {
			this.center()
		}
	}

	private async enterFullscreen() {
		this.app.showTopBar = !(await this.electronService.fullscreenWindow(true))
	}

	private async exitFullscreen() {
		this.app.showTopBar = !(await this.electronService.fullscreenWindow(false))
	}

	private async retrieveCoordinateInterpolation() {
		const path = this.imagePath

		if (path) {
			const coordinate = await this.api.coordinateInterpolation(path)

			if (coordinate && this.imageInfo) {
				const { ma, md, x0, y0, x1, y1, delta } = coordinate
				const x = Math.max(0, Math.min(this.mouseCoordinate.x, this.imageInfo.width))
				const y = Math.max(0, Math.min(this.mouseCoordinate.y, this.imageInfo.height))
				this.mouseCoordinate.interpolator = new CoordinateInterpolator(ma, md, x0, y0, x1, y1, delta)
				this.mouseCoordinate.show = true
				this.imageMouseMovedWithCoordinates(x, y)
				return
			}
		}

		this.mouseCoordinate.interpolator = undefined
		this.mouseCoordinate.show = false
	}

	protected async solverStart() {
		const path = this.imagePath

		if (path) {
			this.solver.running = true

			try {
				const request: PlateSolverRequest = {
					...this.solver.request,
					...this.preferenceService.settings.get().plateSolver[this.solver.request.type],
				}

				const solved = await this.api.solverStart(request, path)

				this.updateImageSolved(solved)
			} catch {
				this.updateImageSolved(this.imageInfo?.solved)
			} finally {
				this.solver.running = false

				if (this.solver.solved.solved) {
					await this.retrieveCoordinateInterpolation()
				}
			}
		}
	}

	protected solverStop() {
		return this.api.solverStop()
	}

	private updateImageSolved(solved?: ImageSolved) {
		Object.assign(this.solver.solved, solved ?? DEFAULT_IMAGE_SOLVED)
		this.annotationMenuItem.disabled = !this.solver.solved.solved
		this.fovMenuItem.disabled = !this.solver.solved.solved
		this.pointMountHereMenuItem.disabled = !this.solver.solved.solved
		this.frameAtThisCoordinateMenuItem.disabled = !this.solver.solved.solved

		if (solved) this.fov.fovs.forEach((e) => this.computeFOV(e))
		else this.fov.fovs.forEach((e) => (e.computed = undefined))
	}

	protected mountSync(coordinate: EquatorialCoordinateJ2000) {
		return this.executeMount((mount) => {
			return this.api.mountSync(mount, coordinate.rightAscensionJ2000, coordinate.declinationJ2000, true)
		})
	}

	protected mountGoTo(coordinate: EquatorialCoordinateJ2000) {
		return this.executeMount((mount) => {
			return this.api.mountGoTo(mount, coordinate.rightAscensionJ2000, coordinate.declinationJ2000, true)
		})
	}

	protected mountSlew(coordinate: EquatorialCoordinateJ2000) {
		return this.executeMount((mount) => {
			return this.api.mountSlew(mount, coordinate.rightAscensionJ2000, coordinate.declinationJ2000, true)
		})
	}

	protected async frame(coordinate: EquatorialCoordinateJ2000) {
		if (this.solver.solved.solved) {
			await this.browserWindowService.openFraming({
				rightAscension: coordinate.rightAscensionJ2000,
				declination: coordinate.declinationJ2000,
				fov: this.solver.solved.width / 60,
				rotation: this.solver.solved.orientation,
			})
		}
	}

	protected imageLoaded() {
		const image = this.image.nativeElement
		const imageWrapper = image.parentElement

		URL.revokeObjectURL(image.src)

		if (!this.zoom.panZoom && imageWrapper) {
			const panZoom = createPanZoom(imageWrapper, {
				minZoom: 0.1,
				maxZoom: 500.0,
				autocenter: true,
				zoomDoubleClickSpeed: 1,
				zoomSpeed: 1,
				filterKey: () => {
					return true
				},
				beforeWheel: (e) => {
					return e.target !== this.image.nativeElement && e.target !== this.roi.nativeElement && (e.target as HTMLElement).tagName !== 'circle'
				},
				beforeMouseDown: (e) => {
					return e.target !== this.image.nativeElement && (e.target as HTMLElement).tagName !== 'circle'
				},
			})

			panZoom.on('transform', () => {
				const { scale } = panZoom.getTransform()
				this.zoom.scale = scale
			})

			this.zoom.panZoom = panZoom
		}
	}

	protected async showFOVCameraDialog() {
		if (!this.fov.cameras.length) {
			this.fov.cameras = await this.api.fovCameras()
		}

		this.fov.camera = undefined
		this.fov.showCameraDialog = true
	}

	protected async showFOVTelescopeDialog() {
		if (!this.fov.telescopes.length) {
			this.fov.telescopes = await this.api.fovTelescopes()
		}

		this.fov.telescope = undefined
		this.fov.showTelescopeDialog = true
	}

	protected chooseCamera() {
		if (this.fov.camera) {
			this.fov.selected.cameraSize.width = this.fov.camera.width
			this.fov.selected.cameraSize.height = this.fov.camera.height
			this.fov.selected.pixelSize.width = this.fov.camera.pixelSize
			this.fov.selected.pixelSize.height = this.fov.camera.pixelSize
			this.fov.camera = undefined
			this.fov.showCameraDialog = false
		}
	}

	protected chooseTelescope() {
		if (this.fov.telescope) {
			this.fov.selected.aperture = this.fov.telescope.aperture
			this.fov.selected.focalLength = this.fov.telescope.focalLength
			this.fov.telescope = undefined
			this.fov.showTelescopeDialog = false
		}
	}

	protected addFOV() {
		if (this.computeFOV(this.fov.selected)) {
			this.fov.fovs.push(structuredClone(this.fov.selected))
			this.savePreference()
		}
	}

	private removeSelectedFOV() {
		this.fov.selected = structuredClone(DEFAULT_FOV)
	}

	protected selectFOV(fov: FOV) {
		if (this.fov.selected === fov) {
			this.removeSelectedFOV()
		} else {
			this.fov.selected = fov
		}
	}

	protected saveFOV(compute: boolean = true) {
		// Edited.
		if (this.fov.fovs.includes(this.fov.selected) && (!compute || this.computeFOV(this.fov.selected))) {
			this.savePreference()
		}
	}

	private computeFOV(fov: FOV) {
		if (this.imageInfo && this.solver.solved.scale > 0) {
			const focalLength = fov.focalLength * (fov.barlowReducer || 1)

			const resolution = {
				width: (fov.pixelSize.width / focalLength) * 206.265, // arcsec/pixel
				height: (fov.pixelSize.height / focalLength) * 206.265, // arcsec/pixel
			}

			const svg = {
				x: this.imageInfo.width / 2,
				y: this.imageInfo.height / 2,
				width: fov.cameraSize.width * (resolution.width / this.solver.solved.scale),
				height: fov.cameraSize.height * (resolution.height / this.solver.solved.scale),
			}

			svg.x += (this.imageInfo.width - svg.width) / 2
			svg.y += (this.imageInfo.height - svg.height) / 2

			fov.computed = {
				cameraResolution: {
					width: resolution.width * fov.bin,
					height: resolution.height * fov.bin,
				},
				focalRatio: focalLength / fov.aperture,
				fieldSize: {
					width: (resolution.width * fov.cameraSize.width) / 3600, // deg
					height: (resolution.height * fov.cameraSize.height) / 3600, // deg
				},
				svg,
			}

			return true
		} else {
			return false
		}
	}

	protected deleteFOV(fov: FOV) {
		const index = this.fov.fovs.indexOf(fov)

		if (index >= 0) {
			this.fov.fovs.splice(index, 1)
			this.savePreference()

			if (this.fov.selected === this.fov.fovs[index]) {
				this.removeSelectedFOV()
			}
		}
	}

	private loadPreference() {
		Object.assign(this.preference, this.preferenceService.image.get())
		this.solver.request = this.preference.solver
		this.starDetector.request = this.preference.starDetector
		this.settings.preference = this.preference
		this.transformation = this.preference.transformation
		this.saveAs.transformation = this.transformation
		this.stretch.transformation = this.transformation.stretch
		this.scnr.transformation = this.transformation.scnr
		this.annotation.request = this.preference.annotation
		this.fov.fovs = this.preference.fovs

		this.autoStretchMenuItem.selected = this.transformation.stretch.auto
		this.invertMenuItem.selected = this.transformation.invert
		this.horizontalMirrorMenuItem.selected = this.transformation.mirrorHorizontal
		this.verticalMirrorMenuItem.selected = this.transformation.mirrorVertical
		this.crosshairMenuItem.selected = this.preference.crossHair
	}

	protected savePreference() {
		this.preferenceService.image.set(this.preference)
	}

	private async executeCamera(action: (camera: Camera) => void | Promise<void>, showConfirmation: boolean = true) {
		if (showConfirmation && (await this.angularService.confirm('Are you sure that you want to proceed?'))) {
			return false
		}

		const cameras = await this.api.cameras()

		if (cameras.length === 1) {
			await action(cameras[0])
			return true
		} else {
			const camera = await this.deviceMenu.show(cameras, undefined, 'CAMERA')

			if (camera && camera !== 'NONE' && camera.connected) {
				await action(camera)
				return true
			}
		}

		return false
	}

	private async executeMount(action: (mount: Mount) => void | Promise<void>, showConfirmation: boolean = true) {
		if (showConfirmation && (await this.angularService.confirm('Are you sure that you want to proceed?'))) {
			return false
		}

		const mounts = await this.api.mounts()

		if (mounts.length === 1) {
			await action(mounts[0])
			return true
		} else {
			const mount = await this.deviceMenu.show(mounts, undefined, 'MOUNT')

			if (mount && mount !== 'NONE' && mount.connected) {
				await action(mount)
				return true
			}
		}

		return false
	}
}
