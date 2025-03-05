import type { IpcMainInvokeEvent, OpenDialogOptions, Rectangle, WebContents } from 'electron'
import electron, { Notification, app, dialog, screen, shell } from 'electron'
import Store from 'electron-store'
import type { ChildProcessWithoutNullStreams } from 'node:child_process'
import { existsSync, statSync } from 'node:fs'
import { join } from 'path'
import { WebSocket } from 'ws'
import type { ConfirmationEvent, MessageEvent, NotificationEvent } from '../src/shared/types/api.types'
import type { CloseWindowCommand, FullscreenWindowCommand, OpenDirectoryCommand, OpenFileCommand, OpenWindowCommand, ResizeWindowCommand, WindowCommand } from '../src/shared/types/app.types'
import type { ParsedArgument } from './argument.parser'

export interface BrowserWindow extends electron.BrowserWindow {
	command?: OpenWindowCommand
	parent?: BrowserWindow
	ws?: WebSocket
	api?: ChildProcessWithoutNullStreams
	resolver?: (data: unknown) => void
}

// eslint-disable-next-line @typescript-eslint/consistent-indexed-object-style
export interface WindowInfo {
	[key: `window.${string}`]: Rectangle | undefined
}

const store = new Store<WindowInfo>({ name: 'nebulosa' })

export function isParent(window: BrowserWindow) {
	return !window.parent
}

export function isModal(window: BrowserWindow) {
	return !!window.parent && window.command?.id.endsWith('.modal')
}

export function isHome(window: BrowserWindow) {
	return !window.ws || window.command?.id === 'home'
}

export function sendMessage(window: BrowserWindow, event: MessageEvent) {
	window.webContents.send(event.eventName, event)
}

export class ApplicationWindow {
	constructor(
		public readonly browserWindow: BrowserWindow,
		public readonly data: OpenWindowCommand,
		public readonly parentWindow?: BrowserWindow,
		public webSocket?: WebSocket,
		public apiProcess?: ChildProcessWithoutNullStreams,
		public resolver?: (data: unknown) => void,
	) {}

	get electronId() {
		return this.browserWindow.id
	}

	get id() {
		return this.data.id
	}

	close() {
		this.browserWindow.close()
	}

	toggleFullscreen() {
		this.browserWindow.setFullScreen(!this.browserWindow.isFullScreen())
	}

	toggleMaximize() {}

	sendMessage(event: MessageEvent) {
		this.browserWindow.webContents.send(event.eventName, event)
	}

	openImage(path: string) {
		this.sendMessage({ eventName: 'IMAGE.OPEN', path } as never)
	}
}

export function isNotificationEvent(event: MessageEvent): event is NotificationEvent {
	return event.eventName === 'NOTIFICATION'
}

export function isConfirmationEvent(event: MessageEvent): event is ConfirmationEvent {
	return event.eventName === 'CONFIRMATION'
}

export class WindowManager {
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

	async createWindow(command: OpenWindowCommand, parent?: BrowserWindow) {
		let window = this.findWindow(command.id)

		if (window) {
			if (command.data) {
				window.webContents.send('DATA.CHANGED', command.data)
			}

			return window
		}

		const preference = command.preference

		const encodedPreference = encodeURIComponent(JSON.stringify(preference))
		const encodedData = encodeURIComponent(JSON.stringify(command.data ?? {}))

		const minWidth = preference.minWidth ?? 100
		const computedWidth = preference.width ? Math.trunc(this.computeWidth(preference.width)) : 320
		const minHeight = preference.minHeight ?? 100
		const computedHeight = preference.height ? Math.trunc(this.computeHeight(preference.height, computedWidth)) : 412

		const screenSize = screen.getPrimaryDisplay().workAreaSize
		const data = store.get(`window.${command.id}`)
		const resizable = preference.resizable
		const width = resizable ? Math.max(minWidth, Math.min(data?.width ?? computedWidth, screenSize.width)) : computedWidth
		const height = resizable ? Math.max(minHeight, Math.min(data?.height ?? computedHeight, screenSize.height)) : computedHeight
		const x = Math.max(0, Math.min(data?.x ?? 0, screenSize.width - width))
		const y = Math.max(0, Math.min(data?.y ?? 0, screenSize.height - height))

		window = new electron.BrowserWindow({
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
				additionalArguments: [`--host=${this.host}`, `--port=${this.port}`, `--id=${command.id}`, `--data=${encodedData}`, `--preference=${encodedPreference}`],
				preload: join(__dirname, 'preload.js'),
				devTools: this.args.serve || this.args.devTools,
				spellcheck: false,
			},
		})

