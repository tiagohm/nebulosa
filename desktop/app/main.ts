import { type BrowserWindow, Menu, app, dialog, ipcMain } from 'electron'
import * as fs from 'fs'
import type { ChildProcessWithoutNullStreams } from 'node:child_process'
import { spawn } from 'node:child_process'
import { join } from 'path'
import type { InternalEventType, JsonFile } from '../src/shared/types/app.types'
import { ArgumentParser } from './argument.parser'
import { WindowManager } from './window.manager'

const argParser = new ArgumentParser()
const parsedArgs = argParser.parse(process.argv.slice(1))

app.commandLine.appendSwitch('disable-http-cache')
app.commandLine.appendSwitch('lang', 'en-US')

if (parsedArgs.apiMode) {
	// https://github.com/electron/electron/issues/32760#issuecomment-2227575986
	app.commandLine.appendSwitch('ignore-gpu-blacklist')
	app.commandLine.appendSwitch('disable-gpu')
	app.commandLine.appendSwitch('disable-gpu-compositing')
	app.disableHardwareAcceleration()
}

const appIcon = join(__dirname, parsedArgs.serve ? `../src/assets/icons/nebulosa.png` : `assets/icons/nebulosa.png`)
const windowManager = new WindowManager(parsedArgs, appIcon)
let api: ChildProcessWithoutNullStreams | null

process.on('beforeExit', () => {
	windowManager.close()
	api?.kill()
})

function showErrorBox(title: string, message: string) {
	dialog.showMessageBoxSync({ message, title, type: 'error' })
}

function createApiProcess(splashWindow?: BrowserWindow, port: number = parsedArgs.port) {
	const apiJar = join(process.resourcesPath, 'api.jar')

	try {
		const files = parsedArgs.files.map((e) => ['-f', e]).flat()
		const apiProcess = spawn('java', ['-jar', apiJar, `-p`, `${port}`, ...files])

		apiProcess.on('close', (code) => {
			if (code === 129) {
				splashWindow?.hide()
				showErrorBox('Failed to start', 'There is already an instance running!')
			} else {
				splashWindow?.hide()
			}

			console.warn(`api process exited with code: ${code}`)
			process.exit(code ?? 0)
		})

		apiProcess.on('error', () => {
			splashWindow?.hide()
			showErrorBox('Failed to start', 'Do you have Java 17+ installed?')
			process.exit(1)
		})

		return apiProcess
	} catch {
		splashWindow?.hide()
		showErrorBox('Failed to start', 'Do you have Java 17+ installed?')
		return process.exit(1)
	}
}

let started = false

async function startApp() {
	if (!started) {
		started = true

		try {
			if (parsedArgs.apiMode) {
				api = createApiProcess()
			} else if (parsedArgs.uiMode) {
				await windowManager.createMainWindow()
			} else if (parsedArgs.serve) {
				await windowManager.createMainWindow()
			} else {
				const splashWindow = await windowManager.createSplashWindow()

				api = createApiProcess(splashWindow)

				const regex = /server is started at port: (\d+)/i

				api.stdout.on('data', (data: Buffer) => {
					const text = data.toString('utf-8')

					if (text) {
						const match = regex.exec(text)

						if (match) {
							const port = parseInt(match[1])
							api?.stdout.removeAllListeners('data')
							console.info(`server was started at ${parsedArgs.host}@${port}`)
							splashWindow?.close()
							void windowManager.createMainWindow(api!, port)
						}
					}
				})
			}
		} catch (e) {
			console.error(e)

			api?.kill()
			showErrorBox('Failed to start', `${e}`)
			process.exit(0)
		}
	}
}

try {
	if (!parsedArgs.serve) {
		Menu.setApplicationMenu(null)
	}

	app.on('ready', () =>
		setTimeout(() => {
			void startApp()
		}, 400),
	)

	app.on('window-all-closed', () => {
		api?.kill()

		if (process.platform !== 'darwin') {
			app.quit()
		}
	})

	app.on('activate', () => {
		void startApp()
	})

	ipcMain.handle('JSON.WRITE', (_, data: JsonFile) => {
		try {
			if (data.path) {
				const json = JSON.stringify(data.json)
				fs.writeFileSync(data.path, json)
				return true
			}
		} catch (e) {
			console.error(e)
		}

		return false
	})

	ipcMain.handle('JSON.READ', (_, path: string) => {
		try {
			if (fs.existsSync(path)) {
				const buffer = fs.readFileSync(path)
				return { path, json: JSON.parse(buffer.toString('utf-8')) } as JsonFile
			}
		} catch (e) {
			console.error(e)
		}

		return false
	})

	ipcMain.handle('WINDOW.OPEN', (e, command) => windowManager.handleWindowOpen(e, command))
	ipcMain.handle('FILE.OPEN', (e, command) => windowManager.handleFileOpen(e, command))
	ipcMain.handle('FILE.SAVE', (e, command) => windowManager.handleFileSave(e, command))
	ipcMain.handle('DIRECTORY.OPEN', (e, command) => windowManager.handleDirectoryOpen(e, command))
	ipcMain.handle('WINDOW.PIN', (e, command) => windowManager.handleWindowPin(e, command))
	ipcMain.handle('WINDOW.UNPIN', (e, command) => windowManager.handleWindowUnpin(e, command))
	ipcMain.handle('WINDOW.MINIMIZE', (e, command) => windowManager.handleWindowMinimize(e, command))
	ipcMain.handle('WINDOW.MAXIMIZE', (e, command) => windowManager.handleWindowMaximize(e, command))
	ipcMain.handle('WINDOW.RESIZE', (e, command) => windowManager.handleWindowResize(e, command))
	ipcMain.handle('WINDOW.FULLSCREEN', (e, command) => windowManager.handleWindowFullscreen(e, command))
	ipcMain.handle('WINDOW.CLOSE', (e, command) => windowManager.handleWindowClose(e, command))
	ipcMain.handle('WINDOW.OPEN_DEV_TOOLS', (e, command) => windowManager.handleWindowOpenDevTools(e, command))

	const events: InternalEventType[] = ['WHEEL.RENAMED', 'LOCATION.CHANGED', 'CALIBRATION.CHANGED', 'ROI.SELECTED']

	for (const eventName of events) {
		ipcMain.handle(eventName, (_, data) => {
			windowManager.dispatchEvent({ ...data, eventName })
			return true
		})
	}
} catch (e) {
	console.error(e)
}
