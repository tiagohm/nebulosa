import { AfterViewInit, Component } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import { APP_CONFIG } from '../environments/environment'
import { ElectronService } from '../shared/services/electron.service'
import { PreferenceService } from '../shared/services/preference.service'

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements AfterViewInit {

    pinned = false
    maximizable = false
    subTitle = ''

    get backgroundColor() {
        return this.isNightMode ? '#B71C1C' : '#002457'
    }

    private night!: HTMLElement

    get title() {
        return this.windowTitle.getTitle()
    }

    set title(value: string) {
        this.windowTitle.setTitle(value)
    }

    constructor(
        private windowTitle: Title,
        private route: ActivatedRoute,
        private electronService: ElectronService,
        private preference: PreferenceService,
    ) {
        console.info('APP_CONFIG', APP_CONFIG)

        if (electronService.isElectron) {
            console.info('Run in electron')
        } else {
            console.info('Run in browser')
        }
    }

    ngAfterViewInit() {
        this.route.queryParams.subscribe(e => {
            this.maximizable = e.resizable === 'true'
        })

        this.night = document.getElementsByTagName('night')[0] as HTMLElement
        this.updateNightMode(this.isNightMode)
    }

    get isNightMode() {
        return this.preference.isNightMode
    }

    set isNightMode(enabled: boolean) {
        if (enabled) {
            this.night.classList.replace('hidden', 'block')
            this.night.style.background = '#ff00003b'
        } else {
            this.night.style.background = 'transparent'
            this.night.classList.replace('block', 'hidden')
        }

        // TODO: UPDATE ONLY ON SETTINGS WINDOW
        this.preference.isNightMode = enabled
        this.updateNightMode(enabled)
    }

    private updateNightMode(enabled: boolean) {
        if (enabled) {
            this.night.classList.replace('hidden', 'block')
        } else {
            this.night.classList.replace('block', 'hidden')
        }

        // TODO: NOTIFY ALL WINDOWS PREFERENCE_UPDATED(name, oldValue, newValue)
    }

    pin() {
        this.pinned = !this.pinned
        if (this.pinned) this.electronService.sendSync('PIN_WINDOW')
        else this.electronService.sendSync('UNPIN_WINDOW')
    }

    minimize() {
        this.electronService.sendSync('MINIMIZE_WINDOW')
    }

    maximize() {
        this.electronService.sendSync('MAXIMIZE_WINDOW')
    }

    close() {
        this.electronService.sendSync('CLOSE_WINDOW')
    }
}
