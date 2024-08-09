import { Component, ElementRef, HostListener, NgZone, OnDestroy } from '@angular/core'
import { Title } from '@angular/platform-browser'
import { APP_CONFIG } from '../environments/environment'
import { MenuItem } from '../shared/components/menu-item/menu-item.component'
import { ConfirmationService } from '../shared/services/confirmation.service'
import { ElectronService } from '../shared/services/electron.service'

@Component({
	selector: 'neb-root',
	templateUrl: './app.component.html',
})
export class AppComponent implements OnDestroy {
	readonly maximizable = !!window.preference.resizable
	readonly modal = window.preference.modal ?? false
	readonly topMenu: MenuItem[] = []

	subTitle? = ''
	pinned = false
	showTopBar = true
	beforeClose?: () => boolean | Promise<boolean>

	private readonly resizeObserver?: ResizeObserver

	get title() {
		return this.windowTitle.getTitle()
	}

	set title(value: string) {
		this.windowTitle.setTitle(value)
	}

	constructor(
		private readonly windowTitle: Title,
		private readonly electronService: ElectronService,
		confirmationService: ConfirmationService,
		ngZone: NgZone,
		hostElementRef: ElementRef<Element>,
	) {
		console.info('APP_CONFIG', APP_CONFIG)

		if (electronService.isElectron) {
			console.info('Run in electron', window.preference)
		} else {
			console.info('Run in browser', window.preference)
		}

		if (!window.preference.resizable && window.preference.autoResizable !== false) {
			this.resizeObserver = new ResizeObserver((entries) => {
				const height = entries[0].target.clientHeight

				if (height) {
					void this.electronService.resizeWindow(height)
				}
			})

			this.resizeObserver.observe(hostElementRef.nativeElement)
		} else {
			this.resizeObserver = undefined
		}

		electronService.on('CONFIRMATION', (event) => {
			if (confirmationService.has(event.idempotencyKey)) {
				void ngZone.run(() => {
					return confirmationService.processConfirmationEvent(event)
				})
			}
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.resizeObserver?.disconnect()
	}

	pin() {
		this.pinned = !this.pinned
		if (this.pinned) return this.electronService.pinWindow()
		else return this.electronService.unpinWindow()
	}

	minimize() {
		return this.electronService.minimizeWindow()
	}

	maximize() {
		return this.electronService.maximizeWindow()
	}

	async close(data?: unknown, force: boolean = false) {
		if (!this.beforeClose || (await this.beforeClose()) || force) {
			return await this.electronService.closeWindow(data)
		} else {
			return undefined
		}
	}
}
