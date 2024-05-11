function argWith(name) {
    return process.argv.find(e => e.startsWith(`--${name}=`))?.split('=')?.[1]
}

window.apiHost = argWith('host')
window.apiPort = parseInt(argWith('port'))
window.id = argWith('id')
window.options = JSON.parse(Buffer.from(argWith('options'), 'base64').toString('utf-8'))
