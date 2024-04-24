import { Client } from '@stomp/stompjs'
import { BrowserWindow, Menu, Notification, Point, Size, app, dialog, ipcMain, screen, shell } from 'electron'
import * as fs from 'fs'
import { ChildProcessWithoutNullStreams, spawn } from 'node:child_process'
import * as path from 'path'

import { WebSocket } from 'ws'
import { MessageEvent } from '../src/shared/types/api.types'
import { CloseWindow, InternalEventType, JsonFile, NotificationEvent, OpenDirectory, OpenFile, OpenWindow } from '../src/shared/types/app.types'

Object.assign(global, { WebSocket })

app.commandLine.appendSwitch('disable-http-cache')

interface CreatedWindow {
    options: OpenWindow<any>
    window: BrowserWindow
}

interface CreatedModalWindow extends CreatedWindow {
    resolve: (data: any) => void
}

const browserWindows = new Map<string, CreatedWindow>()
const modalWindows = new Map<string, CreatedModalWindow>()
let api: ChildProcessWithoutNullStreams | null = null
let apiPort = 7000
let webSocket: Client

const args = process.argv.slice(1)
const serve = args.some(e => e === '--serve')
const appDir = path.join(app.getPath('appData'), 'nebulosa')
const appIcon = path.join(__dirname, serve ? `../src/assets/icons/nebulosa.png` : `assets/icons/nebulosa.png`)

if (!fs.existsSync(appDir)) {
    fs.mkdirSync(appDir)
}

class SimpleDB {

    private readonly data: Record<string, any>

    constructor(private path: fs.PathLike) {
        try {
            if (fs.existsSync(path)) {
                const text = fs.readFileSync(path).toString('utf-8')
                this.data = text.length > 0 ? JSON.parse(text) : {}
            } else {
                this.data = {}
            }
        } catch (e) {
            this.data = {}
            console.error(e)
        }
    }

    get<T = any>(key: string) {
        return this.data[key] as T | undefined
    }

    set(key: string, value: any) {
        if (value === undefined || value === null) delete this.data[key]
        else this.data[key] = value

        try {
            this.save()
        } catch (e) {
            console.error(e)
        }
    }

    save() {
        fs.writeFileSync(this.path, JSON.stringify(this.data))
    }
}

const database = new SimpleDB(path.join(appDir, 'nebulosa.data.json'))

function isNotificationEvent(event: MessageEvent): event is NotificationEvent {
    return event.eventName === 'NOTIFICATION.SENT'
}

