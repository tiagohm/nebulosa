import { contextBridge, ipcRenderer } from 'electron'
import { basename, dirname, extname, join } from 'node:path'

function argWith(name: string) {
	return process.argv.find((e) => e.startsWith(`--${name}=`))!.split('=')[1]
}

contextBridge.exposeInMainWorld('apiHost', argWith('host'))
contextBridge.exposeInMainWorld('apiPort', parseInt(argWith('port')))
contextBridge.exposeInMainWorld('id', argWith('id'))

contextBridge.exposeInMainWorld('context', {
	data: JSON.parse(decodeURIComponent(argWith('data'))) as never,
	...JSON.parse(decodeURIComponent(argWith('preference'))),
})

contextBridge.exposeInMainWorld('path', {
	basename: (path: string) => basename(path),
	dirname: (path: string) => dirname(path),
	extname: (path: string) => extname(path),
	join: (...paths: string[]) => join(...paths),
})

contextBridge.exposeInMainWorld('electron', {
	invoke: (channel: string, data: unknown) => ipcRenderer.invoke(channel, data),
	on: (channel: string, listener: (arg: unknown) => void) => {
		ipcRenderer.on(channel, (_, a) => {
			listener(a)
		})
	},
})
