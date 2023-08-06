import { Client } from '@stomp/stompjs'
import { app, BrowserWindow, dialog, ipcMain, Menu, screen, shell } from 'electron'
import { ChildProcessWithoutNullStreams, spawn } from 'node:child_process'
import * as path from 'path'
import { Camera, FilterWheel, Focuser, INDI_EVENT_TYPES, INTERNAL_EVENT_TYPES, Mount, OpenWindow } from './types'

import { WebSocket } from 'ws'
Object.assign(global, { WebSocket })

let homeWindow: BrowserWindow | null = null
const secondaryWindows = new Map<string, BrowserWindow>()
let api: ChildProcessWithoutNullStreams | null = null
let apiPort = 7000
let wsClient: Client

let selectedCamera: Camera
let selectedMount: Mount
let selectedFocuser: Focuser
let selectedFilterWheel: FilterWheel

const args = process.argv.slice(1)
const serve = args.some(e => e === '--serve')

app.commandLine.appendSwitch('disable-http-cache')

function createMainWindow() {
    createWindow({ id: 'home', path: 'home' })

    wsClient = new Client({
        brokerURL: `ws://localhost:${apiPort}/ws`,
        onConnect: () => {
            for (const item of INDI_EVENT_TYPES) {
                if (item === 'ALL' || item === 'DEVICE' || item === 'CAMERA' ||
                    item === 'FOCUSER' || item === 'MOUNT') {
                    continue
                }

                wsClient.subscribe(item, (message) => {
                    const data = JSON.parse(message.body)

                    if (serve) {
                        console.log(item, message.body)
                    }

                    sendToAllWindows(item, data)
                })
            }
        },
    })

    wsClient.activate()
}

function createWindow(data: OpenWindow<any>) {
    if (secondaryWindows.has(data.id)) {
        const window = secondaryWindows.get(data.id)!

        if (data.params) {
            window.webContents.send('PARAMS_CHANGED', data.params)
        }

        return window
    } else if (data.id === 'home' && homeWindow) {
        return homeWindow
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

    const height = data.height ? Math.trunc(computeHeight(data.height)) : 384

    const resizable = data.resizable ?? false
    const icon = data.icon ?? 'nebulosa'
    const params = encodeURIComponent(JSON.stringify(data.params || {}))

    const window = new BrowserWindow({
        x: size.width / 2 - width / 2,
        y: size.height / 2 - height / 2,
        width, height,
        resizable: serve || resizable,
        autoHideMenuBar: true,
        title: 'Nebulosa',
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

    window.setContentSize(width, height)

    if (serve) {
        const debug = require('electron-debug')
        debug()

        require('electron-reloader')(module)
        window.loadURL(`http://localhost:4200/${data.path}?params=${params}`)
    } else {
        const url = new URL(path.join('file:', __dirname, `index.html#/${data.path}?params=${params}`))
        window.loadURL(url.href)
    }

    window.webContents.setWindowOpenHandler(({ url }) => {
        shell.openExternal(url)
        return { action: 'deny' }
    })

    window.on('close', () => {
        if (window === homeWindow) {
            for (const [_, value] of secondaryWindows) {
                value.close()
            }

            homeWindow = null

            api?.kill('SIGHUP')
        } else {
            for (const [key, value] of secondaryWindows) {
                if (value === window) {
                    secondaryWindows.delete(key)
                    break
                }
            }
        }
    })

    if (data.id === 'home') {
        homeWindow = window
    } else {
        secondaryWindows.set(data.id, window)
    }

    return window
}

function startApp() {
    if (api === null) {
        if (serve) {
            createMainWindow()
        } else {
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
                        console.log(`server is started at port: ${apiPort}`)
                        createMainWindow()
                    }
                }
            })

            api.on('close', (code) => {
                console.log(`server process exited with code ${code}`)
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
        if (process.platform !== 'darwin') {
            app.quit()
        }
    })

    app.on('activate', () => {
        if (homeWindow === null) {
            startApp()
        }
    })

    ipcMain.handle('OPEN_WINDOW', async (_, data: OpenWindow<any>) => {
        const newWindow = !secondaryWindows.has(data.id)

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

    ipcMain.on('OPEN_FITS', async (event) => {
        const value = await dialog.showOpenDialog(homeWindow!, {
            filters: [{ name: 'FITS files', extensions: ['fits', 'fit'] }],
            properties: ['openFile'],
        })

        event.returnValue = !value.canceled && value.filePaths[0]
    })

    ipcMain.on('SAVE_FITS_AS', async (event) => {
        const value = await dialog.showSaveDialog(homeWindow!, {
            filters: [
                { name: 'FITS files', extensions: ['fits', 'fit'] },
                { name: 'Image files', extensions: ['png', 'jpe?g'] },
            ],
            properties: ['createDirectory', 'showOverwriteConfirmation'],
        })

        event.returnValue = !value.canceled && value.filePath
    })

    ipcMain.on('OPEN_DIRECTORY', async (event) => {
        const value = await dialog.showOpenDialog(homeWindow!, {
            properties: ['openDirectory'],
        })

        event.returnValue = !value.canceled && value.filePaths[0]
    })

    ipcMain.on('CLOSE_WINDOW', (event, id: string) => {
        for (const [key, value] of secondaryWindows) {
            if (key === id) {
                value.close()
                event.returnValue = true
                return
            }
        }

        event.returnValue = false
    })

    for (const item of INTERNAL_EVENT_TYPES) {
        ipcMain.on(item, (event, data) => {
            switch (item) {
                case 'CAMERA_CHANGED':
                    selectedCamera = data
                    break
                case 'MOUNT_CHANGED':
                    selectedMount = data
                    break
                case 'FOCUSER_CHANGED':
                    selectedFocuser = data
                    break
                case 'FILTER_WHEEL_CHANGED':
                    selectedFilterWheel = data
                    break
            }

            switch (item) {
                case 'SELECTED_CAMERA':
                    event.returnValue = selectedCamera
                    break
                case 'SELECTED_MOUNT':
                    event.returnValue = selectedMount
                    break
                case 'SELECTED_FOCUSER':
                    event.returnValue = selectedFocuser
                    break
                case 'SELECTED_FILTER_WHEEL':
                    event.returnValue = selectedFilterWheel
                    break
                default:
                    sendToAllWindows(item, data)
                    break
            }
        })
    }
} catch (e) {
    console.error(e)
}

function sendToAllWindows(channel: string, data: any, home: boolean = true) {
    for (const [_, value] of secondaryWindows) {
        value.webContents.send(channel, data)
    }

    if (home) {
        homeWindow?.webContents?.send(channel, data)
    }

    if (serve) {
        console.log(channel, data)
    }
}
