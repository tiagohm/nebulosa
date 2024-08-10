function decodeParams(hex) {
    const buffer = new Uint8Array(hex.length / 4)

    for (let i = 0; i < hex.length; i += 4) {
        buffer[i / 4] = parseInt(hex.substr(i, 4), 16)
    }

    return JSON.parse(String.fromCharCode.apply(null, buffer))
}

function luminanceCombination() {
    const data = {
        success: true,
        errorMessage: null,
        outputImage: null,
    }

    try {
        const input = decodeParams(jsArguments[0])

        const outputPath = input.outputPath
        const statusPath = input.statusPath
        const weight = input.weight
        const luminancePath = input.luminancePath
        const targetPath = input.targetPath

        console.writeln("Luminance combination started")
        console.writeln("outputPath=" + outputPath)
        console.writeln("statusPath=" + statusPath)
        console.writeln("weight=" + weight)
        console.writeln("luminancePath=" + luminancePath)
        console.writeln("targetPath=" + targetPath)

        const luminanceWindow = luminancePath ? ImageWindow.open(luminancePath)[0] : undefined
        const targetWindow = targetPath ? ImageWindow.open(targetPath)[0] : undefined

        var P = new LRGBCombination
        P.channels = [ // enabled, id, k
            [false, "", 1.0],
            [false, "", 1.0],
            [false, "", 1.0],
            [true, luminanceWindow.mainView.id, weight]
        ]
        P.mL = 0.500
        P.mc = 0.500
        P.clipHighlights = true
        P.noiseReduction = false
        P.layersRemoved = 4
        P.layersProtected = 2
        P.inheritAstrometricSolution = true

        P.executeOn(targetWindow.mainView)

        targetWindow.saveAs(outputPath, false, false, false, false)
        window.forceClose()

        luminanceWindow.forceClose()

        data.outputImage = outputPath

        console.writeln("Luminance combination finished")
    } catch (e) {
        data.success = false
        data.errorMessage = e.message
        console.criticalln(data.errorMessage)
    } finally {
        File.writeTextFile(statusPath, "@" + JSON.stringify(data) + "#")
    }
}

luminanceCombination()
