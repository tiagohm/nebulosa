import { AfterViewInit, Component, ElementRef, HostListener, NgZone, OnDestroy, ViewChild, computed, model } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { Interactable } from '@interactjs/types/index'
import hotkeys from 'hotkeys-js'
import interact from 'interactjs'
import createPanZoom, { PanZoom } from 'panzoom'
import { basename, dirname, extname } from 'path'
import { ContextMenu } from 'primeng/contextmenu'
import { DeviceListMenuComponent } from '../../shared/components/device-list-menu/device-list-menu.component'
import { HistogramComponent } from '../../shared/components/histogram/histogram.component'
import { MenuItem } from '../../shared/components/menu-item/menu-item.component'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PrimeService } from '../../shared/services/prime.service'
import { Angle, EquatorialCoordinateJ2000 } from '../../shared/types/atlas.types'
import { Camera } from '../../shared/types/camera.types'
import {
	AnnotationInfoDialog,
	DEFAULT_FOV,
	DetectedStar,
	EMPTY_IMAGE_SOLVED,
	FITSHeaderItem,
	FOV,
	IMAGE_STATISTICS_BIT_OPTIONS,
	ImageAnnotation,
	ImageAnnotationDialog,
	ImageChannel,
	ImageData,
	ImageFITSHeadersDialog,
	ImageFOVDialog,
	ImageInfo,
	ImageROI,
	ImageSCNRDialog,
	ImageSaveDialog,
	ImageSolved,
	ImageSolverDialog,
	ImageStatisticsBitOption,
	ImageStretchDialog,
	ImageTransformation,
	LiveStackingMode,
	StarDetectionDialog,
} from '../../shared/types/image.types'
import { Mount } from '../../shared/types/mount.types'
import { CoordinateInterpolator, InterpolatedCoordinate } from '../../shared/utils/coordinate-interpolation'
import { AppComponent } from '../app.component'

@Component({
	selector: 'app-image',
	templateUrl: './image.component.html',
	styleUrls: ['./image.component.scss'],
})
export class ImageComponent implements AfterViewInit, OnDestroy {
	@ViewChild('image')
	private readonly image!: ElementRef<HTMLImageElement>

	@ViewChild('roi')
	private readonly roi!: ElementRef<HTMLDivElement>

	@ViewChild('menu')
	private readonly menu!: ContextMenu

	@ViewChild('deviceMenu')
	private readonly deviceMenu!: DeviceListMenuComponent

	@ViewChild('histogram')
	private readonly histogram!: HistogramComponent

	@ViewChild('detectedStarCanvas')
	private readonly detectedStarCanvas!: ElementRef<HTMLCanvasElement>

	imageInfo?: ImageInfo
	private imageURL!: string
	imageData: ImageData = {}
	liveStackingMode: LiveStackingMode = 'NONE'

	readonly scnrChannels: { name: string; value?: ImageChannel }[] = [
		{ name: 'None', value: undefined },
		{ name: 'Red', value: 'RED' },
		{ name: 'Green', value: 'GREEN' },
		{ name: 'Blue', value: 'BLUE' },
	]
	readonly scnr: ImageSCNRDialog = {
		showDialog: false,
		amount: 0.5,
		method: 'AVERAGE_NEUTRAL',
	}

	readonly stretch: ImageStretchDialog = {
		showDialog: false,
		auto: true,
		shadow: 0,
		highlight: 1,
		midtone: 0.5,
	}

	readonly stretchShadow = model<number>(0)
	readonly stretchHighlight = model<number>(65536)
	readonly stretchMidtone = model<number>(32768)
	readonly stretchShadowAndHighlight = computed(() => [this.stretchShadow(), this.stretchHighlight()])

	readonly transformation: ImageTransformation = {
		force: false,
		debayer: true,
		stretch: this.stretch,
		mirrorHorizontal: false,
		mirrorVertical: false,
		invert: false,
		scnr: this.scnr,
	}

	calibrationViaCamera = true

	readonly annotation: ImageAnnotationDialog = {
		showDialog: false,
		running: false,
		visible: false,
		useStarsAndDSOs: true,
		useMinorPlanets: false,
		minorPlanetsMagLimit: 18.0,
		useSimbad: false,
		data: [],
	}

	readonly annotationInfo: AnnotationInfoDialog = {
		showDialog: false,
	}

	readonly starDetection: StarDetectionDialog = {
		showDialog: false,
		running: false,
		type: 'ASTAP',
		minSNR: 0,
		maxStars: 0,
		visible: false,
		stars: [],
		computed: {
			hfd: 0,
			snr: 0,
			stdDev: 0,
			fluxMax: 0,
			fluxMin: 0,
		},
		selected: {
			x: 0,
			y: 0,
			snr: 0,
			hfd: 0,
			flux: 0,
		},
	}

