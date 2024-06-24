import { Menu, app, ipcMain } from 'electron'
import * as fs from 'fs'
import type { ChildProcessWithoutNullStreams } from 'node:child_process'
import { spawn } from 'node:child_process'
import { join, resolve } from 'path'
import { WebSocket } from 'ws'
import type { InternalEventType, JsonFile, StoredWindowData } from '../src/shared/types/app.types'
import { ArgumentParser } from './argument.parser'
import { LocalStorage } from './local.storage'
import { WindowManager } from './window.manager'

Object.assign(global, { WebSocket })

app.commandLine.appendSwitch('disable-http-cache')

const argParser = new ArgumentParser()
const parsedArgs = argParser.parse(process.argv.slice(1))
const configPath = resolve(app.getPath('userData'), 'config.json')
const storage = new LocalStorage<StoredWindowData>(configPath)
const appIcon = join(__dirname, parsedArgs.serve ? `../src/assets/icons/nebulosa.png` : `assets/icons/nebulosa.png`)
const windowManager = new WindowManager(parsedArgs, storage, appIcon)
let apiProcess: ChildProcessWithoutNullStreams | null

process.on('beforeExit', () => {
	windowManager.close()
	apiProcess?.kill()
})

function createApiProcess(port: number = parsedArgs.port) {
	const apiJar = join(process.resourcesPath, 'api.jar')
	const apiProcess = spawn('java', ['-jar', apiJar, `--server.port=${port}`])

	apiProcess.on('close', (code) => {
		console.warn(`server process exited with code: ${code}`)
		process.exit(code ?? 0)
	})

	return apiProcess
}

let started = false

async function startApp() {
	if (!started) {
		started = true

		try {
			if (parsedArgs.apiMode) {
				apiProcess = createApiProcess()
			} else if (parsedArgs.uiMode) {
				await windowManager.createMainWindow()
			} else if (parsedArgs.serve) {
				await windowManager.createMainWindow()
			} else {
				const splashWindow = await windowManager.createSplashWindow()

				apiProcess = createApiProcess()

				apiProcess.stdout.on('data', (data: Buffer) => {
					const text = data.toString('utf-8')

					console.info(text)

					if (text) {
						const regex = /server is started at port: (\d+)/i
						const match = text.match(regex)

						if (match) {
							const port = parseInt(match[1])
							apiProcess?.stdout.removeAllListeners('data')
							console.info(`server was started at ${parsedArgs.host}@${port}`)
							splashWindow?.close()
							void windowManager.createMainWindow(apiProcess!, port)
						}
					}
				})
			}
		} catch (e) {
			console.error(e)

			apiProcess?.kill()
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
		apiProcess?.kill()

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

	ipcMain.handle('WINDOW.OPEN', (e, data) => windowManager.handleWindowOpen(e, data))
	ipcMain.handle('FILE.OPEN', (e, data) => windowManager.handleFileOpen(e, data))
	ipcMain.handle('FILE.SAVE', (e, data) => windowManager.handleFileSave(e, data))
	ipcMain.handle('DIRECTORY.OPEN', (e, data) => windowManager.handleDirectoryOpen(e, data))
	ipcMain.handle('WINDOW.PIN', (e) => windowManager.handleWindowPin(e))
	ipcMain.handle('WINDOW.UNPIN', (e) => windowManager.handleWindowUnpin(e))
	ipcMain.handle('WINDOW.MINIMIZE', (e) => windowManager.handleWindowMinimize(e))
	ipcMain.handle('WINDOW.MAXIMIZE', (e) => windowManager.handleWindowMaximize(e))
	ipcMain.handle('WINDOW.RESIZE', (e, data) => windowManager.handleWindowResize(e, data))
	ipcMain.handle('WINDOW.FULLSCREEN', (e, data) => windowManager.handleWindowFullscreen(e, data))
	ipcMain.handle('WINDOW.CLOSE', (e, data) => windowManager.handleWindowClose(e, data))

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
