import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { LocalStorageService } from '../../shared/services/local-storage.service'
import { Focuser } from '../../shared/types'
import { AppComponent } from '../app.component'

export interface FocuserPreference {
    stepsRelative?: number
    stepsAbsolute?: number
}

@Component({
    selector: 'app-focuser',
    templateUrl: './focuser.component.html',
    styleUrls: ['./focuser.component.scss'],
})
export class FocuserComponent implements AfterViewInit, OnDestroy {

    focuser?: Focuser
    connected = false

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

        electron.on('FOCUSER_UPDATED', event => {
            if (event.device.name === this.focuser?.name) {
                ngZone.run(() => {
                    Object.assign(this.focuser!, event.device)
                    this.update()
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
        this.focuser = focuser

        if (this.focuser) {
            this.app.subTitle = this.focuser.name

            const focuser = await this.api.focuser(this.focuser.name)
            Object.assign(this.focuser, focuser)

            this.loadPreference()
            this.update()
        } else {
            this.app.subTitle = ''
        }
    }

    connect() {
        if (this.connected) {
            this.api.focuserDisconnect(this.focuser!)
        } else {
            this.api.focuserConnect(this.focuser!)
        }
    }

    moveIn() {
        this.moving = true
        this.api.focuserMoveIn(this.focuser!, this.stepsRelative)
        this.savePreference()
    }

    moveOut() {
        this.moving = true
        this.api.focuserMoveOut(this.focuser!, this.stepsRelative)
        this.savePreference()
    }

    moveTo() {
        this.moving = true
        this.api.focuserMoveTo(this.focuser!, this.stepsAbsolute)
        this.savePreference()
    }

    sync() {
        this.api.focuserSync(this.focuser!, this.stepsAbsolute)
        this.savePreference()
    }

    abort() {
        this.api.focuserAbort(this.focuser!)
    }

    private async update() {
        if (!this.focuser) {
            return
        }

        this.connected = this.focuser.connected
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
        if (this.focuser) {
            const preference = this.storage.get<FocuserPreference>(`focuser.${this.focuser.name}`, {})
            this.stepsRelative = preference.stepsRelative ?? 100
            this.stepsAbsolute = preference.stepsAbsolute ?? this.focuser.position
        }
    }

    private savePreference() {
        if (this.focuser && this.focuser.connected) {
            const preference: FocuserPreference = {
                stepsRelative: this.stepsRelative,
                stepsAbsolute: this.stepsAbsolute,
            }

            this.storage.set(`focuser.${this.focuser.name}`, preference)
        }
    }
}