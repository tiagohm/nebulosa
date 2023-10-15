import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ChartData, ChartOptions } from 'chart.js'
import { UIChart } from 'primeng/chart'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { GuideDirection, GuideOutput, GuideStar, GuideState, GuideStep, GuiderStatus, HistoryStep } from '../../shared/types'

export type PlotMode = 'RA/DEC' | 'DX/DY'

export type YAxisUnit = 'ARCSEC' | 'PIXEL'

@Component({
    selector: 'app-guider',
    templateUrl: './guider.component.html',
    styleUrls: ['./guider.component.scss'],
})
export class GuiderComponent implements AfterViewInit, OnDestroy {

    guideOutputs: GuideOutput[] = []
    guideOutput?: GuideOutput
    guideOutputConnected = false
    pulseGuiding = false

    guideNorthDuration = 1000
    guideSouthDuration = 1000
    guideWestDuration = 1000
    guideEastDuration = 1000

    phdConnected = false
    phdHost = 'localhost'
    phdPort = 4400
    phdState: GuideState = 'STOPPED'
    phdGuideStep?: GuideStep
    phdMessage = ''

    phdDitherPixels = 5
    phdDitherRAOnly = false
    phdSettleAmount = 1.5
    phdSettleTime = 10
    phdSettleTimeout = 30
    readonly phdGuideHistory: HistoryStep[] = []

    phdPixelScale = 1.0
    phdRmsRA = 0.0
    phdRmsDEC = 0.0
    phdRmsTotal = 0.0

    readonly plotModes: PlotMode[] = ['RA/DEC', 'DX/DY']
    plotMode: PlotMode = 'RA/DEC'
    readonly yAxisUnits: YAxisUnit[] = ['ARCSEC', 'PIXEL']
    yAxisUnit: YAxisUnit = 'ARCSEC'

    @ViewChild('phdChart')
    private readonly phdChart!: UIChart

    get stopped() {
        return this.phdState === 'STOPPED'
    }

    get looping() {
        return this.phdState === 'LOOPING'
    }

    get guiding() {
        return this.phdState === 'GUIDING'
    }

    readonly phdChartData: ChartData = {
        datasets: [
            // RA.
            {
                type: 'line',
                fill: false,
                borderColor: 'red',
                borderWidth: 0.5,
                data: [],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // DEC.
            {
                type: 'line',
                fill: false,
                borderColor: 'blue',
                borderWidth: 0.5,
                data: [],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // RA.
            {
                type: 'bar',
                backgroundColor: 'green',
                data: [],
            },
            // DEC.
            {
                type: 'bar',
                backgroundColor: 'green',
                data: [],
            },
        ]
    }

    readonly phdChartOptions: ChartOptions = {
        responsive: true,
        aspectRatio: 1.8,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: false,
            },
            tooltip: {
                displayColors: false,
                intersect: false,
                callbacks: {
                    title: () => {
                        return ''
                    },
                    label: (context) => {
                        return context.parsed.y.toFixed(2)
                    }
                }
            },
            zoom: {
                zoom: {
                    wheel: {
                        enabled: true,
                    },
                    pinch: {
                        enabled: false,
                    },
                    mode: 'x',
                    scaleMode: 'xy',
                },
                pan: {
                    enabled: true,
                    mode: 'xy',
                },
                limits: {
                    x: {
                        min: 0,
                        max: 100,
                    },
                    y: {
                        min: -16,
                        max: 16,
                    },
                }
            },
        },
        scales: {
            y: {
                stacked: true,
                beginAtZero: false,
                suggestedMin: -16,
                suggestedMax: 16,
                ticks: {
                    autoSkip: false,
                    count: 7,
                    callback: (value) => {
                        return (value as number).toFixed(1).padStart(5, ' ')
                    }
                },
                border: {
                    display: true,
                    dash: [2, 4],
                },
                grid: {
                    display: true,
                    drawTicks: false,
                    drawOnChartArea: true,
                    color: '#212121',
                }
            },
            x: {
                stacked: true,
                type: 'linear',
                min: 0,
                max: 100,
                border: {
                    display: true,
                    dash: [2, 4],
                },
                ticks: {
                    stepSize: 5.0,
                    maxRotation: 0,
                    minRotation: 0,
                    callback: (value) => {
                        return (value as number).toFixed(0)
                    }
                },
                grid: {
                    display: true,
                    drawTicks: false,
                    color: '#212121',
                }
            }
        }
    }

