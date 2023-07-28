import { Component, HostListener, NgZone, OnDestroy, OnInit } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ApiService } from '../../shared/services/api.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { Camera, Focuser } from '../../shared/types'

@Component({
    selector: 'app-focuser',
    templateUrl: './focuser.component.html',
    styleUrls: ['./focuser.component.scss']
})
export class FocuserComponent implements OnInit, OnDestroy {

    focusers: Focuser[] = []
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
    hasBackslash = false
    maxPosition = 0

    stepsRelative = 0
    stepsAbsolute = 0

    camera?: Camera

    constructor(
        private title: Title,
        private api: ApiService,
        private electron: ElectronService,
        private preference: PreferenceService,
        ngZone: NgZone,
    ) {
        title.setTitle('Focuser')

        this.api.indiStartListening('FOCUSER')

        electron.ipcRenderer.on('FOCUSER_UPDATED', (_, focuser: Focuser) => {
            if (focuser.name === this.focuser?.name) {
                ngZone.run(() => {
                    Object.assign(this.focuser!, focuser)
                    this.update()
                })
            }
        })

        electron.ipcRenderer.on('CAMERA_CHANGED', (_, camera?: Camera) => {
            ngZone.run(() => {
                this.camera = camera
            })
        })
    }

    async ngOnInit() {
        this.focusers = await this.api.attachedFocusers()
    }

    @HostListener('window:unload')
    ngOnDestroy() {
        this.api.indiStopListening('FOCUSER')
    }

    async focuserChanged() {
        if (this.focuser) {
            this.title.setTitle(`Focuser ・ ${this.focuser.name}`)

            const focuser = await this.api.focuser(this.focuser.name)
            Object.assign(this.focuser, focuser)

            this.loadPreference()
            this.update()
            this.savePreference()
        } else {
            this.title.setTitle(`Focuser`)
        }

        this.electron.ipcRenderer.send('FOCUSER_CHANGED', this.focuser)
    }

    async connect() {
        if (this.connected) {
            await this.api.focuserDisconnect(this.focuser!)
        } else {
            await this.api.focuserConnect(this.focuser!)
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

    syncTo() {
        this.api.focuserSyncTo(this.focuser!, this.stepsAbsolute)
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
        this.hasBackslash = this.focuser.hasBackslash
        this.maxPosition = this.focuser.maxPosition
    }

    private loadPreference() {
        if (this.focuser) {
            this.stepsRelative = this.preference.get(`focuser.${this.focuser.name}.stepsRelative`, 0)
            this.stepsAbsolute = this.preference.get(`focuser.${this.focuser.name}.stepsAbsolute`, 0)
        }
    }

    private savePreference() {
        if (this.focuser) {
            this.preference.set(`focuser.${this.focuser.name}.stepsRelative`, this.stepsRelative)
            this.preference.set(`focuser.${this.focuser.name}.stepsAbsolute`, this.stepsAbsolute)
        }
    }
}