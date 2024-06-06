function decodeParams(hex) {
    let decoded = ''

    for (let i = 0; i < hex.length; i += 2) {
        decoded += String.fromCharCode(parseInt(hex.substr(i, 2), 16))
    }

    return JSON.parse(decoded)
}

function abe() {
    const input = decodeParams(jsArguments[0])

    const targetPath = input.targetPath
    const outputPath = input.outputPath
    const statusPath = input.statusPath

    console.writeln("targetPath=" + targetPath)
    console.writeln("outputPath=" + outputPath)
    console.writeln("statusPath=" + statusPath)

    const window = ImageWindow.open(targetPath)[0]

    const P = new AutomaticBackgroundExtractor
    P.tolerance = 1.000
    P.deviation = 0.800
    P.unbalance = 1.800
    P.minBoxFraction = 0.050
    P.maxBackground = 1.0000
    P.minBackground = 0.0000
    P.useBrightnessLimits = false
    P.polyDegree = 4
    P.boxSize = 5
    P.boxSeparation = 5
    P.modelImageSampleFormat = AutomaticBackgroundExtractor.prototype.f32
    P.abeDownsample = 2.00
    P.writeSampleBoxes = false
    P.justTrySamples = false
    P.targetCorrection = AutomaticBackgroundExtractor.prototype.Subtract
    P.normalize = true
    P.discardModel = true
    P.replaceTarget = true
    P.correctedImageId = ""
    P.correctedImageSampleFormat = AutomaticBackgroundExtractor.prototype.SameAsTarget
    P.verboseCoefficients = false
    P.compareModel = false
    P.compareFactor = 10.00

    P.executeOn(window.mainView)

    window.saveAs(outputPath, false, false, false, false)

    window.forceClose()

    console.writeln("abe finished")

    const json = {
         outputImage: outputPath,
    }

    File.writeTextFile(statusPath, "@" + JSON.stringify(json) + "#")
}

abe()