    constructor(
        title: Title,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private preference: PreferenceService,
        ngZone: NgZone,
    ) {
        title.setTitle('Guider')

        api.startListening('GUIDING')

        electron.on('GUIDE_OUTPUT_UPDATED', (_, event: GuideOutput) => {
            if (event.name === this.guideOutput?.name) {
                ngZone.run(() => {
                    Object.assign(this.guideOutput!, event)
                    this.update()
                })
            }
        })

        electron.on('GUIDE_OUTPUT_ATTACHED', (_, event: GuideOutput) => {
            ngZone.run(() => {
                this.guideOutputs.push(event)
            })
        })

        electron.on('GUIDE_OUTPUT_DETACHED', (_, event: GuideOutput) => {
            ngZone.run(() => {
                const index = this.guideOutputs.findIndex(e => e.name === event.name)
                if (index) this.guideOutputs.splice(index, 1)
            })
        })

        electron.on('GUIDER_CONNECTED', () => {
            ngZone.run(() => {
                this.phdConnected = true
            })
        })

        electron.on('GUIDER_DISCONNECTED', () => {
            ngZone.run(() => {
                this.phdConnected = false
            })
        })

        electron.on('GUIDER_UPDATED', (_, event: GuiderStatus) => {
            ngZone.run(() => {
                this.processGuiderStatus(event)
            })
        })

        electron.on('GUIDER_STEPPED', (_, event: HistoryStep | GuideStar) => {
            ngZone.run(() => {
                if ("id" in event) {
                    if (this.phdGuideHistory.length >= 100) {
                        this.phdGuideHistory.splice(0, 1)
                    }

                    this.phdGuideHistory.push(event)
                    this.updateGuideHistoryChart()
                }

                if ("guideStep" in event && event.guideStep) {
                    this.phdGuideStep = event.guideStep
                }
            })
        })

        electron.on('GUIDER_MESSAGE_RECEIVED', (_, event: { message: string }) => {
            ngZone.run(() => {
                this.phdMessage = event.message
            })
        })
    }

    async ngAfterViewInit() {
        this.phdSettleAmount = this.preference.get('guiding.settleAmount', 1.5)
        this.phdSettleTime = this.preference.get('guiding.settleTime', 10)
        this.phdSettleTimeout = this.preference.get('guiding.settleTimeout', 30)

        this.guideOutputs = await this.api.guideOutputs()

        const status = await this.api.guidingStatus()
        this.processGuiderStatus(status)

        const history = await this.api.guidingHistory()
        this.phdGuideHistory.push(...history)
        this.updateGuideHistoryChart()
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.api.stopListening('GUIDING')
    }

    private processGuiderStatus(event: GuiderStatus) {
        this.phdConnected = event.connected
        this.phdState = event.state
        this.phdPixelScale = event.pixelScale
    }

    plotModeChanged() {
        this.updateGuideHistoryChart()
    }

    yAxisUnitChanged() {
        this.updateGuideHistoryChart()
    }

