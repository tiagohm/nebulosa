import type { Rectangle } from 'electron'
import { BrowserWindow, Notification, dialog, screen, shell } from 'electron'
import Store from 'electron-store'
import type { ChildProcessWithoutNullStreams } from 'node:child_process'
import { join } from 'path'
import { WebSocket } from 'ws'
import type { MessageEvent } from '../src/shared/types/api.types'
import type { CloseWindow, ConfirmationEvent, FullscreenWindow, NotificationEvent, OpenDirectory, OpenFile, OpenWindow, ResizeWindow, WindowCommand } from '../src/shared/types/app.types'
import type { Nullable } from '../src/shared/utils/types'
import type { ParsedArgument } from './argument.parser'

// eslint-disable-next-line @typescript-eslint/consistent-indexed-object-style
export interface WindowInfo {
	[key: `window.${string}`]: Rectangle | undefined
}

const store = new Store<WindowInfo>({ name: 'nebulosa' })

export class ApplicationWindow {
	constructor(
		public readonly browserWindow: BrowserWindow,
		public readonly data: OpenWindow,
		public readonly parentWindow?: BrowserWindow,
		public webSocket?: WebSocket,
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

	get electronId() {
		return this.browserWindow.id
	}

	get windowId() {
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
		const data = store.get(`window.${open.id}`)
		const resizable = preference.resizable
		const width = resizable ? Math.max(minWidth, Math.min(data?.width ?? computedWidth, screenSize.width)) : computedWidth
		const height = resizable ? Math.max(minHeight, Math.min(data?.height ?? computedHeight, screenSize.height)) : computedHeight
		const x = Math.max(0, Math.min(data?.x ?? 0, screenSize.width - width))
		const y = Math.max(0, Math.min(data?.y ?? 0, screenSize.height - height))

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
			resizable: this.args.serve || resizable,
			autoHideMenuBar: true,
			icon: preference.icon ? join(__dirname, this.args.serve ? `../src/assets/icons/${preference.icon}.png` : `assets/icons/${preference.icon}.png`) : this.appIcon,
			show: false,
			webPreferences: {
				nodeIntegration: true,
				allowRunningInsecureContent: this.args.serve,
				contextIsolation: true,
				additionalArguments: [`--host=${this.host}`, `--port=${this.port}`, `--id=${open.id}`, `--data=${encodedData}`, `--preference=${encodedPreference}`],
				preload: join(__dirname, 'preload.js'),
				devTools: this.args.serve,
			},
		})

