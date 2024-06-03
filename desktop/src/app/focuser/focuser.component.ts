import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import hotkeys from 'hotkeys-js'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { Pingable, Pinger } from '../../shared/services/pinger.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { EMPTY_FOCUSER, Focuser } from '../../shared/types/focuser.types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-focuser',
    templateUrl: './focuser.component.html',
    styleUrls: ['./focuser.component.scss'],
})
export class FocuserComponent implements AfterViewInit, OnDestroy, Pingable {

    readonly focuser = structuredClone(EMPTY_FOCUSER)

    moving = false
    stepsRelative = 0
    stepsAbsolute = 0

    constructor(
        private app: AppComponent,
        private api: ApiService,
        electron: ElectronService,
        private preference: PreferenceService,
        private route: ActivatedRoute,
        private pinger: Pinger,
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

        hotkeys('left', event => { event.preventDefault(); this.moveIn() })
        hotkeys('ctrl+left', event => { event.preventDefault(); this.moveIn(2) })
        hotkeys('alt+left', event => { event.preventDefault(); this.moveIn(0.5) })
        hotkeys('right', event => { event.preventDefault(); this.moveOut() })
        hotkeys('ctrl+right', event => { event.preventDefault(); this.moveOut(2) })
        hotkeys('alt+right', event => { event.preventDefault(); this.moveOut(0.5) })
        hotkeys('space', event => { event.preventDefault(); this.abort() })
        hotkeys('enter', event => { event.preventDefault(); this.moveTo() })
        hotkeys('up', event => { event.preventDefault(); this.stepsRelative = Math.min(this.focuser.maxPosition, this.stepsRelative + 1) })
        hotkeys('down', event => { event.preventDefault(); this.stepsRelative = Math.max(0, this.stepsRelative - 1) })
        hotkeys('ctrl+up', event => { event.preventDefault(); this.stepsAbsolute = Math.max(0, this.stepsAbsolute - 1) })
        hotkeys('ctrl+down', event => { event.preventDefault(); this.stepsAbsolute = Math.min(this.focuser.maxPosition, this.stepsAbsolute + 1) })
    }

    async ngAfterViewInit() {
        this.route.queryParams.subscribe(async e => {
            const focuser = JSON.parse(decodeURIComponent(e.data)) as Focuser
            await this.focuserChanged(focuser)
            this.pinger.register(this, 30000)
        })
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.pinger.unregister(this)
        this.abort()
    }

    ping() {
        this.api.focuserListen(this.focuser)
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
        if (!this.moving && this.stepsAbsolute !== this.focuser.position) {
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
        if (this.focuser.id) {
            this.moving = this.focuser.moving
        }
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