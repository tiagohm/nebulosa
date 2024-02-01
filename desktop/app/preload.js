function argWith(name) {
    return process.argv.find(e => e.startsWith(`--${name}=`))?.split('=')?.[1]
}

window.apiPort = parseInt(argWith('port'))
window.id = argWith('id')
window.modal = argWith('modal') === 'true'
