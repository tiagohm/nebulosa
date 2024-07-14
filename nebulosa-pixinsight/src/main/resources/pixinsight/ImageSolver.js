/*
 * Image Plate Solver
 *
 * Plate solving of astronomical images.
 *
 * Copyright (C) 2012-2024, Andres del Pozo
 * Copyright (C) 2019-2024, Juan Conejero (PTeam)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

#define __PJSR_USE_STAR_DETECTOR_V2

#include <pjsr/BRQuadTree.jsh>
#include <pjsr/ColorSpace.jsh>
#include <pjsr/DataType.jsh>
#include <pjsr/LinearTransformation.jsh>
#include <pjsr/StarDetector.jsh>
#include <pjsr/UndoFlag.jsh>

#define SOLVERVERSION "6.2.7"
#define STAR_CSV_FILE (File.systemTempDirectory + format("/stars-%03d.csv", CoreApplication.instance))
#define SETTINGS_MODULE "SOLVER"
#define SETTINGS_MODULE_SCRIPT "SOLVER"

#include "/opt/PixInsight/src/scripts/AdP/WCSmetadata.jsh"
#include "/opt/PixInsight/src/scripts/AdP/AstronomicalCatalogs.jsh"
#include "/opt/PixInsight/src/scripts/AdP/CatalogDownloader.js"

function CatalogMode() { }
CatalogMode.prototype.LocalText = 0
CatalogMode.prototype.Online = 1
CatalogMode.prototype.Automatic = 2
CatalogMode.prototype.LocalXPSDServer = 3

function SolverConfiguration(module) {
    this.__base__ = ObjectWithSettings
    this.__base__(
        module,
        "solver",
        new Array(
            ["version", DataType_UCString],
            ["magnitude", DataType_Float],
            ["autoMagnitude", DataType_Boolean],
            ["databasePath", DataType_UCString],
            ["generateErrorImg", DataType_Boolean],
            ["structureLayers", DataType_UInt8],
            ["minStructureSize", DataType_UInt8],
            ["hotPixelFilterRadius", DataType_UInt8],
            ["noiseReductionFilterRadius", DataType_UInt8],
            ["sensitivity", DataType_Double],
            ["peakResponse", DataType_Double],
            ["brightThreshold", DataType_Double],
            ["maxStarDistortion", DataType_Double],
            ["autoPSF", DataType_Boolean],
            ["catalogMode", DataType_UInt8],
            ["vizierServer", DataType_UCString],
            ["showStars", DataType_Boolean],
            ["showStarMatches", DataType_Boolean],
            ["showSimplifiedSurfaces", DataType_Boolean],
            ["showDistortion", DataType_Boolean],
            ["generateDistortModel", DataType_Boolean],
            ["catalog", DataType_UCString],
            ["distortionCorrection", DataType_Boolean],
            ["splineOrder", DataType_UInt8],
            ["splineSmoothing", DataType_Float],
            ["enableSimplifier", DataType_Boolean],
            ["simplifierRejectFraction", DataType_Float],
            ["outlierDetectionRadius", DataType_Int32],
            ["outlierDetectionMinThreshold", DataType_Float],
            ["outlierDetectionSigma", DataType_Float],
            ["useDistortionModel", DataType_Boolean],
            ["distortionModelPath", DataType_UCString],
            ["useActive", DataType_Boolean],
            ["outSuffix", DataType_UCString],
            ["files", Ext_DataType_StringArray],
            ["projection", DataType_UInt8],
            ["projectionOriginMode", DataType_UInt8],
            ["restrictToHQStars", DataType_Boolean],
            ["tryApparentCoordinates", DataType_Boolean]
        )
    )

    this.version = SOLVERVERSION
    this.useActive = true
    this.files = []
    this.catalogMode = CatalogMode.prototype.Automatic
    this.availableCatalogs = [
        new PPMXLCatalog(),
        new TychoCatalog(),
        new HR_Catalog(),
        new GaiaDR2_Catalog()
    ]
    this.availableXPSDServers = [
        new GaiaDR3XPSDCatalog(),
        new GaiaEDR3XPSDCatalog(),
        new GaiaDR2XPSDCatalog()
    ]
    this.vizierServer = "https://vizier.cds.unistra.fr/"
    this.magnitude = 12
    this.maxIterations = 100
    this.structureLayers = 5
    this.minStructureSize = 0
    this.hotPixelFilterRadius = 1
    this.noiseReductionFilterRadius = 0
    this.sensitivity = 0.5
    this.peakResponse = 0.5
    this.brightThreshold = 3.0
    this.maxStarDistortion = 0.6
    this.autoPSF = false
    this.generateErrorImg = false
    this.showStars = false
    this.catalog = "PPMXL"
    this.autoMagnitude = true
    this.showStarMatches = false
    this.showSimplifiedSurfaces = false
    this.showDistortion = false
    this.distortionCorrection = true
    this.splineOrder = 2
    this.splineSmoothing = 0.005
    this.enableSimplifier = true
    this.simplifierRejectFraction = 0.10
    this.outlierDetectionRadius = 160
    this.outlierDetectionMinThreshold = 4.0
    this.outlierDetectionSigma = 5.0
    this.generateDistortModel = false
    this.useDistortionModel = false
    this.distortionModelPath = null
    this.outSuffix = "_ast"
    this.projection = 0
    this.projectionOriginMode = 0
    this.restrictToHQStars = false
    this.tryApparentCoordinates = true

    this.ResetSettings = function () {
        Settings.remove(SETTINGS_MODULE)
    }
}

SolverConfiguration.prototype = new ObjectWithSettings

function ImageSolver() {
    this.config = new SolverConfiguration(SETTINGS_MODULE_SCRIPT)
    this.metadata = new ImageMetadata(SETTINGS_MODULE_SCRIPT)

    this.config.catalogMode = 2
    this.config.vizierServer = "https://vizier.cds.unistra.fr/"
    this.config.magnitude = 12
    this.config.maxIterations = 100
    this.config.structureLayers = 5
    this.config.minStructureSize = 0
    this.config.hotPixelFilterRadius = 1
    this.config.noiseReductionFilterRadius = 0
    this.config.sensitivity = 0.5
    this.config.peakResponse = 0.5
    this.config.brightThreshold = 3
    this.config.maxStarDistortion = 0.6
    this.config.autoPSF = false
    this.config.generateErrorImg = false
    this.config.showStars = false
    this.config.catalog = "PPMXL"
    this.config.autoMagnitude = true
    this.config.showStarMatches = false
    this.config.showSimplifiedSurfaces = false
    this.config.showDistortion = false
    this.config.distortionCorrection = true
    this.config.splineOrder = 2
    this.config.splineSmoothing = 0.005
    this.config.enableSimplifier = true
    this.config.simplifierRejectFraction = 0.10
    this.config.outlierDetectionRadius = 160
    this.config.outlierDetectionMinThreshold = 4
    this.config.outlierDetectionSigma = 5
    this.config.generateDistortModel = false
    this.config.useDistortionModel = false
    this.config.distortionModelPath = null
    this.config.outSuffix = "_ast"
    this.config.projection = 0
    this.config.projectionOriginMode = 0
    this.config.restrictToHQStars = false
    this.config.tryApparentCoordinates = true

    /*
     * Initializes the image solver. If the parameter prioritizeSettings is
     * defined and is true, the solver will use the values stored in preferences
     * instead of the values obtained from the image.
     */
    this.Init = function (window, prioritizeSettings) {
        if (prioritizeSettings)
            if (window && window.isWindow)
                this.metadata.ExtractMetadata(window)

        this.metadata.LoadSettings()
        this.metadata.LoadParameters()

        if (!prioritizeSettings)
            if (window && window.isWindow)
                this.metadata.ExtractMetadata(window)

        this.metadata.ensureValidReferenceSystemForSolution()
    }

    this.InitialAlignment = function (window) {
        const SA = new StarAlignment
        SA.referenceImage = STAR_CSV_FILE
        SA.referenceIsFile = true
        SA.mode = StarAlignment.prototype.OutputMatrix
        SA.writeKeywords = false
        SA.structureLayers = this.config.structureLayers
        SA.minStructureSize = this.config.minStructureSize
        SA.hotPixelFilterRadius = this.config.hotPixelFilterRadius
        SA.noiseReductionFilterRadius = this.config.noiseReductionFilterRadius
        SA.sensitivity = this.config.sensitivity
        SA.peakResponse = this.config.peakResponse
        SA.brightThreshold = this.config.brightThreshold
        SA.maxStarDistortion = this.config.maxStarDistortion
        SA.allowClusteredSources = true; // because we want it to match as many stars as possible at this stage
        SA.ransacTolerance = 2
        SA.ransacMaxIterations = 5000
        SA.ransacMaximizeInliers = 1
        SA.ransacMaximizeOverlapping = 0
        SA.ransacMaximizeRegularity = 0
        SA.ransacMinimizeError = 0
        SA.useTriangles = false
        SA.polygonSides = 7
        SA.descriptorsPerStar = 100
        SA.restrictToPreviews = false

        if (this.config.useDistortionModel) {
            SA.distortionModel = this.config.distortionModelPath
            SA.undistortedReference = true
        }

        if (!SA.executeOn(window.currentView, false)) {
            /*
             * If we are using polygonal descriptors, try again using triangle
             * similarity, just in case we have a mirrored image.
             */
            console.noteln("<end><cbr><br>* Previous attempt with polygonal descriptors failed - trying with triangle similarity.")
            SA.useTriangles = true
            if (!SA.executeOn(window.currentView, false))
                return null
        }

        let numPairs = Math.min(SA.outputData[0][2], 4000)
        let pairs = {
            pS: new Array(numPairs),
            pI: new Array(numPairs)
        }

        for (let i = 0; i < numPairs; ++i) {
            pairs.pS[i] = new Point(SA.outputData[0][29][i], SA.outputData[0][30][i])
            pairs.pI[i] = new Point(SA.outputData[0][31][i] + 0.5, SA.outputData[0][32][i] + 0.5)
        }

        return pairs
    }

    this.GenerateTemplate = function (metadata, templateGeom) {
        if (this.config.catalogMode == CatalogMode.prototype.LocalText) {
            this.catalog = new CustomCatalog(this.config.databasePath)
        } else {
            this.catalog = __catalogRegister__.GetCatalog(this.catalogName)
            this.catalog.magMax = this.limitMagnitude
            this.catalog.restrictToHQStars = this.config.restrictToHQStars
        }

        this.catalog.Load(metadata, this.config.vizierServer)

        if (this.catalog.objects == null)
            throw "Catalog error: " + this.catalogName

        let ref_G_S = templateGeom.ref_S_G.inverse()

        let file = File.createFileForWriting(STAR_CSV_FILE)
        file.outTextLn(templateGeom.width + "," + templateGeom.height)
        let elements = this.catalog.objects
        let numStars = 0
        let clipRectS = templateGeom.clipRectS || new Rect(0, 0, templateGeom.width, templateGeom.height)

        for (let i = 0; i < elements.length; ++i)
            if (elements[i]) {
                let flux = (elements[i].magnitude == null) ? 0 : Math.pow(2.512, -1.5 - elements[i].magnitude)
                let pos_G = templateGeom.projection.Direct(elements[i].posRD)

                if (pos_G) {
                    let pos_S = ref_G_S.apply(pos_G)

                    if (pos_S.x > clipRectS.left
                        && pos_S.x < clipRectS.right
                        && pos_S.y > clipRectS.top
                        && pos_S.y < clipRectS.bottom) {
                        file.outTextLn(format("%.4f,%.4f,%.3e", pos_S.x, pos_S.y, flux))
                        numStars++
                    }
                }
            }

        file.close()

        if (numStars < 8)
            throw "Found too few stars. The magnitude filter could be too strict, or the catalog server could be malfunctioning."
    }

    this.DoIterationSA = function (window, metadata) {
        try {
            /*
             * Render a star field around the original coordinates.
             */
            let templateSize = Math.max(metadata.width, metadata.height)
            let templateGeom = {
                ref_S_G: new Matrix(-metadata.resolution, 0, metadata.resolution * templateSize / 2,
                    0, -metadata.resolution, metadata.resolution * templateSize / 2,
                    0, 0, 1),
                projection: ProjectionFactory(this.config, metadata.ra, metadata.dec),
                width: templateSize,
                height: templateSize,
                clipRectS: null
            }

            this.GenerateTemplate(metadata, templateGeom)

            /*
             * Perform the initial image registration using a projective
             * transformation.
             */
            let pairs = this.InitialAlignment(window)

            if (pairs === null) {
                /*
                 * Some image acquisition applications store apparent or 'of the
                 * date' coordinates in image metadata without providing the
                 * required metadata items to let us know. If the corresponding
                 * option is enabled, make a second attempt assuming that the
                 * center coordinates are apparent.
                 */
                if (this.config.tryApparentCoordinates)
                    if (metadata.mightBeApparent) {
                        console.noteln("<end><cbr><br>* Previous attempt failed - trying again assuming apparent coordinates.")
                        metadata.convertRADecFromApparentToAstrometric()
                        metadata.mightBeApparent = false
                        templateGeom.projection = ProjectionFactory(this.config, metadata.ra, metadata.dec)
                        this.GenerateTemplate(metadata, templateGeom)
                        pairs = this.InitialAlignment(window)

                        if (pairs === null) {
                            if (!console.isAborted) {
                                console.criticalln("<end><cbr><br>*** Error: The image could not be aligned with the reference star field.")
                                console.writeln(
                                    "<html>" +
                                    "<p><strong>Please check the following items:</strong></p>" +
                                    "<ul>" +
                                    "<li>The initial center coordinates should be inside the image.</li>" +
                                    "<li>The initial image resolution should be within a factor of 2 from the correct value.</li>" +
                                    "<li>If you use an online star catalog through the VizieR service, consider using " +
                                    "the Gaia DR3 catalog with local XPSD databases instead.</li>" +
                                    "<li>If the image has extreme noise levels, bad tracking, or is poorly focused, you may " +
                                    "need to adjust some star detection parameters.</li>" +
                                    "</ul>" +
                                    "</html>")
                            }
                            throw ""
                        }
                        else {
                            console.warningln("<end><cbr><br>** Warning: The image provides apparent or 'of the date' coordinates in " +
                                "image metadata but does not include the appropriate Observation:CelestialReferenceSystem XISF property " +
                                "or RADESYS FITS keyword. We suggest you inform the authors of your image acquisition application about " +
                                "this error, which they should fix.")
                        }
                    }
            }

            /*
             * Adjust to a projection with the origin at the center of the image.
             */
            let pG = pairs.pS.map(p => templateGeom.ref_S_G.apply(p))
            let ref_S_G = Math.homography(pairs.pI, pG)
            let centerRD = templateGeom.projection.Inverse(ref_S_G.apply(new Point(metadata.width / 2, metadata.height / 2)))
            let newProjection = ProjectionFactory(this.config, centerRD.x, centerRD.y)
            pairs.pG = pG.map(p => newProjection.Direct(templateGeom.projection.Inverse(p)))
            templateGeom.projection = newProjection

            /*
             * Initialize a new metadata structure appropriate for the selected
             * working mode.
             */
            let newMetadata = metadata.Clone()
            newMetadata.projection = templateGeom.projection

            if (this.config.distortionCorrection) {
                // Using surface splines.
                newMetadata.ref_I_G_linear = Math.homography(pairs.pI, pairs.pG)

                newMetadata.ref_I_G = new ReferSpline(pairs.pI, pairs.pG,
                    this.config.splineOrder,
                    this.config.splineSmoothing,
                    this.config.enableSimplifier,
                    this.config.simplifierRejectFraction)
                processEvents()

                newMetadata.ref_G_I = new ReferSpline(pairs.pG, pairs.pI,
                    this.config.splineOrder,
                    this.config.splineSmoothing,
                    this.config.enableSimplifier,
                    this.config.simplifierRejectFraction)
                processEvents()

                newMetadata.controlPoints = {
                    pI: pairs.pI,
                    pG: pairs.pG
                }
            }
            else {
                // Using a linear solution.
                newMetadata.ref_I_G = Math.homography(pairs.pI, pairs.pG)
                newMetadata.ref_I_G_linear = newMetadata.ref_I_G
                newMetadata.ref_G_I = newMetadata.ref_I_G.inverse()
                newMetadata.controlPoints = null
            }

            /*
             * Find the celestial coordinates (RD) of the center of the original
             * image. First transform from I to G and then unproject the native
             * projection coordinates (G) to celestial (RD).
             */
            let centerI = new Point(metadata.width / 2, metadata.height / 2)
            let centerG = newMetadata.ref_I_G.apply(centerI)
            centerRD = newMetadata.projection.Inverse(centerG)
            while (centerRD.x < 0)
                centerRD.x += 360
            while (centerRD.x >= 360)
                centerRD.x -= 360
            newMetadata.ra = centerRD.x
            newMetadata.dec = centerRD.y
            let ref = newMetadata.ref_I_G_linear
            let resx = Math.sqrt(ref.at(0, 0) * ref.at(0, 0) + ref.at(0, 1) * ref.at(0, 1))
            let resy = Math.sqrt(ref.at(1, 0) * ref.at(1, 0) + ref.at(1, 1) * ref.at(1, 1))
            newMetadata.resolution = (resx + resy) / 2
            newMetadata.focal = newMetadata.FocalFromResolution(newMetadata.resolution)
            newMetadata.useFocal = false

            return newMetadata
        }
        catch (ex) {
            if (!console.isAborted)
                if (ex.length === undefined || ex.length > 0)
                    console.criticalln("<end><cbr>*** Error: ", ex.toString())
            return null
        }
        finally {
            try {
                if (File.exists(STAR_CSV_FILE))
                    File.remove(STAR_CSV_FILE)
            }
            catch (x) {
                // Propagate no further filesystem exceptions here.
            }
        }
    }

    this.MatchStars = function (window, predictedCoords) {
        /*
         * Putative point matches by proximity search.
         */
        let actualCoords = new Array(predictedCoords.length)
        for (let i = 0; i < predictedCoords.length; ++i) {
            let p = predictedCoords[i]
            if (p) {
                let s = this.starTree.search({
                    x0: p.x - this.psfSearchRadius,
                    y0: p.y - this.psfSearchRadius,
                    x1: p.x + this.psfSearchRadius,
                    y1: p.y + this.psfSearchRadius
                })
                if (s.length > 0) {
                    let j = 0
                    if (s.length > 1) {
                        let star = this.starTree.objects[s[0]]
                        let dx = star.x - p.x
                        let dy = star.y - p.y
                        let d2 = dx * dx + dy * dy
                        for (let i = 1; i < s.length; ++i) {
                            let star = this.starTree.objects[s[i]]
                            let dx = star.x - p.x
                            let dy = star.y - p.y
                            let d2i = dx * dx + dy * dy
                            if (d2i < d2) {
                                j = i
                                d2 = d2i
                            }
                        }
                    }
                    let star = this.starTree.objects[s[j]]
                    actualCoords[i] = new Point(star.x, star.y)
                }
            }
        }

        if (!this.config.distortionCorrection)
            return { matchedPoints: actualCoords, meanSparsity: 0, sigmaSparsity: 0, rejectionThreshold: 0 }

        /*
         * Adaptive spline outlier rejection based on local sparsity estimation.
         * In this context, outliers are wrongly extrapolated points that can
         * prevent modeling non-convex surfaces and regions of strongly varying
         * distortion by stalling surface spline generation in subsequent
         * iterations.
         */
        let P = []
        for (let i = 0; i < actualCoords.length; ++i) {
            let p = actualCoords[i]
            if (p)
                P.push({
                    x: p.x, y: p.y,
                    rect: {
                        x0: p.x - 0.5, y0: p.y - 0.5,
                        x1: p.x + 0.5, y1: p.y + 0.5
                    },
                    idx: i
                })
        }
        let T = new BRQuadTree(P, 256/*bucketSize*/)
        let S = new Float32Array(P.length)
        for (let i = 0; i < P.length; ++i) {
            let p = P[i]
            let r = {
                x0: p.x - this.config.outlierDetectionRadius,
                y0: p.y - this.config.outlierDetectionRadius,
                x1: p.x + this.config.outlierDetectionRadius,
                y1: p.y + this.config.outlierDetectionRadius
            }
            let s1 = this.starTree.search(r)
            let s2 = T.search(r)
            S[i] = s1.length / s2.length
        }
        let m = Math.median(S)
        let s = 1.1926 * Math.Sn(S)
        let d = Math.max(this.config.outlierDetectionMinThreshold,
            m + this.config.outlierDetectionSigma * s)
        for (let i = 0; i < P.length; ++i)
            if (S[i] > d)
                P[i] = null; // outlier removed

        /*
         * Output coordinates.
         */
        let Q = new Array(predictedCoords.length)
        for (let i = 0; i < P.length; ++i) {
            let p = P[i]
            if (p)
                Q[p.idx] = actualCoords[p.idx]
        }
        return { matchedPoints: Q, meanSparsity: m, sigmaSparsity: s, rejectionThreshold: d }
    }

    // This warning is now silenced.
    this.showedWarningOnTruncatedInputSet = true; //false

    this.DetectStars = function (window, metadata) {
        /*
         * Load reference stars.
         */
        if (!this.catalog)
            if (this.config.catalogMode == CatalogMode.prototype.LocalText) {
                this.catalog = new CustomCatalog(this.config.databasePath)
            }
            else {
                this.catalog = __catalogRegister__.GetCatalog(this.catalogName)
                this.catalog.magMax = this.limitMagnitude
                this.catalog.restrictToHQStars = this.config.restrictToHQStars
            }
        this.catalog.Load(metadata, this.config.vizierServer)
        let catalogObjects = this.catalog.objects
        if (catalogObjects == null)
            throw "Catalog error: " + this.catalogName
        if (catalogObjects.length < 10)
            throw "Insufficient stars found in catalog: " + this.catalogName
        if (catalogObjects.length > WCS_MAX_STARS_IN_SOLUTION)
            if (!this.showedWarningOnTruncatedInputSet) {
                console.warningln("<end><cbr>** Warning: Exceeded the maximum number of stars allowed. " +
                    "Truncating the input set to the ", WCS_MAX_STARS_IN_SOLUTION, " brightest stars.")
                this.showedWarningOnTruncatedInputSet = true
            }

        /*
         * Sort reference stars by magnitude in ascending order (brighter stars
         * first). Possible objects with undefined magnitudes are packed at the
         * tail of the array.
         */
        catalogObjects.sort((a, b) => a.magnitude ? (b.magnitude ? a.magnitude - b.magnitude : -1) : (b.magnitude ? +1 : 0))

        /*
         * Calculate image coordinates of catalog stars with the current
         * transformation.
         */
        let result = {
            projection: ProjectionFactory(this.config, metadata.ra, metadata.dec),
            starCoords: [],
            coordsG: [],
            magnitudes: [],
            actualCoords: null
        }
        let predictedCoords = []
        {
            let posRD = [], magnitudes = []
            for (let i = 0, n = Math.min(WCS_MAX_STARS_IN_SOLUTION, catalogObjects.length); i < n; ++i)
                if (catalogObjects[i]) {
                    posRD.push(catalogObjects[i].posRD)
                    magnitudes.push(catalogObjects[i].magnitude)
                }
            let posI = metadata.Convert_RD_I_Points(posRD, true/*unscaled*/)

            for (let i = 0; i < posI.length; ++i) {
                let pI = posI[i]
                if (pI &&
                    pI.x >= 0 &&
                    pI.y >= 0 &&
                    pI.x <= metadata.width &&
                    pI.y <= metadata.height) {
                    let pG = result.projection.Direct(posRD[i])
                    if (pG) {
                        result.coordsG.push(pG)
                        result.starCoords.push(posRD[i])
                        result.magnitudes.push(magnitudes[i])
                        predictedCoords.push(pI)
                    }
                }
            }
        }

        if (predictedCoords.length < 4)
            throw "Unable to define a valid set of reference star positions."

        /*
         * Find the stars in the image using predictedCoords as starting point.
         */
        let matches = this.MatchStars(window, predictedCoords)

        result.actualCoords = matches.matchedPoints

        /*
         * Remove control points with identical coordinates.
         */
        {
            let A = []
            for (let i = 0; i < result.actualCoords.length; ++i)
                if (result.actualCoords[i])
                    A.push({ i: i, x: result.actualCoords[i].x, y: result.actualCoords[i].y })
            A.sort((a, b) => (a.x != b.x) ? a.x - b.x : a.y - b.y)
            for (let i = 1; i < A.length; ++i)
                if (A[i].x == A[i - 1].x)
                    if (A[i].y == A[i - 1].y) {
                        result.actualCoords[A[i].i] = null
                        result.coordsG[A[i].i] = null
                    }
            A = []
            for (let i = 0; i < result.coordsG.length; ++i)
                if (result.coordsG[i])
                    A.push({ i: i, x: result.coordsG[i].x, y: result.coordsG[i].y })
            A.sort((a, b) => (a.x != b.x) ? a.x - b.x : a.y - b.y)
            for (let i = 1; i < A.length; ++i)
                if (A[i].x == A[i - 1].x)
                    if (A[i].y == A[i - 1].y) {
                        result.actualCoords[A[i].i] = null
                        result.coordsG[A[i].i] = null
                    }
        }

        /*
         * Gather information on matching errors.
         */
        result.errors = new Array(predictedCoords.length)
        result.numValid = 0
        let meanError, sigmaError, peakError = 0, sum2 = 0
        {
            let E = []
            for (let i = 0; i < predictedCoords.length; ++i)
                if (result.actualCoords[i]) {
                    let ex = predictedCoords[i].x - result.actualCoords[i].x
                    let ey = predictedCoords[i].y - result.actualCoords[i].y
                    let e = Math.sqrt(ex * ex + ey * ey)
                    result.errors[i] = e
                    E.push(e)
                    if (e > peakError)
                        peakError = e
                    result.numValid++
                    sum2 += e * e
                }

            meanError = Math.median(E)
            sigmaError = Math.sqrt(Math.biweightMidvariance(E, meanError))
        }
        result.rms = (result.numValid > 0) ? Math.sqrt(sum2 / result.numValid) : 0
        result.score = Math.roundTo(result.numValid / (1 + result.rms), 3)

        if (this.config.distortionCorrection)
            console.writeln(format("Surface sparsity : median = %.2f, sigma = %.2f, threshold = %.2f",
                matches.meanSparsity, matches.sigmaSparsity, matches.rejectionThreshold))
        console.writeln(format("Matching errors  : median = %.2f px, sigma = %.2f px, peak = %.2f px",
            meanError, sigmaError, peakError))
        console.writeln(format("Matched stars    : %d (%.2f%%)",
            result.numValid, 100.0 * result.numValid / predictedCoords.length))
        console.flush()

        return result
    }

    this.DoIterationLinear = function (metadata, stars) {
        console.flush()
        processEvents()

        /*
         * Find linear transformations.
         */
        let newMetadata = metadata.Clone()
        newMetadata.projection = stars.projection
        newMetadata.ref_I_G = Math.homography(stars.actualCoords, stars.coordsG)
        newMetadata.ref_I_G_linear = newMetadata.ref_I_G
        newMetadata.ref_G_I = newMetadata.ref_I_G.inverse()
        newMetadata.controlPoints = null

        /*
         * Find the celestial coordinates (RD) of the center of the original
         * image. First transform from I to G and then unproject from native
         * projection coordinates (G) to celestial (RD).
         */
        let centerI = new Point(metadata.width / 2, metadata.height / 2)
        let centerG = newMetadata.ref_I_G.apply(centerI)
        let centerRD = newMetadata.projection.Inverse(centerG)
        while (centerRD.x < 0)
            centerRD.x += 360
        while (centerRD.x >= 360)
            centerRD.x -= 360
        newMetadata.ra = (Math.abs(metadata.ra - centerRD.x) < 1) ? (metadata.ra + centerRD.x * 2) / 3 : centerRD.x
        newMetadata.dec = (Math.abs(metadata.dec - centerRD.y) < 1) ? (metadata.dec + centerRD.y * 2) / 3 : centerRD.y
        let ref = newMetadata.ref_I_G_linear
        let resx = Math.sqrt(ref.at(0, 0) * ref.at(0, 0) + ref.at(0, 1) * ref.at(0, 1))
        let resy = Math.sqrt(ref.at(1, 0) * ref.at(1, 0) + ref.at(1, 1) * ref.at(1, 1))
        newMetadata.resolution = (resx + resy) / 2
        newMetadata.focal = newMetadata.FocalFromResolution(newMetadata.resolution)
        newMetadata.useFocal = false

        return newMetadata
    }

    this.DoIterationSpline = function (metadata, stars) {
        console.flush()
        processEvents()

        /*
         * Build surface splines.
         */
        let newMetadata = metadata.Clone()
        newMetadata.projection = stars.projection
        newMetadata.ref_I_G_linear = Math.homography(stars.actualCoords, stars.coordsG)

        newMetadata.ref_I_G = new ReferSpline(stars.actualCoords, stars.coordsG,
            this.config.splineOrder,
            this.config.splineSmoothing,
            this.config.enableSimplifier,
            this.config.simplifierRejectFraction)
        processEvents()

        newMetadata.ref_G_I = new ReferSpline(stars.coordsG, stars.actualCoords,
            this.config.splineOrder,
            this.config.splineSmoothing,
            this.config.enableSimplifier,
            this.config.simplifierRejectFraction)
        processEvents()

        newMetadata.controlPoints = {
            pI: stars.actualCoords,
            pG: stars.coordsG,
            weights: null
        }

        /*
         * Find the celestial coordinates (RD) of the center of the original
         * image. First transform from I to G and then unproject from native
         * projection coordinates (G) to celestial (RD).
         */
        let centerI = new Point(metadata.width / 2, metadata.height / 2)
        let centerG = newMetadata.ref_I_G.apply(centerI)
        let centerRD = newMetadata.projection.Inverse(centerG)
        while (centerRD.x < 0)
            centerRD.x += 360
        while (centerRD.x >= 360)
            centerRD.x -= 360
        newMetadata.ra = (Math.abs(metadata.ra - centerRD.x) < 1) ? (metadata.ra + centerRD.x * 2) / 3 : centerRD.x
        newMetadata.dec = (Math.abs(metadata.dec - centerRD.y) < 1) ? (metadata.dec + centerRD.y * 2) / 3 : centerRD.y
        let ref = newMetadata.ref_I_G_linear
        let resx = Math.sqrt(ref.at(0, 0) * ref.at(0, 0) + ref.at(0, 1) * ref.at(0, 1))
        let resy = Math.sqrt(ref.at(1, 0) * ref.at(1, 0) + ref.at(1, 1) * ref.at(1, 1))
        newMetadata.resolution = (resx + resy) / 2
        newMetadata.focal = newMetadata.FocalFromResolution(newMetadata.resolution)
        newMetadata.useFocal = false

        return newMetadata
    }

    this.GenerateWorkingImage = function (targetWindow) {
        // Convert the image to grayscale.
        // The chrominance is not necessary for the astrometry.
        let grayscaleImage = new Image
        grayscaleImage.assign(targetWindow.mainView.image)
        grayscaleImage.colorSpace = ColorSpace_HSI
        grayscaleImage.selectedChannel = 2; // intensity component

        let workingWindow = new ImageWindow(grayscaleImage.width, grayscaleImage.height,
            1/*channels*/, 32/*bits*/, true/*float*/, false/*color*/,
            targetWindow.mainView.id + "_working")
        workingWindow.mainView.beginProcess(UndoFlag_NoSwapFile)
        workingWindow.mainView.image.apply(grayscaleImage)
        workingWindow.mainView.endProcess()

        // Deallocate now, don't wait for garbage collection.
        grayscaleImage.free()

        return workingWindow
    }

    this.MetadataDelta = function (metadata1, metadata2, pI) {
        /*
         * Calculate the difference between the last two iterations using the
         * displacement between the center and the given point pI.
         */
        let pRD2 = metadata2.Convert_I_RD(pI)
        let pRD1 = metadata1.ref_I_G ? metadata1.Convert_I_RD(pI) : pRD2
        let delta1 = 0
        if (pRD1)
            delta1 = Math.sqrt(Math.pow((pRD1.x - pRD2.x) * Math.cos(Math.rad(pRD2.y)), 2) +
                Math.pow(pRD1.y - pRD2.y, 2)) * 3600
        let delta2 = Math.sqrt(Math.pow((metadata2.ra - metadata1.ra) * Math.cos(Math.rad(metadata2.dec)), 2) +
            Math.pow(metadata2.dec - metadata1.dec, 2)) * 3600
        return Math.max(delta1, delta2)
    }

    this.OptimizeSolution = function (workingWindow, currentMetadata, stars) {
        let finished = false
        let iteration = 1
        let numItersWithoutImprovement = 0
        let maxItersWithoutImprovement = 4
        let bestMetadata = currentMetadata
        let bestScore = stars.score
        let bestRMS = stars.rms
        let bestStarCount = stars.numValid
        let converged = false

        do {
            console.abortEnabled = true

            let result
            try {
                if (this.config.distortionCorrection)
                    result = this.DoIterationSpline(currentMetadata, stars)
                else
                    result = this.DoIterationLinear(currentMetadata, stars)

                if (result == null)
                    throw ""
            }
            catch (ex) {
                let haveException = !console.isAborted && (ex.length === undefined || ex.length > 0)
                if (haveException)
                    console.criticalln("<end><cbr><br>*** Error: " + ex.toString())
                console.criticalln("<end><cbr>" +
                    (haveException ? "" : "<br>*** Error: ") +
                    "The image could not be fully solved. We have tagged it with the latest known valid solution.")
                console.abortEnabled = false
                break
            }

            stars = this.DetectStars(workingWindow, result)

            /*
             * Calculate the difference between the current and previous
             * iterations using the displacements between the center and eight
             * points located on the image borders. Report the maximum difference.
             */
            let delta = Math.max(this.MetadataDelta(currentMetadata, result, new Point(0, 0)),
                this.MetadataDelta(currentMetadata, result, new Point(result.width, 0)),
                this.MetadataDelta(currentMetadata, result, new Point(0, result.height)),
                this.MetadataDelta(currentMetadata, result, new Point(result.width, result.height)),
                this.MetadataDelta(currentMetadata, result, new Point(result.width / 2, 0)),
                this.MetadataDelta(currentMetadata, result, new Point(result.width / 2, result.height)),
                this.MetadataDelta(currentMetadata, result, new Point(0, result.height / 2)),
                this.MetadataDelta(currentMetadata, result, new Point(result.width, result.height / 2)))
            let deltaPx = delta / (result.resolution * 3600)

            console.writeln("<end><cbr><br>*****")
            console.writeln(format("Iteration %d, delta = %.3f as (%.2f px)", iteration, delta, deltaPx))
            console.writeln("Image center ... RA: ", DMSangle.FromAngle(result.ra / 15).ToString(true),
                "  Dec: ", DMSangle.FromAngle(result.dec).ToString())
            console.writeln(format("Resolution ..... %.2f as/px", result.resolution * 3600))
            console.writeln(format("RMS error ...... %.3f px (%d stars)", stars.rms, stars.numValid))

            converged = deltaPx < 0.005 && Math.abs(stars.rms - bestRMS) < 0.01
            if (converged || stars.numValid > bestStarCount && (stars.rms <= bestRMS || stars.rms - bestRMS < 0.01))
                stars.score = Math.max(stars.score, bestScore + 1)

            if (stars.score > bestScore)
                console.writeln(format("Score .......... \x1b[38;2;128;255;128m%.3f\x1b[0m", stars.score))
            else
                console.writeln(format("Score .......... %.3f", stars.score))
            console.writeln("*****")

            /*
             * Prevent degenerate cases where we cannot match any stars. This
             * happens, among other causes, when projection systems are used
             * beyond their capabilities.
             */
            if (stars.numValid < 4) {
                console.criticalln("*** Error: Unable to find a valid set of star pair matches.")
                break
            }

            currentMetadata = result

            // Store the best model so far
            if (stars.score > bestScore) {
                numItersWithoutImprovement = 0
                bestMetadata = result
                bestScore = stars.score
                bestRMS = stars.rms
                bestStarCount = stars.numValid
            }
            else {
                if (iteration == 1)
                    bestMetadata = result
                numItersWithoutImprovement++
            }

            // Finish condition
            finished = true
            if (converged || numItersWithoutImprovement > maxItersWithoutImprovement) {
                if (this.distortModel) {
                    converged = false
                    numItersWithoutImprovement = 0
                    finished = false
                    this.distortModel = null
                    console.noteln("* The solution with distortion model has converged. Trying to optimize it without the model.")
                }
                else if (converged)
                    console.noteln(format("* Convergence reached after %d iterations.", iteration))
                else
                    console.warningln(format("** Warning: Process stalled after %d iterations.", iteration))
            }
            else if (iteration > this.config.maxIterations)
                console.warningln("** Warning: Reached maximum number of iterations without convergence.")
            else
                finished = false

            ++iteration

            console.abortEnabled = true
            processEvents()
            if (console.abortRequested) {
                finished = true
                console.criticalln("*** User requested abort ***")
            }
            gc(true)
        }
        while (!finished)

        if (converged)
            console.noteln(format("* Successful astrometry optimization. Score = %.3f", bestScore))
        else
            console.warningln(format("** Partial astrometry optimization. Score = %.3f", bestScore))
        console.writeln()

        return bestMetadata
    }

    this.SolveImage = function (targetWindow) {
        this.error = false

        let abortableBackup = jsAbortable
        jsAbortable = true
        let auxWindow = null

        try {
            console.show()
            console.abortEnabled = true

            let workingWindow = targetWindow
            if (targetWindow.mainView.image.isColor)
                auxWindow = workingWindow = this.GenerateWorkingImage(targetWindow)

            /*
             * Build a bucket region quadtree structure with all detected stars in
             * the image for fast star matching.
             */
            try {
                /*
                 * Step 1 - Star detection
                 */
                let D = new StarDetector
                D.structureLayers = this.config.structureLayers
                D.hotPixelFilterRadius = this.config.hotPixelFilterRadius
                D.noiseReductionFilterRadius = this.config.noiseReductionFilterRadius
                D.sensitivity = this.config.sensitivity
                D.peakResponse = this.config.peakResponse
                D.allowClusteredSources = false
                D.maxDistortion = this.config.maxStarDistortion
                D.brightThreshold = this.config.brightThreshold
                D.minStructureSize = this.config.minStructureSize
                let lastProgressPc = 0
                D.progressCallback =
                    (count, total) => {
                        if (count == 0) {
                            console.write("<end><cbr>Detecting stars:   0%")
                            lastProgressPc = 0
                            processEvents()
                        }
                        else {
                            let pc = Math.round(100 * count / total)
                            if (pc > lastProgressPc) {
                                console.write(format("<end>\b\b\b\b%3d%%", pc))
                                lastProgressPc = pc
                                processEvents()
                            }
                        }
                        return true
                    }

                let S = D.stars(workingWindow.mainView.image)
                this.numberOfDetectedStars = S.length
                if (this.numberOfDetectedStars < 6)
                    throw "Insufficient stars detected: found " + this.numberOfDetectedStars.toString() + ", at least 6 are required."


                console.writeln(format("<end><cbr>%d stars found ", this.numberOfDetectedStars))
                console.flush()

                /*
                 * Step 2 - PSF fitting
                 */
                let stars = []
                let minStructSize = Number.POSITIVE_INFINITY
                for (let i = 0; i < S.length; ++i) {
                    let p = S[i].pos
                    let r = S[i].rect
                    stars.push([0, 0, DynamicPSF.prototype.Star_DetectedOk,
                        r.x0, r.y0, r.x1, r.y1,
                        p.x, p.y])
                    let m = Math.max(r.x1 - r.x0, r.y1 - r.y0)
                    if (m < minStructSize)
                        minStructSize = m
                }

                let P = new DynamicPSF
                P.views = [[workingWindow.mainView.id]]
                P.stars = stars
                P.astrometry = false
                P.autoAperture = true
                P.searchRadius = minStructSize
                P.circularPSF = false
                P.autoPSF = this.config.autoPSF
                P.gaussianPSF = true
                P.moffatPSF = P.moffat10PSF = P.moffat8PSF =
                    P.moffat6PSF = P.moffat4PSF = P.moffat25PSF =
                    P.moffat15PSF = P.lorentzianPSF = this.config.autoPSF
                P.variableShapePSF = false
                if (!P.executeGlobal())
                    throw "Unable to execute DynamicPSF process."

                console.flush()

                stars = []
                for (let psf = P.psf, i = 0; i < psf.length; ++i) {
                    let p = psf[i]
                    if (p[3] == DynamicPSF.prototype.PSF_FittedOk) {
                        let x = p[6]
                        let y = p[7]
                        let rx = p[8] / 2
                        let ry = p[9] / 2
                        stars.push({
                            x: x, y: y,
                            rect: {
                                x0: x - rx, y0: y - ry,
                                x1: x + rx, y1: y + ry
                            }
                        })
                    }
                }

                /*
                 * Step 3 - Remove potential duplicate objects
                 */
                this.starTree = new BRQuadTree(stars.slice(), 256/*bucketSize*/)
                stars = []
                for (let i = 0; i < this.starTree.objects.length; ++i) {
                    let o = this.starTree.objects[i]
                    let s = this.starTree.search({
                        x0: o.x - 1, y0: o.y - 1,
                        x1: o.x + 1, y1: o.y + 1
                    })
                    if (s.length == 1)
                        stars.push(o)
                }
                if (stars.length < 6)
                    throw "Insufficient number of objects: found " + stars.length.toString() + ", at least 6 are required."

                console.write(format("<end><cbr>* Removed %d conflicting sources (%.2f %%)",
                    this.starTree.objects.length - stars.length, 100 * (this.starTree.objects.length - stars.length) / stars.length))

                /*
                 * Step 4 - Quadtree generation
                 */
                this.starTree.build(stars.slice(), 256/*bucketSize*/)
                console.write(format("<end><cbr>* Search quadtree generated with %d objects, %d node(s), height = %d",
                    this.starTree.objects.length, this.starTree.numberOfNodes(), this.starTree.height()))

                /*
                 * Step 5 - Calculate search and matching tolerances
                 */
                this.psfMinimumDistance = Math.min(stars[0].rect.x1 - stars[0].rect.x0,
                    stars[0].rect.y1 - stars[0].rect.y0)
                for (let i = 1; i < stars.length; ++i) {
                    let s = stars[i]
                    let d = Math.min(stars[i].rect.x1 - stars[i].rect.x0,
                        stars[i].rect.y1 - stars[i].rect.y0)
                    if (d < this.psfMinimumDistance)
                        this.psfMinimumDistance = d
                }
                this.psfMinimumDistance = Math.max(2, Math.trunc(0.75 * (this.psfMinimumDistance - 2))); // StarDetector inflates detection regions
                this.psfSearchRadius = 1.0 * this.psfMinimumDistance
                console.writeln(format("<end><cbr>* Star matching tolerance: %d px", this.psfMinimumDistance))
                console.flush()
            }
            catch (ex) {
                this.starTree = null
                gc()
                throw ex
            }

            /*
             * Find limit magnitude.
             */
            if (this.config.autoMagnitude || this.config.catalogMode == CatalogMode.prototype.Automatic) {
                let fov = this.metadata.resolution * Math.max(this.metadata.width, this.metadata.height)
                // Empiric formula for 1000 stars at 20 deg of galactic latitude
                let m = 14.5 * Math.pow(fov, -0.179)
                m = Math.round(100 * Math.min(20, Math.max(7, m))) / 100

                /*
                 * Identify a local XPSD server and use it if available to find an
                 * optimal magnitude limit adaptively.
                 */
                let xpsd = ((typeof Gaia) != 'undefined') ? (new Gaia) : null
                if (xpsd) {
                    xpsd.command = "get-info"
                    xpsd.dataRelease = Gaia.prototype.DataRelease_BestAvailable
                    xpsd.executeGlobal()
                    if (xpsd.isValid) {
                        if (this.config.autoMagnitude) {
                            let radiusPx = Math.SQRT2 * Math.sqrt(this.metadata.width * this.metadata.height) / 2
                            let targetStarCount = this.numberOfDetectedStars * 1.25

                            console.writeln(format("<end><cbr><br>Searching for optimal magnitude limit. Target: %u stars", targetStarCount))

                            xpsd.command = "search"
                            xpsd.centerRA = this.metadata.ra
                            xpsd.centerDec = this.metadata.dec
                            xpsd.radius = this.metadata.resolution * radiusPx
                            xpsd.magnitudeLow = -1.5
                            xpsd.sourceLimit = 0; // do not retrieve objects, just count them.
                            xpsd.exclusionFlags = GaiaFlag_NoPM
                            xpsd.inclusionFlags = this.config.restrictToHQStars ? GaiaFlag_GoodAstrometry : 0
                            xpsd.verbosity = 0; // work quietly
                            xpsd.generateTextOutput = false

                            const MAX_AUTOMAG_ITER = 100; // prevent a hypothetical case where the loop might stall
                            for (let m0 = 7, m1 = xpsd.databaseMagnitudeHigh, i = 0; i < MAX_AUTOMAG_ITER; ++i) {
                                xpsd.magnitudeHigh = m
                                xpsd.executeGlobal()
                                console.writeln(format("<end><cbr>m = %.2f, %u stars", m, xpsd.excessCount))
                                if (xpsd.excessCount < targetStarCount) {
                                    if (m1 - m < 0.05)
                                        break
                                    m0 = m
                                    m += (m1 - m) / 2
                                }
                                else if (xpsd.excessCount > 1.05 * targetStarCount) {
                                    if (m - m0 < 0.05)
                                        break
                                    m1 = m
                                    m -= (m - m0) / 2
                                }
                                else
                                    break
                            }
                        }
                    }
                    else {
                        /*
                         * We have a local XPSD server, but either it is not well
                         * configured, or there are no database files available.
                         */
                        xpsd = null
                    }
                }

                if (this.config.autoMagnitude) {
                    this.limitMagnitude = m
                    console.noteln("<end><cbr><br>* Using an automatically calculated limit magnitude of " + format("%.2f", m) + ".")
                }
                else
                    this.limitMagnitude = this.config.magnitude

                if (this.config.catalogMode == CatalogMode.prototype.Automatic) {
                    /*
                     * - For magnitude limits below 8, use the Bright Stars catalog.
                     * - Otherwise:
                     *    - Use a local XPSD server when available.
                     *    - Otherwise:
                     *       - Use the online Gaia DR2 catalog if FOV <= 3 deg.
                     *       - Use the online TYCHO-2 catalog if FOV > 3 deg.
                     */
                    if (this.limitMagnitude < 8)
                        this.catalogName = "Bright Stars"
                    else if (fov > 3 && !xpsd)
                        this.catalogName = "TYCHO-2"
                    else if (xpsd) {
                        switch (xpsd.outputDataRelease) {
                            default:
                            case Gaia.prototype.DataRelease_3:
                                this.catalogName = "GaiaDR3_XPSD"
                                break
                            case Gaia.prototype.DataRelease_E3:
                                this.catalogName = "GaiaEDR3_XPSD"
                                break
                            case Gaia.prototype.DataRelease_2:
                                this.catalogName = "GaiaDR2_XPSD"
                                break
                        }
                    }
                    else
                        this.catalogName = "GaiaDR2"

                    console.noteln("<end><cbr>* Using the automatically selected " + this.catalogName + " catalog.")
                }
                else
                    this.catalogName = this.config.catalog
            }
            else {
                this.limitMagnitude = this.config.magnitude
                this.catalogName = this.config.catalog
            }

            console.writeln("Seed parameters for plate solving:")
            console.writeln("   Center coordinates: RA = ",
                DMSangle.FromAngle(this.metadata.ra / 15).ToString(true), ", Dec = ",
                DMSangle.FromAngle(this.metadata.dec).ToString())
            console.writeln(format("   Resolution: %.3f as/px", this.metadata.resolution * 3600))
            console.writeln()

            let stars = null

            this.distortModel = null

            /*
             * Initial Alignment.
             */
            try {
                let result = this.DoIterationSA(targetWindow, this.metadata)
                if (!result)
                    throw ""
                this.metadata = result

                stars = this.DetectStars(workingWindow, this.metadata)

                console.writeln("<end><cbr><br>*****")
                console.writeln("Initial alignment")
                console.writeln("Image center ... RA: ", DMSangle.FromAngle(this.metadata.ra / 15).ToString(true),
                    "  Dec: ", DMSangle.FromAngle(this.metadata.dec).ToString())
                console.writeln(format("Resolution ..... %.2f as/px", this.metadata.resolution * 3600))
                console.writeln(format("RMS error ...... %.3f px (%d stars)", stars.rms, stars.numValid))
                console.writeln(format("Score .......... %.3f", stars.score))
                console.writeln("*****")
            }
            catch (ex) {
                if (!console.isAborted)
                    if (ex.length === undefined || ex.length > 0)
                        console.criticalln("<end><cbr><br>*** Error: " + ex.toString())
                this.error = true
                return false
            }

            /*
             * Optimize the solution.
             */
            this.metadata = this.OptimizeSolution(workingWindow, this.metadata, stars)

            /*
             * Update metadata and regenerate the astrometric solution.
             */
            targetWindow.mainView.beginProcess(UndoFlag_Keywords | UndoFlag_AstrometricSolution)
            this.metadata.SaveKeywords(targetWindow, false/*beginProcess*/)
            this.metadata.SaveProperties(targetWindow, "ImageSolver " + SOLVERVERSION, this.catalog.name)
            targetWindow.regenerateAstrometricSolution()
            targetWindow.mainView.endProcess()

            return true
        }
        catch (ex) {
            this.error = true
            throw ex
        }
        finally {
            jsAbortable = abortableBackup

            if (auxWindow)
                auxWindow.forceClose()
        }
    }
}

