import { Client } from '@stomp/stompjs'
import { BrowserWindow, Notification, dialog, screen, shell } from 'electron'
import type { ChildProcessWithoutNullStreams } from 'node:child_process'
import { join } from 'path'
import type { MessageEvent } from '../src/shared/types/api.types'
import type { CloseWindow, ConfirmationEvent, NotificationEvent, OpenDirectory, OpenFile, OpenWindow, StoredWindowData } from '../src/shared/types/app.types'
import type { ParsedArgument } from './argument.parser'
import type { LocalStorage } from './local.storage'

export class ApplicationWindow {
	constructor(
		public readonly browserWindow: BrowserWindow,
		public readonly data: OpenWindow,
		public readonly parentWindow?: BrowserWindow,
		public webSocket?: Client,
		public apiProcess?: ChildProcessWithoutNullStreams,
		public resolver?: (data: unknown) => void,
	) {}

	get isParent() {
		return !this.parentWindow
	}

	get isModal() {
		return !!this.parentWindow || this.data.id.endsWith('.modal')
	}

	get isHome() {
		return !this.webSocket || this.data.id === 'home'
	}

	get windowId() {
		return this.browserWindow.id
	}

	get appId() {
		return this.data.id
	}

	close() {
		this.browserWindow.close()
	}

	toggleFullscreen() {
		this.browserWindow.setFullScreen(!this.browserWindow.isFullScreen())
	}

	toggleMaximize() {
		if (this.browserWindow.isMaximized()) {
			this.browserWindow.unmaximize()
		} else {
			this.browserWindow.maximize()
		}

		return this.browserWindow.isMaximized()
	}

	sendMessage(event: MessageEvent) {
		this.browserWindow.webContents.send(event.eventName, event)
	}
}

export function isNotificationEvent(event: MessageEvent): event is NotificationEvent {
	return event.eventName === 'NOTIFICATION'
}

export function isConfirmationEvent(event: MessageEvent): event is ConfirmationEvent {
	return event.eventName === 'CONFIRMATION'
}

export class WindowManager {
	private readonly windows = new Map<string, ApplicationWindow>()
	private readonly appIcon: string
	private port = 0
	private host = 'localhost'

	constructor(
		public readonly args: ParsedArgument,
		public readonly storage: LocalStorage<StoredWindowData>,
		defaultAppIcon: string = 'nebulosa.png',
	) {
		this.appIcon = join(__dirname, args.serve ? `../src/assets/icons/${defaultAppIcon}` : `assets/icons/${defaultAppIcon}`)
		this.port = args.port
		this.host = args.host
	}

	async createWindow(open: OpenWindow, parent?: BrowserWindow) {
		let appWindow = this.windows.get(open.id)

		if (appWindow) {
			if (open.data) {
				console.info('window data changed. id=%s, data=%s', open.id, open.data)
				appWindow.browserWindow.webContents.send('DATA.CHANGED', open.data)
			}

			return appWindow
		}

		const preference = open.preference

		const encodedPreference = encodeURIComponent(JSON.stringify(preference))
		const encodedData = encodeURIComponent(JSON.stringify(open.data ?? {}))

		const minWidth = preference.minWidth ?? 100
		const computedWidth = preference.width ? Math.trunc(this.computeWidth(preference.width)) : 320
		const minHeight = preference.minHeight ?? 100
		const computedHeight = preference.height ? Math.trunc(this.computeHeight(preference.height, computedWidth)) : 416

		const screenSize = screen.getPrimaryDisplay().workAreaSize
		const storedData = this.storage.get(`window.${open.id}`)
		const width = preference.resizable ? Math.max(minWidth, Math.min(storedData?.width ?? computedWidth, screenSize.width)) : computedWidth
		const height = preference.resizable ? Math.max(minHeight, Math.min(storedData?.height ?? computedHeight, screenSize.height)) : computedHeight
		const x = Math.max(0, Math.min(storedData?.x ?? 0, screenSize.width - width))
		const y = Math.max(0, Math.min(storedData?.y ?? 0, screenSize.height - height))

		const browserWindow = new BrowserWindow({
			title: 'Nebulosa',
			frame: false,
			modal: preference.modal,
			parent,
			width,
			height,
			minWidth,
			minHeight,
			x,
			y,
			resizable: this.args.serve || preference.resizable,
			autoHideMenuBar: true,
			icon: preference.icon ? join(__dirname, this.args.serve ? `../src/assets/icons/${preference.icon}.png` : `assets/icons/${preference.icon}.png`) : this.appIcon,
			webPreferences: {
				nodeIntegration: true,
				allowRunningInsecureContent: this.args.serve,
				contextIsolation: false,
				additionalArguments: [`--host=${this.host}`, `--port=${this.port}`, `--id=${open.id}`, `--data=${encodedData}`, `--preference=${encodedPreference}`],
				preload: join(__dirname, 'preload.js'),
				devTools: this.args.serve,
			},
		})

		if (!storedData) {
			browserWindow.center()
		}

		if (this.args.serve) {
			await browserWindow.loadURL(`http://localhost:4200/${open.path}?data=${encodedData}`)
		} else {
			const url = new URL(join('file:', __dirname, `index.html`) + `#/${open.path}?data=${encodedData}`)
			await browserWindow.loadURL(url.href)
		}

		browserWindow.webContents.setWindowOpenHandler(({ url }) => {
			void shell.openExternal(url)
			return { action: 'deny' }
		})

		appWindow = new ApplicationWindow(browserWindow, open, parent)

		browserWindow.on('close', () => {
			const homeWindow = this.windows.get('home')

			if (!preference.modal) {
				this.saveWindowData(appWindow)
			}

			if (browserWindow === homeWindow?.browserWindow || open.id === homeWindow?.data.id) {
				this.windows.delete('home')

				for (const [, value] of this.windows) {
					value.browserWindow.close()
				}

				this.windows.clear()

				homeWindow.apiProcess?.kill()
				homeWindow.apiProcess = undefined
			} else {
				for (const [key, value] of this.windows) {
					if (value.browserWindow === browserWindow || value.data.id === open.id) {
						this.windows.delete(key)
						break
					}
				}
			}
		})

		this.windows.set(open.id, appWindow)

		return appWindow
	}