	readonly solver: ImageSolverDialog = {
		showDialog: false,
		running: false,
		type: 'ASTAP',
		blind: true,
		centerRA: '',
		centerDEC: '',
		radius: 4,
		focalLength: 0,
		pixelSize: 0,
		solved: structuredClone(EMPTY_IMAGE_SOLVED),
	}

	crossHair = false

	readonly fitsHeaders: ImageFITSHeadersDialog = {
		showDialog: false,
		headers: [],
	}

	showStatisticsDialog = false

	readonly statisticsBitOptions: ImageStatisticsBitOption[] = IMAGE_STATISTICS_BIT_OPTIONS
	statisticsBitLength = this.statisticsBitOptions[0]

	readonly fov: ImageFOVDialog = {
		...structuredClone(DEFAULT_FOV),
		showDialog: false,
		fovs: [],
		showCameraDialog: false,
		cameras: [],
		showTelescopeDialog: false,
		telescopes: [],
	}

	get canAddFOV() {
		return this.fov.aperture && this.fov.focalLength && this.fov.cameraSize.width && this.fov.cameraSize.height && this.fov.pixelSize.width && this.fov.pixelSize.height && this.fov.bin
	}

	private panZoom?: PanZoom
	private imageMouseX = 0
	private imageMouseY = 0

	roiInteractable?: Interactable
	readonly imageROI: ImageROI = {
		x: 0,
		y: 0,
		width: 128,
		height: 128,
	}

	readonly saveAs: ImageSaveDialog = {
		showDialog: false,
		format: 'FITS',
		bitpix: 'BYTE',
		path: '',
		shouldBeTransformed: true,
		transformation: this.transformation,
	}

	private readonly saveAsMenuItem: MenuItem = {
		label: 'Save as...',
		icon: 'mdi mdi-content-save',
		command: async () => {
			const preference = this.preference.imagePreference.get()

			const path = await this.electron.saveImage({ defaultPath: preference.savePath })

			if (path) {
				const extension = extname(path).toLowerCase()
				this.saveAs.format =
					extension === '.xisf' ? 'XISF'
					: extension === '.png' ? 'PNG'
					: extension === '.jpg' ? 'JPG'
					: 'FITS'
				this.saveAs.bitpix = this.imageInfo?.bitpix || 'BYTE'
				this.saveAs.path = path
				this.saveAs.showDialog = true

				preference.savePath = dirname(path)
				this.preference.imagePreference.set(preference)
			}
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
			this.toggleStretch()
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
			this.loadImage()
		},
	}

	private readonly verticalMirrorMenuItem: MenuItem = {
		label: 'Vertical mirror',
		icon: 'mdi mdi-flip-vertical',
		selected: false,
		command: () => {
			this.transformation.mirrorVertical = !this.transformation.mirrorVertical
			this.verticalMirrorMenuItem.selected = this.transformation.mirrorVertical
			this.loadImage()
		},
	}

	private readonly invertMenuItem: MenuItem = {
		label: 'Invert',
		icon: 'mdi mdi-invert-colors',
		selected: false,
		command: () => {
			this.invertImage()
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
			this.showStatisticsDialog = true
			this.computeHistogram()
		},
	}

	private readonly fitsHeaderMenuItem: MenuItem = {
		icon: 'mdi mdi-list-box',
		label: 'FITS Header',
		command: () => {
			this.fitsHeaders.showDialog = true
		},
	}

	private readonly pointMountHereMenuItem: MenuItem = {
		label: 'Point mount here',
		icon: 'mdi mdi-telescope',
		disabled: true,
		command: () => {
			this.executeMount((mount) => {
				this.api.pointMountHere(mount, this.imagePath!, this.imageMouseX, this.imageMouseY)
			})
		},
	}

