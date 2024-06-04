function calibrate() {
    const targetPath = jsArguments[0]
    const outputDirectory = jsArguments[1]
    const statusPath = jsArguments[2]
    const masterDark = jsArguments[3]
    const masterFlat = jsArguments[4]
    const masterBias = jsArguments[5]
    const compress = jsArguments[6].toLowerCase() === 'true'
    const use32Bit = jsArguments[7].toLowerCase() === 'true'

    console.writeln("targetPath=" + targetPath)
    console.writeln("outputDirectory=" + outputDirectory)
    console.writeln("statusPath=" + statusPath)
    console.writeln("masterDark=" + masterDark)
    console.writeln("masterFlat=" + masterFlat)
    console.writeln("masterBias=" + masterBias)
    console.writeln("compress=" + compress)
    console.writeln("use32Bit=" + use32Bit)

    const P = new ImageCalibration

    P.targetFrames = [ // enabled, path
       [true, targetPath]
    ]
    P.enableCFA = true
    P.cfaPattern = ImageCalibration.prototype.Auto
    P.inputHints = "fits-keywords normalize raw cfa signed-is-physical"
    P.outputHints = "properties fits-keywords no-compress-data no-embedded-data no-resolution"
    P.pedestal = 0
    P.pedestalMode = ImageCalibration.prototype.Keyword
    P.pedestalKeyword = ""
    P.overscanEnabled = false
    P.overscanImageX0 = 0
    P.overscanImageY0 = 0
    P.overscanImageX1 = 0
    P.overscanImageY1 = 0
    P.overscanRegions = [ // enabled, sourceX0, sourceY0, sourceX1, sourceY1, targetX0, targetY0, targetX1, targetY1
       [false, 0, 0, 0, 0, 0, 0, 0, 0],
       [false, 0, 0, 0, 0, 0, 0, 0, 0],
       [false, 0, 0, 0, 0, 0, 0, 0, 0],
       [false, 0, 0, 0, 0, 0, 0, 0, 0]
    ]
    P.masterBiasEnabled = masterBias !== "0"
    P.masterBiasPath = masterBias
    P.masterDarkEnabled = masterDark !== "0"
    P.masterDarkPath = masterDark
    P.masterFlatEnabled = masterFlat !== "0"
    P.masterFlatPath = masterFlat
    P.calibrateBias = false
    P.calibrateDark = false
    P.calibrateFlat = false
    P.optimizeDarks = false
    P.darkOptimizationThreshold = 0.00000
    P.darkOptimizationLow = 3.0000
    P.darkOptimizationWindow = 0
    P.darkCFADetectionMode = ImageCalibration.prototype.DetectCFA
    P.separateCFAFlatScalingFactors = true
    P.flatScaleClippingFactor = 0.05
    P.evaluateNoise = false
    P.noiseEvaluationAlgorithm = ImageCalibration.prototype.NoiseEvaluation_MRS
    P.evaluateSignal = false
    P.structureLayers = 5
    P.saturationThreshold = 1.00
    P.saturationRelative = false
    P.noiseLayers = 1
    P.hotPixelFilterRadius = 1
    P.noiseReductionFilterRadius = 0
    P.minStructureSize = 0
    P.psfType = ImageCalibration.prototype.PSFType_Moffat4
    P.psfGrowth = 1.00
    P.maxStars = 24576
    P.outputDirectory = outputDirectory
    P.outputExtension = ".fits"
    P.outputPrefix = ""
    P.outputPostfix = "_c"
    P.outputSampleFormat = use32Bit ? ImageCalibration.prototype.f32 : ImageCalibration.prototype.i16
    P.outputPedestal = 0
    P.outputPedestalMode = ImageCalibration.prototype.OutputPedestal_Literal
    P.autoPedestalLimit = 0.00010
    P.overwriteExistingFiles = true
    P.onError = ImageCalibration.prototype.Continue
    P.noGUIMessages = true
    P.useFileThreads = true
    P.fileThreadOverload = 1.00
    P.maxFileReadThreads = 0
    P.maxFileWriteThreads = 0

    P.executeGlobal()

    console.writeln("calibration finished")

    const json = {
         outputImage: P.outputData[0][0] || null,
    }

    File.writeTextFile(statusPath, "@" + JSON.stringify(json) + "#")
}

calibrate()