	saveWindowData(window: ApplicationWindow) {
		const [x, y] = window.browserWindow.getPosition()
		const [width, height] = window.browserWindow.getSize()
		this.storage.set(`window.${window.data.id}`, { x, y, width, height })
		this.storage.save()
	}

	async createMainWindow(apiProcess?: ChildProcessWithoutNullStreams, port: number = this.port, host: string = this.host) {
		this.port = port
		this.host = host

		const open: OpenWindow = { id: 'home', path: 'home', preference: {} }
		const appWindow = await this.createWindow(open)

		const webSocket = new Client({
			brokerURL: `ws://${host}:${port}/ws`,
			onConnect: () => {
				webSocket.subscribe('NEBULOSA.EVENT', (message) => {
					const event = JSON.parse(message.body) as MessageEvent

					if (isNotificationEvent(event)) {
						this.showNotification(event)
					} else if (isConfirmationEvent(event)) {
						this.showConfirmation(event)
					} else if (event.eventName) {
						this.dispatchEvent(event)
					} else {
						console.warn('invalid message event', event)
					}
				})

				console.info('Web Socket connected')
			},
			onDisconnect: () => {
				console.warn('Web Socket disconnected')
			},
			onWebSocketClose: () => {
				console.warn('Web Socket closed')
			},
			onWebSocketError: () => {
				console.error('Web Socket error')
			},
		})

		webSocket.activate()

		appWindow.webSocket = webSocket
		appWindow.apiProcess = apiProcess
	}

	async createSplashWindow() {
		if (!this.args.serve && !this.windows.has('splash')) {
			const browserWindow = new BrowserWindow({
				width: 512,
				height: 512,
				transparent: true,
				frame: false,
				alwaysOnTop: true,
				show: false,
				resizable: false,
			})

			const url = new URL(join('file:', __dirname, 'assets', 'images', 'splash.png'))

			await browserWindow.loadURL(url.href)
			browserWindow.show()
			browserWindow.center()

			return browserWindow
		} else {
			return undefined
		}
	}

	close() {
		const splashWindow = this.windows.get('splash')
		splashWindow?.close()

		const homeWindow = this.windows.get('home')
		homeWindow?.close()
	}

	findWindow(id: number) {
		for (const [, window] of this.windows) {
			if (window.windowId === id) {
				return window
			}
		}

		return undefined
	}

	async handleFileOpen(event: Electron.IpcMainInvokeEvent, data: OpenFile) {
		const window = this.findWindow(event.sender.id)

		if (window) {
			const ret = await dialog.showOpenDialog(window.browserWindow, {
				filters: data.filters,
				properties: ['openFile'],
				defaultPath: data.defaultPath || undefined,
			})

			return !ret.canceled && ret.filePaths[0]
		} else {
			return false
		}
	}

	async handleFileSave(event: Electron.IpcMainInvokeEvent, data: OpenFile) {
		const window = this.findWindow(event.sender.id)

		if (window) {
			const ret = await dialog.showSaveDialog(window.browserWindow, {
				filters: data.filters,
				properties: ['createDirectory', 'showOverwriteConfirmation'],
				defaultPath: data.defaultPath || undefined,
			})

			return !ret.canceled && ret.filePath
		} else {
			return false
		}
	}