	private readonly frameAtThisCoordinateMenuItem: MenuItem = {
		label: 'Frame at this coordinate',
		icon: 'mdi mdi-image',
		disabled: true,
		command: () => {
			const coordinate = this.mouseCoordinateInterpolation?.interpolate(this.imageMouseX, this.imageMouseY, false, false)

			if (coordinate) {
				this.frame(coordinate)
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
			event.originalEvent?.stopImmediatePropagation()
			this.annotation.visible = event.checked
		},
	}

	private readonly detectStarsMenuItem: MenuItem = {
		label: 'Detect stars',
		icon: 'mdi mdi-creation',
		disabled: false,
		checkable: false,
		selected: false,
		command: () => {
			this.starDetection.showDialog = true
		},
		check: (event) => {
			this.starDetection.visible = event.checked
			event.originalEvent?.stopImmediatePropagation()
		},
	}

	private readonly roiMenuItem: MenuItem = {
		label: 'ROI',
		icon: 'mdi mdi-select',
		selected: false,
		command: () => {
			if (this.roiInteractable) {
				this.roiInteractable.unset()
				this.roiInteractable = undefined
			} else {
				this.roiInteractable = interact(this.roi.nativeElement)
					.origin({ x: 0, y: 0 })
					.resizable({
						edges: { left: true, right: true, bottom: true, top: true },
						inertia: true,
						listeners: { move: (event: any) => this.roiResizableMove(event) },
						modifiers: [
							interact.modifiers.restrictEdges({
								outer: 'parent',
							}),
							interact.modifiers.restrictSize({
								min: { width: 8, height: 8 },
							}),
						],
					})
					.draggable({
						listeners: { move: (event: any) => this.roiDraggableMove(event) },
						inertia: true,
						modifiers: [
							interact.modifiers.restrictRect({
								restriction: 'parent',
								endOnly: true,
							}),
						],
					})
			}

			this.roiMenuItem.selected = !!this.roiInteractable
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

	readonly contextMenuItems = [
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

	mouseCoordinate?: InterpolatedCoordinate<Angle> & Partial<{ x: number; y: number }>
	private mouseCoordinateInterpolation?: CoordinateInterpolator

	get isMouseCoordinateVisible() {
		return !!this.mouseCoordinate && !this.transformation.mirrorHorizontal && !this.transformation.mirrorVertical
	}

	get imagePath() {
		if (this.liveStackingMode === 'NONE' || this.liveStackingMode === 'RAW' || !this.imageData.liveStackedPath) {
			return this.imageData.path
		} else {
			return this.imageData.liveStackedPath
		}
	}

	get canPlateSolve() {
		return this.solver.type !== 'SIRIL' || (this.solver.focalLength > 0 && this.solver.pixelSize > 0)
	}

	private readonly liveStackingMenuItem: MenuItem = {
		label: 'RAW',
		icon: 'mdi mdi-image-multiple',
		tooltip: 'Live Stacking',
		visible: false,
		splitButtonMenu: [
			{
				label: 'RAW',
				command: () => this.changeLiveStackingMode('RAW'),
			},
			{
				label: 'STACKED',
				command: () => this.changeLiveStackingMode('STACKED'),
			},
		],
	}

	constructor(
		private app: AppComponent,
		private route: ActivatedRoute,
		private api: ApiService,
		private electron: ElectronService,
		private browserWindow: BrowserWindowService,
		private preference: PreferenceService,
		private prime: PrimeService,
		private ngZone: NgZone,
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
			command: () => this.zoomOut(),
		})

		app.topMenu.push({
			icon: 'mdi mdi-plus',
			label: 'Zoom In',
			command: () => this.zoomIn(),
		})

		app.topMenu.push({
			icon: 'mdi mdi-numeric-0',
			label: 'Reset Zoom',
			command: () => this.resetZoom(false),
		})

		app.topMenu.push({
			icon: 'mdi mdi-fit-to-screen',
			label: 'Fit to Screen',
			command: () => this.resetZoom(true),
		})

		this.stretchShadow.subscribe((value) => {
			this.stretch.shadow = value / 65536
		})

		this.stretchHighlight.subscribe((value) => {
			this.stretch.highlight = value / 65536
		})

		this.stretchMidtone.subscribe((value) => {
			this.stretch.midtone = value / 65536
		})

		electron.on('CAMERA.CAPTURE_ELAPSED', async (event) => {
			if (event.state === 'EXPOSURE_FINISHED' && event.camera.id === this.imageData.camera?.id) {
				ngZone.run(() => {
					if (this.liveStackingMode === 'NONE') {
						if (event.liveStackedPath) {
							this.changeLiveStackingMode('STACKED')
						}
					} else if (!event.liveStackedPath) {
						this.changeLiveStackingMode('NONE')
					}

					this.imageData.path = event.savedPath
					this.imageData.liveStackedPath = event.liveStackedPath
					this.imageData.capture = event.capture
					this.imageData.exposureCount = event.exposureCount

					this.clearOverlay()
					this.loadImage(true)
				})
			}
		})

		electron.on('DATA.CHANGED', async (event: ImageData) => {
			ngZone.run(() => {
				this.loadImageFromData(event)
			})
		})

		electron.on('CALIBRATION.CHANGED', () => {
			ngZone.run(() => {
				this.loadCalibrationGroups()
			})
		})

		hotkeys('ctrl+a', (event) => {
			event.preventDefault()
			this.toggleStretch()
		})
		hotkeys('ctrl+i', (event) => {
			event.preventDefault()
			this.invertImage()
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
			if (this.app.showTopBar) {
				event.preventDefault()
				this.enterFullscreen()
			}
		})
		hotkeys('escape', (event) => {
			if (!this.app.showTopBar) {
				event.preventDefault()
				this.exitFullscreen()
			}
		})

		this.loadPreference()
	}

	async ngAfterViewInit() {
		await this.loadCalibrationGroups()

		this.route.queryParams.subscribe((e) => {
			const data = JSON.parse(decodeURIComponent(e.data)) as ImageData
			this.loadImageFromData(data)
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.closeImage(true)

		this.roiInteractable?.unset()
	}

	private markCalibrationGroupItem(name?: string) {
		this.calibrationMenuItem.items![2].disabled = !this.imageInfo?.camera?.id
		this.calibrationMenuItem.items![2].selected = this.calibrationViaCamera

		for (let i = 3; i < this.calibrationMenuItem.items!.length; i++) {
			const item = this.calibrationMenuItem.items![i]
			item.selected = !this.calibrationViaCamera && item.data === name
		}
	}

	private async loadCalibrationGroups() {
		const groups = await this.api.calibrationGroups()
		const found = !!groups.find((e) => this.transformation.calibrationGroup === e)
		let reloadImage = false

		if (!found) {
			reloadImage = !!this.transformation.calibrationGroup
			this.transformation.calibrationGroup = undefined
			this.calibrationViaCamera = true
		}

		const makeItem = (name?: string) => {
			const label = name ?? 'None'
			const icon = name ? 'mdi mdi-wrench' : 'mdi mdi-close'

			return <MenuItem>{
				label,
				icon,
				selected: !this.calibrationViaCamera && this.transformation.calibrationGroup === name,
				data: name,
				command: async () => {
					this.calibrationViaCamera = false
					this.transformation.calibrationGroup = name
					this.markCalibrationGroupItem(name)
					await this.loadImage()
				},
			}
		}

		const menu: MenuItem[] = []

		menu.push({
			label: 'Open',
			icon: 'mdi mdi-wrench',
			command: () => this.browserWindow.openCalibration(),
		})

		menu.push(SEPARATOR_MENU_ITEM)

		menu.push({
			label: 'Camera',
			icon: 'mdi mdi-camera-iris',
			selected: this.calibrationViaCamera,
			disabled: !this.imageInfo?.camera?.id,
			data: 0,
			command: async () => {
				if (this.imageInfo?.camera?.id) {
					this.calibrationViaCamera = !this.calibrationViaCamera
					this.markCalibrationGroupItem(this.transformation.calibrationGroup)
					await this.loadImage()
				}
			},
		})

		menu.push(makeItem())

		for (const group of groups) {
			menu.push(makeItem(group))
		}

		this.calibrationMenuItem.items = menu
		this.menu.model = this.contextMenuItems
		this.menu.cd.markForCheck()

		if (reloadImage) {
			this.loadImage()
		}
	}

	private async closeImage(force: boolean = false) {
		if (this.imageData.path && force) {
			await this.api.closeImage(this.imageData.path)
		}
		if (this.imageData.liveStackedPath && force) {
			await this.api.closeImage(this.imageData.liveStackedPath)
		}
	}

	private changeLiveStackingMode(mode: LiveStackingMode) {
		this.liveStackingMode = mode

		if (this.liveStackingMode !== 'NONE') {
			this.disableCalibration(true)
		}

		this.liveStackingMenuItem.visible = this.liveStackingMode !== 'NONE'
		this.liveStackingMenuItem.label = mode

		this.loadImage(true)
	}

	private roiResizableMove(event: any) {
		const target = event.target

		const { scale } = this.panZoom!.getTransform()

		let x = parseFloat(target.getAttribute('data-x')) || 0
		let y = parseFloat(target.getAttribute('data-y')) || 0

		target.style.width = event.rect.width / scale + 'px'
		target.style.height = event.rect.height / scale + 'px'

		x += event.deltaRect.left / scale
		y += event.deltaRect.top / scale

		target.style.transform = 'translate(' + x + 'px,' + y + 'px)'

		target.setAttribute('data-x', x)
		target.setAttribute('data-y', y)

		this.ngZone.run(() => {
			this.imageROI.x = Math.round(x)
			this.imageROI.y = Math.round(y)
			this.imageROI.width = Math.round(event.rect.width / scale)
			this.imageROI.height = Math.round(event.rect.height / scale)
		})
	}

	private roiDraggableMove(event: any) {
		const target = event.target

		const { scale } = this.panZoom!.getTransform()

		const x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx / scale
		const y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy / scale

		target.style.transform = 'translate(' + x + 'px, ' + y + 'px)'

		target.setAttribute('data-x', x)
		target.setAttribute('data-y', y)

		this.ngZone.run(() => {
			this.imageROI.x = Math.round(x)
			this.imageROI.y = Math.round(y)
		})
	}

	roiForCamera() {
		this.executeCamera((camera) => {
			const x = camera.x + this.imageROI.x
			const y = camera.y + this.imageROI.y
			const width = camera.binX * this.imageROI.width
			const height = camera.binY * this.imageROI.height

			this.electron.send('ROI.SELECTED', { camera, x, y, width, height })
		}, false)
	}

	private loadImageFromData(data: ImageData) {
		console.info('loading image from data: %s', data)

		this.imageData = data

		// Not clicked on menu item.
		if (this.calibrationViaCamera && this.transformation.calibrationGroup !== data.capture?.calibrationGroup) {
			this.transformation.calibrationGroup = data.capture?.calibrationGroup
			this.markCalibrationGroupItem(this.transformation.calibrationGroup)
		}

		if (data.source === 'FRAMING') {
			this.disableAutoStretch()

			if (this.transformation.stretch.auto) {
				this.resetStretch(false)
			}
		} else if (data.source === 'FLAT_WIZARD') {
			this.disableCalibration(false)
		}

		this.clearOverlay()
		this.loadImage(true)
	}

	private clearOverlay() {
		this.annotation.data = []
		this.annotation.visible = false
		this.annotationMenuItem.checkable = false

		this.starDetection.stars = []
		this.starDetection.visible = false
		this.detectStarsMenuItem.checkable = false

		Object.assign(this.solver.solved, EMPTY_IMAGE_SOLVED)

		this.histogram?.update([])
	}

	private async computeHistogram() {
		const data = await this.api.imageHistogram(this.imagePath!, this.statisticsBitLength.bitLength)
		this.histogram.update(data)
	}

	statisticsBitLengthChanged() {
		this.computeHistogram()
	}

	async detectStars() {
		const options = this.preference.starDetectionRequest(this.starDetection.type).get()
		options.minSNR = this.starDetection.minSNR
		options.maxStars = this.starDetection.maxStars

		try {
			this.starDetection.running = true
			this.starDetection.stars = await this.api.detectStars(this.imagePath!, options)
		} finally {
			this.starDetection.running = false
		}

		let hfd = 0
		let stdDev = 0
		let snr = 0
		let fluxMin = 0
		let fluxMax = 0

		const starCount = this.starDetection.stars.length

		if (starCount) {
			fluxMax = this.starDetection.stars[0].flux

			for (const star of this.starDetection.stars) {
				hfd += star.hfd
				snr += star.snr
				fluxMax = Math.min(fluxMax, star.flux)
				fluxMin = Math.max(fluxMin, star.flux)
			}

			hfd = hfd / starCount
			snr = snr / starCount

			let squared = 0

			for (const star of this.starDetection.stars) {
				squared += Math.pow(star.hfd - hfd, 2)
			}

			stdDev = Math.sqrt(squared / starCount)
		}

		this.starDetection.computed.hfd = hfd
		this.starDetection.computed.stdDev = stdDev
		this.starDetection.computed.snr = snr
		this.starDetection.computed.fluxMax = fluxMin
		this.starDetection.computed.fluxMin = fluxMax

		this.savePreference()

		this.starDetection.visible = this.starDetection.stars.length > 0
		this.detectStarsMenuItem.checkable = this.starDetection.visible
		this.detectStarsMenuItem.checked = this.starDetection.visible
	}

	selectDetectedStar(star: DetectedStar) {
		Object.assign(this.starDetection.selected, star)

		const canvas = this.detectedStarCanvas.nativeElement
		const ctx = canvas.getContext('2d')
		ctx?.drawImage(this.image.nativeElement, star.x - 8, star.y - 8, 16, 16, 0, 0, canvas.width, canvas.height)
	}

	private async loadImage(force: boolean = false) {
		await this.closeImage(force)

		const path = this.imagePath

		if (path) {
			await this.loadImageFromPath(path)
		}

		let extraInfo = ''

		if (this.imageData.exposureCount) {
			extraInfo += ` · ${this.imageData.exposureCount}`
		}

		if (this.imageData.title) {
			this.app.subTitle = this.imageData.title
		} else if (this.imageData.camera) {
			this.app.subTitle = this.imageData.camera.name
		} else if (this.imageData.path) {
			this.app.subTitle = basename(this.imageData.path)
		} else {
			this.app.subTitle = ''
		}

		this.app.subTitle += extraInfo
	}

	private async loadImageFromPath(path: string) {
		const image = this.image.nativeElement

		const transformation = structuredClone(this.transformation)
		if (this.calibrationViaCamera && this.liveStackingMode !== 'NONE') transformation.calibrationGroup = this.imageData.capture?.calibrationGroup
		const { info, blob } = await this.api.openImage(path, transformation, this.imageData.camera)

		this.imageInfo = info
		this.scnrMenuItem.disabled = info.mono

		if (info.rightAscension) this.solver.centerRA = info.rightAscension
		if (info.declination) this.solver.centerDEC = info.declination
		this.solver.blind = !this.solver.centerRA || !this.solver.centerDEC

		if (this.stretch.auto) {
			this.stretchShadow.set(Math.trunc(info.stretchShadow * 65536))
			this.stretchHighlight.set(Math.trunc(info.stretchHighlight * 65536))
			this.stretchMidtone.set(Math.trunc(info.stretchMidtone * 65536))
		}

		this.updateImageSolved(info.solved)

		this.fitsHeaders.headers = info.headers

		this.retrieveInfoFromImageHeaders(info.headers)

		if (this.imageURL) window.URL.revokeObjectURL(this.imageURL)
		this.imageURL = window.URL.createObjectURL(blob)
		image.src = this.imageURL

		if (!info.camera?.id) {
			this.calibrationViaCamera = false
			this.markCalibrationGroupItem(this.transformation.calibrationGroup)
		} else {
			this.calibrationMenuItem.items![2].disabled = false
		}

		this.retrieveCoordinateInterpolation()
	}

	private retrieveInfoFromImageHeaders(headers: FITSHeaderItem[]) {
		const imagePreference = this.preference.imagePreference.get()

		for (const item of headers) {
			if (item.name === 'FOCALLEN') {
				this.solver.focalLength = parseFloat(item.value)
			} else if (item.name === 'XPIXSZ') {
				this.solver.pixelSize = parseFloat(item.value)
			}
		}

		this.solver.focalLength ||= imagePreference.solver?.focalLength || 0
		this.solver.pixelSize ||= imagePreference.solver?.pixelSize || 0
	}

	imageClicked(event: MouseEvent, contextMenu: boolean) {
		this.imageMouseX = event.offsetX
		this.imageMouseY = event.offsetY

		if (contextMenu) {
			this.menu.show(event)
		}
	}

	imageMouseMoved(event: MouseEvent) {
		this.imageMouseMovedWithCoordinates(event.offsetX, event.offsetY)
	}

	imageMouseMovedWithCoordinates(x: number, y: number) {
		if (!this.menu.visible()) {
			this.mouseCoordinate = this.mouseCoordinateInterpolation?.interpolateAsText(x, y, true, true, false)

			if (this.mouseCoordinate) {
				this.mouseCoordinate.x = x
				this.mouseCoordinate.y = y
			}
		}
	}

	async saveImageAs() {
		await this.api.saveImageAs(this.imagePath!, this.saveAs, this.imageData.camera)
		this.saveAs.showDialog = false
	}

	async annotateImage() {
		try {
			this.annotation.running = true
			this.annotation.data = await this.api.annotationsOfImage(this.imagePath!, this.annotation.useStarsAndDSOs, this.annotation.useMinorPlanets, this.annotation.minorPlanetsMagLimit, this.annotation.useSimbad)
			this.annotation.visible = this.annotation.data.length > 0
			this.annotationMenuItem.checkable = this.annotation.visible
			this.annotationMenuItem.checked = this.annotation.visible
			this.annotation.showDialog = false
		} finally {
			this.annotation.running = false
		}
	}

	showAnnotationInfo(annotation: ImageAnnotation) {
		this.annotationInfo.info = annotation.star ?? annotation.dso ?? annotation.minorPlanet
		this.annotationInfo.showDialog = true
	}

	private disableAutoStretch() {
		this.stretch.auto = false
		this.autoStretchMenuItem.selected = false
	}

	private disableCalibration(canEnable: boolean = true) {
		this.transformation.calibrationGroup = undefined
		this.markCalibrationGroupItem(undefined)
		this.calibrationMenuItem.disabled = !canEnable
	}

	autoStretch() {
		this.stretch.auto = true
		this.autoStretchMenuItem.selected = true

		this.loadImage()
	}

	resetStretch(load: boolean = true) {
		this.stretchShadow.set(0)
		this.stretchHighlight.set(65536)
		this.stretchMidtone.set(32768)

		if (load) {
			this.stretchImage()
		}
	}

	toggleStretch() {
		this.stretch.auto = !this.stretch.auto
		this.autoStretchMenuItem.checked = this.stretch.auto

		if (!this.stretch.auto) {
			this.resetStretch()
		} else {
			this.loadImage()
		}
	}

	stretchImage() {
		this.disableAutoStretch()
		return this.loadImage()
	}

	invertImage() {
		this.transformation.invert = !this.transformation.invert
		this.invertMenuItem.selected = this.transformation.invert
		this.loadImage()
	}

	scnrImage() {
		this.loadImage()
	}

	toggleCrosshair() {
		this.crossHair = !this.crossHair
		this.crosshairMenuItem.selected = this.crossHair
	}

	zoomIn() {
		if (!this.panZoom) return
		const { scale } = this.panZoom.getTransform()
		this.panZoom.smoothZoomAbs(window.innerWidth / 2, window.innerHeight / 2, scale * 1.1)
	}

	zoomOut() {
		if (!this.panZoom) return
		const { scale } = this.panZoom.getTransform()
		this.panZoom.smoothZoomAbs(window.innerWidth / 2, window.innerHeight / 2, scale * 0.9)
	}

	center() {
		const { width, height } = this.image.nativeElement.getBoundingClientRect()
		this.panZoom?.moveTo(window.innerWidth / 2 - width / 2, (window.innerHeight - 42) / 2 - height / 2)
	}

	resetZoom(fitToScreen: boolean = false, center: boolean = true) {
		if (fitToScreen) {
			const { width, height } = this.image.nativeElement
			const factor = Math.min(window.innerWidth, window.innerHeight - 42) / Math.min(width, height)
			this.panZoom?.smoothZoomAbs(window.innerWidth / 2, window.innerHeight / 2, factor)
		} else {
			this.panZoom?.smoothZoomAbs(window.innerWidth / 2, window.innerHeight / 2, 1.0)
		}

		if (center) {
			this.center()
		}
	}

	async enterFullscreen() {
		this.app.showTopBar = !(await this.electron.fullscreenWindow(true))
	}

	async exitFullscreen() {
		this.app.showTopBar = !(await this.electron.fullscreenWindow(false))
	}

	private async retrieveCoordinateInterpolation() {
		const coordinate = await this.api.coordinateInterpolation(this.imagePath!)

		if (coordinate) {
			const { ma, md, x0, y0, x1, y1, delta } = coordinate
			const x = Math.max(0, Math.min(this.mouseCoordinate?.x ?? 0, this.imageInfo!.width))
			const y = Math.max(0, Math.min(this.mouseCoordinate?.y ?? 0, this.imageInfo!.height))
			this.mouseCoordinateInterpolation = new CoordinateInterpolator(ma, md, x0, y0, x1, y1, delta)
			this.imageMouseMovedWithCoordinates(x, y)
		} else {
			this.mouseCoordinateInterpolation = undefined
			this.mouseCoordinate = undefined
		}
	}

	async solveImage() {
		this.solver.running = true

		try {
			const solver = this.preference.plateSolverRequest(this.solver.type).get()
			const solved = await this.api.solveImage(solver, this.imagePath!, this.solver.blind, this.solver.centerRA, this.solver.centerDEC, this.solver.radius)

			this.savePreference()
			this.updateImageSolved(solved)
		} catch {
			this.updateImageSolved(this.imageInfo?.solved)
		} finally {
			this.solver.running = false

			if (this.solver.solved.solved) {
				this.retrieveCoordinateInterpolation()
			}
		}
	}

	private updateImageSolved(solved?: ImageSolved) {
		Object.assign(this.solver.solved, solved ?? EMPTY_IMAGE_SOLVED)
		this.annotationMenuItem.disabled = !this.solver.solved.solved
		this.fovMenuItem.disabled = !this.solver.solved.solved
		this.pointMountHereMenuItem.disabled = !this.solver.solved.solved
		this.frameAtThisCoordinateMenuItem.disabled = !this.solver.solved.solved

		if (solved) this.fov.fovs.forEach((e) => this.computeFOV(e))
		else this.fov.fovs.forEach((e) => (e.computed = undefined))
	}

	mountSync(coordinate: EquatorialCoordinateJ2000) {
		this.executeMount((mount) => {
			this.api.mountSync(mount, coordinate.rightAscensionJ2000, coordinate.declinationJ2000, true)
		})
	}

	async mountGoTo(coordinate: EquatorialCoordinateJ2000) {
		this.executeMount((mount) => {
			this.api.mountGoTo(mount, coordinate.rightAscensionJ2000, coordinate.declinationJ2000, true)
		})
	}

	async mountSlew(coordinate: EquatorialCoordinateJ2000) {
		this.executeMount((mount) => {
			this.api.mountSlew(mount, coordinate.rightAscensionJ2000, coordinate.declinationJ2000, true)
		})
	}

	frame(coordinate: EquatorialCoordinateJ2000) {
		this.browserWindow.openFraming({
			data: {
				rightAscension: coordinate.rightAscensionJ2000,
				declination: coordinate.declinationJ2000,
				fov: this.solver.solved!.width / 60,
				rotation: this.solver.solved!.orientation,
			},
		})
	}

	imageLoaded() {
		if (!this.panZoom) {
			this.panZoom = createPanZoom(this.image.nativeElement.parentElement!, {
				minZoom: 0.1,
				maxZoom: 500.0,
				autocenter: true,
				zoomDoubleClickSpeed: 1,
				filterKey: () => {
					return true
				},
				beforeWheel: (e) => {
					return e.target !== this.image.nativeElement && e.target !== this.roi.nativeElement
				},
				beforeMouseDown: (e) => {
					return e.target !== this.image.nativeElement
				},
			})
		}
	}

	async showFOVCameras() {
		if (!this.fov.cameras.length) {
			this.fov.cameras = await this.api.fovCameras()
		}

		this.fov.camera = undefined
		this.fov.showCameraDialog = true
	}

	async showFOVTelescopes() {
		if (!this.fov.telescopes.length) {
			this.fov.telescopes = await this.api.fovTelescopes()
		}

		this.fov.telescope = undefined
		this.fov.showTelescopeDialog = true
	}

	chooseCamera() {
		if (this.fov.camera) {
			this.fov.cameraSize.width = this.fov.camera.width
			this.fov.cameraSize.height = this.fov.camera.height
			this.fov.pixelSize.width = this.fov.camera.pixelSize
			this.fov.pixelSize.height = this.fov.camera.pixelSize
			this.fov.camera = undefined
			this.fov.showCameraDialog = false
		}
	}

	chooseTelescope() {
		if (this.fov.telescope) {
			this.fov.aperture = this.fov.telescope.aperture
			this.fov.focalLength = this.fov.telescope.focalLength
			this.fov.telescope = undefined
			this.fov.showTelescopeDialog = false
		}
	}

	addFOV() {
		if (this.computeFOV(this.fov)) {
			this.fov.fovs.push(structuredClone(this.fov))
			this.preference.imageFOVs.set(this.fov.fovs)
		}
	}

	editFOV(fov: FOV) {
		Object.assign(this.fov, structuredClone(fov))
		this.fov.edited = fov
	}

	cancelEditFOV() {
		this.fov.edited = undefined
	}

	saveFOV() {
		if (this.fov.edited && this.computeFOV(this.fov)) {
			Object.assign(this.fov.edited, structuredClone(this.fov))
			this.preference.imageFOVs.set(this.fov.fovs)
			this.fov.edited = undefined
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

	deleteFOV(fov: FOV) {
		const index = this.fov.fovs.indexOf(fov)

		if (index >= 0) {
			if (this.fov.fovs[index] === this.fov.edited) {
				this.fov.edited = undefined
			}

			this.fov.fovs.splice(index, 1)
			this.preference.imageFOVs.set(this.fov.fovs)
		}
	}

	private loadPreference() {
		const preference = this.preference.imagePreference.get()
		this.solver.radius = preference.solver?.radius ?? this.solver.radius
		this.solver.type = preference.solver?.type ?? 'ASTAP'
		this.solver.focalLength = preference.solver?.focalLength ?? 0
		this.solver.pixelSize = preference.solver?.pixelSize ?? 0
		this.starDetection.type = preference.starDetection?.type ?? this.starDetection.type
		this.starDetection.minSNR = preference.starDetection?.minSNR ?? this.preference.starDetectionRequest(this.starDetection.type).get().minSNR ?? this.starDetection.minSNR
		this.starDetection.maxStars = preference.starDetection?.maxStars ?? this.preference.starDetectionRequest(this.starDetection.type).get().maxStars ?? this.starDetection.maxStars

		this.fov.fovs = this.preference.imageFOVs.get()
		this.fov.fovs.forEach((e) => {
			e.enabled = false
			e.computed = undefined
		})
	}

	private savePreference() {
		const preference = this.preference.imagePreference.get()

		preference.solver = {
			type: this.solver.type,
			focalLength: this.solver.focalLength,
			pixelSize: this.solver.pixelSize,
			radius: this.solver.radius,
		}
		preference.starDetection = {
			type: this.starDetection.type,
			maxStars: this.starDetection.maxStars,
			minSNR: this.starDetection.minSNR,
		}

		this.preference.imagePreference.set(preference)
	}

	private async executeCamera(action: (camera: Camera) => void, showConfirmation: boolean = true) {
		if (showConfirmation && (await this.prime.confirm('Are you sure that you want to proceed?'))) {
			return
		}

		const cameras = await this.api.cameras()

		if (cameras.length === 1) {
			action(cameras[0])
			return true
		} else {
			this.deviceMenu.header = 'CAMERA'
			const camera = await this.deviceMenu.show(cameras)

			if (camera && camera !== 'NONE' && camera.connected) {
				action(camera)
				return true
			}
		}

		return false
	}

	private async executeMount(action: (mount: Mount) => void, showConfirmation: boolean = true) {
		if (showConfirmation && (await this.prime.confirm('Are you sure that you want to proceed?'))) {
			return
		}

		const mounts = await this.api.mounts()

		if (mounts.length === 1) {
			action(mounts[0])
			return true
		} else {
			this.deviceMenu.header = 'MOUNT'
			const mount = await this.deviceMenu.show(mounts)

			if (mount && mount !== 'NONE' && mount.connected) {
				action(mount)
				return true
			}
		}

		return false
	}
}
