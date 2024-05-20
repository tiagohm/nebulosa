import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import hotkeys from 'hotkeys-js'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { EMPTY_FOCUSER, Focuser } from '../../shared/types/focuser.types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-focuser',
    templateUrl: './focuser.component.html',
    styleUrls: ['./focuser.component.scss'],
})
export class FocuserComponent implements AfterViewInit, OnDestroy {

    readonly focuser = structuredClone(EMPTY_FOCUSER)

    moving = false
    position = 0
    hasThermometer = false
    temperature = 0
    canAbsoluteMove = false
    canRelativeMove = false
    canAbort = false
    canReverse = false
    reversed = false
    canSync = false
    hasBacklash = false
    maxPosition = 0

    stepsRelative = 0
    stepsAbsolute = 0

    constructor(
        private app: AppComponent,
        private api: ApiService,
        electron: ElectronService,
        private preference: PreferenceService,
        private route: ActivatedRoute,
        ngZone: NgZone,
    ) {
        app.title = 'Focuser'

        electron.on('FOCUSER.UPDATED', event => {
            if (event.device.id === this.focuser.id) {
                ngZone.run(() => {
                    Object.assign(this.focuser, event.device)
                    this.update()
                })
            }
        })

        electron.on('FOCUSER.DETACHED', event => {
            if (event.device.id === this.focuser.id) {
                ngZone.run(() => {
                    Object.assign(this.focuser, EMPTY_FOCUSER)
                })
            }
        })

        hotkeys('left', (event) => { event.preventDefault(); this.moveIn() })
        hotkeys('alt+left', (event) => { event.preventDefault(); this.moveIn(10) })
        hotkeys('ctrl+left', (event) => { event.preventDefault(); this.moveIn(2) })
        hotkeys('shift+left', (event) => { event.preventDefault(); this.moveIn(0.5) })
        hotkeys('right', (event) => { event.preventDefault(); this.moveOut() })
        hotkeys('alt+right', (event) => { event.preventDefault(); this.moveOut(10) })
        hotkeys('ctrl+right', (event) => { event.preventDefault(); this.moveOut(2) })
        hotkeys('shift+right', (event) => { event.preventDefault(); this.moveOut(0.5) })
        hotkeys('space', (event) => { event.preventDefault(); this.abort() })
        hotkeys('ctrl+enter', (event) => { event.preventDefault(); this.moveTo() })
        hotkeys('up', (event) => { event.preventDefault(); this.stepsRelative = Math.min(this.maxPosition, this.stepsRelative + 1) })
        hotkeys('down', (event) => { event.preventDefault(); this.stepsRelative = Math.max(0, this.stepsRelative - 1) })
        hotkeys('-', (event) => { event.preventDefault(); this.stepsAbsolute = Math.max(0, this.stepsAbsolute - 1) })
        hotkeys('=', (event) => { event.preventDefault(); this.stepsAbsolute = Math.min(this.maxPosition, this.stepsAbsolute + 1) })
    }

    async ngAfterViewInit() {
        this.route.queryParams.subscribe(e => {
            const focuser = JSON.parse(decodeURIComponent(e.data)) as Focuser
            this.focuserChanged(focuser)
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.abort()
    }

    async focuserChanged(focuser?: Focuser) {
        if (focuser && focuser.id) {
            focuser = await this.api.focuser(focuser.id)
            Object.assign(this.focuser, focuser)

            this.loadPreference()
            this.update()
        }

        if (this.app) {
            this.app.subTitle = focuser?.name ?? ''
        }
    }

    connect() {
        if (this.focuser.connected) {
            this.api.focuserDisconnect(this.focuser)
        } else {
            this.api.focuserConnect(this.focuser)
        }
    }

    async moveIn(stepSize: number = 1) {
        if (!this.moving) {
            this.moving = true
            await this.api.focuserMoveIn(this.focuser, Math.trunc(this.stepsRelative * stepSize))
            this.savePreference()
        }
    }

    async moveOut(stepSize: number = 1) {
        if (!this.moving) {
            this.moving = true
            await this.api.focuserMoveOut(this.focuser, Math.trunc(this.stepsRelative * stepSize))
            this.savePreference()
        }
    }

    async moveTo() {
        if (!this.moving && this.stepsAbsolute !== this.position) {
            this.moving = true
            await this.api.focuserMoveTo(this.focuser, this.stepsAbsolute)
            this.savePreference()
        }
    }

    async sync() {
        if (!this.moving) {
            await this.api.focuserSync(this.focuser, this.stepsAbsolute)
            this.savePreference()
        }
    }

    abort() {
        this.api.focuserAbort(this.focuser)
    }

    private update() {
        if (!this.focuser.id) {
            return
        }

        this.moving = this.focuser.moving
        this.position = this.focuser.position
        this.hasThermometer = this.focuser.hasThermometer
        this.temperature = this.focuser.temperature
        this.canAbsoluteMove = this.focuser.canAbsoluteMove
        this.canRelativeMove = this.focuser.canRelativeMove
        this.canAbort = this.focuser.canAbort
        this.canReverse = this.focuser.canReverse
        this.reversed = this.focuser.reversed
        this.canSync = this.focuser.canSync
        this.hasBacklash = this.focuser.hasBacklash
        this.maxPosition = this.focuser.maxPosition
    }

    private loadPreference() {
        if (this.focuser.id) {
            const preference = this.preference.focuserPreference(this.focuser).get()
            this.stepsRelative = preference.stepsRelative ?? 100
            this.stepsAbsolute = preference.stepsAbsolute ?? this.focuser.position
        }
    }

    private savePreference() {
        if (this.focuser.connected) {
            const preference = this.preference.focuserPreference(this.focuser).get()
            preference.stepsAbsolute = this.stepsAbsolute
            preference.stepsRelative = this.stepsRelative
            this.preference.focuserPreference(this.focuser).set(preference)
        }
    }
}