import { Component, ElementRef, HostListener, NgZone, OnDestroy, inject } from '@angular/core'
import { Title } from '@angular/platform-browser'
import hotkeys from 'hotkeys-js'
import { APP_CONFIG } from '../environments/environment'
import { MenuItem } from '../shared/components/menu-item/menu-item.component'
import { ConfirmationService } from '../shared/services/confirmation.service'
import { ElectronService } from '../shared/services/electron.service'

@Component({
	selector: 'neb-root',
	templateUrl: './app.component.html',
})
export class AppComponent implements OnDestroy {
	private readonly windowTitle = inject(Title)
	private readonly electronService = inject(ElectronService)

	readonly maximizable = !!window.context.resizable
	readonly modal = window.context.modal ?? false
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

	constructor() {
		const confirmationService = inject(ConfirmationService)
		const ngZone = inject(NgZone)
		const hostElementRef = inject<ElementRef<Element>>(ElementRef)

		console.info('APP_CONFIG', APP_CONFIG)

		if (!window.context.resizable && window.context.autoResizable !== false) {
			this.resizeObserver = new ResizeObserver((entries) => {
				this.resizeWindowFromElement(entries[0].target)
			})

			this.resizeObserver.observe(hostElementRef.nativeElement)

			setTimeout(() => {
				const root = document.getElementsByTagName('neb-root')[0]
				this.resizeWindowFromElement(root)
			}, 1000)
		} else {
			this.resizeObserver = undefined
		}

		this.electronService.on('CONFIRMATION', (event) => {
			if (confirmationService.has(event.idempotencyKey)) {
				void ngZone.run(() => {
					return confirmationService.processConfirmationEvent(event)
				})
			}
		})

		hotkeys('ctrl+alt+shift+d', (event) => {
			event.preventDefault()
			void this.electronService.openDevTools()
		})
	}

	@HostListener('window:unload')
	ngOnDestroy() {
		this.resizeObserver?.disconnect()
	}

	private resizeWindowFromElement(element: Element) {
		const height = element.clientHeight

		if (height) {
			void this.electronService.resizeWindow(height)
		}
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
