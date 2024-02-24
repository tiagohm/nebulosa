import { Client } from '@stomp/stompjs'
import { BrowserWindow, Menu, Notification, app, dialog, ipcMain, screen, shell } from 'electron'
import * as ElectronStore from 'electron-store'
import * as fs from 'fs'
import { ChildProcessWithoutNullStreams, spawn } from 'node:child_process'
import * as path from 'path'

import { WebSocket } from 'ws'
import { MessageEvent } from '../src/shared/types/api.types'
import { CloseWindow, InternalEventType, JsonFile, NotificationEvent, OpenDirectory, OpenFile, OpenWindow } from '../src/shared/types/app.types'
Object.assign(global, { WebSocket })

const store = new ElectronStore()

const browserWindows = new Map<string, BrowserWindow>()
const modalWindows = new Map<string, { window: BrowserWindow, resolve: (data: any) => void }>()
let api: ChildProcessWithoutNullStreams | null = null
let apiPort = 7000
let webSocket: Client

const args = process.argv.slice(1)
const serve = args.some(e => e === '--serve')

app.commandLine.appendSwitch('disable-http-cache')

function createMainWindow() {
    browserWindows.get('splash')?.close()
    browserWindows.delete('splash')

    createWindow({ id: 'home', path: 'home', data: undefined })

    webSocket = new Client({
        brokerURL: `ws://localhost:${apiPort}/ws`,
        onConnect: () => {
            webSocket.subscribe('NEBULOSA.EVENT', message => {
                const event = JSON.parse(message.body) as MessageEvent

                if (event.eventName) {
                    if (event.eventName === 'NOTIFICATION.SENT') {
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
        onWebSocketError: () => {
            console.error('Web Socket error')
        },
    })

    webSocket.activate()
}

function createWindow(options: OpenWindow<any>, parent?: BrowserWindow) {
    let window = browserWindows.get(options.id)

    if (window && !options.modal) {
        if (options.data) {
            console.info('window data changed. id=%s, data=%s', options.id, options.data)
            window.webContents.send('DATA.CHANGED', options.data)
        }

        return window
    }

    const screenSize = screen.getPrimaryDisplay().workAreaSize

    function computeWidth(value: number | string) {
        if (typeof value === 'number') {
            return value
        } else if (value.endsWith('%')) {
            return parseFloat(value.substring(0, value.length - 1)) * screenSize.width / 100
        } else {
            return parseFloat(value)
        }
    }

    const width = options.width ? Math.trunc(computeWidth(options.width)) : 320

    function computeHeight(value: number | string) {
        if (typeof value === 'number') {
            return value
        } else if (value.endsWith('%')) {
            return parseFloat(value.substring(0, value.length - 1)) * screenSize.height / 100
        } else if (value.endsWith('w')) {
            return parseFloat(value.substring(0, value.length - 1)) * width
        } else {
            return parseFloat(value)
        }
    }

    const height = options.height ? Math.trunc(computeHeight(options.height)) : 416

    const resizable = options.resizable ?? false
    const icon = options.icon ?? 'nebulosa'
    const data = encodeURIComponent(JSON.stringify(options.data || {}))

    const savedPos = !options.modal ? store.get(`window.${options.id}.position`, undefined) as { x: number, y: number } | undefined : undefined
    const savedSize = !options.modal && options.resizable ? store.get(`window.${options.id}.size`, undefined) as { width: number, height: number } | undefined : undefined

    if (savedPos) {
        savedPos.x = Math.max(0, Math.min(savedPos.x, screenSize.width))
        savedPos.y = Math.max(0, Math.min(savedPos.y, screenSize.height))
    }

    if (savedSize) {
        savedSize.width = Math.max(0, Math.min(savedSize.width, screenSize.width))
        savedSize.height = Math.max(0, Math.min(savedSize.height, screenSize.height))
    }

    window = new BrowserWindow({
        title: 'Nebulosa',
        frame: false,
        modal: options.modal,
        parent,
        width: savedSize?.width || width,
        height: savedSize?.height || height,
        x: savedPos?.x ?? undefined,
        y: savedPos?.y ?? undefined,
        resizable: serve || resizable,
        autoHideMenuBar: true,
        icon: path.join(__dirname, serve ? `../src/assets/icons/${icon}.png` : `assets/icons/${icon}.png`),
        webPreferences: {
            nodeIntegration: true,
            allowRunningInsecureContent: serve,
            contextIsolation: false,
            additionalArguments: [`--port=${apiPort}`, `--id=${options.id}`, `--modal=${options.modal ?? false}`],
            preload: path.join(__dirname, 'preload.js'),
            devTools: serve,
        },
    })

    if (!savedPos) {
        window.center()
    }

    if (serve) {
        const debug = require('electron-debug')
        debug({ showDevTools: false })

        require('electron-reloader')(module)
        window.loadURL(`http://localhost:4200/${options.path}?data=${data}&resizable=${resizable}`)
    } else {
        const url = new URL(path.join('file:', __dirname, `index.html`) + `#/${options.path}?data=${data}&resizable=${resizable}`)
        window.loadURL(url.href)
    }

    window.webContents.setWindowOpenHandler(({ url }) => {
        shell.openExternal(url)
        return { action: 'deny' }
    })

    window.on('moved', () => {
        if (window) {
            const [x, y] = window.getPosition()
            store.set(`window.${options.id}.position`, { x, y })
        }
    })

    if (!serve && window.isResizable()) {
        window.on('resized', () => {
            if (window) {
                const [width, height] = window.getSize()
                store.set(`window.${options.id}.size`, { width, height })
            }
        })
    }

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

            for (const [key, value] of modalWindows) {
                if (value.window === window) {
                    modalWindows.delete(key)
                    break
                }
            }
        }
    })

    browserWindows.set(options.id, window)

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
    for (const [key, window] of browserWindows) if (window.id === id) return { window, key }
    for (const [key, window] of modalWindows) if (window.window.id === id) return { window: window.window, key }
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

    ipcMain.handle('WINDOW.OPEN', async (event, data: OpenWindow<any>) => {
        if (data.modal) {
            const parent = findWindowById(event.sender.id)
            const window = createWindow(data, parent?.window)

            const promise = new Promise<any>((resolve) => {
                modalWindows.set(data.id, {
                    window, resolve: (value) => {
                        window.close()
                        resolve(value)
                    }
                })
            })

            return promise
        }

        const isNew = !browserWindows.has(data.id)

        const window = createWindow(data)

        if (data.bringToFront) {
            window.show()
        } else if (data.requestFocus) {
            window.focus()
        }

        return new Promise<boolean>((resolve) => {
            if (isNew) {
                window.webContents.once('did-finish-load', () => {
                    resolve(true)
                })
            } else {
                resolve(true)
            }
        })
    })

    ipcMain.handle('FILE.OPEN', async (event, data?: OpenFile) => {
        const ownerWindow = findWindowById(event.sender.id)

        const value = await dialog.showOpenDialog(ownerWindow!.window, {
            filters: data?.filters,
            properties: ['openFile'],
            defaultPath: data?.defaultPath || undefined,
        })

        return !value.canceled && value.filePaths[0]
    })

    ipcMain.handle('FILE.SAVE', async (event, data?: OpenFile) => {
        const ownerWindow = findWindowById(event.sender.id)
        const value = await dialog.showSaveDialog(ownerWindow!.window, {
            filters: data?.filters,
            properties: ['createDirectory', 'showOverwriteConfirmation'],
            defaultPath: data?.defaultPath || undefined,
        })

        return !value.canceled && value.filePath
    })

    ipcMain.handle('JSON.WRITE', async (_, data: JsonFile) => {
        try {
            const json = JSON.stringify(data.json)
            fs.writeFileSync(data.path!, json)
            return true
        } catch (e) {
            console.error(e)
            return false
        }
    })

    ipcMain.handle('JSON.READ', async (_, path: string) => {
        try {
            if (fs.existsSync(path)) {
                const buffer = fs.readFileSync(path)
                return <JsonFile>{ path, json: JSON.parse(buffer.toString('utf-8')) }
            }
        } catch (e) {
            console.error(e)
        }

        return false
    })

    ipcMain.handle('DIRECTORY.OPEN', async (event, data?: OpenDirectory) => {
        const ownerWindow = findWindowById(event.sender.id)
        const value = await dialog.showOpenDialog(ownerWindow!.window, {
            properties: ['openDirectory'],
            defaultPath: data?.defaultPath || undefined,
        })

        return !value.canceled && value.filePaths[0]
    })

    ipcMain.handle('WINDOW.PIN', (event) => {
        const window = findWindowById(event.sender.id)?.window
        window?.setAlwaysOnTop(true)
        return !!window
    })

    ipcMain.handle('WINDOW.UNPIN', (event) => {
        const window = findWindowById(event.sender.id)?.window
        window?.setAlwaysOnTop(false)
        return !!window
    })

    ipcMain.handle('WINDOW.MINIMIZE', (event) => {
        const window = findWindowById(event.sender.id)?.window
        window?.minimize()
        return !!window
    })

    ipcMain.handle('WINDOW.MAXIMIZE', (event) => {
        const window = findWindowById(event.sender.id)?.window

        if (window?.isMaximized()) window.unmaximize()
        else window?.maximize()

        return window?.isMaximized() ?? false
    })

    ipcMain.handle('WINDOW.RESIZE', (event, data: number) => {
        const window = findWindowById(event.sender.id)?.window

        if (!window || (!serve && window.isResizable())) return false

        const size = window.getSize()
        const maxHeight = screen.getPrimaryDisplay().workAreaSize.height
        const height = Math.max(0, Math.min(data, maxHeight))
        window.setSize(size[0], height)
        console.info('window resized', size[0], height)

        return true
    })

    ipcMain.handle('WINDOW.CLOSE', (event, data: CloseWindow<any>) => {
        if (data.id) {
            for (const [key, value] of browserWindows) {
                if (key === data.id) {
                    value.close()
                    return true
                }
            }
        } else {
            const window = findWindowById(event.sender.id)

            if (window) {
                modalWindows.get(window.key)?.resolve(data.data)
                window.window.close()
                return true
            }
        }

        return false
    })

    const events: InternalEventType[] = ['WHEEL.RENAMED', 'LOCATION.CHANGED']

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