    private updateGuideHistoryChart() {
        if (this.phdGuideHistory.length > 0) {
            const history = this.phdGuideHistory[this.phdGuideHistory.length - 1]
            this.phdRmsTotal = history.rmsTotal
            this.phdRmsDEC = history.rmsDEC
            this.phdRmsRA = history.rmsRA
        } else {
            return
        }

        const startId = this.phdGuideHistory[0].id
        const guideSteps = this.phdGuideHistory.filter(e => e.guideStep)
        const scale = this.yAxisUnit === 'ARCSEC' ? this.phdPixelScale : 1.0

        let maxDuration = 0

        for (const step of guideSteps) {
            maxDuration = Math.max(maxDuration, Math.abs(step.guideStep!.raDuration))
            maxDuration = Math.max(maxDuration, Math.abs(step.guideStep!.decDuration))
        }

        const durationScale = maxDuration / 16.0

        if (this.plotMode === 'RA/DEC') {
            this.phdChartData.datasets[0].data = guideSteps
                .map(e => [e.id - startId, e.guideStep!.raDistance * scale])
            this.phdChartData.datasets[1].data = guideSteps
                .map(e => [e.id - startId, e.guideStep!.decDistance * scale])
        } else {
            this.phdChartData.datasets[0].data = guideSteps
                .map(e => [e.id - startId, e.guideStep!.dx * scale])
            this.phdChartData.datasets[1].data = guideSteps
                .map(e => [e.id - startId, e.guideStep!.dy * scale])
        }

        this.phdChartData.datasets[2].data = this.phdGuideHistory
            // .map(e => (e.guideStep?.raDuration ?? 0) / durationScale)
            .map(e => 12)
        this.phdChartData.datasets[3].data = this.phdGuideHistory
            // .map(e => (e.guideStep?.decDuration ?? 0) / durationScale)
            .map(e => 8)

        this.phdChart?.refresh()
    }

    async guideOutputChanged() {
        if (this.guideOutput) {
            const guideOutput = await this.api.guideOutput(this.guideOutput.name)
            Object.assign(this.guideOutput, guideOutput)

            this.update()
        }

        this.electron.send('GUIDE_OUTPUT_CHANGED', this.guideOutput)
    }

    connectGuideOutput() {
        if (this.guideOutputConnected) {
            this.api.guideOutputDisconnect(this.guideOutput!)
        } else {
            this.api.guideOutputConnect(this.guideOutput!)
        }
    }

    guidePulseStart(...directions: GuideDirection[]) {
        for (const direction of directions) {
            switch (direction) {
                case 'NORTH':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideNorthDuration)
                    break
                case 'SOUTH':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideSouthDuration)
                    break
                case 'WEST':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideWestDuration)
                    break
                case 'EAST':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideEastDuration)
                    break
            }
        }
    }

    guidePulseStop() {
        this.api.guideOutputPulse(this.guideOutput!, 'NORTH', 0)
        this.api.guideOutputPulse(this.guideOutput!, 'SOUTH', 0)
        this.api.guideOutputPulse(this.guideOutput!, 'WEST', 0)
        this.api.guideOutputPulse(this.guideOutput!, 'EAST', 0)
    }

    guidingConnect() {
        if (this.phdConnected) {
            this.api.guidingDisconnect()
        } else {
            this.api.guidingConnect(this.phdHost, this.phdPort)
        }
    }

    async guidingStart(event: MouseEvent) {
        await this.api.guidingLoop(true)
        await this.api.guidingStart(event.shiftKey)
    }

    async guidingSettleChanged() {
        await this.api.guidingSettle(this.phdSettleAmount, this.phdSettleTime, this.phdSettleTimeout)
        this.preference.set('guiding.settleAmount', this.phdSettleAmount)
        this.preference.set('guiding.settleTime', this.phdSettleTime)
        this.preference.set('guiding.settleTimeout', this.phdSettleTimeout)
    }

    guidingClearHistory() {
        this.phdGuideHistory.length = 0
        this.api.guidingClearHistory()
    }

    guidingStop() {
        this.api.guidingStop()
    }

    private update() {
        if (this.guideOutput) {
            this.guideOutputConnected = this.guideOutput.connected
            this.pulseGuiding = this.guideOutput.pulseGuiding
        }
    }
}