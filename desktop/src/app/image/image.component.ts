import { AfterViewInit, Component, ElementRef, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { Interactable } from '@interactjs/types/index'
import interact from 'interactjs'
import createPanZoom, { PanZoom } from 'panzoom'
import * as path from 'path'
import { MenuItem } from 'primeng/api'
import { ContextMenu } from 'primeng/contextmenu'
import { DeviceListMenuComponent } from '../../shared/components/device-list-menu/device-list-menu.component'
import { HistogramComponent } from '../../shared/components/histogram/histogram.component'
import { SEPARATOR_MENU_ITEM } from '../../shared/constants'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { PrimeService } from '../../shared/services/prime.service'
import { CheckableMenuItem, ToggleableMenuItem } from '../../shared/types/app.types'
import { Angle, AstronomicalObject, DeepSkyObject, EquatorialCoordinateJ2000, Star } from '../../shared/types/atlas.types'
import { Camera } from '../../shared/types/camera.types'
import { DetectedStar, FITSHeaderItem, ImageAnnotation, ImageChannel, ImageInfo, ImageSolved, ImageSource, ImageStatisticsBitOption, SCNRProtectionMethod, SCNR_PROTECTION_METHODS } from '../../shared/types/image.types'
import { Mount } from '../../shared/types/mount.types'
import { CoordinateInterpolator, InterpolatedCoordinate } from '../../shared/utils/coordinate-interpolation'
import { AppComponent } from '../app.component'

export function imagePreferenceKey(camera?: Camera) {
    return camera ? `image.${camera.name}` : 'image'
}

export interface ImagePreference {
    solverRadius?: number
}

export interface ImageData {
    camera?: Camera
    path?: string
    source?: ImageSource
    title?: string
}

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

    debayer = true
    calibrate = true
    mirrorHorizontal = false
    mirrorVertical = false
    invert = false

    readonly scnrChannelOptions: ImageChannel[] = ['NONE', 'RED', 'GREEN', 'BLUE']
    readonly scnrProtectionMethodOptions: SCNRProtectionMethod[] = [...SCNR_PROTECTION_METHODS]

    showSCNRDialog = false
    scnrChannel: ImageChannel = 'NONE'
    scnrAmount = 0.5
    scnrProtectionMethod: SCNRProtectionMethod = 'AVERAGE_NEUTRAL'

    showAnnotationDialog = false
    annotateWithStars = true
    annotateWithDSOs = true
    annotateWithMinorPlanets = false
    annotateWithMinorPlanetsMagLimit = 12.0

    autoStretched = true
    showStretchingDialog = false
    stretchShadowhHighlight = [0, 65536]
    stretchMidtone = 32768

    showSolverDialog = false
    solving = false
    solved = false
    solverBlind = true
    solverCenterRA = ''
    solverCenterDEC = ''
    solverRadius = 4
    solverData?: ImageSolved

    crossHair = false
    annotations: ImageAnnotation[] = []
    annotating = false
    showAnnotationInfoDialog = false
    annotationInfo?: AstronomicalObject & Partial<Star & DeepSkyObject>
    annotationIsVisible = false

    detectedStars: DetectedStar[] = []
    detectedStarsIsVisible = false

    showFITSHeadersDialog = false
    fitsHeaders: FITSHeaderItem[] = []

    showStatisticsDialog = false

    readonly statisticsBitOptions: ImageStatisticsBitOption[] = [
        { name: 'Normalized: [0, 1]', rangeMax: 1, bitLength: 16 },
        { name: '8-bit: [0, 255]', rangeMax: 255, bitLength: 8 },
        { name: '9-bit: [0, 511]', rangeMax: 511, bitLength: 9 },
        { name: '10-bit: [0, 1023]', rangeMax: 1023, bitLength: 10 },
        { name: '12-bit: [0, 4095]', rangeMax: 4095, bitLength: 12 },
        { name: '14-bit: [0, 16383]', rangeMax: 16383, bitLength: 14 },
        { name: '16-bit: [0, 65535]', rangeMax: 65535, bitLength: 16 },
    ]

    statisticsBitLength = this.statisticsBitOptions[0]
    imageInfo?: ImageInfo

    private panZoom?: PanZoom
    private imageURL!: string
    private imageMouseX = 0
    private imageMouseY = 0
    private imageData: ImageData = {}

    roiX = 0
    roiY = 0
    roiWidth = 128
    roiHeight = 128
    roiInteractable?: Interactable

    private readonly saveAsMenuItem: MenuItem = {
        label: 'Save as...',
        icon: 'mdi mdi-content-save',
        command: async () => {
            const path = await this.electron.saveFits()
            if (path) this.api.saveImageAs(this.imageData.path!, path)
        },
    }

    private readonly plateSolveMenuItem: MenuItem = {
        label: 'Plate Solve',
        icon: 'mdi mdi-sigma',
        command: () => {
            this.showSolverDialog = true
        },
    }

    private readonly stretchMenuItem: MenuItem = {
        label: 'Stretch',
        icon: 'mdi mdi-chart-histogram',
        command: () => {
            this.showStretchingDialog = true
        },
    }

    private readonly autoStretchMenuItem: CheckableMenuItem = {
        id: 'auto-stretch-menuitem',
        label: 'Auto stretch',
        icon: 'mdi mdi-auto-fix',
        checked: true,
        command: () => {
            this.autoStretched = !this.autoStretched
            this.autoStretchMenuItem.checked = this.autoStretched

            if (!this.autoStretched) {
                this.resetStretch()
            } else {
                this.loadImage()
            }
        },
    }

    private readonly scnrMenuItem: MenuItem = {
        label: 'SCNR',
        icon: 'mdi mdi-palette',
        disabled: true,
        command: () => {
            this.showSCNRDialog = true
        },
    }

    private readonly horizontalMirrorMenuItem: CheckableMenuItem = {
        label: 'Horizontal mirror',
        icon: 'mdi mdi-flip-horizontal',
        checked: false,
        command: () => {
            this.mirrorHorizontal = !this.mirrorHorizontal
            this.horizontalMirrorMenuItem.checked = this.mirrorHorizontal
            this.loadImage()
        },
    }

    private readonly verticalMirrorMenuItem: CheckableMenuItem = {
        label: 'Vertical mirror',
        icon: 'mdi mdi-flip-vertical',
        checked: false,
        command: () => {
            this.mirrorVertical = !this.mirrorVertical
            this.verticalMirrorMenuItem.checked = this.mirrorVertical
            this.loadImage()
        },
    }

    private readonly invertMenuItem: CheckableMenuItem = {
        label: 'Invert',
        icon: 'mdi mdi-invert-colors',
        checked: false,
        command: () => {
            this.invert = !this.invert
            this.invertMenuItem.checked = this.invert
            this.loadImage()
        },
    }

    private readonly calibrateMenuItem: CheckableMenuItem = {
        label: 'Calibrate',
        icon: 'mdi mdi-tools',
        checked: true,
        command: () => {
            this.calibrate = !this.calibrate
            this.calibrateMenuItem.checked = this.calibrate
            this.loadImage()
        },
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
            this.showFITSHeadersDialog = true
        },
    }

    private readonly pointMountHereMenuItem: MenuItem = {
        label: 'Point mount here',
        icon: 'mdi mdi-telescope',
        disabled: true,
        command: () => {
            this.executeMount((mount) => {
                this.api.pointMountHere(mount, this.imageData.path!, this.imageMouseX, this.imageMouseY)
            })
        },
    }

    private readonly crosshairMenuItem: CheckableMenuItem = {
        label: 'Crosshair',
        icon: 'mdi mdi-bullseye',
        checked: false,
        command: () => {
            this.crossHair = !this.crossHair
            this.crosshairMenuItem.checked = this.crossHair
        },
    }

    private readonly annotationMenuItem: ToggleableMenuItem = {
        label: 'Annotate',
        icon: 'mdi mdi-marker',
        disabled: true,
        toggleable: true,
        toggled: false,
        command: () => {
            this.showAnnotationDialog = true
        },
        toggle: (event) => {
            event.originalEvent?.stopImmediatePropagation()
            this.annotationIsVisible = event.checked
        },
    }

    private readonly detectStarsMenuItem: ToggleableMenuItem = {
        label: 'Detect stars',
        icon: 'mdi mdi-creation',
        disabled: false,
        toggleable: false,
        toggled: false,
        command: async () => {
            this.detectedStars = await this.api.detectStars(this.imageData.path!)
            this.detectedStarsIsVisible = this.detectedStars.length > 0
            this.detectStarsMenuItem.toggleable = this.detectedStarsIsVisible
            this.detectStarsMenuItem.toggled = this.detectedStarsIsVisible
        },
        toggle: (event) => {
            event.originalEvent?.stopImmediatePropagation()
            this.detectedStarsIsVisible = event.checked
        },
    }

    private readonly roiMenuItem: CheckableMenuItem = {
        label: 'ROI',
        icon: 'mdi mdi-select',
        checked: false,
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
                            })
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
                        ]
                    })
            }

            this.roiMenuItem.checked = !!this.roiInteractable
        },
    }

    private readonly overlayMenuItem: MenuItem = {
        label: 'Overlay',
        icon: 'mdi mdi-layers',
        items: [
            this.crosshairMenuItem,
            this.annotationMenuItem,
            this.detectStarsMenuItem,
            this.roiMenuItem,
        ]
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
        this.calibrateMenuItem,
        SEPARATOR_MENU_ITEM,
        this.overlayMenuItem,
        this.statisticsMenuItem,
        this.fitsHeaderMenuItem,
        SEPARATOR_MENU_ITEM,
        this.pointMountHereMenuItem,
    ]

    mouseCoordinate?: InterpolatedCoordinate<Angle> & Partial<{ x: number, y: number }>
    private mouseCoordinateInterpolation?: CoordinateInterpolator

    get isMouseCoordinateVisible() {
        return !!this.mouseCoordinate && !this.mirrorHorizontal && !this.mirrorVertical
    }

    constructor(
        private app: AppComponent,
        private route: ActivatedRoute,
        private api: ApiService,
        private electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private storage: LocalStorageService,
        private prime: PrimeService,
        private ngZone: NgZone,
    ) {
        app.title = 'Image'

        electron.on('CAMERA.CAPTURE_ELAPSED', async (event) => {
            if (event.state === 'EXPOSURE_FINISHED' && event.camera.name === this.imageData.camera?.name) {
                await this.closeImage()

                ngZone.run(() => {
                    this.imageData.path = event.savePath
                    this.clearOverlay()
                    this.loadImage()
                })
            }
        })

        electron.on('DATA.CHANGED', async (event: ImageData) => {
            await this.closeImage()

            ngZone.run(() => {
                this.loadImageFromData(event)
            })
        })
    }

    ngAfterViewInit() {
        this.loadPreference()

        this.route.queryParams.subscribe(e => {
            const data = JSON.parse(decodeURIComponent(e.data)) as ImageData
            this.loadImageFromData(data)
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.closeImage(true)

        this.roiInteractable?.unset()
    }

    private async closeImage(force: boolean = false) {
        if (this.imageData.path) {
            if (force || !this.imageData.path.startsWith('@')) {
                await this.api.closeImage(this.imageData.path)
            }
        }
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
            this.roiX = Math.round(x)
            this.roiY = Math.round(y)
            this.roiWidth = Math.round(event.rect.width / scale)
            this.roiHeight = Math.round(event.rect.height / scale)
        })
    }

    private roiDraggableMove(event: any) {
        const target = event.target

        const { scale } = this.panZoom!.getTransform()

        const x = (parseFloat(target.getAttribute('data-x')) || 0) + (event.dx / scale)
        const y = (parseFloat(target.getAttribute('data-y')) || 0) + (event.dy / scale)

        target.style.transform = 'translate(' + x + 'px, ' + y + 'px)'

        target.setAttribute('data-x', x)
        target.setAttribute('data-y', y)

        this.ngZone.run(() => {
            this.roiX = Math.round(x)
            this.roiY = Math.round(y)
        })
    }

    private loadImageFromData(data: ImageData) {
        console.info('loading image from data: %s', data)

        this.imageData = data

        if (data.source === 'FRAMING') {
            this.disableAutoStretch()
        } else if (data.source === 'FLAT_WIZARD') {
            this.disableCalibrate(false)
        }

        if (!data.camera) {
            this.disableCalibrate()
        }

        this.clearOverlay()

        this.loadImage()
    }

    private clearOverlay() {
        this.annotations = []
        this.annotationIsVisible = false
        this.annotationMenuItem.toggleable = false

        this.detectedStars = []
        this.detectedStarsIsVisible = false
        this.detectStarsMenuItem.toggleable = false

        this.histogram?.update([])
    }

    private async computeHistogram() {
        const data = await this.api.imageHistogram(this.imageData.path!, this.statisticsBitLength.bitLength)
        this.histogram.update(data)
    }

    statisticsBitLengthChanged() {
        this.computeHistogram()
    }

    private async loadImage() {
        if (this.imageData.path) {
            await this.loadImageFromPath(this.imageData.path)
        }

        this.loadPreference(this.imageData.camera)

        if (this.imageData.title) {
            this.app.subTitle = this.imageData.title
        } else if (this.imageData.camera) {
            this.app.subTitle = this.imageData.camera.name
        } else if (this.imageData.path) {
            this.app.subTitle = path.basename(this.imageData.path)
        } else {
            this.app.subTitle = ''
        }
    }

    private async loadImageFromPath(path: string) {
        const image = this.image.nativeElement
        const scnrEnabled = this.scnrChannel !== 'NONE'
        const { info, blob } = await this.api.openImage(path, this.imageData.camera, this.calibrate, this.debayer, this.autoStretched,
            this.stretchShadowhHighlight[0] / 65536, this.stretchShadowhHighlight[1] / 65536, this.stretchMidtone / 65536,
            this.mirrorHorizontal, this.mirrorVertical,
            this.invert, scnrEnabled, scnrEnabled ? this.scnrChannel : 'GREEN', this.scnrAmount, this.scnrProtectionMethod)

        this.imageInfo = info
        this.scnrMenuItem.disabled = info.mono

        if (info.rightAscension) this.solverCenterRA = info.rightAscension
        if (info.declination) this.solverCenterDEC = info.declination
        this.solverBlind = !this.solverCenterRA || !this.solverCenterDEC

        if (this.autoStretched) {
            this.stretchShadowhHighlight[0] = Math.trunc(info.stretchShadow * 65536)
            this.stretchShadowhHighlight[1] = Math.trunc(info.stretchHighlight * 65536)
            this.stretchMidtone = Math.trunc(info.stretchMidtone * 65536)
        }

        this.annotationMenuItem.disabled = !info.solved
        this.pointMountHereMenuItem.disabled = !info.solved
        this.fitsHeaders = info.headers

        if (this.imageURL) window.URL.revokeObjectURL(this.imageURL)
        this.imageURL = window.URL.createObjectURL(blob)
        image.src = this.imageURL

        this.retrieveCoordinateInterpolation()
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

    async annotateImage() {
        try {
            this.annotating = true
            this.annotations = await this.api.annotationsOfImage(this.imageData.path!,
                this.annotateWithStars, this.annotateWithDSOs, this.annotateWithMinorPlanets, this.annotateWithMinorPlanetsMagLimit)
            this.annotationIsVisible = true
            this.annotationMenuItem.toggleable = this.annotations.length > 0
            this.annotationMenuItem.toggled = this.annotationMenuItem.toggleable
            this.showAnnotationDialog = false
        } finally {
            this.annotating = false
        }
    }

    showAnnotationInfo(annotation: ImageAnnotation) {
        this.annotationInfo = annotation.star ?? annotation.dso ?? annotation.minorPlanet
        this.showAnnotationInfoDialog = true
    }

    private disableAutoStretch() {
        this.autoStretched = false
        this.autoStretchMenuItem.checked = false
    }

    private disableCalibrate(canEnable: boolean = true) {
        this.calibrate = false
        this.calibrateMenuItem.checked = false
        this.calibrateMenuItem.disabled = !canEnable
    }

    autoStretch() {
        this.autoStretched = true
        this.autoStretchMenuItem.checked = true

        this.loadImage()
    }

    resetStretch() {
        this.stretchShadowhHighlight[0] = 0
        this.stretchShadowhHighlight[1] = 65536
        this.stretchMidtone = 32768

        this.stretchImage()
    }

    stretchImage() {
        this.disableAutoStretch()
        this.loadImage()
    }

    scnrImage() {
        this.loadImage()
    }

    private async retrieveCoordinateInterpolation() {
        const coordinate = await this.api.coordinateInterpolation(this.imageData.path!)

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
        this.solving = true

        try {
            this.solverData = await this.api.solveImage(this.imageData.path!, this.solverBlind,
                this.solverCenterRA, this.solverCenterDEC, this.solverRadius)

            this.savePreference()

            this.solved = true
            this.annotationMenuItem.disabled = false
            this.pointMountHereMenuItem.disabled = false
        } catch {
            this.solved = false
            this.solverData = undefined
            this.annotationMenuItem.disabled = true
            this.pointMountHereMenuItem.disabled = true
        } finally {
            this.solving = false
            this.retrieveCoordinateInterpolation()
        }
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
        this.browserWindow.openFraming({ data: { rightAscension: coordinate.rightAscensionJ2000, declination: coordinate.declinationJ2000, fov: this.solverData!.width / 60, rotation: this.solverData!.orientation } })
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

    private loadPreference(camera?: Camera) {
        const preference = this.storage.get<ImagePreference>(imagePreferenceKey(camera), {})
        this.solverRadius = preference.solverRadius ?? this.solverRadius
    }

    private savePreference() {
        const preference: ImagePreference = {
            solverRadius: this.solverRadius
        }

        this.storage.set(imagePreferenceKey(this.imageData.camera), preference)
    }

    private async executeMount(action: (mount: Mount) => void) {
        if (await this.prime.confirm('Are you sure that you want to proceed?')) {
            return
        }

        const mounts = await this.api.mounts()

        if (mounts.length === 1) {
            action(mounts[0])
            return true
        } else {
            const mount = await this.deviceMenu.show(mounts)

            if (mount && mount.connected) {
                action(mount)
                return true
            }
        }

        return false
    }
}
