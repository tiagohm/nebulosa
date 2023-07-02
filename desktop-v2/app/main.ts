import { app, BrowserWindow, dialog, ipcMain, screen } from 'electron'
import * as fs from 'fs'
import * as path from 'path'

let mainWindow: BrowserWindow | null = null
const windows = new Map<string, BrowserWindow>()

const args = process.argv.slice(1)
const serve = args.some(val => val === '--serve')

function createMainWindow() {
    createWindow('HOME', 360, 448)
}

function createWindow(token: string,
    width: number, height: number,
    title: string = 'Nebulosa',
    resizable: boolean = false,
) {
    if (windows.has(token)) {
        return windows.get(token)
    }

    const size = screen.getPrimaryDisplay().workAreaSize

    let window = new BrowserWindow({
        x: size.width / 2 - width / 2,
        y: size.height / 2 - height / 2,
        width,
        height,
        resizable,
        autoHideMenuBar: true,
        title,
        webPreferences: {
            nodeIntegration: true,
            allowRunningInsecureContent: serve,
            contextIsolation: false,
            devTools: false,
        },
    })

    const [type, uuid = ''] = token.split('.')

    if (serve) {
        const debug = require('electron-debug')
        debug()

        require('electron-reloader')(module)
        window.loadURL(`http://localhost:4200?type=${type}`)
    } else {
        let pathIndex = `./index.html?type=${type}`

        if (fs.existsSync(path.join(__dirname, '../dist/index.html'))) {
            pathIndex = `../dist/index.html?type=${type}`
        }

        const url = new URL(path.join('file:', __dirname, pathIndex))
        window.loadURL(url.href)
    }

    window.on('close', () => {
        if (window === mainWindow) {
            windows.clear()
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

    if (token === 'HOME') {
        mainWindow = window
    } else {
        windows.set(token, window)
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

    ipcMain.on('open-window', async (event, data) => {
        const type = data.type as string
        const width = data.width as number || 360
        const height = data.height as number || 448
        const title = data.title as string || 'Nebulosa'
        createWindow(type, width, height, title)
    })

    ipcMain.on('load-fits', async (event) => {
        const data = await dialog.showOpenDialog(mainWindow!, {
            filters: [{ name: 'FITS files', extensions: ['fits', 'fit'] }],
            properties: ['openFile'],
        })

        if (!data.canceled) {
            event.sender.send('fits-loaded', { path: data.filePaths[0] })
        }
    })
} catch (e) {
    console.error(e)
}
