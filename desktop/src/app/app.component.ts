import { AfterViewInit, Component } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import { MenuItem } from 'primeng/api'
import { APP_CONFIG } from '../environments/environment'
import { ElectronService } from '../shared/services/electron.service'

export interface ExtendedMenuItem extends MenuItem {
    badgeSeverity?: 'success' | 'info' | 'warning' | 'danger'
}

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements AfterViewInit {

    pinned = false
    maximizable = false
    readonly modal = window.modal
    subTitle? = ''
    backgroundColor = '#212121'
    topMenu: ExtendedMenuItem[] = []

    get title() {
        return this.windowTitle.getTitle()
    }

    set title(value: string) {
        this.windowTitle.setTitle(value)
    }

    constructor(
        private windowTitle: Title,
        private route: ActivatedRoute,
        private electron: ElectronService,
    ) {
        console.info('APP_CONFIG', APP_CONFIG)

        if (electron.isElectron) {
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
        if (this.pinned) this.electron.send('WINDOW.PIN')
        else this.electron.send('WINDOW.UNPIN')
    }

    minimize() {
        this.electron.send('WINDOW.MINIMIZE')
    }

    maximize() {
        this.electron.send('WINDOW.MAXIMIZE')
    }

    close(data?: any) {
        this.electron.closeWindow({ data })
    }
}
