function decodeParams(hex) {
    const buffer = new Uint8Array(hex.length / 4)

    for (let i = 0; i < hex.length; i += 4) {
        buffer[i / 4] = parseInt(hex.substr(i, 4), 16)
    }

    return JSON.parse(String.fromCharCode.apply(null, buffer))
}

function fileFormatConversion() {
    const data = {
        success: true,
        errorMessage: null,
        outputImage: null,
    }

    try {
        const input = decodeParams(jsArguments[0])

        const outputPath = input.outputPath
        const statusPath = input.statusPath
        const inputPath = input.inputPath

        console.writeln("Format conversion started")
        console.writeln("outputPath=" + outputPath)
        console.writeln("statusPath=" + statusPath)
        console.writeln("inputPath=" + inputPath)

        const window = ImageWindow.open(inputPath)[0]
        window.saveAs(outputPath, false, false, false, false)
        window.forceClose()

        data.outputImage = outputPath

        console.writeln("Format conversion finished")
    } catch (e) {
        data.success = false
        data.errorMessage = e.message
        console.writeln(data.errorMessage)
    } finally {
        File.writeTextFile(statusPath, "@" + JSON.stringify(data) + "#")
    }
}

fileFormatConversion()