function createMainWindow() {
    browserWindows.get('splash')?.window?.close()
    browserWindows.delete('splash')

    createWindow({ id: 'home', path: 'home', data: undefined })

    webSocket = new Client({
        brokerURL: `ws://localhost:${apiPort}/ws`,
        onConnect: () => {
            webSocket.subscribe('NEBULOSA.EVENT', message => {
                const event = JSON.parse(message.body) as MessageEvent

                if (isNotificationEvent(event)) {
                    showNotification(event)
                } else if (event.eventName) {
                    sendToAllWindows(event.eventName, event)
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
    const createdWindow = browserWindows.get(options.id)

    let window = createdWindow?.window

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

    const minHeight = options.minHeight ?? 0
    const height = Math.max(minHeight, options.height ? Math.trunc(computeHeight(options.height)) : 416)

    const id = options.id
    const resizable = options.resizable ?? false
    const autoResizable = options.autoResizable !== false
    const modal = options.modal ?? false
    const icon = options.icon ?? 'nebulosa'
    const data = encodeURIComponent(JSON.stringify(options.data || {}))

    const savedPosition = !modal ? database.get<Point>(`window.${id}.position`) : undefined
    const savedSize = !modal && resizable ? database.get<Size>(`window.${id}.size`) : undefined

    if (savedPosition) {
        savedPosition.x = Math.max(0, Math.min(savedPosition.x, screenSize.width))
        savedPosition.y = Math.max(0, Math.min(savedPosition.y, screenSize.height))
    }

    if (savedSize) {
        savedSize.width = Math.max(0, Math.min(savedSize.width, screenSize.width))
        savedSize.height = Math.max(0, Math.min(savedSize.height, screenSize.height))
    }

    window = new BrowserWindow({
        title: 'Nebulosa',
        frame: false, modal, parent,
        width: savedSize?.width || width,
        height: savedSize?.height || height,
        minHeight,
        x: savedPosition?.x ?? undefined,
        y: savedPosition?.y ?? undefined,
        resizable: serve || resizable,
        autoHideMenuBar: true,
        icon: path.join(__dirname, serve ? `../src/assets/icons/${icon}.png` : `assets/icons/${icon}.png`),
        webPreferences: {
            nodeIntegration: true,
            allowRunningInsecureContent: serve,
            contextIsolation: false,
            additionalArguments: [`--port=${apiPort}`, `--options=${Buffer.from(JSON.stringify(options)).toString('base64')}`],
            preload: path.join(__dirname, 'preload.js'),
            devTools: serve,
        },
    })

    if (!savedPosition) {
        window.center()
    }

    if (serve) {
        const debug = require('electron-debug')
        debug({ showDevTools: false })

        window.loadURL(`http://localhost:4200/${options.path}?data=${data}`)
    } else {
        const url = new URL(path.join('file:', __dirname, `index.html`) + `#/${options.path}?data=${data}`)
        window.loadURL(url.href)
    }

    window.webContents.setWindowOpenHandler(({ url }) => {
        shell.openExternal(url)
        return { action: 'deny' }
    })

    window.on('close', () => {
        console.info('window closed:', id, window.id)

        const homeWindow = browserWindows.get('home')

        if (!modal) {
            const [x, y] = window!.getPosition()
            const [width, height] = window!.getSize()

            database.set(`window.${id}.position`, { x, y })

            if (resizable) {
                database.set(`window.${id}.size`, { width, height })
            }
        }

        if (window === homeWindow?.window || id === homeWindow?.options?.id) {
            browserWindows.delete('home')

            for (const [_, value] of browserWindows) {
                value.window.close()
            }

            browserWindows.clear()

            api?.kill()
        } else {
            for (const [key, value] of browserWindows) {
                if (value.window === window || value.options.id === id) {
                    browserWindows.delete(key)
                    break
                }
            }

            for (const [key, value] of modalWindows) {
                if (value.window === window || value.options.id === id) {
                    modalWindows.delete(key)
                    break
                }
            }
        }
    })

    browserWindows.set(id, { window, options })

    console.info('window created:', id, window.id)

    return window
}

function createSplashScreen() {
    if (!serve && !browserWindows.has('splash')) {
        const window = new BrowserWindow({
            width: 512,
            height: 512,
            transparent: true,
            frame: false,
            alwaysOnTop: true,
            show: false,
            resizable: false,
        })

        const url = new URL(path.join('file:', __dirname, 'assets', 'images', 'splash.png'))
        window.loadURL(url.href)

        window.show()
        window.center()

        browserWindows.set('splash', { window, options: { id: 'splash', path: '', data: undefined } })
    }
}

function showNotification(event: NotificationEvent) {
    if (event.silent) {
        sendToAllWindows(event.type, event)
    } else {
        new Notification({ ...event, icon: appIcon })
            .on('click', () => sendToAllWindows(event.type, event))
            .show()
    }
}

function findWindowById(id: number | string) {
    for (const [_, window] of browserWindows) if (window.window.id === id || window.options.id === id) return window
    for (const [_, window] of modalWindows) if (window.window.id === id || window.options.id === id) return window
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

    ipcMain.handle('WINDOW.OPEN', async (event, options: OpenWindow<any>) => {
        if (options.modal) {
            const parent = findWindowById(event.sender.id)
            const window = createWindow(options, parent?.window)

            const promise = new Promise<any>((resolve) => {
                modalWindows.set(options.id, {
                    window, options, resolve: (value) => {
                        window.close()
                        resolve(value)
                    }
                })
            })

            return promise
        }

        const isNew = !browserWindows.has(options.id)

        const window = createWindow(options)

        if (options.bringToFront) {
            window.show()
        } else if (options.requestFocus) {
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

        const size = window.getContentSize()
        const maxHeight = screen.getPrimaryDisplay().workAreaSize.height
        const height = Math.max(window.getMinimumSize()[1] || 0, Math.max(0, Math.min(data, maxHeight)))
        resizeWindow(window, size[0], height)
        console.info('window auto resized:', size[0], height)

        return true
    })

    ipcMain.handle('WINDOW.FULLSCREEN', (event, enabled?: boolean) => {
        const window = findWindowById(event.sender.id)?.window
        if (!window) return false
        const flag = enabled ?? !window.isFullScreen()
        window.setFullScreen(flag)
        return flag
    })

    ipcMain.handle('WINDOW.CLOSE', (event, data: CloseWindow<any>) => {
        if (data.id) {
            for (const [key, value] of browserWindows) {
                if (key === data.id || value.options.id === data.id) {
                    value.window.close()
                    return true
                }
            }
        } else {
            const window = findWindowById(event.sender.id)

            if (window) {
                modalWindows.get(window.options.id)?.resolve(data.data)
                window.window.close()
                return true
            }
        }

        return false
    })

    const events: InternalEventType[] = ['WHEEL.RENAMED', 'LOCATION.CHANGED', 'CALIBRATION.CHANGED']

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
        if (window.window !== homeWindow?.window || home) {
            window.window.webContents.send(channel, data)
        }
    }

    if (serve) {
        console.info(data)
    }
}

function resizeWindow(window: BrowserWindow, width: number, height: number) {
    if (process.platform === 'win32') {
        window.setSize(width, height, false)
    } else {
        window.setContentSize(width, height, false)
    }
}
