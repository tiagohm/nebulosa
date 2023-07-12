import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ChartData, ChartOptions } from 'chart.js'
import { UIChart } from 'primeng/chart'
import { ListboxChangeEvent } from 'primeng/listbox'
import { ApiService } from '../../shared/services/api.service'
import * as moment from 'moment'
import { BodyPosition, DeepSkyObject, Location, MinorPlanet, Star } from '../../shared/types'

export interface PlanetItem {
    name: string
    type: string
    code: string
}

@Component({
    selector: 'app-atlas',
    templateUrl: './atlas.component.html',
    styleUrls: ['./atlas.component.scss']
})
export class AtlasComponent implements OnInit, OnDestroy {

    tab = 0
    refreshing = false

    bodyPosition: BodyPosition = {
        rightAscensionJ2000: '00h00m00s',
        declinationJ2000: `00°00'00"`,
        rightAscension: '00h00m00s',
        declination: `+00°00'00"`,
        azimuth: `000°00'00"`,
        altitude: `+00°00'00"`,
        magnitude: 0,
        constellation: 'AND',
        distance: 0,
        distanceUnit: 'ly',
        illuminated: 0,
        elongation: 0,
    }

    locations: Location[] = []
    private readonly emptyLocation: Location = { id: 0, name: '', latitude: 0, longitude: 0, elevation: 0, offsetInMinutes: 0 }
    location: Location = { ...this.emptyLocation }
    editedLocation: Location = { ...this.emptyLocation }
    showLocationDialog = false
    useManualDateTime = false
    dateTime = new Date()

    planet?: PlanetItem
    readonly planets: PlanetItem[] = [
        { name: 'Mercury', type: `Planet`, code: '199' },
        { name: 'Venus', type: `Planet`, code: '299' },
        { name: 'Mars', type: `Planet`, code: '499' },
        { name: 'Jupiter', type: `Planet`, code: '599' },
        { name: 'Saturn', type: `Planet`, code: '699' },
        { name: 'Uranus', type: `Planet`, code: '799' },
        { name: 'Neptune', type: `Planet`, code: '899' },
        { name: 'Pluto', type: `Dwarf Planet`, code: '999' },
        { name: 'Phobos', type: `Mars' Satellite`, code: '401' },
        { name: 'Deimos', type: `Mars' Satellite`, code: '402' },
        { name: 'Io', type: `Jupiter's Satellite`, code: '501' },
        { name: 'Europa', type: `Jupiter's Satellite`, code: '402' },
        { name: 'Ganymede', type: `Jupiter's Satellite`, code: '403' },
        { name: 'Callisto', type: `Jupiter's Satellite`, code: '504' },
        { name: 'Mimas', type: `Saturn's Satellite`, code: '601' },
        { name: 'Enceladus', type: `Saturn's Satellite`, code: '602' },
        { name: 'Tethys', type: `Saturn's Satellite`, code: '603' },
        { name: 'Dione', type: `Saturn's Satellite`, code: '604' },
        { name: 'Rhea', type: `Saturn's Satellite`, code: '605' },
        { name: 'Titan', type: `Saturn's Satellite`, code: '606' },
        { name: 'Hyperion', type: `Saturn's Satellite`, code: '607' },
        { name: 'Iapetus', type: `Saturn's Satellite`, code: '608' },
        { name: 'Ariel', type: `Uranus' Satellite`, code: '701' },
        { name: 'Umbriel', type: `Uranus' Satellite`, code: '702' },
        { name: 'Titania', type: `Uranus' Satellite`, code: '703' },
        { name: 'Oberon', type: `Uranus' Satellite`, code: '704' },
        { name: 'Miranda', type: `Uranus' Satellite`, code: '705' },
        { name: 'Triton', type: `Neptune's Satellite`, code: '801' },
        { name: 'Charon', type: `Pluto's Satellite`, code: '901' },
        { name: '1 Ceres', type: `Dwarf Planet`, code: '1;' },
        { name: '90377 Sedna', type: `Dwarf Planet`, code: '90377;' },
        { name: '136199 Eris', type: `Dwarf Planet`, code: '136199;' },
        { name: '2 Pallas', type: `Asteroid`, code: '2;' },
        { name: '3 Juno', type: `Asteroid`, code: '3;' },
        { name: '4 Vesta', type: `Asteroid`, code: '4;' },
    ]

