const port = process.argv.find(e => e.startsWith('--port=')).split('=')[1]

window.apiPort = parseInt(port)
