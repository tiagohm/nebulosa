function decodeParams(hex) {
    const buffer = new Uint8Array(hex.length / 4)

    for (let i = 0; i < hex.length; i += 4) {
        buffer[i / 4] = parseInt(hex.substr(i, 4), 16)
    }

    return JSON.parse(String.fromCharCode.apply(null, buffer))
}

function alignment() {
    const data = {
        success: true,
        errorMessage: null,
        outputImage: null,
        outputMaskImage: null,
        totalPairMatches: 0,
        inliers: 0,
        overlapping: 0,
        regularity: 0,
        quality: 0,
        rmsError: 0,
        rmsErrorDev: 0,
        peakErrorX: 0,
        peakErrorY: 0,
        h11: 0,
        h12: 0,
        h13: 0,
        h21: 0,
        h22: 0,
        h23: 0,
        h31: 0,
        h32: 0,
        h33: 0,
    }

    try {
        const input = decodeParams(jsArguments[0])

        const referencePath = input.referencePath
        const targetPath = input.targetPath
        const outputDirectory = input.outputDirectory
        const statusPath = input.statusPath

        console.writeln("alignment started")
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
        P.outputExtension = ".xisf"
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

        data.outputImage = P.outputData[0][0] || null
        data.outputMaskImage = P.outputData[0][1] || null
        data.totalPairMatches = P.outputData[0][2]
        data.inliers = P.outputData[0][3]
        data.overlapping = P.outputData[0][4]
        data.regularity = P.outputData[0][5]
        data.quality = P.outputData[0][6]
        data.rmsError = P.outputData[0][7]
        data.rmsErrorDev = P.outputData[0][8]
        data.peakErrorX = P.outputData[0][9]
        data.peakErrorY = P.outputData[0][10]
        data.h11 = P.outputData[0][11]
        data.h12 = P.outputData[0][12]
        data.h13 = P.outputData[0][13]
        data.h21 = P.outputData[0][14]
        data.h22 = P.outputData[0][15]
        data.h23 = P.outputData[0][16]
        data.h31 = P.outputData[0][17]
        data.h32 = P.outputData[0][18]
        data.h33 = P.outputData[0][19]

        console.writeln("alignment finished")
    } catch (e) {
        data.success = false
        data.errorMessage = e.message
        console.criticalln(data.errorMessage)
    } finally {
        File.writeTextFile(statusPath, "@" + JSON.stringify(data) + "#")
    }
}

alignment()
