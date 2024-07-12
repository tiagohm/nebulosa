function decodeParams(hex) {
    const buffer = new Uint8Array(hex.length / 4)

    for (let i = 0; i < hex.length; i += 4) {
        buffer[i / 4] = parseInt(hex.substr(i, 4), 16)
    }

    return JSON.parse(String.fromCharCode.apply(null, buffer))
}

function lrgbCombination() {
    const data = {
        success: true,
        errorMessage: null,
        outputImage: null,
    }

    try {
        const input = decodeParams(jsArguments[0])

        const outputPath = input.outputPath
        const statusPath = input.statusPath
        const channelWeights = input.channelWeights
        const luminancePath = input.luminancePath
        const redPath = input.redPath
        const greenPath = input.greenPath
        const bluePath = input.bluePath

        console.writeln("LRGB combination started")
        console.writeln("outputPath=" + outputPath)
        console.writeln("statusPath=" + statusPath)
        console.writeln("channelWeights=" + channelWeights)
        console.writeln("luminancePath=" + luminancePath)
        console.writeln("redPath=" + redPath)
        console.writeln("greenPath=" + greenPath)
        console.writeln("bluePath=" + bluePath)

        const luminanceWindow = luminancePath ? ImageWindow.open(luminancePath)[0] : undefined
        const redWindow = redPath ? ImageWindow.open(redPath)[0] : undefined
        const greenWindow = greenPath ? ImageWindow.open(greenPath)[0] : undefined
        const blueWindow = bluePath ? ImageWindow.open(bluePath)[0] : undefined

        var P = new LRGBCombination
        P.channels = [ // enabled, id, k
           [!!redPath, redWindow ? redWindow.mainView.id : "", channelWeights[1]],
           [!!greenPath, greenWindow ? greenWindow.mainView.id : "", channelWeights[2]],
           [!!bluePath, blueWindow ? blueWindow.mainView.id : "", channelWeights[3]],
           [!!luminancePath, luminanceWindow ? luminanceWindow.mainView.id : "", channelWeights[0]]
        ]
        P.mL = 0.500
        P.mc = 0.500
        P.clipHighlights = true
        P.noiseReduction = false
        P.layersRemoved = 4
        P.layersProtected = 2
        P.inheritAstrometricSolution = true

        P.executeGlobal()

        const window = ImageWindow.windows[ImageWindow.windows.length - 1]
        window.saveAs(outputPath, false, false, false, false)
        window.forceClose()

        if (luminanceWindow) luminanceWindow.forceClose()
        if (redWindow) redWindow.forceClose()
        if (greenWindow) greenWindow.forceClose()
        if (blueWindow) blueWindow.forceClose()

        data.outputImage = outputPath

        console.writeln("LRGB combination finished")
    } catch (e) {
        data.success = false
        data.errorMessage = e.message
        console.writeln(data.errorMessage)
    } finally {
        File.writeTextFile(statusPath, "@" + JSON.stringify(data) + "#")
    }
}

lrgbCombination()