    minorPlanet?: MinorPlanet
    minorPlanetSearchText = ''
    minorPlanetChoiceItems: { name: string, pdes: string }[] = []
    showMinorPlanetChoiceDialog = false

    star?: Star
    starItems: Star[] = []
    starSearchText = ''

    dso?: DeepSkyObject
    dsoItems: DeepSkyObject[] = []
    dsoSearchText = ''

    name = 'Sun'
    tags: { title: string, severity: string }[] = []

    @ViewChild('imageOfSun')
    private readonly imageOfSun!: ElementRef<HTMLImageElement>

    @ViewChild('imageOfMoon')
    private readonly imageOfMoon!: ElementRef<HTMLImageElement>

    @ViewChild('chart')
    private readonly chart!: UIChart

    readonly altitudeData: ChartData = {
        labels: [
            '12h', '13h', '14h', '15h', '16h', '17h', '18h', '19h', '20h', '21h', '22h', '23h',
            '0h', '1h', '2h', '3h', '4h', '5h', '6h', '7h', '8h', '9h', '10h', '11h', '12h',
        ],
        datasets: [
            // Day.
            {
                type: 'line',
                fill: true,
                backgroundColor: '#FFFF0040',
                data: [
                    [0, 90],
                    [5.4, 90],
                ],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // Civil Dusk.
            {
                type: 'line',
                fill: true,
                backgroundColor: '#FF6F0040',
                data: [
                    [5.4, 90],
                    [5.9, 90],
                ],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // Nautical Dusk.
            {
                type: 'line',
                fill: true,
                backgroundColor: '#AB47BC40',
                data: [
                    [5.9, 90],
                    [6.4, 90],
                ],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // Astronomical Dusk.
            {
                type: 'line',
                fill: true,
                backgroundColor: '#5E35B140',
                data: [
                    [6.4, 90],
                    [6.8, 90],
                ],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // Night.
            {
                type: 'line',
                fill: true,
                backgroundColor: '#1A237E40',
                data: [
                    [6.8, 90],
                    [17.4, 90],
                ],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // Astronomical Dawn.
            {
                type: 'line',
                fill: true,
                backgroundColor: '#5E35B140',
                data: [
                    [17.4, 90],
                    [17.8, 90],
                ],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // Nautical Dawn.
            {
                type: 'line',
                fill: true,
                backgroundColor: '#AB47BC40',
                data: [
                    [17.8, 90],
                    [18.3, 90],
                ],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // Civil Dawn.
            {
                type: 'line',
                fill: true,
                backgroundColor: '#FF6F0040',
                data: [
                    [18.3, 90],
                    [18.7, 90],
                ],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // Day.
            {
                type: 'line',
                fill: true,
                backgroundColor: '#FFFF0040',
                data: [
                    [18.7, 90],
                    [24.0, 90],
                ],
                pointRadius: 0,
                pointHitRadius: 0,
            },
            // Altitude.
            {
                type: 'line',
                fill: false,
                borderColor: '#1976D2',
                data: [],
                cubicInterpolationMode: 'monotone',
                pointRadius: 0,
            }]
    }

    readonly altitudeOptions: ChartOptions = {
        responsive: true,
        aspectRatio: 1.8,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: false,
            },
            tooltip: {
                displayColors: false,
                callbacks: {
                    title: function () {
                        return ''
                    },
                    label: function (context) {
                        const hours = (context.parsed.x + 12) % 24
                        const minutes = (hours - Math.trunc(hours)) * 60
                        const a = `${Math.trunc(hours)}`.padStart(2, '0')
                        const b = `${Math.trunc(minutes)}`.padStart(2, '0')
                        return `${a}:${b} ・ ${context.parsed.y.toFixed(2)}°`
                    }
                }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                suggestedMin: 0,
                suggestedMax: 90,
                ticks: {
                    autoSkip: false,
                    count: 10,
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
                type: 'linear',
                min: 0,
                max: 24.0,
                border: {
                    display: true,
                    dash: [2, 4],
                },
                ticks: {
                    stepSize: 1.0,
                    maxRotation: 0,
                    minRotation: 0,
                    callback: function (value, index, ticks) {
                        return `${(index + 12) % 24}h`
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

    private twilightDate = ''

    constructor(
        private title: Title,
        private api: ApiService,
    ) {
        title.setTitle('Sky Atlas')

        // TODO: Refresh graph and twilight if hours past 12 (noon)

        setInterval(() => this.refreshTab(), 60000)
    }

    async ngOnInit() {
        this.locations = await this.api.locations()
    }

    ngOnDestroy() { }

    tabChanged() {
        this.refreshTab(false, true)
    }

    planetChanged() {
        this.refreshTab(false, true)
    }

    async searchMinorPlanet() {
        const minorPlanet = await this.api.searchMinorPlanet(this.minorPlanetSearchText)

        if (minorPlanet.found) {
            this.minorPlanet = minorPlanet
            this.refreshTab(false, true)
        } else {
            this.minorPlanetChoiceItems = minorPlanet.searchItems
            this.showMinorPlanetChoiceDialog = true
        }
    }

    minorPlanetChoosen(event: ListboxChangeEvent) {
        this.minorPlanetSearchText = event.value.pdes
        this.searchMinorPlanet()
        this.showMinorPlanetChoiceDialog = false
    }

    starChanged() {
        this.refreshTab(false, true)
    }

    dsoChanged() {
        this.refreshTab(false, true)
    }

    locationChanged() {
        this.refreshTab(true, true)
    }

    async searchStar() {
        this.starItems = await this.api.searchStar(this.starSearchText)
    }

    async searchDSO() {
        this.dsoItems = await this.api.searchDSO(this.dsoSearchText)
    }

    addLocation() {
        this.editedLocation = { ...this.emptyLocation }
        this.showLocationDialog = true
    }

    editLocation() {
        this.editedLocation = { ...this.location }
        this.showLocationDialog = true
    }

    async deleteLocation() {
        await this.api.deleteLocation(this.location)
        this.locations = await this.api.locations()
    }

    async saveLocation() {
        await this.api.saveLocation(this.editedLocation)
        this.locations = await this.api.locations()
        this.showLocationDialog = false
        this.refreshTab(true, true)
    }

    async refreshTab(
        refreshTwilight: boolean = false,
        refreshChart: boolean = false,
    ) {
        this.refreshing = true

        if (!this.useManualDateTime) {
            this.dateTime = new Date()
        }

        const date = moment(this.dateTime).format('YYYY-MM-DD')
        const time = moment(this.dateTime).format('HH:mm')
        this.title.setTitle(`Sky Atlas ・ ${date} ${time}`)

        try {
            // Sun.
            if (this.tab === 0) {
                this.name = 'Sun'
                this.tags = []
                this.imageOfSun.nativeElement.src = `${this.api.baseUri}/imageOfSun`
                this.bodyPosition = await this.api.positionOfSun(this.location!, this.dateTime)
            }
            // Moon.
            else if (this.tab === 1) {
                this.name = 'Moon'
                this.tags = []
                this.imageOfMoon.nativeElement.src = `${this.api.baseUri}/imageOfMoon?location=${this.location!.id}&date=${date}&time=${time}`
                this.bodyPosition = await this.api.positionOfMoon(this.location!, this.dateTime)
            }
            // Planet.
            else if (this.tab === 2 && this.planet) {
                this.name = this.planet.name
                this.tags = []
                this.bodyPosition = await this.api.positionOfPlanet(this.location!, this.planet.code, this.dateTime)
            }
            // Minor Planet.
            else if (this.tab === 3 && this.minorPlanet) {
                this.name = this.minorPlanet.name
                this.tags = []
                if (this.minorPlanet.kind) this.tags.push({ title: this.minorPlanet.kind, severity: 'success' })
                if (this.minorPlanet.pha) this.tags.push({ title: 'PHA', severity: 'danger' })
                if (this.minorPlanet.neo) this.tags.push({ title: 'NEO', severity: 'danger' })
                if (this.minorPlanet.orbitType) this.tags.push({ title: this.minorPlanet.orbitType, severity: 'info' })
                const code = `DES=${this.minorPlanet.spkId};`
                this.bodyPosition = await this.api.positionOfPlanet(this.location!, code, this.dateTime)
            }
            // Star.
            else if (this.tab === 4 && this.star) {
                this.name = this.star.names
                this.tags = []
                this.bodyPosition = await this.api.positionOfStar(this.location!, this.star, this.dateTime)
            }
            // DSO.
            else if (this.tab === 5 && this.dso) {
                this.name = this.dso.names
                this.tags = []
                this.bodyPosition = await this.api.positionOfDSO(this.location!, this.dso, this.dateTime)
            }

            if (refreshTwilight || date !== this.twilightDate) {
                this.twilightDate = date
                const twilight = await this.api.twilight(this.location!, this.dateTime)
                this.altitudeData.datasets[0].data = [[0.0, 90], [twilight.civilDusk[0], 90]]
                this.altitudeData.datasets[1].data = [[twilight.civilDusk[0], 90], [twilight.civilDusk[1], 90]]
                this.altitudeData.datasets[2].data = [[twilight.nauticalDusk[0], 90], [twilight.nauticalDusk[1], 90]]
                this.altitudeData.datasets[3].data = [[twilight.astronomicalDusk[0], 90], [twilight.astronomicalDusk[1], 90]]
                this.altitudeData.datasets[4].data = [[twilight.night[0], 90], [twilight.night[1], 90]]
                this.altitudeData.datasets[5].data = [[twilight.astronomicalDawn[0], 90], [twilight.astronomicalDawn[1], 90]]
                this.altitudeData.datasets[6].data = [[twilight.nauticalDawn[0], 90], [twilight.nauticalDawn[1], 90]]
                this.altitudeData.datasets[7].data = [[twilight.civilDawn[0], 90], [twilight.civilDawn[1], 90]]
                this.altitudeData.datasets[8].data = [[twilight.civilDawn[1], 90], [24.0, 90]]
                this.chart?.refresh()
            }

            if (refreshChart) {
                await this.refreshChart()
            }
        } finally {
            this.refreshing = false
        }
    }

    private async refreshChart() {
        // Sun.
        if (this.tab === 0) {
            const points = await this.api.altitudePointsOfSun(this.location!, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Moon.
        else if (this.tab === 1) {
            const points = await this.api.altitudePointsOfMoon(this.location!, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Planet.
        else if (this.tab === 2 && this.planet) {
            const points = await this.api.altitudePointsOfPlanet(this.location!, this.planet.code, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Minor Planet.
        else if (this.tab === 3 && this.minorPlanet) {
            const code = `DES=${this.minorPlanet.spkId};`
            const points = await this.api.altitudePointsOfPlanet(this.location!, code, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // Star.
        else if (this.tab === 4 && this.star) {
            const points = await this.api.altitudePointsOfStar(this.location!, this.star, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        }
        // DSO.
        else if (this.tab === 5 && this.dso) {
            const points = await this.api.altitudePointsOfDSO(this.location!, this.dso, this.dateTime)
            AtlasComponent.belowZeroPoints(points)
            this.altitudeData.datasets[9].data = points
        } else {
            return
        }

        this.chart?.refresh()
    }

    private static belowZeroPoints(points: [number, number][]) {
        for (const point of points) {
            if (point[1] < 0) {
                point[1] = NaN
            }
        }
    }
}
