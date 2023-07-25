const port = process.argv.filter(e => e.startsWith('--port='))[0].split('=')[1]

window.apiPort = parseInt(port)