function decodeParams(hex) {
    const buffer = new Uint8Array(hex.length / 4)

    for (let i = 0; i < hex.length; i += 4) {
        buffer[i / 4] = parseInt(hex.substr(i, 4), 16)
    }

    return JSON.parse(String.fromCharCode.apply(null, buffer))
}

function imageSolver() {
    const data = {
        success: true,
        errorMessage: null,
        outputImage: null,
    }

    try {
        const input = decodeParams(jsArguments[0])

        const targetPath = input.targetPath
        const statusPath = input.statusPath
        const centerRA = input.centerRA
        const centerDEC = input.centerDEC
        const pixelSize = input.pixelSize
        const resolution = input.resolution // arcsec/px
        const focalDistance = input.focalDistance

        console.writeln("image solver started")
        console.writeln("targetPath=" + targetPath)
        console.writeln("statusPath=" + statusPath)
        console.writeln("centerRA=" + centerRA)
        console.writeln("centerDEC=" + centerDEC)
        console.writeln("pixelSize=" + pixelSize)
        console.writeln("resolution=" + resolution)
        console.writeln("focalDistance=" + focalDistance)

        const P = new ImageSolver

        const targetWindow = ImageWindow.open(targetPath)[0]

        P.Init(targetWindow)

        P.metadata.topocentric = false
        P.metadata.referenceSystem = "ICRS"
        P.metadata.ra = centerRA
        P.metadata.dec = centerDEC

        if (focalDistance > 0) {
            P.metadata.useFocal = false
            P.metadata.focal = focalDistance
            P.metadata.resolution = pixelSize / focalDistance * 0.18 / Math.PI
        } else {
            P.metadata.useFocal = false
            P.metadata.resolution = resolution / 3600 // deg?
        }

        P.metadata.xpixsz = pixelSize

        P.metadata.width = targetWindow.mainView.image.width
        P.metadata.height = targetWindow.mainView.image.height

        if (P.SolveImage(targetWindow, false)) {
            console.writeln(targetWindow.astrometricSolutionSummary())
            data.resolution = P.metadata.resolution * Math.PI / 180.0
            data.pixelSize = P.metadata.xpixsz
            data.focalDistance = P.metadata.focal
            data.rightAscension = P.metadata.ra * Math.PI / 180.0
            data.declination = P.metadata.dec * Math.PI / 180.0
            data.imageWidth = P.metadata.width
            data.imageHeight = P.metadata.height
            data.width = data.resolution * data.imageWidth
            data.height = data.resolution * data.imageHeight
            data.rotation = 0
            data.astrometricSolutionSummary = targetWindow.astrometricSolutionSummary()
        } else {
            data.success = false
            data.errorMessage = "the image could not be plate solved"
            console.criticalln(data.errorMessage)
        }

        console.writeln("image solver finished")
    } catch (e) {
        data.success = false
        data.errorMessage = e.message
        console.criticalln(data.errorMessage)
    } finally {
        if (targetWindow)
            targetWindow.forceClose()

        File.writeTextFile(statusPath, "@" + JSON.stringify(data) + "#")
        gc(true)
    }
}

imageSolver()
