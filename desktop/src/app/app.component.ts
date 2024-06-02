import { AfterViewInit, Component } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { ActivatedRoute } from '@angular/router'
import { APP_CONFIG } from '../environments/environment'
import { MenuItem } from '../shared/components/menu-item/menu-item.component'
import { ElectronService } from '../shared/services/electron.service'

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements AfterViewInit {

    pinned = false
    readonly maximizable = !!window.options.resizable
    readonly modal = window.options.modal ?? false
    subTitle? = ''
    backgroundColor = '#212121'
    topMenu: MenuItem[] = []
    showTopBar = true

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
            console.info('Run in electron', window.options)
        } else {
            console.info('Run in browser', window.options)
        }
    }

    async ngAfterViewInit() {
        if (window.options.autoResizable !== false) {
            this.electron.autoResizeWindow()
        }
    }

    pin() {
        this.pinned = !this.pinned
        if (this.pinned) this.electron.pinWindow()
        else this.electron.unpinWindow()
    }

    minimize() {
        this.electron.minimizeWindow()
    }

    maximize() {
        this.electron.maximizeWindow()
    }

    close(data?: any) {
        this.electron.closeWindow({ data })
    }
}
