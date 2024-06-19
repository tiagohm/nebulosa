function argWith(name: string) {
	return process.argv.find((e) => e.startsWith(`--${name}=`))?.split('=')?.[1]!
}

window.apiHost = argWith('host')
window.apiPort = parseInt(argWith('port'))
window.id = argWith('id')
window.data = JSON.parse(decodeURIComponent(argWith('data')))
window.preference = JSON.parse(decodeURIComponent(argWith('preference')))
