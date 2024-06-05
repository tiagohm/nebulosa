function decodeParams(hex) {
    let decoded = ''

    for (let i = 0; i < hex.length; i += 2) {
        decoded += String.fromCharCode(parseInt(hex.substr(i, 2), 16))
    }

    return JSON.parse(decoded)
}

function pixelMath() {
    const input = decodeParams(jsArguments[0])

    const outputDirectory = input.outputDirectory
    const statusPath = input.statusPath
    const inputPaths = input.inputPaths
    const outputPath = input.outputPath
    let expressionRK = input.expressionRK
    let expressionG = input.expressionG
    let expressionB = input.expressionB

    console.writeln("outputDirectory=" + outputDirectory)
    console.writeln("statusPath=" + statusPath)
    console.writeln("inputPaths=" + inputPaths)
    console.writeln("outputPath=" + outputPath)

    const windows = []

    for(let i = 0; i < input.inputPaths.length; i++) {
        windows.push(ImageWindow.open(input.inputPaths[i])[0])
    }

    for(let i = 0; i < windows.length; i++) {
        if (expressionRK) {
            expressionRK = expressionRK.replace("{{" + i + "}}", windows[i].mainView.id)
        }
        if (expressionG) {
            expressionG = expressionG.replace("{{" + i + "}}", windows[i].mainView.id)
        }
        if (expressionB) {
            expressionB = expressionB.replace("{{" + i + "}}", windows[i].mainView.id)
        }
    }

    console.writeln("expressionRK=" + expressionRK)
    console.writeln("expressionG=" + expressionG)
    console.writeln("expressionB=" + expressionB)

    var P = new PixelMath
    P.expression = expressionRK || ""
    P.expression1 = expressionG || ""
    P.expression2 = expressionB || ""
    P.expression3 = ""
    P.useSingleExpression = false
    P.symbols = ""
    P.clearImageCacheAndExit = false
    P.cacheGeneratedImages = false
    P.generateOutput = true
    P.singleThreaded = false
    P.optimization = true
    P.use64BitWorkingImage = false
    P.rescale = false
    P.rescaleLower = 0
    P.rescaleUpper = 1
    P.truncate = true
    P.truncateLower = 0
    P.truncateUpper = 1
    P.createNewImage = false
    P.showNewImage = false
    P.newImageId = ""
    P.newImageWidth = 0
    P.newImageHeight = 0
    P.newImageAlpha = false
    P.newImageColorSpace = PixelMath.prototype.SameAsTarget
    P.newImageSampleFormat = PixelMath.prototype.SameAsTarget

    P.executeOn(windows[0].mainView)

    windows[0].saveAs(outputPath, false, false, false, false)

    for(let i = 0; i < windows.length; i++) {
        windows[i].forceClose()
    }

    console.writeln("stacking finished")

    const json = {
         stackedImage: outputPath,
    }

    File.writeTextFile(statusPath, "@" + JSON.stringify(json) + "#")
}

pixelMath()
