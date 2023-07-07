import { app, BrowserWindow, dialog, ipcMain, screen } from 'electron'
import * as fs from 'fs'
import * as path from 'path'
import { OpenWindow } from '../src/shared/models/OpenWindow.model'
import { encodeHex } from '../src/shared/utils'

let mainWindow: BrowserWindow | null = null
const windows = new Map<string, BrowserWindow>()

const args = process.argv.slice(1)
const serve = args.some(e => e === '--serve')

function createMainWindow() {
    createWindow({ id: 'home', path: 'home' })
}

function createWindow(data: OpenWindow) {
    if (windows.has(data.id)) {
        return windows.get(data.id)!
    } else if (data.id === 'home' && mainWindow) {
        return mainWindow
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

    function computeHeight(value: number | string) {
        if (typeof value === 'number') {
            return value
        } else if (value.endsWith('%')) {
            return parseFloat(value.substring(0, value.length - 1)) * size.height / 100
        } else {
            return parseFloat(value)
        }
    }

    const width = data.width ? computeWidth(data.width) : 360
    const height = data.height ? computeHeight(data.height) : 424
    const resizable = data.resizable ?? false
    const icon = data.icon ?? 'nebulosa'
    const params = encodeHex(JSON.stringify(data.params || {}))

    const window = new BrowserWindow({
        x: size.width / 2 - width / 2,
        y: size.height / 2 - height / 2,
        width,
        height,
        resizable,
        autoHideMenuBar: true,
        title: 'Nebulosa',
        icon: path.join(__dirname, `../src/assets/icons/${icon}.png`),
        webPreferences: {
            nodeIntegration: true,
            allowRunningInsecureContent: serve,
            contextIsolation: false,
            // devTools: false,
        },
    })

    // window.removeMenu()

    if (serve) {
        const debug = require('electron-debug')
        debug()

        require('electron-reloader')(module)
        window.loadURL(`http://localhost:4200/${data.path}?params=${params}`)
    } else {
        let pathIndex = `./index.html/#/${data.path}?params=${params}`

        if (fs.existsSync(path.join(__dirname, '../dist/index.html'))) {
            pathIndex = `../dist/index.html/#/${data.path}?params=${params}`
        }

        const url = new URL(path.join('file:', __dirname, pathIndex))
        window.loadURL(url.href)
    }

    window.on('close', () => {
        if (window === mainWindow) {
            for (const [_, value] of windows) {
                value.close()
            }

            mainWindow = null
        } else {
            for (const [key, value] of windows) {
                if (value === window) {
                    windows.delete(key)
                    break
                }
            }
        }
    })

    if (data.id === 'home') {
        mainWindow = window
    } else {
        windows.set(data.id, window)
    }

    return window
}

try {
    app.on('ready', () => setTimeout(createMainWindow, 400))

    app.on('window-all-closed', () => {
        if (process.platform !== 'darwin') {
            app.quit()
        }
    })

    app.on('activate', () => {
        if (mainWindow === null) {
            createMainWindow()
        }
    })

    ipcMain.on('open-window', async (_, data: OpenWindow) => {
        const window = createWindow(data)

        if (data.bringToFront) {
            window.show()
        } else if (data.requestFocus) {
            window.focus()
        }
    })

    ipcMain.on('open-fits', async (event) => {
        const value = await dialog.showOpenDialog(mainWindow!, {
            filters: [{ name: 'FITS files', extensions: ['fits', 'fit'] }],
            properties: ['openFile'],
        })

        if (!value.canceled) {
            event.returnValue = value.filePaths[0]
        } else {
            event.returnValue = false
        }
    })

    ipcMain.on('open-directory', async (event) => {
        const value = await dialog.showOpenDialog(mainWindow!, {
            properties: ['openDirectory'],
        })

        if (!value.canceled) {
            event.returnValue = value.filePaths[0]
        } else {
            event.returnValue = false
        }
    })
} catch (e) {
    console.error(e)
}
