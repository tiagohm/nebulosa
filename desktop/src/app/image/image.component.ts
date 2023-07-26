import { AfterViewInit, Component, ElementRef, HostListener, NgZone, OnDestroy, OnInit, ViewChild } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import createPanZoom, { PanZoom } from 'panzoom'
import * as path from 'path'
import { MenuItem, MenuItemCommandEvent } from 'primeng/api'
import { ContextMenu } from 'primeng/contextmenu'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Calibration, Camera, FITSHeaderItem, ImageAnnotation, ImageChannel, ImageSource, PlateSolverType, SCNRProtectionMethod, SavedCameraImage } from '../../shared/types'

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
export class ImageComponent implements OnInit, AfterViewInit, OnDestroy {

    @ViewChild('image')
    private readonly image!: ElementRef<HTMLImageElement>

    @ViewChild('menu')
    private readonly menu!: ContextMenu

    debayer = false
    mirrorHorizontal = false
    mirrorVertical = false
    invert = false

    readonly scnrChannelOptions: ImageChannel[] = ['NONE', 'RED', 'GREEN', 'BLUE']
    readonly scnrProtectionModeOptions: SCNRProtectionMethod[] = ['MAXIMUM_MASK',
        'ADDTIVIVE_MASK',
        'AVERAGE_NEUTRAL',
        'MAXIMUM_NEUTRAL',
        'MINIMUM_NEUTRAL'
    ]

    showSCNRDialog = false
    scnrChannel: ImageChannel = 'NONE'
    scnrAmount = 0.5
    scnrProtectionMode: SCNRProtectionMethod = 'AVERAGE_NEUTRAL'

    showAnnotationDialog = false
    annotateWithStars = true
    annotateWithDSOs = true
    annotateWithMinorPlanets = false

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

    showFITSHeadersDialog = false
    fitsHeaders: FITSHeaderItem[] = []

    private panZoom?: PanZoom
    private imageURL!: string
    private imageInfo?: SavedCameraImage
    private imageMouseX = 0
    private imageMouseY = 0
    private imageParams: ImageParams = {}

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
        icon: 'mdi mdi-target',
        disabled: true,
        command: (e) => {

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
                const path = await this.electron.ipcRenderer.sendSync('save-fits-as')
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
                this.toggleMenuItemChecked(e)

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
                this.toggleMenuItemChecked(e)
                this.loadImage()
            },
        },
        {
            label: 'Vertical mirror',
            icon: 'mdi mdi-flip-vertical',
            command: (e) => {
                this.mirrorVertical = !this.mirrorVertical
                this.toggleMenuItemChecked(e)
                this.loadImage()
            },
        },
        {
            label: 'Invert',
            icon: 'mdi mdi-invert-colors',
            command: (e) => {
                this.invert = !this.invert
                this.toggleMenuItemChecked(e)
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
                        this.toggleMenuItemChecked(e)
                    },
                },
                this.annotationMenuItem,
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
        ngZone: NgZone,
    ) {
        title.setTitle('Image')

        electron.ipcRenderer.on('CAMERA_IMAGE_SAVED', async (_, data: SavedCameraImage) => {
            if (data.camera === this.imageParams.camera?.name) {
                if (this.imageParams.path) {
                    await this.api.closeImage(this.imageParams.path)
                }

                ngZone.run(() => {
                    this.annotations = []
                    this.imageParams.path = data.path
                    this.loadImage()
                })
            }
        })

        electron.ipcRenderer.on('PARAMS_CHANGED', (_, data: ImageParams) => {
            this.loadImageFromParams(data)
        })
    }

    ngOnInit() {
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
        if (this.imageParams.path) {
            this.api.closeImage(this.imageParams.path)
        }
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
                this.imageInfo = await this.api.latestImageOfCamera(this.imageParams.camera)
                await this.loadImageFromPath(this.imageInfo.path)
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
            this.invert, scnrEnabled, scnrEnabled ? this.scnrChannel : 'GREEN', this.scnrAmount, this.scnrProtectionMode)

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
        this.fitsHeaders = info.headers

        if (this.imageURL) window.URL.revokeObjectURL(this.imageURL)
        this.imageURL = window.URL.createObjectURL(blob)
        image.src = this.imageURL
    }

    imageClicked(event: MouseEvent) {
        this.imageMouseX = event.offsetX
        this.imageMouseY = event.offsetY

        this.menu.show(event)
    }

    async annotateImage() {
        this.annotations = await this.api.annotationsOfImage(this.imageParams.path!, this.annotateWithStars, this.annotateWithDSOs, this.annotateWithMinorPlanets)
        this.showAnnotationDialog = false
    }

    private disableAutoStretch() {
        this.autoStretch = false
        document.getElementById('auto-stretch-menuitem')!.parentElement!.classList.remove('p-menuitem-checked')
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
        } catch {
            this.solved = false
            this.solverCalibration = undefined
            this.annotationMenuItem.disabled = true
        } finally {
            this.solving = false
        }
    }

    frameSolvedPosition() {
        this.browserWindow.openFraming({ rightAscension: this.solverCalibration!.rightAscension, declination: this.solverCalibration!.declination })
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
                    return e.target !== this.image.nativeElement
                },
                beforeMouseDown: (e) => {
                    return e.target !== this.image.nativeElement
                },
            })
        }
    }

    private toggleMenuItemChecked(event: MenuItemCommandEvent) {
        const menuItem = (event.originalEvent!.target as HTMLElement).closest(".p-menuitem") as HTMLElement
        menuItem.classList.toggle('p-menuitem-checked')
    }
}
