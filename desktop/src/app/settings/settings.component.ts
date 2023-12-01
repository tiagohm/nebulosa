import { AfterViewInit, Component, HostListener, NgZone, OnDestroy } from '@angular/core'
import { ActivatedRoute } from '@angular/router'
import { MessageService } from 'primeng/api'
import { PLATE_SOLVER_SETTINGS } from '../../shared/constants'
import { ApiService } from '../../shared/services/api.service'
import { BrowserWindowService } from '../../shared/services/browser-window.service'
import { ElectronService } from '../../shared/services/electron.service'
import { PreferenceService } from '../../shared/services/preference.service'
import { PlateSolverSettings, PlateSolverType } from '../../shared/types'
import { AppComponent } from '../app.component'

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent implements AfterViewInit, OnDestroy {

    activeTab = 0

    readonly plateSolvers: PlateSolverType[] = ['ASTAP', 'ASTROMETRY_NET']
    readonly plateSolver: PlateSolverSettings = {}

    constructor(
        app: AppComponent,
        private route: ActivatedRoute,
        private api: ApiService,
        private browserWindow: BrowserWindowService,
        private electron: ElectronService,
        private preference: PreferenceService,
        private message: MessageService,
        ngZone: NgZone,
    ) {
        app.title = 'Settings'
    }

    async ngAfterViewInit() {
        Object.assign(this.plateSolver, await this.preference.get(PLATE_SOLVER_SETTINGS, this.plateSolver))
        if (!this.plateSolver.type) this.plateSolver.type = 'ASTAP'
    }

    @HostListener('window:unload')
    ngOnDestroy() { }

    save() {
        this.preference.set(PLATE_SOLVER_SETTINGS, this.plateSolver)
    }
}