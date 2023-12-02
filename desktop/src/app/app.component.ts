import { AfterViewInit, Component } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import { MenuItem } from 'primeng/api'
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
    backgroundColor = '#212121'
    extra: MenuItem[] = []

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

    async ngAfterViewInit() {
        this.route.queryParams.subscribe(e => {
            this.maximizable = e.resizable === 'true'
        })
    }

    pin() {
        this.pinned = !this.pinned
        if (this.pinned) this.electronService.send('PIN_WINDOW')
        else this.electronService.send('UNPIN_WINDOW')
    }

    minimize() {
        this.electronService.send('MINIMIZE_WINDOW')
    }

    maximize() {
        this.electronService.send('MAXIMIZE_WINDOW')
    }

    close() {
        this.electronService.send('CLOSE_WINDOW')
    }
}