	async handleDirectoryOpen(event: Electron.IpcMainInvokeEvent, data: OpenDirectory) {
		const window = this.findWindow(event.sender.id)

		if (window) {
			const ret = await dialog.showOpenDialog(window.browserWindow, {
				properties: ['openDirectory'],
				defaultPath: data.defaultPath || undefined,
			})

			return !ret.canceled && ret.filePaths[0]
		} else {
			return false
		}
	}

	async handleWindowOpen(event: Electron.IpcMainInvokeEvent, data: OpenWindow) {
		if (data.preference.modal) {
			const parentWindow = this.findWindow(event.sender.id)
			const appWindow = await this.createWindow(data, parentWindow?.browserWindow)

			return new Promise<unknown>((resolve) => {
				appWindow.resolver = resolve
			})
		} else {
			const appWindow = await this.createWindow(data)

			if (data.preference.bringToFront) {
				appWindow.browserWindow.show()
			} else if (data.preference.requestFocus) {
				appWindow.browserWindow.focus()
			}

			return true
		}
	}

	handleWindowClose(event: Electron.IpcMainInvokeEvent, data: CloseWindow) {
		const window = this.findWindow(event.sender.id)

		if (window) {
			window.resolver?.(data.data)
			window.resolver = undefined
			window.close()
			return true
		} else {
			return false
		}
	}

	handleWindowResize(event: Electron.IpcMainInvokeEvent, newHeight: number) {
		const window = this.findWindow(event.sender.id)

		if (window && !window.data.preference.resizable && window.data.preference.autoResizable !== false) {
			const data = window.data

			const [width] = window.browserWindow.getSize()
			const maxHeight = screen.getPrimaryDisplay().workAreaSize.height
			const height = Math.max(data.preference.minHeight ?? 0, Math.min(newHeight, maxHeight))

			// https://github.com/electron/electron/issues/16711#issuecomment-1311824063
			window.browserWindow.setResizable(true)
			window.browserWindow.setSize(width, height)
			window.browserWindow.setResizable(this.args.serve)

			return true
		} else {
			return false
		}
	}

	handleWindowMinimize(event: Electron.IpcMainInvokeEvent) {
		const window = this.findWindow(event.sender.id)
		window?.browserWindow.minimize()
		return !!window && window.browserWindow.isMinimized()
	}

	handleWindowMaximize(event: Electron.IpcMainInvokeEvent) {
		const window = this.findWindow(event.sender.id)
		return !!window && window.toggleMaximize()
	}

	handleWindowPin(event: Electron.IpcMainInvokeEvent) {
		const window = this.findWindow(event.sender.id)
		window?.browserWindow.setAlwaysOnTop(true)
		return !!window && window.browserWindow.isAlwaysOnTop()
	}

	handleWindowUnpin(event: Electron.IpcMainInvokeEvent) {
		const window = this.findWindow(event.sender.id)
		window?.browserWindow.setAlwaysOnTop(false)
		return !!window && window.browserWindow.isAlwaysOnTop()
	}

	handleWindowFullscreen(event: Electron.IpcMainInvokeEvent, enabled?: boolean) {
		const window = this.findWindow(event.sender.id)

		if (window) {
			if (enabled) window.browserWindow.setFullScreen(true)
			else if (enabled === false) window.browserWindow.setFullScreen(false)
			else window.toggleFullscreen()
		}

		return !!window && window.browserWindow.isFullScreen()
	}

	showNotification(event: NotificationEvent) {
		if (event.target) {
			this.dispatchEvent(event)
		} else {
			new Notification({ ...event, icon: this.appIcon }).show()
		}
	}

	showConfirmation(event: ConfirmationEvent) {
		this.dispatchEvent(event)
	}

	dispatchEvent(event: MessageEvent) {
		for (const [, window] of this.windows) {
			if (window.isParent) {
				window.sendMessage(event)
			}
		}
	}

	private computeWidth(value: number | string) {
		if (typeof value === 'number') {
			return value
		} else if (value.endsWith('%')) {
			const screenSize = screen.getPrimaryDisplay().workAreaSize
			return (parseFloat(value.substring(0, value.length - 1)) * screenSize.width) / 100
		} else {
			return parseFloat(value)
		}
	}

	private computeHeight(value: number | string, width?: number) {
		if (typeof value === 'number') {
			return value
		} else if (value.endsWith('%')) {
			const screenSize = screen.getPrimaryDisplay().workAreaSize
			return (parseFloat(value.substring(0, value.length - 1)) * screenSize.height) / 100
		} else if (value.endsWith('w') && width) {
			return parseFloat(value.substring(0, value.length - 1)) * width
		} else {
			return parseFloat(value)
		}
	}
}
