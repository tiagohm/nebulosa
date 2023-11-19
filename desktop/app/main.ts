import { Client } from '@stomp/stompjs'
import { BrowserWindow, Menu, Notification, app, dialog, ipcMain, screen, shell } from 'electron'
import { ChildProcessWithoutNullStreams, spawn } from 'node:child_process'
import * as path from 'path'
import { InternalEventType, MessageEvent, NotificationEvent, OpenDirectory, OpenWindow } from './types'

import { WebSocket } from 'ws'
Object.assign(global, { WebSocket })

const browserWindows = new Map<string, BrowserWindow>()
let api: ChildProcessWithoutNullStreams | null = null
let apiPort = 7000
let wsClient: Client

const args = process.argv.slice(1)
const serve = args.some(e => e === '--serve')

app.commandLine.appendSwitch('disable-http-cache')

function createMainWindow() {
    const splashWindow = browserWindows.get('splash')
    splashWindow?.close()
    browserWindows.delete('splash')

    createWindow({ id: 'home', path: 'home' })

    wsClient = new Client({
        brokerURL: `ws://localhost:${apiPort}/ws`,
        onConnect: () => {
            wsClient.subscribe('NEBULOSA_EVENT', message => {
                const event = JSON.parse(message.body) as MessageEvent

                if (event.eventName) {
                    if (event.eventName === 'NOTIFICATION') {
                        showNotification(event as NotificationEvent)
                    } else {
                        sendToAllWindows(event.eventName, event)
                    }
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
        onWebSocketError: (e) => {
            console.error('Web Socket error', e)
        },
    })

    wsClient.activate()
}

function createWindow(data: OpenWindow<any>) {
    let window = browserWindows.get(data.id)

    if (window) {
        if (data.params) {
            console.info('params changed. id=%s, params=%s', data.id, data.params)
            window.webContents.send('PARAMS_CHANGED', data.params)
        }

        return window
    }

    const size = screen.getPrimaryDisplay().workAreaSize

    function computeWidth(value: number | string) {
        if (typeof value === 'number') {
            return value
        } else if (value.endsWith('%')) {
            return parseFloat(value.substring(0, value.length - 1)) * size.width / 100
        } else {
            return parseFloat(value)
        }
    }

    const width = data.width ? Math.trunc(computeWidth(data.width)) : 320

    function computeHeight(value: number | string) {
        if (typeof value === 'number') {
            return value
        } else if (value.endsWith('%')) {
            return parseFloat(value.substring(0, value.length - 1)) * size.height / 100
        } else if (value.endsWith('w')) {
            return parseFloat(value.substring(0, value.length - 1)) * width
        } else {
            return parseFloat(value)
        }
    }

    const height = data.height ? Math.trunc(computeHeight(data.height)) : 420

    const resizable = data.resizable ?? false
    const icon = data.icon ?? 'nebulosa'
    const params = encodeURIComponent(JSON.stringify(data.params || {}))

    window = new BrowserWindow({
        title: 'Nebulosa',
        frame: false,
        width, height,
        resizable: serve || resizable,
        autoHideMenuBar: true,
        icon: path.join(__dirname, serve ? `../src/assets/icons/${icon}.png` : `assets/icons/${icon}.png`),
        webPreferences: {
            nodeIntegration: true,
            allowRunningInsecureContent: serve,
            contextIsolation: false,
            additionalArguments: [`--port=${apiPort}`],
            preload: path.join(__dirname, 'preload.js'),
            devTools: serve,
        },
    })

    window.center()

    if (serve) {
        const debug = require('electron-debug')
        debug({ showDevTools: false })

        require('electron-reloader')(module)
        window.loadURL(`http://localhost:4200/${data.path}?params=${params}&resizable=${resizable}`)
    } else {
        const url = new URL(path.join('file:', __dirname, `index.html`) + `#/${data.path}?params=${params}&resizable=${resizable}`)
        window.loadURL(url.href)
    }

    window.webContents.setWindowOpenHandler(({ url }) => {
        shell.openExternal(url)
        return { action: 'deny' }
    })

    window.on('close', () => {
        const homeWindow = browserWindows.get('home')

        if (window === homeWindow) {
            browserWindows.delete('home')

            for (const [_, value] of browserWindows) {
                value.close()
            }

            browserWindows.clear()

            api?.kill()
        } else {
            for (const [key, value] of browserWindows) {
                if (value === window) {
                    browserWindows.delete(key)
                    break
                }
            }
        }
    })

    browserWindows.set(data.id, window)

    return window
}

function createSplashScreen() {
    let splashWindow = browserWindows.get('splash')

    if (!serve && !splashWindow) {
        splashWindow = new BrowserWindow({
            width: 512,
            height: 512,
            transparent: true,
            frame: false,
            alwaysOnTop: true,
            show: false,
        })

        const url = new URL(path.join('file:', __dirname, 'assets', 'images', 'splash.png'))
        splashWindow.loadURL(url.href)

        splashWindow.show()
        splashWindow.center()

        browserWindows.set('splash', splashWindow)
    }
}

function showNotification(event: NotificationEvent) {
    const icon = path.join(__dirname, serve ? `../src/assets/icons/nebulosa.png` : `assets/icons/nebulosa.png`)

    new Notification({ ...event, icon })
        .on('click', () => sendToAllWindows(event.type, event))
        .show()
}

function findWindowById(id: number) {
    for (const [_, window] of browserWindows) if (window.id === id) return window
    return undefined
}

function startApp() {
    if (api === null) {
        if (serve) {
            createMainWindow()
        } else {
            createSplashScreen()

            const apiJar = path.join(process.resourcesPath, 'api.jar')

            api = spawn('java', ['-jar', apiJar])

            api.stdout.on('data', (data) => {
                const text = `${data}`

                if (text) {
                    const regex = /server is started at port: (\d+)/i
                    const match = text.match(regex)

                    if (match) {
                        apiPort = parseInt(match[1])
                        api!.stdout.removeAllListeners('data')
                        console.info(`server is started at port: ${apiPort}`)
                        createMainWindow()
                    }
                }
            })

            api.on('close', (code) => {
                console.warn(`server process exited with code ${code}`)
                process.exit(code || 0)
            })
        }
    }
}

try {
    if (!serve) {
        Menu.setApplicationMenu(null)
    }

    app.on('ready', () => setTimeout(startApp, 400))

    app.on('window-all-closed', () => {
        api?.kill()

        if (process.platform !== 'darwin') {
            app.quit()
        }
    })

    app.on('activate', () => {
        const homeWindow = browserWindows.get('home')

        if (!homeWindow) {
            startApp()
        }
    })

    ipcMain.handle('OPEN_WINDOW', async (_, data: OpenWindow<any>) => {
        const newWindow = !browserWindows.has(data.id)

        const window = createWindow(data)

        if (data.bringToFront) {
            window.show()
        } else if (data.requestFocus) {
            window.focus()
        }

        return new Promise<boolean>((resolve) => {
            if (newWindow) {
                window.webContents.once('did-finish-load', () => {
                    resolve(true)
                })
            } else {
                resolve(true)
            }
        })
    })

    ipcMain.handle('OPEN_FITS', async (event) => {
        const ownerWindow = findWindowById(event.sender.id)

        const value = await dialog.showOpenDialog(ownerWindow!, {
            filters: [{ name: 'FITS files', extensions: ['fits', 'fit'] }],
            properties: ['openFile'],
        })

        return !value.canceled && value.filePaths[0]
    })

    ipcMain.handle('SAVE_FITS_AS', async (event) => {
        const ownerWindow = findWindowById(event.sender.id)
        const value = await dialog.showSaveDialog(ownerWindow!, {
            filters: [
                { name: 'FITS files', extensions: ['fits', 'fit'] },
                { name: 'Image files', extensions: ['png', 'jpe?g'] },
            ],
            properties: ['createDirectory', 'showOverwriteConfirmation'],
        })

        return !value.canceled && value.filePath
    })

    ipcMain.handle('OPEN_DIRECTORY', async (event, data?: OpenDirectory) => {
        const ownerWindow = findWindowById(event.sender.id)
        const value = await dialog.showOpenDialog(ownerWindow!, {
            properties: ['openDirectory'],
            defaultPath: data?.defaultPath,
        })

        return !value.canceled && value.filePaths[0]
    })

    ipcMain.handle('PIN_WINDOW', (event) => {
        const window = findWindowById(event.sender.id)
        window?.setAlwaysOnTop(true)
        return !!window
    })

    ipcMain.handle('UNPIN_WINDOW', (event) => {
        const window = findWindowById(event.sender.id)
        window?.setAlwaysOnTop(false)
        return !!window
    })

    ipcMain.handle('MINIMIZE_WINDOW', (event) => {
        const window = findWindowById(event.sender.id)
        window?.minimize()
        return !!window
    })

    ipcMain.handle('MAXIMIZE_WINDOW', (event) => {
        const window = findWindowById(event.sender.id)

        if (window?.isMaximized()) window.unmaximize()
        else window?.maximize()

        return window?.isMaximized() ?? false
    })

    ipcMain.handle('CLOSE_WINDOW', (event, id?: string) => {
        if (id) {
            for (const [key, value] of browserWindows) {
                if (key === id) {
                    value.close()
                    return true
                }
            }

            return false
        } else {
            const window = findWindowById(event.sender.id)
            window?.close()
            return !!window
        }
    })

    const events: InternalEventType[] = ['WHEEL_RENAMED']

    for (const item of events) {
        ipcMain.handle(item, (_, data) => {
            sendToAllWindows(item, data)
            return true
        })
    }
} catch (e) {
    console.error(e)
}

function sendToAllWindows(channel: string, data: any, home: boolean = true) {
    const homeWindow = browserWindows.get('home')

    for (const [_, window] of browserWindows) {
        if (window !== homeWindow || home) {
            window.webContents.send(channel, data)
        }
    }

    if (serve) {
        console.info(data)
    }
}
