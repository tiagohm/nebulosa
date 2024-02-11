import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { EMPTY_FOCUSER, Focuser, FocuserPreference, focuserPreferenceKey } from '../../shared/types/focuser.types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-focuser',
    templateUrl: './focuser.component.html',
    styleUrls: ['./focuser.component.scss'],
})
export class FocuserComponent implements AfterViewInit, OnDestroy {

    readonly focuser = Object.assign({}, EMPTY_FOCUSER)

    moving = false
    position = 0
    hasThermometer = false
    temperature = 0
    canAbsoluteMove = false
    canRelativeMove = false
    canAbort = false
    canReverse = false
    reverse = false
    canSync = false
    hasBacklash = false
    maxPosition = 0

    stepsRelative = 0
    stepsAbsolute = 0

    constructor(
        private app: AppComponent,
        private api: ApiService,
        private electron: ElectronService,
        private storage: LocalStorageService,
        private route: ActivatedRoute,
        ngZone: NgZone,
    ) {
        app.title = 'Focuser'

        electron.on('FOCUSER.UPDATED', event => {
            if (event.device.name === this.focuser.name) {
                ngZone.run(() => {
                    Object.assign(this.focuser, event.device)
                    this.update()
                })
            }
        })

        electron.on('FOCUSER.DETACHED', event => {
            if (event.device.name === this.focuser.name) {
                ngZone.run(() => {
                    Object.assign(this.focuser, EMPTY_FOCUSER)
                })
            }
        })
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
        if (focuser && focuser.name) {
            focuser = await this.api.focuser(focuser.name)
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

    moveIn() {
        this.moving = true
        this.api.focuserMoveIn(this.focuser, this.stepsRelative)
        this.savePreference()
    }

    moveOut() {
        this.moving = true
        this.api.focuserMoveOut(this.focuser, this.stepsRelative)
        this.savePreference()
    }

    moveTo() {
        this.moving = true
        this.api.focuserMoveTo(this.focuser, this.stepsAbsolute)
        this.savePreference()
    }

    sync() {
        this.api.focuserSync(this.focuser, this.stepsAbsolute)
        this.savePreference()
    }

    abort() {
        this.api.focuserAbort(this.focuser)
    }

    private update() {
        if (!this.focuser) {
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
        this.reverse = this.focuser.reverse
        this.canSync = this.focuser.canSync
        this.hasBacklash = this.focuser.hasBacklash
        this.maxPosition = this.focuser.maxPosition
    }

    private loadPreference() {
        if (this.focuser.name) {
            const preference = this.storage.get<FocuserPreference>(focuserPreferenceKey(this.focuser), {})
            this.stepsRelative = preference.stepsRelative ?? 100
            this.stepsAbsolute = preference.stepsAbsolute ?? this.focuser.position
        }
    }

    private savePreference() {
        if (this.focuser.connected) {
            const preference: FocuserPreference = {
                stepsRelative: this.stepsRelative,
                stepsAbsolute: this.stepsAbsolute,
            }

            this.storage.set(focuserPreferenceKey(this.focuser), preference)
        }
    }
}