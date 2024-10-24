import { WebviewWindow } from '@tauri-apps/api/webviewWindow'

export function createWindow() {
	const webview = new WebviewWindow('my-label', {
		url: 'https://github.com/tauri-apps/tauri',
		width: 200,
		height: 800,
		titleBarStyle: 'overlay',
	})

	webview.once('tauri://created', function () {
		console.info('webview window successfully created')
	})

	webview.once('tauri://error', function (e) {
		console.info('an error happened creating the webview window', e)
	})
}
