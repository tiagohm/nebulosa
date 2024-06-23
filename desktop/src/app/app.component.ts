import { AfterViewInit, Component } from '@angular/core'
import { Title } from '@angular/platform-browser'
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
	readonly maximizable = !!window.preference.resizable
	readonly modal = window.preference.modal ?? false
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
		private electron: ElectronService,
	) {
		console.info('APP_CONFIG', APP_CONFIG)

		if (electron.isElectron) {
			console.info('Run in electron', window.preference)
		} else {
			console.info('Run in browser', window.preference)
		}
	}

	async ngAfterViewInit() {
		if (window.preference.autoResizable !== false) {
			await this.electron.autoResizeWindow()
		}
	}

	pin() {
		this.pinned = !this.pinned
		if (this.pinned) return this.electron.pinWindow()
		else return this.electron.unpinWindow()
	}

	minimize() {
		return this.electron.minimizeWindow()
	}

	maximize() {
		return this.electron.maximizeWindow()
	}

	close(data?: unknown) {
		return this.electron.closeWindow({ id: window.id, data })
	}
}
