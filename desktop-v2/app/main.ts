import { app, BrowserWindow, screen } from 'electron'
import * as fs from 'fs'
import * as path from 'path'

let mainWindow: BrowserWindow | null = null
const args = process.argv.slice(1)
const serve = args.some(val => val === '--serve')

function createWindow(): BrowserWindow {
    const size = screen.getPrimaryDisplay().workAreaSize

    mainWindow = new BrowserWindow({
        x: size.width / 2 - 360 / 2,
        y: size.height / 2 - 480 / 2,
        width: 360,
        height: 456,
        resizable: false,
        autoHideMenuBar: true,
        webPreferences: {
            nodeIntegration: true,
            allowRunningInsecureContent: serve,
            contextIsolation: false,
        },
    })

    if (serve) {
        const debug = require('electron-debug')
        debug()

        require('electron-reloader')(module)
        mainWindow.loadURL('http://localhost:4200')
    } else {
        let pathIndex = './index.html'

        if (fs.existsSync(path.join(__dirname, '../dist/index.html'))) {
            pathIndex = '../dist/index.html'
        }

        const url = new URL(path.join('file:', __dirname, pathIndex))
        mainWindow.loadURL(url.href)
    }

    mainWindow.on('closed', () => {
        mainWindow = null
    })

    return mainWindow
}

try {
    app.on('ready', () => setTimeout(createWindow, 400))

    app.on('window-all-closed', () => {
        if (process.platform !== 'darwin') {
            app.quit()
        }
    })

    app.on('activate', () => {
        if (mainWindow === null) {
            createWindow()
        }
    })
} catch (e) {
    console.error(e)
}
