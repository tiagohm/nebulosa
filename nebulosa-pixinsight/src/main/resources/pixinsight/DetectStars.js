//     ____       __ _____  ____
//    / __ \     / // ___/ / __ \
//   / /_/ /__  / / \__ \ / /_/ /
//  / ____// /_/ / ___/ // _, _/   PixInsight JavaScript Runtime
// /_/     \____/ /____//_/ |_|    PJSR Version 1.0
// ----------------------------------------------------------------------------
// pjsr/StarDetector.jsh - Released 2024-02-28T16:25:35Z
// ----------------------------------------------------------------------------
// This file is part of the PixInsight JavaScript Runtime (PJSR).
// PJSR is an ECMA-262-5 compliant framework for development of scripts on the
// PixInsight platform.
//
// Copyright (c) 2003-2024 Pleiades Astrophoto S.L. All Rights Reserved.
//
// Redistribution and use in both source and binary forms, with or without
// modification, is permitted provided that the following conditions are met:
//
// 1. All redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//
// 2. All redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// 3. Neither the names "PixInsight" and "Pleiades Astrophoto", nor the names
//    of their contributors, may be used to endorse or promote products derived
//    from this software without specific prior written permission. For written
//    permission, please contact info@pixinsight.com.
//
// 4. All products derived from this software, in any form whatsoever, must
//    reproduce the following acknowledgment in the end-user documentation
//    and/or other materials provided with the product:
//
//    "This product is based on software from the PixInsight project, developed
//    by Pleiades Astrophoto and its contributors (https://pixinsight.com/)."
//
//    Alternatively, if that is where third-party acknowledgments normally
//    appear, this acknowledgment must be reproduced in the product itself.
//
// THIS SOFTWARE IS PROVIDED BY PLEIADES ASTROPHOTO AND ITS CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL PLEIADES ASTROPHOTO OR ITS
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, BUSINESS
// INTERRUPTION; PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; AND LOSS OF USE,
// DATA OR PROFITS) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
// ----------------------------------------------------------------------------

#include <pjsr/ImageOp.jsh>
#include <pjsr/MorphOp.jsh>

function Star(pos, flux, bkg, rect, size, nmax, snr, peak) {
    // Centroid position in pixels, image coordinates. This property is an
    // object with x and y Number properties.
    this.pos = pos
    // Total flux, normalized intensity units.
    this.flux = flux
    // Mean local background, normalized intensity units.
    this.bkg = bkg
    // Detection region, image coordinates.
    this.rect = rect
    // Area of detected star structure in square pixels.
    this.size = size
    // Number of local maxima in the detection structure. A value greater than
    // one denotes a double/multiple star or a crowded source. A value of zero
    // signals that detection of local maxima has been disabled, either globally
    // or for this particular structure.
    this.nmax = nmax

    this.snr = snr
    this.peak = peak
}

