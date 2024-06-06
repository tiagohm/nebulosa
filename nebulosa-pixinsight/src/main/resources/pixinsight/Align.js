function decodeParams(hex) {
    let decoded = ''

    for (let i = 0; i < hex.length; i += 2) {
        decoded += String.fromCharCode(parseInt(hex.substr(i, 2), 16))
    }

    return JSON.parse(decoded)
}

function alignment() {
    const input = decodeParams(jsArguments[0])

    const referencePath = input.referencePath
    const targetPath = input.targetPath
    const outputDirectory = input.outputDirectory
    const statusPath = input.statusPath

    console.writeln("referencePath=" + referencePath)
    console.writeln("targetPath=" + targetPath)
    console.writeln("outputDirectory=" + outputDirectory)
    console.writeln("statusPath=" + statusPath)

    var P = new StarAlignment
    P.structureLayers = 5
    P.noiseLayers = 0
    P.hotPixelFilterRadius = 1
    P.noiseReductionFilterRadius = 0
    P.minStructureSize = 0
    P.sensitivity = 0.50
    P.peakResponse = 0.50
    P.brightThreshold = 3.00
    P.maxStarDistortion = 0.60
    P.allowClusteredSources = false
    P.localMaximaDetectionLimit = 0.75
    P.upperLimit = 1.000
    P.invert = false
    P.distortionModel = ""
    P.undistortedReference = false
    P.distortionCorrection = false
    P.distortionMaxIterations = 20
    P.distortionMatcherExpansion = 1.00
    P.splineOrder = 2
    P.splineSmoothness = 0.005
    P.matcherTolerance = 0.0500
    P.ransacMaxIterations = 2000
    P.ransacMaximizeInliers = 1.00
    P.ransacMaximizeOverlapping = 1.00
    P.ransacMaximizeRegularity = 1.00
    P.ransacMinimizeError = 1.00
    P.maxStars = 0
    P.fitPSF = StarAlignment.prototype.FitPSF_DistortionOnly
    P.psfTolerance = 0.50
    P.useTriangles = false
    P.polygonSides = 5
    P.descriptorsPerStar = 20
    P.restrictToPreviews = true
    P.intersection = StarAlignment.prototype.MosaicOnly
    P.useBrightnessRelations = false
    P.useScaleDifferences = false
    P.scaleTolerance = 0.100
    P.referenceImage = referencePath
    P.referenceIsFile = true
    P.targets = [ // enabled, isFile, image
        [true, true, targetPath]
    ]
    P.inputHints = ""
    P.outputHints = ""
    P.mode = StarAlignment.prototype.RegisterMatch
    P.writeKeywords = true
    P.generateMasks = false
    P.generateDrizzleData = false
    P.generateDistortionMaps = false
    P.inheritAstrometricSolution = false
    P.frameAdaptation = false
    P.randomizeMosaic = false
    P.pixelInterpolation = StarAlignment.prototype.Auto
    P.clampingThreshold = 0.30
    P.outputDirectory = outputDirectory
    P.outputExtension = ".fits"
    P.outputPrefix = ""
    P.outputPostfix = "_a"
    P.maskPostfix = "_m"
    P.distortionMapPostfix = "_dm"
    P.outputSampleFormat = StarAlignment.prototype.SameAsTarget
    P.overwriteExistingFiles = true
    P.onError = StarAlignment.prototype.Continue
    P.useFileThreads = true
    P.noGUIMessages = true
    P.fileThreadOverload = 1.00
    P.maxFileReadThreads = 0
    P.maxFileWriteThreads = 0
    P.memoryLoadControl = true
    P.memoryLoadLimit = 0.85

    P.executeGlobal()

    console.writeln("alignment finished")

    const json = {
        outputImage: P.outputData[0][0] || null,
        outputMaskImage: P.outputData[0][1] || null,
        totalPairMatches: P.outputData[0][2],
        inliers: P.outputData[0][3],
        overlapping: P.outputData[0][4],
        regularity: P.outputData[0][5],
        quality: P.outputData[0][6],
        rmsError: P.outputData[0][7],
        rmsErrorDev: P.outputData[0][8],
        peakErrorX: P.outputData[0][9],
        peakErrorY: P.outputData[0][10],
        h11: P.outputData[0][11],
        h12: P.outputData[0][12],
        h13: P.outputData[0][13],
        h21: P.outputData[0][14],
        h22: P.outputData[0][15],
        h23: P.outputData[0][16],
        h31: P.outputData[0][17],
        h32: P.outputData[0][18],
        h33: P.outputData[0][19],
    }

    File.writeTextFile(statusPath, "@" + JSON.stringify(json) + "#")
}

alignment()
