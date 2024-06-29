import { Component, ElementRef, NgZone, OnDestroy } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { APP_CONFIG } from '../environments/environment'
import { MenuItem } from '../shared/components/menu-item/menu-item.component'
import { ConfirmationService } from '../shared/services/confirmation.service'
import { ElectronService } from '../shared/services/electron.service'

@Component({
	selector: 'app-root',
	templateUrl: './app.component.html',
	styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnDestroy {
	pinned = false
	readonly maximizable = !!window.preference.resizable
	readonly modal = window.preference.modal ?? false
	subTitle? = ''
	topMenu: MenuItem[] = []
	showTopBar = true

	private readonly resizeObserver?: ResizeObserver

	get title() {
		return this.windowTitle.getTitle()
	}

	set title(value: string) {
		this.windowTitle.setTitle(value)
	}

	constructor(
		private readonly windowTitle: Title,
		private readonly electron: ElectronService,
		confirmation: ConfirmationService,
		ngZone: NgZone,
		hostElementRef: ElementRef<Element>,
	) {
		console.info('APP_CONFIG', APP_CONFIG)

		if (electron.isElectron) {
			console.info('Run in electron', window.preference)
		} else {
			console.info('Run in browser', window.preference)
		}

		if (!window.preference.resizable && window.preference.autoResizable !== false) {
			this.resizeObserver = new ResizeObserver((entries) => {
				const height = entries[0].target.clientHeight

				if (height) {
					void this.electron.resizeWindow(height)
				}
			})

			this.resizeObserver.observe(hostElementRef.nativeElement)
		} else {
			this.resizeObserver = undefined
		}

		electron.on('CONFIRMATION', (event) => {
			if (confirmation.has(event.idempotencyKey)) {
				void ngZone.run(() => {
					return confirmation.processConfirmationEvent(event)
				})
			}
		})
	}

	ngOnDestroy() {
		this.resizeObserver?.disconnect()
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
		return this.electron.closeWindow(data)
	}
}