function StarDetector() {
    this.__base__ = Object
    this.__base__()

    /*
     * Number of wavelet layers for structure detection. (default=5)
     */
    this.structureLayers = 5

    /*
     * Half size in pixels of a morphological median filter, for hot pixel
     * removal. (default=1)
     */
    this.hotPixelFilterRadius = 1

    /*
     * Whether the hot pixel filter removal should be applied to the image used
     * for star detection, or only to the working image used to build the
     * structure map. (default=false)
     *
     * By setting this parameter to true, the detection algorithm is completely
     * robust to hot pixels (of sizes not larger than hotPixelFilterRadius), but
     * it is also less sensitive, so less stars will in general be detected.
     * With the default value of false, some hot pixels may be wrongly detected
     * as stars but the number of true stars detected will generally be larger.
     */
    this.applyHotPixelFilterToDetectionImage = false

    /*
     * Half size in pixels of a Gaussian convolution filter applied for noise
     * reduction. Useful for star detection in low-SNR images. (default=0)
     *
     * N.B. Setting the value of this parameter > 0 implies
     * applyHotPixelFilterToDetectionImage=true.
     */
    this.noiseReductionFilterRadius = 0

    /*
     * Sensitivity of the star detection device.
     *
     * Internally, the sensitivity of the star detection algorithm is expressed
     * in signal-to-noise ratio units with respect to the evaluated dispersion
     * of local background pixels for each detected structure. Given a source
     * with estimated brightness s, local background b and local background
     * dispersion n, sensitivity is the minimum value of (s - b)/n necessary to
     * trigger star detection.
     *
     * To isolate the public interface of this class from its internal
     * implementation, this parameter is normalized to the [0,1] range, where 0
     * and 1 represent minimum and maximum sensitivity, respectively. This
     * abstraction allows us to change the star detection engine without
     * breaking dependent tools and processes.
     *
     * Increase this value to favor detection of fainter stars. Decrease it to
     * restrict detection to brighter stars. (default=0.5).
     */
    this.sensitivity = 0.5

    /*!
     * Peak sensitivity of the star detection device.
     *
     * Internally, the peak response property of the star detection algorithm is
     * expressed in kurtosis units. For each detected structure, kurtosis is
     * evaluated from all significant pixels with values greater than the
     * estimated mean local background. Peak response is the minimum value of
     * kurtosis necessary to trigger star detection.
     *
     * To isolate the public interface of this class from its internal
     * implementation, this parameter is normalized to the [0,1] range, where 0
     * and 1 represent minimum and maximum peak response, respectively. This
     * abstraction allows us to change the star detection engine without
     * breaking dependent tools and processes.
     *
     * If you decrease this parameter, stars will need to have stronger (or more
     * prominent) peaks to be detected. This is useful to prevent detection of
     * saturated stars, as well as small nonstellar features. By increasing this
     * parameter, the star detection algorithm will be more sensitive to
     * 'peakedness', and hence more tolerant with relatively flat image
     * features. (default=0.5).
     */
    this.peakResponse = 0.5

    /*!
     * If this parameter is false, a local maxima map will be generated to
     * identify and prevent detection of multiple sources that are too close to
     * be separated as individual structures, such as double and multiple stars.
     * In general, barycenter positions cannot be accurately determined for
     * sources with several local maxima. If this parameter is true,
     * non-separable multiple sources will be detectable as single objects.
     * (default=false)
     */
    this.allowClusteredSources = false

    /*
     * Half size in pixels of the local maxima detection filter. (default=2)
     */
    this.localDetectionFilterRadius = 2

    /*!
     * This parameter is a normalized pixel value in the [0,1] range. Structures
     * with pixels above this value will be excluded for local maxima detection.
     * (default=0.75)
     */
    this.localMaximaDetectionLimit = 0.75

    /*
     * Set this flag true to avoid detection of local maxima. (default=false)
     * Setting this parameter to true implies allowClusteredSources = true.
     */
    this.noLocalMaximaDetection = false

    /*!
     * Maximum star distortion.
     *
     * Internally, star distortion is evaluated in units of coverage of a square
     * region circumscribed to each detected structure. The coverage of a
     * perfectly circular star is pi/4 (about 0.8). Lower values denote
     * elongated or irregular sources.
     *
     * To isolate the public interface of this class from its internal
     * implementation, this parameter is normalized to the [0,1] range, where 0
     * and 1 represent minimum and maximum distortion, respectively. This
     * abstraction allows us to change the star detection engine without
     * breaking dependent tools and processes.
     *
     * Use this parameter, if necessary, to control inclusion of elongated
     * stars, complex clusters of stars, and nonstellar image features.
     * (default=0.6)
     */
    this.maxDistortion = 0.6

    /*!
     * Stars with measured SNR above this parameter in units of the minimum
     * detection level (as defined by the sensitivity parameter) will always be
     * detected, even if their profiles are too flat for the current peak
     * response. This allows us to force inclusion of bright stars. (default=3)
     */
    this.brightThreshold = 3

    /*
     * Minimum signal-to-noise ratio of a detectable star.
     *
     * Given a source with estimated brightness s, local background b and local
     * background dispersion n, SNR is evaluated as (s - b)/n. Stars with
     * measured SNR below this parameter won't be detected. (default=0)
     *
     * The value of this parameter can be increased to limit star detection to a
     * subset of the brightest sources in the image adaptively, instead of
     * imposing an arbitrary limit on the number of detected stars.
     */
    this.minSNR = 0

    /*!
     * Minimum size of a detectable star structure in square pixels.
     *
     * This parameter can be used to prevent detection of small and bright image
     * artifacts as stars. This can be useful to work with uncalibrated or
     * wrongly calibrated data, especially demosaiced CFA frames where hot
     * pixels have generated large bright artifacts that cannot be removed with
     * a median filter, poorly focused images, and images with poor tracking.
     * (default=1)
     */
    this.minStructureSize = 1

    /*
     * Stars with peak values greater than this value won't be detected.
     * (default=1)
     */
    this.upperLimit = 1.0

    /*
     * Detect dark structures over a bright background, instead of bright
     * structures over a dark background. (default=false)
     */
    this.invert = false

    /*
     * Optional callback progress function with the following signature:
     *
     * Boolean progressCallback( int count, int total )
     *
     * If defined, this function will be called by the stars() method for each
     * row of its target image. The count argument is the current number of
     * processed pixel rows, and total is the height of the target image. If the
     * function returns false, the star detection task will be aborted. If the
     * function returns true, the task will continue. (default=undefined)
     */
    this.progressCallback = undefined

    /*
     * Optional mask image. If defined, star detection will be restricted to
     * nonzero mask pixels. (default=undefined)
     */
    this.mask = undefined

    /*
     * Stretch factor for the barycenter search algorithm, in sigma units.
     * Increase it to make the algorithm more robust to nearby structures, such
     * as multiple/crowded stars and small nebular features. However, too large
     * of a stretch factor will make the algorithm less accurate. (default=1.5)
     */
    this.xyStretch = 1.5

    /*
     * Square structuring element
     */
    function BoxStructure(size) {
        let B = new Array(size * size)
        for (let i = 0; i < B.length; ++i)
            B[i] = 1
        let S = new Array
        S.push(B)
        return S
    }

    /*
     * Circular structuring element
     */
    function CircularStructure(size) {
        size |= 1
        let C = new Array(size * size)
        let s2 = size >> 1
        let n2 = size / 2
        let n22 = n2 * n2
        for (let i = 0; i < s2; ++i) {
            let di = i + 0.5 - n2
            let di2 = di * di
            let i2 = i * size
            let i1 = i2 + size - 1
            let i3 = (size - i - 1) * size
            let i4 = i3 + size - 1
            for (let j = 0; j < s2; ++j) {
                let dj = j + 0.5 - n2
                C[i1 - j] = C[i2 + j] = C[i3 + j] = C[i4 - j] = (di2 + dj * dj <= n22) ? 1 : 0
            }
        }
        for (let i = 0; i < size; ++i)
            C[i * size + s2] = C[s2 * size + i] = 1
        let S = new Array
        S.push(C)
        return S
    }

    /*
     * Hot pixel removal with a median filter
     */
    this.hotPixelFilter = function (image) {
        if (this.hotPixelFilterRadius > 0)
            if (this.hotPixelFilterRadius > 1)
                image.morphologicalTransformation(MorphOp_Median, CircularStructure(2 * this.hotPixelFilterRadius + 1))
            else
                image.morphologicalTransformation(MorphOp_Median, BoxStructure(3))
    }

    /*
     * Isolate star detection structures in an image. Replaces the specified map
     * image with its binary star detection map.
     */
    this.getStructureMap = function (map) {
        // Flatten the image with a high-pass filter
        let s = new Image(map)
        let G = Matrix.gaussianFilterBySize(1 + (1 << this.structureLayers))
        s.convolveSeparable(G.rowVector(G.rows >> 1), G.rowVector(G.rows >> 1))
        map.apply(s, ImageOp_Sub)
        s.free()
        map.truncate()
        map.rescale()

        // Strength the smallest structures with a dilation filter
        map.morphologicalTransformation(MorphOp_Dilation, BoxStructure(3))

        // Adaptive binarization
        let m = map.median()
        if (1 + m == 1) {
            // Black background - probably a noiseless synthetic star field
            let wasRangeClippingEnabled = map.rangeClippingEnabled
            let wasRangeClipLow = map.rangeClipLow
            let wasRangeClipHigh = map.rangeClipHigh
            map.rangeClippingEnabled = true
            map.rangeClipLow = 0
            map.rangeClipHigh = 1
            if (!wasRangeClippingEnabled || wasRangeClipLow != 0 || wasRangeClipHigh != 1)
                m = map.median()
            map.binarize(m + map.MAD(m))
            map.rangeClippingEnabled = wasRangeClippingEnabled
            map.rangeClipLow = wasRangeClipLow
            map.rangeClipHigh = wasRangeClipHigh
        }
        else {
            // A "natural" image - binarize at 3*noise_stdDev
            let n = map.noiseKSigma(1)[0]
            map.binarize(m + 3 * n)
        }

        // Remove noise residuals with an erosion filter
        map.morphologicalTransformation(MorphOp_Erosion, BoxStructure(3))

        // Optional star detection mask
        if (this.mask != undefined)
            map.apply(this.mask, ImageOp_Mul)
    }

    /*
     * Local maxima detection.
     */
    this.getLocalMaximaMap = function (map) {
        // We apply a dilation filter with a flat structuring element without its
        // central element. Local maxima are those pixels in the input image with
        // values greater than the dilated image.
        // The localMaximaDetectionLimit parameter allows us to prevent detection of
        // false multiple maxima on saturated or close to saturated structures.
        let Bh = BoxStructure((this.localDetectionFilterRadius << 1) | 1)
        Bh[0][Bh[0].length >> 1] = 0
        let l = new Image(map)
        l.binarize(this.localMaximaDetectionLimit)
        l.invert()
        let t = new Image(map)
        t.morphologicalTransformation(MorphOp_Dilation, Bh)
        map.apply(t, ImageOp_Sub)
        t.free()
        map.binarize(0)
        map.apply(l, ImageOp_Mul)
        l.free()
    }

    /*
     * Compute star parameters
     */
    this.starParameters = function (image, rect, starPoints, lmMap) {
        let params = {
            pos: { x: 0, y: 0 }, // barycenter image coordinates
            rect: { x0: 0, y0: 0, x1: 0, y1: 0 }, // detection rectangle
            bkg: 0,            // local background
            sigma: 0,            // local background dispersion
            flux: 0,            // total flux
            max: 0,            // maximum pixel value
            nmax: 0,            // number of local maxima in structure
            peak: 0,            // robust peak value
            kurt: 0,            // kurtosis
            count: 0,            // sample length
            size: 0
        }           // structure size in square pixels

        // Mean local background and local background dispersion
        for (let delta = 4, it = 0, m0 = 1; ; ++delta, ++it) {
            let r = rect.inflatedBy(delta)
            let a = [], b = [], c = [], d = []
            image.getSamples(a, new Rect(r.x0, r.y0, r.x1, rect.y0))
            image.getSamples(b, new Rect(r.x0, rect.y0, rect.x0, rect.y1))
            image.getSamples(c, new Rect(r.x0, rect.y1, r.x1, r.y1))
            image.getSamples(d, new Rect(rect.x1, rect.y0, r.x1, rect.y1))
            let B = a.concat(b, c, d)
            let m = Math.median(B)
            if (m > m0 || (m0 - m) / m0 < 0.01) {
                params.bkg = m
                params.sigma = Math.max(1.4826 * Math.MAD(B), Math.EPSILON32)
                break
            }
            // Guard us against rare ill-posed conditions
            if (it == 200)
                return null
            m0 = m
        }

        // Detection region
        params.rect = rect.inflatedBy(2).intersection(image.bounds)

        // Structure size
        params.size = starPoints.length

        // Significant subset
        let v = []
        for (let i = 0; i < starPoints.length; ++i) {
            let p = starPoints[i]
            let f = image.sample(p.x, p.y)
            if (f > params.bkg) {
                // Local maxima
                if (!this.noLocalMaximaDetection)
                    if (lmMap.sample(p.x, p.y) != 0)
                        ++params.nmax
                v.push(f)
                // Total flux above local background
                params.flux += f
            }
        }

        // Fail if no significant data
        if (v.length == 0)
            return null

        // Fail if we have multiple maxima and those are not allowed
        if (params.nmax > 1)
            if (!this.allowClusteredSources)
                return null

        // Barycenter coordinates
        let M = Matrix.fromImage(image, rect)
        M.truncate(Math.range(M.median() + this.xyStretch * M.stdDev(), 0.0, 1.0), 1.0)
        M.rescale()
        let sx = 0, sy = 0, sz = 0
        for (let y = rect.y0, i = 0; i < M.rows; ++y, ++i)
            for (let x = rect.x0, j = 0; j < M.cols; ++x, ++j) {
                let z = M.at(i, j)
                if (z > 0) {
                    sx += z * x
                    sy += z * y
                    sz += z
                }
            }
        params.pos.x = sx / sz + 0.5
        params.pos.y = sy / sz + 0.5

        // Sort significant pixels in decreasing flux order
        v.sort((a, b) => (a < b) ? +1 : ((b < a) ? -1 : 0))
        // Maximum pixel value
        params.max = v[0]

        // Find subset of significant high pixel values
        let mn = 0
        for (let i = 0; i < v.length && (mn < 5 || v[i] == v[i - 1]); ++i, ++mn) { }
        for (let i = 0; i < mn; ++i)
            params.peak += v[i]
        // Significant peak value
        params.peak /= mn
        // Significant sample length
        params.count = v.length

        // Kurtosis
        let s = Math.stdDev(v)
        if (1 + s != 1) {
            let m = params.flux / v.length
            let k = 0
            for (let i = 0; i < v.length; ++i) {
                let d = (v[i] - m) / s
                d *= d
                k += d * d
            }
            params.kurt = k / params.count
        }

        return params
    }

    /*
     * Finds all the stars in an image. Returns an array of Star objects.
     */
    this.stars = function (image) {
        // We work on a duplicate of the source grayscale image, or on its HSI
        // intensity component if it is a color image.
        let wrk = Image.newFloatImage()
        image.getIntensity(wrk)

        // Hot pixel removal, if applied to the image where we are going to find
        // stars, not just to the image used to build the structure map.
        // When noise reduction is enabled, always remove hot pixels first, or
        // hot pixels would be promoted to "stars".
        let alreadyFixedHotPixels = false
        if (this.applyHotPixelFilterToDetectionImage || this.noiseReductionFilterRadius > 0) {
            this.hotPixelFilter(wrk)
            alreadyFixedHotPixels = true
        }

        // If the invert flag is set, then we are looking for dark structures on
        // a bright background.
        if (this.invert)
            wrk.invert()

        // Optional noise reduction
        if (this.noiseReductionFilterRadius > 0) {
            let G = Matrix.gaussianFilterBySize((this.noiseReductionFilterRadius << 1) | 1)
            wrk.convolveSeparable(G.rowVector(G.rows >> 1), G.rowVector(G.rows >> 1))
        }

        // Structure map
        let map = Image.newFloatImage()
        map.assign(wrk)
        // Hot pixel removal, if applied just to the image used to build the
        // structure map.
        if (!alreadyFixedHotPixels)
            this.hotPixelFilter(map)
        this.getStructureMap(map)

        // Use matrices instead of images for faster access
        let M = map.toMatrix()
        map.free()

        // Local maxima map
        let lmMap
        if (!this.noLocalMaximaDetection) {
            lmMap = Image.newFloatImage()
            lmMap.assign(wrk)
            this.getLocalMaximaMap(lmMap)
        }

        /*
         * Internal detection parameters
         */
        // Signal detection threshold in local sigma units.
        let snrThreshold = 0.1 + 4.8 * (1 - Math.range(this.sensitivity, 0, 1))
        // Peak detection threshold in kurtosis units.
        let peakThreshold = 0.1 + 9.8 * (1 - Math.range(this.peakResponse, 0, 1))
        // Maximum distortion in coverage units.
        let minCoverage = Math.PI4 * (1 - Math.range(this.maxDistortion, 0, 1))

        // The detected stars
        let S = new Array

        // Structure scanner
        for (let y0 = 0, x1 = M.cols - 1, y1 = M.rows - 1; y0 < y1; ++y0) {
            if (this.progressCallback != undefined)
                if (!this.progressCallback(y0, M.rows))
                    return null

            for (let x0 = 0; x0 < x1; ++x0) {
                // Exclude background pixels and already visited pixels
                if (M.at(y0, x0) == 0)
                    continue

                // Star pixel coordinates
                let starPoints = new Array

                // Star bounding rectangle
                let r = new Rect(x0, y0, x0 + 1, y0 + 1)

                // Grow star region downward
                for (let y = y0, x = x0, xa, xb; ;) {
                    // Add this pixel to the current star
                    starPoints.push({ x: x, y: y })

                    // Explore the left segment of this row
                    for (xa = x; xa > 0;) {
                        if (M.at(y, xa - 1) == 0)
                            break
                        --xa
                        starPoints.push({ x: xa, y: y })
                    }

                    // Explore the right segment of this row
                    for (xb = x; xb < x1;) {
                        if (M.at(y, xb + 1) == 0)
                            break
                        ++xb
                        starPoints.push({ x: xb, y: y })
                    }

                    // xa and xb are now the left and right boundary limits,
                    // respectively, of this row in the current star.

                    if (xa < r.x0)  // update left boundary
                        r.x0 = xa

                    if (xb >= r.x1) // update right boundary
                        r.x1 = xb + 1  // bottom-right corner excluded (PCL-specific)

                    // Prepare for next row
                    ++y

                    // Decide whether we are done with this star now, or if
                    // there is at least one more row that must be explored.

                    let nextRow = false

                    // Explore the next row from left to right. We'll continue
                    // gathering pixels if we find at least one nonzero map pixel.
                    for (x = xa; x <= xb; ++x)
                        if (M.at(y, x) != 0) {
                            nextRow = true
                            break
                        }

                    if (!nextRow)
                        break

                    // Update bottom boundary
                    r.y1 = y + 1  // Rect *excludes* the bottom-right corner

                    // Terminate if we reach the last row of the image
                    if (y == y1)
                        break
                }

                /*
                 * If this is a reliable star, compute its barycenter coordinates
                 * and add it to the star list.
                 *
                 * Rejection criteria:
                 *
                 * * Stars whose peak values are greater than the upperLimit
                 *   parameter are rejected.
                 *
                 * * If this structure is touching a border of the image, reject
                 *   it. We cannot compute an accurate position for a clipped star.
                 *
                 * * Too small structures are rejected. This mainly prevents
                 *   inclusion of hot (or cold) pixels. This condition is enforced
                 *   by the hot pixel removal and noise reduction steps performed
                 *   during the structure detection phase, and optionally by
                 *   increasing the minStructureSize parameter.
                 *
                 * * Too large structures are rejected. This prevents inclusion of
                 *   extended nonstellar objects and saturated bright stars. This
                 *   is also part of the structure detection algorithm.
                 *
                 * * Too elongated stars are rejected. The minCoverage parameter
                 *   determines the maximum distortion allowed. A perfect square
                 *   has coverage = 1. The coverage of a perfect circle is pi/4.
                 *
                 * * Too sparse sources are rejected. This prevents detection of
                 *   multiple stars where centroids cannot be well determined.
                 *
                 * * Too dim structures are rejected. The sensitivity parameter
                 *   defines the sensitivity of the star detection algorithm in
                 *   local sigma units. The minSNR parameter can be used to limit
                 *   star detection to a subset of the brightest stars adaptively.
                 *
                 * * Too flat structures are rejected. The peakThreshold parameter
                 *   defines the peak sensitivity of the star detection algorithm
                 *   in kurtosis units.
                 */
                if (r.width > 1 && r.height > 1)
                    if (r.y0 > 0 && r.y1 <= y1 && r.x0 > 0 && r.x1 <= x1)
                        if (starPoints.length >= this.minStructureSize) {
                            let p = this.starParameters(wrk, r, starPoints, lmMap)
                            if (p != null)
                                if (p.max <= this.upperLimit) {
                                    let d = Math.max(r.width, r.height)
                                    if (p.count / d / d >= minCoverage) {
                                        let ix = Math.trunc(p.pos.x) | 0
                                        let iy = Math.trunc(p.pos.y) | 0
                                        if (this.mask == undefined || this.mask.sample(ix, iy) != 0) {
                                            let snr = (p.peak - p.bkg) / p.sigma
                                            if (snr >= this.minSNR) {
                                                let s1 = snr / snrThreshold
                                                if (s1 >= 1)
                                                    if (s1 >= this.brightThreshold || p.kurt == 0 || p.kurt / peakThreshold >= 1)
                                                        S.push(new Star(p.pos, p.flux, p.bkg, p.rect, p.size, p.nmax, snr, p.peak))
                                            }
                                        }
                                    }
                                }
                        }

                // Erase this structure.
                for (let i = 0; i < starPoints.length; ++i) {
                    let p = starPoints[i]
                    M.at(p.y, p.x, 0)
                }
            }
        }

        // Sort the list of detected sources in descending brightness order.
        S.sort((a, b) => (a.flux < b.flux) ? +1 : ((b.flux < a.flux) ? -1 : 0))

        // Perform a soft garbage collection. This eases integration with very
        // long batch tasks and has no measurable performance penalty.
        gc(false/*hard*/)

        if (this.progressCallback != undefined)
            if (!this.progressCallback(M.rows, M.rows))
                return null

        return S
    }
}

