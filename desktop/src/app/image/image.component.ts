import { AfterViewInit, Component, ElementRef, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import { Interactable } from '@interactjs/types/index'
import interact from 'interactjs'
import createPanZoom, { PanZoom } from 'panzoom'
import * as path from 'path'
import { MegaMenuItem, MenuItem } from 'primeng/api'
import { ContextMenu } from 'primeng/contextmenu'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import {
    Calibration, Camera, DeepSkyObject, EquatorialCoordinate, FITSHeaderItem, GuideExposureFinished, ImageAnnotation, ImageChannel, ImageInfo, ImageSource,
    ImageStarSelected, PlateSolverType, SCNRProtectionMethod, SCNR_PROTECTION_METHODS, SavedCameraImage, Star
} from '../../shared/types'

export interface ImageParams {
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

    debayer = false
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

    autoStretch = true
    showStretchingDialog = false
    stretchShadowhHighlight = [0, 65536]
    stretchMidtone = 32768

    readonly solverTypeOptions: PlateSolverType[] = ['ASTAP']

    showSolverDialog = false
    solving = false
    solved = false
    solverType: PlateSolverType = 'ASTAP'
    solverBlind = true
    solverCenterRA = ''
    solverCenterDEC = ''
    solverRadius = 4
    solverDownsampleFactor = 1
    solverPathOrUrl = ''
    solverApiKey = ''
    solverCalibration?: Calibration

    crossHair = false
    annotations: ImageAnnotation[] = []
    annotating = false
    showAnnotationInfoDialog = false
    annotationInfo?: Star | DeepSkyObject

    showFITSHeadersDialog = false
    fitsHeaders: FITSHeaderItem[] = []

    private panZoom?: PanZoom
    private imageURL!: string
    private imageInfo?: ImageInfo
    private imageMouseX = 0
    private imageMouseY = 0
    private imageParams: ImageParams = {}

    roiX = 0
    roiY = 0
    roiWidth = 128
    roiHeight = 128
    roiInteractable?: Interactable

    guiding = false

    private readonly scnrMenuItem: MenuItem = {
        label: 'SCNR',
        icon: 'mdi mdi-palette',
        disabled: true,
        command: () => {
            this.showSCNRDialog = true
        },
    }

    private readonly pointMountHereMenuItem: MenuItem = {
        label: 'Point mount here',
        icon: 'mdi mdi-telescope',
        disabled: true,
        command: () => {
            const mount = this.electron.selectedMount()
            if (!mount?.connected) return
            this.api.pointMountHere(mount, this.imageParams.path!, this.imageMouseX, this.imageMouseY, !this.solved)
        },
    }

    private readonly annotationMenuItem: MenuItem = {
        label: 'Annotation',
        icon: 'mdi mdi-format-color-text',
        disabled: true,
        command: () => {
            this.showAnnotationDialog = true
        },
    }

    readonly menuItems: MenuItem[] = [
        {
            label: 'Save as...',
            icon: 'mdi mdi-content-save',
            command: async () => {
                const path = await this.electron.sendSync('SAVE_FITS_AS')
                if (path) this.api.saveImageAs(this.imageParams.path!, path)
            },
        },
        {
            separator: true,
        },
        {
            label: 'Plate Solve',
            icon: 'mdi mdi-sigma',
            command: () => {
                this.showSolverDialog = true
            },
        },
        {
            separator: true,
        },
        {
            label: 'Stretch',
            icon: 'mdi mdi-chart-histogram',
            command: () => {
                this.showStretchingDialog = true
            },
        },
        {
            id: 'auto-stretch-menuitem',
            label: 'Auto stretch',
            icon: 'mdi mdi-chart-histogram',
            styleClass: 'p-menuitem-checked',
            command: (e) => {
                this.autoStretch = !this.autoStretch
                this.checkMenuItem(e.item, this.autoStretch)

                if (!this.autoStretch) {
                    this.resetStretch()
                } else {
                    this.loadImage()
                }
            },
        },
        this.scnrMenuItem,
        {
            label: 'Horizontal mirror',
            icon: 'mdi mdi-flip-horizontal',
            command: (e) => {
                this.mirrorHorizontal = !this.mirrorHorizontal
                this.checkMenuItem(e.item, this.mirrorHorizontal)
                this.loadImage()
            },
        },
        {
            label: 'Vertical mirror',
            icon: 'mdi mdi-flip-vertical',
            command: (e) => {
                this.mirrorVertical = !this.mirrorVertical
                this.checkMenuItem(e.item, this.mirrorVertical)
                this.loadImage()
            },
        },
        {
            label: 'Invert',
            icon: 'mdi mdi-invert-colors',
            command: (e) => {
                this.invert = !this.invert
                this.checkMenuItem(e.item, this.invert)
                this.loadImage()
            },
        },
        {
            separator: true,
        },
        {
            label: 'Overlay',
            icon: 'mdi mdi-layer',
            items: [
                {
                    label: 'Crosshair',
                    icon: 'mdi mdi-bullseye',
                    command: (e) => {
                        this.crossHair = !this.crossHair
                        this.checkMenuItem(e.item, this.crossHair)
                    },
                },
                this.annotationMenuItem,
                {
                    label: 'ROI',
                    icon: 'mdi mdi-select',
                    command: (e) => {
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

                        this.checkMenuItem(e.item, !!this.roiInteractable)
                    },
                },
            ]
        },
        {
            icon: 'mdi mdi-list-box',
            label: 'FITS Header',
            command: () => {
                this.showFITSHeadersDialog = true
            },
        },
        {
            separator: true,
        },
        this.pointMountHereMenuItem,
    ]

    constructor(
        private title: Title,
        private route: ActivatedRoute,
        private api: ApiService,
        private electron: ElectronService,
        private browserWindow: BrowserWindowService,
        private preference: PreferenceService,
        private ngZone: NgZone,
    ) {
        title.setTitle('Image')

        electron.ipcRenderer.on('CAMERA_IMAGE_SAVED', async (_, data: SavedCameraImage) => {
            if (data.camera === this.imageParams.camera?.name) {
                await this.closeImage()

                ngZone.run(() => {
                    this.guiding = false
                    this.annotations = []
                    this.imageParams.path = data.path
                    this.loadImage()
                })
            }
        })

        electron.ipcRenderer.on('GUIDE_EXPOSURE_FINISHED', async (_, data: GuideExposureFinished) => {
            if (data.camera === this.imageParams.camera?.name) {
                await this.closeImage()

                ngZone.run(() => {
                    this.guiding = true
                    this.annotations = []
                    this.imageParams.path = data.path
                    this.loadImage()
                })
            }
        })

        electron.ipcRenderer.on('PARAMS_CHANGED', async (_, data: ImageParams) => {
            await this.closeImage()

            this.loadImageFromParams(data)
        })

        this.solverPathOrUrl = this.preference.get('image.solver.pathOrUrl', '')
        this.solverRadius = this.preference.get('image.solver.radius', 4)
        this.solverDownsampleFactor = this.preference.get('image.solver.downsampleFactor', 1)
    }

    ngAfterViewInit() {
        this.route.queryParams.subscribe(e => {
            const params = JSON.parse(decodeURIComponent(e.params)) as ImageParams
            this.loadImageFromParams(params)
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.closeImage(true)

        this.roiInteractable?.unset()
    }

    private async closeImage(force: boolean = false) {
        if (this.imageParams.path) {
            if (force || !this.imageParams.path.startsWith('@')) {
                await this.api.closeImage(this.imageParams.path)
            }
        }
    }

    private roiResizableMove(event: any) {
        const target = event.target

        const { scale } = this.panZoom!.getTransform()

        var x = parseFloat(target.getAttribute('data-x')) || 0
        var y = parseFloat(target.getAttribute('data-y')) || 0

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

    private loadImageFromParams(params: ImageParams) {
        this.imageParams = params

        if (params.source === 'FRAMING') {
            this.disableAutoStretch()
        }

        if (this.imageParams.path) {
            this.annotations = []
            this.loadImage()
        }
    }

    private async loadImage() {
        if (this.imageParams.path) {
            await this.loadImageFromPath(this.imageParams.path)
        } else if (this.imageParams.camera) {
            try {
                const savedImage = await this.api.latestImageOfCamera(this.imageParams.camera)
                await this.loadImageFromPath(savedImage.path)
            } catch (e) {
                console.error(e)
            }
        }

        if (this.imageParams.title) {
            this.title.setTitle(`Image ・ ${this.imageParams.title}`)
        } else if (this.imageParams.camera) {
            this.title.setTitle(`Image ・ ${this.imageParams.camera.name}`)
        } else if (this.imageParams.path) {
            this.title.setTitle(`Image ・ ${path.basename(this.imageParams.path)}`)
        }
    }

    private async loadImageFromPath(path: string) {
        const image = this.image.nativeElement
        const scnrEnabled = this.scnrChannel !== 'NONE'
        const { info, blob } = await this.api.openImage(path, this.debayer, this.autoStretch,
            this.stretchShadowhHighlight[0] / 65536, this.stretchShadowhHighlight[1] / 65536, this.stretchMidtone / 65536,
            this.mirrorHorizontal, this.mirrorVertical,
            this.invert, scnrEnabled, scnrEnabled ? this.scnrChannel : 'GREEN', this.scnrAmount, this.scnrProtectionMethod)

        this.imageInfo = info
        this.scnrMenuItem.disabled = info.mono

        if (info.rightAscension) this.solverCenterRA = info.rightAscension
        if (info.declination) this.solverCenterDEC = info.declination

        if (this.autoStretch) {
            this.stretchShadowhHighlight[0] = Math.trunc(info.stretchShadow * 65536)
            this.stretchShadowhHighlight[1] = Math.trunc(info.stretchHighlight * 65536)
            this.stretchMidtone = Math.trunc(info.stretchMidtone * 65536)
        }

        this.annotationMenuItem.disabled = !info.calibrated
        this.pointMountHereMenuItem.disabled = !info.calibrated
        this.fitsHeaders = info.headers

        if (this.imageURL) window.URL.revokeObjectURL(this.imageURL)
        this.imageURL = window.URL.createObjectURL(blob)
        image.src = this.imageURL
    }

    imageClicked(event: MouseEvent, menu: boolean) {
        this.imageMouseX = event.offsetX
        this.imageMouseY = event.offsetY

        if (menu) {
            this.menu.show(event)
        } else if (this.imageParams.camera) {
            const event: ImageStarSelected = { camera: this.imageParams.camera, x: this.imageMouseX, y: this.imageMouseY }
            this.electron.send('IMAGE_STAR_SELECTED', event)
        }
    }

    async annotateImage() {
        try {
            this.annotating = true
            this.annotations = await this.api.annotationsOfImage(this.imageParams.path!,
                this.annotateWithStars, this.annotateWithDSOs, this.annotateWithMinorPlanets, this.annotateWithMinorPlanetsMagLimit)
            this.showAnnotationDialog = false
        } finally {
            this.annotating = false
        }
    }

    showAnnotationInfo(annotation: ImageAnnotation) {
        this.annotationInfo = annotation.star ?? annotation.dso
        this.showAnnotationInfoDialog = true
    }

    private disableAutoStretch() {
        this.autoStretch = false
        this.checkMenuItem(this.menuItems[5], false)
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

    async solveImage() {
        this.solving = true

        try {
            this.solverCalibration = await this.api.solveImage(this.imageParams.path!, this.solverType, this.solverBlind,
                this.solverCenterRA, this.solverCenterDEC, this.solverRadius, this.solverDownsampleFactor,
                this.solverPathOrUrl, this.solverApiKey)

            this.preference.set('image.solver.pathOrUrl', this.solverPathOrUrl)
            this.preference.set('image.solver.radius', this.solverRadius)
            this.preference.set('image.solver.downsampleFactor', this.solverDownsampleFactor)

            this.solved = true
            this.annotationMenuItem.disabled = false
            this.pointMountHereMenuItem.disabled = false
        } catch {
            this.solved = false
            this.solverCalibration = undefined
            this.annotationMenuItem.disabled = true
            this.pointMountHereMenuItem.disabled = true
        } finally {
            this.solving = false
        }
    }

    mountSync(coordinate: EquatorialCoordinate) {
        const mount = this.electron.selectedMount()
        if (!mount?.connected) return
        this.api.mountSync(mount, coordinate.rightAscension, coordinate.declination, true)
    }

    mountGoTo(coordinate: EquatorialCoordinate) {
        const mount = this.electron.selectedMount()
        if (!mount?.connected) return
        this.api.mountGoTo(mount, coordinate.rightAscension, coordinate.declination, true)
    }

    mountSlew(coordinate: EquatorialCoordinate) {
        const mount = this.electron.selectedMount()
        if (!mount?.connected) return
        this.api.mountSlewTo(mount, coordinate.rightAscension, coordinate.declination, true)
    }

    frame(coordinate: EquatorialCoordinate) {
        this.browserWindow.openFraming({ rightAscension: coordinate.rightAscension, declination: coordinate.declination })
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

    private checkMenuItem(item?: MenuItem | MegaMenuItem, checked: boolean = true) {
        item && (item.styleClass = checked ? 'p-menuitem-checked' : '')
    }
}
