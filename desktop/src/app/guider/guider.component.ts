import { AfterViewInit, Component, HostListener, NgZone, OnDestroy, ViewChild } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ChartData, ChartOptions } from 'chart.js'
import { UIChart } from 'primeng/chart'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { GuideDirection, GuideOutput, GuideState, GuideStep, Guider, GuiderHistoryStep, GuiderPlotMode, GuiderYAxisUnit } from '../../shared/types/guider.types'

export interface GuiderPreference {
    settleAmount?: number
    settleTime?: number
    settleTimeout?: number
}

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

    connected = false
    host = 'localhost'
    port = 4400
    guideState: GuideState = 'STOPPED'
    guideStep?: GuideStep
    message = ''

    settleAmount = 1.5
    settleTime = 10
    settleTimeout = 30
    readonly phdGuideHistory: GuiderHistoryStep[] = []
    private phdDurationScale = 1.0

    pixelScale = 1.0
    rmsRA = 0.0
    rmsDEC = 0.0
    rmsTotal = 0.0

    readonly plotModes: GuiderPlotMode[] = ['RA/DEC', 'DX/DY']
    plotMode = this.plotModes[0]

    readonly yAxisUnits: GuiderYAxisUnit[] = ['ARCSEC', 'PIXEL']
    yAxisUnit = this.yAxisUnits[0]

    @ViewChild('chart')
    private readonly chart!: UIChart

    get stopped() {
        return this.guideState === 'STOPPED'
    }

    get looping() {
        return this.guideState === 'LOOPING'
    }

    get guiding() {
        return this.guideState === 'GUIDING'
    }

    readonly chartData: ChartData = {
        labels: Array.from({ length: 100 }),
        datasets: [
            // RA.
            {
                type: 'line',
                fill: false,
                borderColor: '#F44336',
                borderWidth: 2,
                data: [],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // DEC.
            {
                type: 'line',
                fill: false,
                borderColor: '#03A9F4',
                borderWidth: 2,
                data: [],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // RA.
            {
                type: 'bar',
                backgroundColor: '#F4433630',
                data: [],
            },
            // DEC.
            {
                type: 'bar',
                backgroundColor: '#03A9F430',
                data: [],
            },
        ]
    }

    readonly chartOptions: ChartOptions = {
        responsive: true,
        plugins: {
            legend: {
                display: false,
            },
            tooltip: {
                displayColors: false,
                intersect: false,
                filter: (item) => {
                    return Math.abs(item.parsed.y) - 0.01 > 0.0
                },
                callbacks: {
                    title: () => {
                        return ''
                    },
                    label: (context) => {
                        const barType = context.dataset.type === 'bar'
                        const raType = context.datasetIndex === 0 || context.datasetIndex === 2
                        const scale = barType ? this.phdDurationScale : 1.0
                        const y = context.parsed.y * scale
                        const prefix = raType ? 'RA: ' : 'DEC: '
                        const barSuffix = ' ms'
                        const lineSuffix = this.yAxisUnit === 'ARCSEC' ? '"' : 'px'
                        const formattedY = prefix + (barType ? y.toFixed(0) + barSuffix : y.toFixed(2) + lineSuffix)
                        return formattedY
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
                stacked: false,
                beginAtZero: false,
                min: -16,
                max: 16,
                ticks: {
                    autoSkip: true,
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
                stacked: false,
                min: 0,
                max: 100,
                border: {
                    display: true,
                    dash: [2, 4],
                },
                ticks: {
                    autoSkip: true,
                    count: 11,
                    maxRotation: 0,
                    minRotation: 0,
                    callback: (value) => {
                        const a = value as number
                        return (a - Math.trunc(a) > 0) ? undefined : a.toFixed(0)
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
        electron: ElectronService,
        ngZone: NgZone,
    ) {
        title.setTitle('Guider')

        electron.on('GUIDE_OUTPUT.UPDATED', event => {
            if (event.device.id === this.guideOutput?.id) {
                ngZone.run(() => {
                    Object.assign(this.guideOutput!, event.device)
                    this.update()
                })
            }
        })

        electron.on('GUIDE_OUTPUT.ATTACHED', event => {
            ngZone.run(() => {
                this.guideOutputs.push(event.device)
            })
        })

        electron.on('GUIDE_OUTPUT.DETACHED', event => {
            ngZone.run(() => {
                const index = this.guideOutputs.findIndex(e => e.id === event.device.id)
                if (index >= 0) this.guideOutputs.splice(index, 1)
            })
        })

        electron.on('GUIDER.CONNECTED', () => {
            ngZone.run(() => {
                this.connected = true
            })
        })

        electron.on('GUIDER.DISCONNECTED', () => {
            ngZone.run(() => {
                this.connected = false
            })
        })

        electron.on('GUIDER.UPDATED', event => {
            ngZone.run(() => {
                this.processGuiderStatus(event.data)
            })
        })

        electron.on('GUIDER.STEPPED', event => {
            ngZone.run(() => {
                if (this.phdGuideHistory.length >= 100) {
                    this.phdGuideHistory.splice(0, this.phdGuideHistory.length - 99)
                }

                this.phdGuideHistory.push(event.data)
                this.updateGuideHistoryChart()

                if (event.data.guideStep) {
                    this.guideStep = event.data.guideStep
                } else {
                    // Dithering.
                }
            })
        })

        electron.on('GUIDER.MESSAGE_RECEIVED', event => {
            ngZone.run(() => {
                this.message = event.data
            })
        })
    }

    async ngAfterViewInit() {
        const settle = await this.api.getGuidingSettle()

        this.settleAmount = settle.amount ?? 1.5
        this.settleTime = settle.time ?? 10
        this.settleTimeout = settle.timeout ?? 30

        this.guideOutputs = await this.api.guideOutputs()

        const status = await this.api.guidingStatus()
        this.processGuiderStatus(status)

        const history = await this.api.guidingHistory()
        this.phdGuideHistory.push(...history)
        this.updateGuideHistoryChart()
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    private processGuiderStatus(event: Guider) {
        this.connected = event.connected
        this.guideState = event.state
        this.pixelScale = event.pixelScale
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
            this.rmsTotal = history.rmsTotal
            this.rmsDEC = history.rmsDEC
            this.rmsRA = history.rmsRA
        } else {
            return
        }

        const startId = this.phdGuideHistory[0].id
        const guideSteps = this.phdGuideHistory.filter(e => e.guideStep)
        const scale = this.yAxisUnit === 'ARCSEC' ? this.pixelScale : 1.0

        let maxDuration = 0

        for (const step of guideSteps) {
            maxDuration = Math.max(maxDuration, Math.abs(step.guideStep!.raDuration))
            maxDuration = Math.max(maxDuration, Math.abs(step.guideStep!.decDuration))
        }

        this.phdDurationScale = maxDuration / 16.0

        if (this.plotMode === 'RA/DEC') {
            this.chartData.datasets[0].data = guideSteps
                .map(e => [e.id - startId, -e.guideStep!.raDistance * scale])
            this.chartData.datasets[1].data = guideSteps
                .map(e => [e.id - startId, e.guideStep!.decDistance * scale])
        } else {
            this.chartData.datasets[0].data = guideSteps
                .map(e => [e.id - startId, -e.guideStep!.dx * scale])
            this.chartData.datasets[1].data = guideSteps
                .map(e => [e.id - startId, e.guideStep!.dy * scale])
        }

        const durationScale = (direction?: GuideDirection) => {
            return !direction || direction === 'NORTH' || direction === 'WEST' ? this.phdDurationScale : -this.phdDurationScale
        }

        this.chartData.datasets[2].data = this.phdGuideHistory
            .map(e => (e.guideStep?.raDuration ?? 0) / durationScale(e.guideStep?.raDirection))
        this.chartData.datasets[3].data = this.phdGuideHistory
            .map(e => (e.guideStep?.decDuration ?? 0) / durationScale(e.guideStep?.decDirection))

        this.chart?.refresh()
    }

    async guideOutputChanged() {
        if (this.guideOutput?.id) {
            const guideOutput = await this.api.guideOutput(this.guideOutput.id)
            Object.assign(this.guideOutput, guideOutput)

            this.update()
        }
    }

    guidePulseStart(...directions: GuideDirection[]) {
        for (const direction of directions) {
            switch (direction) {
                case 'NORTH':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideNorthDuration * 1000)
                    break
                case 'SOUTH':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideSouthDuration * 1000)
                    break
                case 'WEST':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideWestDuration * 1000)
                    break
                case 'EAST':
                    this.api.guideOutputPulse(this.guideOutput!, direction, this.guideEastDuration * 1000)
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
        if (this.connected) {
            this.api.guidingDisconnect()
        } else {
            this.api.guidingConnect(this.host, this.port)
        }
    }

    async guidingStart(event: MouseEvent) {
        await this.api.guidingLoop(true)
        await this.api.guidingStart(event.shiftKey)
    }

    async settleChanged() {
        await this.api.setGuidingSettle({ amount: this.settleAmount, time: this.settleTime, timeout: this.settleTimeout })
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