StarDetector.prototype = new Object

function computeHfr(image, s) {
    let a = 0
    let b = 0

    const r = Math.min(s.rect.y1 - s.rect.y0, s.rect.x1 - s.rect.x0) / 2

    for (let y = s.rect.y0; y <= s.rect.y1; y++) {
        for (let x = s.rect.x0; x <= s.rect.x1; x++) {
            if (x >= 0 && x < image.width && y >= 0 && y < image.height) {
                const d = Math.sqrt((x - s.pos.x) * (x - s.pos.x) + (y - s.pos.y) * (y - s.pos.y))

                if (d <= r) {
                    const p = image.sample(x, y)
                    const v = p - s.bkg
                    a += v * d
                    b += v
                }
            }
        }
    }

    s.hfr = b > 0.0 ? a / b : 0.0
}

function decodeParams(hex) {
    const buffer = new Uint8Array(hex.length / 4)

    for (let i = 0; i < hex.length; i += 4) {
        buffer[i / 4] = parseInt(hex.substr(i, 4), 16)
    }

    return JSON.parse(String.fromCharCode.apply(null, buffer))
}

function detectStars() {
    const data = {
        success: true,
        errorMessage: null,
        stars: [],
    }

    try {
        const input = decodeParams(jsArguments[0])

        const targetPath = input.targetPath
        const statusPath = input.statusPath
        const minSNR = input.minSNR
        const invert = input.invert

        console.writeln("targetPath=" + targetPath)
        console.writeln("statusPath=" + statusPath)
        console.writeln("minSNR=" + minSNR)
        console.writeln("invert=" + invert)

        const P = new StarDetector
        P.structureLayers = 5
        P.hotPixelFilterRadius = 1
        P.applyHotPixelFilterToDetectionImage = false
        P.noiseReductionFilterRadius = 0
        P.sensitivity = 0.5
        P.peakResponse = 0.5
        P.allowClusteredSources = false
        P.localDetectionFilterRadius = 2
        P.localMaximaDetectionLimit = 0.75
        P.noLocalMaximaDetection = false
        P.maxDistortion = 0.6
        P.brightThreshold = 3
        P.minSNR = minSNR
        P.minStructureSize = 1
        P.upperLimit = 1.0
        P.invert = invert
        P.xyStretch = 1.5

        const window = ImageWindow.open(targetPath)
        const image = window[0].mainView.image

        const sl = P.stars(image)

        for (let i = 0; i < sl.length; i++) {
            const s = sl[i]
            computeHfr(image, s)
            data.stars.push({ x: s.pos.x, y: s.pos.y, flux: s.flux * 65536, size: s.size, nmax: s.nmax, bkg: s.bkg, x0: s.rect.x0, y0: s.rect.y0, x1: s.rect.x1, y1: s.rect.y1, snr: s.snr, peak: s.peak, hfd: 2 * s.hfr })
        }

        window[0].forceClose()

        console.writeln("star detection finished. stars=", sl.length)
    } catch (e) {
        data.success = false
        data.errorMessage = e.message
        console.writeln(data.errorMessage)
    } finally {
        File.writeTextFile(statusPath, "@" + JSON.stringify(data) + "#")
    }
}

detectStars()