		window.command = command
		window.parent = parent

		if (this.args.devTools) {
			window.webContents.openDevTools({ mode: 'detach' })
		}

		window.on('ready-to-show', () => {
			window.show()

			if (!data) {
				window.center()
			}
		})

		if (this.args.serve) {
			await window.loadURL(`http://localhost:4200/${command.path}?data=${encodedData}`)
		} else {
			const url = new URL('file://' + join(__dirname, `index.html`) + `#/${command.path}?data=${encodedData}`)
			await window.loadURL(url.href)
		}

		window.webContents.setWindowOpenHandler(({ url }) => {
			void shell.openExternal(url)
			return { action: 'deny' }
		})

		window.on('close', () => {
			const home = this.findWindow('home')

			if (!preference.modal) {
				this.saveWindowData(window)
			}

			if (window === home || command.id === home?.command?.id) {
				for (const value of electron.BrowserWindow.getAllWindows()) {
					if (value !== window) {
						value.close()
					}
				}

				home.api?.kill()
				home.api = undefined
			}
		})

		return window
	}

	saveWindowData(window: BrowserWindow) {
		if (window.command && !window.command.id.endsWith('!')) {
			const [x, y] = window.getPosition()
			const [width, height] = window.getSize()
			store.set(`window.${window.command.id}`, { x, y, width, height })
		}
	}

	private createWebSocket(host: string, port: number, success: (webSocket: WebSocket) => void) {
		const ws = new WebSocket(`ws://${host}:${port}/ws`)

		const reconnect = () => {
			setTimeout(() => this.createWebSocket(host, port, success), 5000)
		}

		ws.on('open', () => {
			console.info('Web Socket connected')
			success(ws)
		})

		ws.on('message', (data: Buffer) => {
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

		ws.on('close', (code, reason) => {
			console.warn('Web Socket closed', code, reason.toString())
			reconnect()
		})

		ws.on('error', () => {
			console.error('Web Socket error')
		})

		return ws
	}

	async createMainWindow(api?: ChildProcessWithoutNullStreams, port: number = this.port, host: string = this.host) {
		this.port = port
		this.host = host

		const command: OpenWindowCommand = { id: 'home', path: 'home', preference: {} }
		const window = await this.createWindow(command)

		this.createWebSocket(host, port, (webSocket) => {
			window.ws = webSocket
		})

		window.api = api

		if (app.isPackaged) {
			for (const path of this.args.files) {
				if (path !== '.' && existsSync(path) && statSync(path).isFile()) {
					console.info('opening image at', path)
					sendMessage(window, { eventName: 'IMAGE.OPEN', path } as never)
				}
			}
		}
	}

	async createSplashWindow() {
		if (!this.args.serve) {
			const browserWindow = new electron.BrowserWindow({
				width: 512,
				height: 512,
				transparent: true,
				frame: false,
				alwaysOnTop: true,
				show: false,
				resizable: false,
			})

			const url = new URL('file://' + join(__dirname, 'assets', 'images', 'splash.png'))

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
		const homeWindow = this.findWindow('home')
		homeWindow?.close()
	}

	findWindow(id?: number | string | null) {
		if (id) {
			for (const window of electron.BrowserWindow.getAllWindows()) {
				const w = window as unknown as BrowserWindow

				if (w.id === id || w.command?.id === id) {
					console.log('found window:', w.id, id)
					return w
				}
			}
		}

		console.log('unable to find window:', id)

		return undefined
	}

	findWindowWith(command: WindowCommand, sender: WebContents) {
		return this.findWindow(command.windowId) ?? this.findWindow(sender.id)
	}

	async handleFileOpen(event: IpcMainInvokeEvent, command: OpenFileCommand) {
		const window = this.findWindowWith(command, event.sender)

		if (window) {
			const properties: OpenDialogOptions['properties'] = ['openFile']

			if (command.multiple) {
				properties.push('multiSelections')
			}

			const ret = await dialog.showOpenDialog(window, {
				filters: command.filters,
				properties,
				defaultPath: command.defaultPath || undefined,
			})

			return !ret.canceled && (command.multiple ? ret.filePaths : ret.filePaths[0])
		} else {
			return false
		}
	}

	async handleFileSave(event: IpcMainInvokeEvent, command: OpenFileCommand) {
		const window = this.findWindowWith(command, event.sender)

		if (window) {
			const ret = await dialog.showSaveDialog(window, {
				filters: command.filters,
				properties: ['createDirectory', 'showOverwriteConfirmation'],
				defaultPath: command.defaultPath || undefined,
			})

			return !ret.canceled && ret.filePath
		} else {
			return false
		}
	}

	async handleDirectoryOpen(event: IpcMainInvokeEvent, command: OpenDirectoryCommand) {
		const window = this.findWindowWith(command, event.sender)

		if (window) {
			const ret = await dialog.showOpenDialog(window, {
				properties: ['openDirectory'],
				defaultPath: command.defaultPath || undefined,
			})

			return !ret.canceled && ret.filePaths[0]
		} else {
			return false
		}
	}

	async handleWindowOpen(event: IpcMainInvokeEvent, command: OpenWindowCommand) {
		if (command.preference.modal) {
			const parent = this.findWindowWith(command, event.sender)
			const window = await this.createWindow(command, parent)

			return new Promise<unknown>((resolve) => {
				window.resolver = resolve
			})
		} else {
			const window = await this.createWindow(command)

			if (command.preference.bringToFront) {
				window.show()
			} else if (command.preference.requestFocus) {
				window.focus()
			}

			return true
		}
	}

	handleWindowClose(event: IpcMainInvokeEvent, command: CloseWindowCommand) {
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

	handleWindowResize(event: IpcMainInvokeEvent, command: ResizeWindowCommand) {
		const window = this.findWindowWith(command, event.sender)

		if (window?.command && !window.command.preference.resizable && window.command.preference.autoResizable !== false) {
			const [width] = window.getSize()
			const maxHeight = screen.getPrimaryDisplay().workAreaSize.height
			const height = Math.max(window.command.preference.minHeight ?? 0, Math.min(command.height, maxHeight))

			// https://github.com/electron/electron/issues/16711#issuecomment-1311824063
			window.setResizable(true)
			window.setSize(width, height)
			window.setResizable(this.args.serve)

			return true
		} else {
			return false
		}
	}

	handleWindowMinimize(event: IpcMainInvokeEvent, command: WindowCommand) {
		const window = this.findWindowWith(command, event.sender)
		window?.minimize()
		return !!window && window.isMinimized()
	}

	handleWindowMaximize(event: IpcMainInvokeEvent, command: WindowCommand) {
		const window = this.findWindowWith(command, event.sender)

		if (!window) return false

		if (window.isMaximized()) {
			window.unmaximize()
		} else {
			window.maximize()
		}

		return window.isMaximized()
	}

	handleWindowPin(event: IpcMainInvokeEvent, command: WindowCommand) {
		const window = this.findWindowWith(command, event.sender)
		window?.setAlwaysOnTop(true)
		return !!window && window.isAlwaysOnTop()
	}

	handleWindowUnpin(event: IpcMainInvokeEvent, command: WindowCommand) {
		const window = this.findWindowWith(command, event.sender)
		window?.setAlwaysOnTop(false)
		return !!window && window.isAlwaysOnTop()
	}

	handleWindowFullscreen(event: IpcMainInvokeEvent, command: FullscreenWindowCommand) {
		const window = this.findWindowWith(command, event.sender)

		if (window) {
			if (command.enabled) window.setFullScreen(true)
			else if (command.enabled === false) window.setFullScreen(false)
			else window.setFullScreen(!window.isFullScreen())
		}

		return !!window && window.isFullScreen()
	}

	handleWindowOpenDevTools(event: IpcMainInvokeEvent, command: WindowCommand) {
		const window = this.findWindowWith(command, event.sender)
		window?.webContents.openDevTools({ mode: 'detach' })
		return !!window
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
		for (const window of electron.BrowserWindow.getAllWindows()) {
			if (!parentOnly || isParent(window)) {
				sendMessage(window, event)
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
