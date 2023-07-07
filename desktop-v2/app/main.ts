import { app, BrowserWindow, dialog, ipcMain, screen } from 'electron'
import Hex from 'hex-encoding'
import { ChildProcessWithoutNullStreams, spawn } from 'node:child_process'
import * as path from 'path'
import { OpenWindow } from '../src/shared/types'

let mainWindow: BrowserWindow | null = null
const secondaryWindows = new Map<string, BrowserWindow>()
let api: ChildProcessWithoutNullStreams | null = null
let apiPort = 7000

const args = process.argv.slice(1)
const serve = args.some(e => e === '--serve')

function createMainWindow() {
    createWindow({ id: 'home', path: 'home' })
}

function createWindow(data: OpenWindow) {
    if (secondaryWindows.has(data.id)) {
        return secondaryWindows.get(data.id)!
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
    const params = Hex.encodeStr(JSON.stringify(data.params || {}))

    console.log(`opening new window. id=${data.id}, width=${width}, height=${height}`)

    const window = new BrowserWindow({
        x: size.width / 2 - width / 2,
        y: size.height / 2 - height / 2,
        width,
        height,
        resizable,
        autoHideMenuBar: true,
        title: 'Nebulosa',
        icon: path.join(__dirname, serve ? `../src/assets/icons/${icon}.png` : `assets/icons/${icon}.png`),
        webPreferences: {
            nodeIntegration: true,
            allowRunningInsecureContent: serve,
            contextIsolation: false,
            additionalArguments: [`--port=${apiPort}`],
            preload: path.join(__dirname, 'preload.js'),
            devTools: !serve,
        },
    })

    window.webContents.executeJavaScript(`sessionStorage.setItem('apiPort', '${apiPort}')`)

    if (serve) {
        const debug = require('electron-debug')
        debug()

        require('electron-reloader')(module)
        window.loadURL(`http://localhost:4200/${data.path}?params=${params}`)
    } else {
        window.removeMenu()

        const url = new URL(path.join('file:', __dirname, `index.html#/${data.path}?params=${params}`))
        window.loadURL(url.href)
    }

    window.on('close', () => {
        if (window === mainWindow) {
            for (const [_, value] of secondaryWindows) {
                value.close()
            }

            mainWindow = null

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
        mainWindow = window
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
            const apiJar = path.join(process.resourcesPath, "api.jar")

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
    app.on('ready', () => setTimeout(startApp, 400))

    app.on('window-all-closed', () => {
        if (process.platform !== 'darwin') {
            app.quit()
        }
    })

    app.on('activate', () => {
        if (mainWindow === null) {
            startApp()
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