		browserWindow.on('ready-to-show', () => {
			browserWindow.show()

			if (!data) {
				browserWindow.center()
			}
		})

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
		if (!window.data.id.endsWith('!')) {
			const [x, y] = window.browserWindow.getPosition()
			const [width, height] = window.browserWindow.getSize()
			store.set(`window.${window.data.id}`, { x, y, width, height })
		}
	}

	private createWebSocket(host: string, port: number, connected: (webSocket: WebSocket) => void) {
		const webSocket = new WebSocket(`ws://${host}:${port}/ws`)

		const reconnect = () => {
			setTimeout(() => this.createWebSocket(host, port, connected), 2000)
		}

		webSocket.on('open', () => {
			console.info('Web Socket connected')
			connected(webSocket)
		})

		webSocket.on('message', (data: Buffer) => {
			const event = JSON.parse(data.toString()) as MessageEvent

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

		webSocket.on('close', (code, reason) => {
			console.warn('Web Socket closed', code, reason.toString())
			reconnect()
		})

		webSocket.on('error', () => {
			console.error('Web Socket error')
		})

		return webSocket
	}

	async createMainWindow(apiProcess?: ChildProcessWithoutNullStreams, port: number = this.port, host: string = this.host) {
		this.port = port
		this.host = host

		const open: OpenWindow = { id: 'home', path: 'home', preference: {} }
		const appWindow = await this.createWindow(open)

		this.createWebSocket(host, port, (webSocket) => (appWindow.webSocket = webSocket))

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

			browserWindow.on('ready-to-show', () => {
				browserWindow.show()
				browserWindow.center()
			})

			await browserWindow.loadURL(url.href)

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

	findWindow(id: Nullable<number | string>) {
		if (id) {
			for (const [, window] of this.windows) {
				if (window.electronId === id || window.windowId === id) {
					return window
				}
			}
		}

		return undefined
	}

	findWindowWith(command: WindowCommand, sender: Electron.WebContents) {
		return this.findWindow(command.windowId) ?? this.findWindow(sender.id)
	}

	async handleFileOpen(event: Electron.IpcMainInvokeEvent, command: OpenFile) {
		const window = this.findWindowWith(command, event.sender)

		if (window) {
			const properties: Electron.OpenDialogOptions['properties'] = ['openFile']

			if (command.multiple) {
				properties.push('multiSelections')
			}

			const ret = await dialog.showOpenDialog(window.browserWindow, {
				filters: command.filters,
				properties,
				defaultPath: command.defaultPath || undefined,
			})

			return !ret.canceled && (command.multiple ? ret.filePaths : ret.filePaths[0])
		} else {
			return false
		}
	}

	async handleFileSave(event: Electron.IpcMainInvokeEvent, command: OpenFile) {
		const window = this.findWindowWith(command, event.sender)

		if (window) {
			const ret = await dialog.showSaveDialog(window.browserWindow, {
				filters: command.filters,
				properties: ['createDirectory', 'showOverwriteConfirmation'],
				defaultPath: command.defaultPath || undefined,
			})

			return !ret.canceled && ret.filePath
		} else {
			return false
		}
	}

	async handleDirectoryOpen(event: Electron.IpcMainInvokeEvent, command: OpenDirectory) {
		const window = this.findWindowWith(command, event.sender)

		if (window) {
			const ret = await dialog.showOpenDialog(window.browserWindow, {
				properties: ['openDirectory'],
				defaultPath: command.defaultPath || undefined,
			})

			return !ret.canceled && ret.filePaths[0]
		} else {
			return false
		}
	}

	async handleWindowOpen(event: Electron.IpcMainInvokeEvent, command: OpenWindow) {
		if (command.preference.modal) {
			const parentWindow = this.findWindowWith(command, event.sender)
			const appWindow = await this.createWindow(command, parentWindow?.browserWindow)

			return new Promise<unknown>((resolve) => {
				appWindow.resolver = resolve
			})
		} else {
			const appWindow = await this.createWindow(command)

			if (command.preference.bringToFront) {
				appWindow.browserWindow.show()
			} else if (command.preference.requestFocus) {
				appWindow.browserWindow.focus()
			}

			return true
		}
	}

	handleWindowClose(event: Electron.IpcMainInvokeEvent, command: CloseWindow) {
		const window = this.findWindowWith(command, event.sender)

		if (window) {
			window.resolver?.(command.data)
			window.resolver = undefined
			window.close()
			return true
		} else {
			return false
		}
	}

	handleWindowResize(event: Electron.IpcMainInvokeEvent, command: ResizeWindow) {
		const window = this.findWindowWith(command, event.sender)

		if (window && !window.data.preference.resizable && window.data.preference.autoResizable !== false) {
			const [width] = window.browserWindow.getSize()
			const maxHeight = screen.getPrimaryDisplay().workAreaSize.height
			const height = Math.max(window.data.preference.minHeight ?? 0, Math.min(command.height, maxHeight))

			// https://github.com/electron/electron/issues/16711#issuecomment-1311824063
			window.browserWindow.setResizable(true)
			window.browserWindow.setSize(width, height)
			window.browserWindow.setResizable(this.args.serve)

			return true
		} else {
			return false
		}
	}

	handleWindowMinimize(event: Electron.IpcMainInvokeEvent, command: WindowCommand) {
		const window = this.findWindowWith(command, event.sender)
		window?.browserWindow.minimize()
		return !!window && window.browserWindow.isMinimized()
	}

	handleWindowMaximize(event: Electron.IpcMainInvokeEvent, command: WindowCommand) {
		const window = this.findWindowWith(command, event.sender)
		return !!window && window.toggleMaximize()
	}

	handleWindowPin(event: Electron.IpcMainInvokeEvent, command: WindowCommand) {
		const window = this.findWindowWith(command, event.sender)
		window?.browserWindow.setAlwaysOnTop(true)
		return !!window && window.browserWindow.isAlwaysOnTop()
	}

	handleWindowUnpin(event: Electron.IpcMainInvokeEvent, command: WindowCommand) {
		const window = this.findWindowWith(command, event.sender)
		window?.browserWindow.setAlwaysOnTop(false)
		return !!window && window.browserWindow.isAlwaysOnTop()
	}

	handleWindowFullscreen(event: Electron.IpcMainInvokeEvent, command: FullscreenWindow) {
		const window = this.findWindowWith(command, event.sender)

		if (window) {
			if (command.enabled) window.browserWindow.setFullScreen(true)
			else if (command.enabled === false) window.browserWindow.setFullScreen(false)
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

	dispatchEvent(event: MessageEvent, parentOnly: boolean = false) {
		for (const [, window] of this.windows) {
			if (!parentOnly || window.isParent) {
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
