package nebulosa.guiding.local

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.star.hfd.FindMode
import java.util.*
import kotlin.math.abs
import kotlin.math.hypot

class MultiStarGuider : Guider() {

    private var primaryStar = GuideStar(0.0, 0.0)
    private val guideStars = LinkedList<GuideStar>()
    private var starsUsed = 0
    private var lastStarsUsed = 0

    private val massChecker = MassChecker()
    private val primaryDistStats = DescriptiveStats()
    private val distanceChecker = DistanceChecker(this)

    var lastPrimaryDistance = 0
    var stabilizing = false
    var lockPositionMoved = false

    var tolerateJumpsEnabled = false
    var tolerateJumpsThreshold = 0.0
    var maxStars = DEFAULT_MAX_STAR_COUNT
    var stabilitySigmaX = DEFAULT_STABILITY_SIGMAX

    var minStarHFD = 1.5

    var calibration = Calibration.EMPTY
    var yAngleError = 0.0

    override val currentPosition
        get() = primaryStar

    var massChangeThresholdEnabled = false

    var massChangeThreshold = 0.0
        set(value) {
            require(value >= 0.0) { "massChangeThreshold < 0" }
            field = value
        }

    var multiStar = false
        set(value) {
            val prevMultiStar = field
            var autoFindForced = false
            field = value

            if (value != prevMultiStar) {
                primaryDistStats.clear()

                if (state >= GuiderState.SELECTED) {
                    stopGuiding()
                    invalidateCurrentPosition(true)

                    if (!autoSelect()) {
                        startGuiding()
                        autoFindForced = true
                    }
                }
            }

            if (!value) stabilizing = false

            // TODO: NotifyGuidingParam(MultiStar, value)
        }

    var searchRegion = 15.0
        set(value) {
            require(searchRegion >= MIN_SEARCH_REGION) { "searchRegion < $MIN_SEARCH_REGION" }
            require(searchRegion <= MAX_SEARCH_REGION) { "searchRegion > $MAX_SEARCH_REGION" }
            field = value
        }

    fun registerListener(listener: GuiderListener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: GuiderListener) {
        listeners.remove(listener)
    }

    fun clearSecondaryStars() {
        while (guideStars.size > 1) {
            guideStars.removeAt(1)
        }
    }

    fun tolerateJumps(enable: Boolean, threshold: Double) {
        tolerateJumpsEnabled = enable
        tolerateJumpsThreshold = threshold
    }

    override fun currentPosition(image: Image, position: Point): Boolean {
        require(position.valid) { "position is invalid" }
        require(position.x > 0.0 && position.x < image.width) { "invalid x value" }
        require(position.y > 0.0 && position.y < image.height) { "invalid y value" }

        massChecker.reset()

        return primaryStar.find(image, searchRegion, position.x, position.y, FindMode.CENTROID, minStarHFD)
    }

    fun autoSelect(): Boolean {
        // TODO:
        return false
    }

    override fun invalidateCurrentPosition(fullReset: Boolean) {
        primaryStar.invalidate()

        if (fullReset) {
            primaryStar.x = 0.0
            primaryStar.y = 0.0
        }
    }

    val starCount
        get() = guideStars.size

    /**
     * Uses secondary stars to refine Offset value if appropriate.
     * Returns of true means offset has been adjusted.
     */
    fun refineOffset(image: Image, offset: GuiderOffset): Boolean {
        var primarySigma = 0.0
        var averaged = false
        var validStars = 0
        val origOffset = offset.copy()
        var refined = false

        starsUsed = 1

        // Primary star is in position 0 of the list.
        if (guiding && starCount > 1 && guidingEnabled && !settling) {
            var sumWeights = 1.0
            var sumX = origOffset.camera.x
            var sumY = origOffset.camera.y
            val primaryDistance = hypot(sumX, sumY)

            primaryDistStats.add(primaryDistance)

            if (primaryDistStats.count > 5) {
                primarySigma = primaryDistStats.sigma

                if (!stabilizing && primaryDistance > stabilitySigmaX * primarySigma) {
                    stabilizing = true
                } else if (stabilizing) {
                    if (primaryDistance <= 2 * primarySigma) {
                        stabilizing = false

                        if (lockPositionMoved) {
                            lockPositionMoved = false

                            val guideStarsIter = guideStars.listIterator(1)

                            while (guideStarsIter.hasNext()) {
                                val star = guideStarsIter.next()

                                if (star.find(image, searchRegion, star.x, star.y, FindMode.CENTROID, minStarHFD)) {
                                    star.referencePoint.set(star)
                                } else {
                                    guideStarsIter.remove()
                                }
                            }

                            return false
                        }
                    }
                }
            } else {
                stabilizing = true
            }

            if (!stabilizing && starCount > 1 && (sumX != 0.0 || sumY != 0.0)) {
                val guideStarsIter = guideStars.listIterator(1)

                while (guideStarsIter.hasNext()) {
                    if (starsUsed >= maxStars || starCount == 1) break

                    // "used" means "considered" for purposes of UI.
                    starsUsed++

                    val star = guideStarsIter.next()

                    if (star.find(image, searchRegion, star.x, star.y, FindMode.CENTROID, minStarHFD)) {
                        val dx = star.x - star.referencePoint.x
                        val dy = star.y - star.referencePoint.y

                        if (star.lostCount > 0) star.lostCount--

                        if (dx != 0.0 || dy != 0.0) {
                            // Handle zero-counting - suspect results of exactly zero movement
                            if (dx == 0.0 || dy == 0.0) star.zeroCount++
                            else if (star.zeroCount > 0) star.zeroCount--

                            if (star.zeroCount == 5) {
                                guideStarsIter.remove()
                                continue
                            }

                            // Handle suspicious excursions - counted as "misses".
                            val secondaryDistance = hypot(dx, dy)

                            if (secondaryDistance > 2.5 * primarySigma) {
                                if (++star.missCount > 10) {
                                    // Reset the reference point to wherever it is now.
                                    star.referencePoint.set(star)
                                    star.missCount = 0
                                }

                                continue
                            } else if (star.missCount > 0) {
                                star.missCount--
                            }

                            // At this point we have usable data from the secondary star
                            val wt = (star.snr / primaryStar.snr)
                            sumX += wt * dx
                            sumY += wt * dy
                            sumWeights += wt
                            averaged = true
                            validStars++
                        } else {
                            guideStarsIter.remove()
                        }
                    } else {
                        // Star not found in its search region.
                        if (++star.lostCount >= 3) {
                            guideStarsIter.remove()
                        }
                    }
                }

                if (averaged) {
                    sumX /= sumWeights
                    sumY /= sumWeights

                    // Apply average only if its smaller than single-star delta.
                    if (hypot(sumX, sumY) < primaryDistance) {
                        offset.camera.x = sumX
                        offset.camera.y = sumY
                        refined = true
                    }
                }
            }
        }

        return refined
    }

    override fun lockPosition(position: Point): Boolean {
        if (!super.lockPosition(position)) return false

        if (multiStar) {
            lockPositionMoved = true
            stabilizing = true
        }

        return true
    }

    // Shift + Left click.
    fun deselectGuideStar() {
        invalidateCurrentPosition(true)
    }

    fun selectGuideStar(
        x: Double, y: Double,
    ) {
        val image = currentImage ?: return

        require(state <= GuiderState.SELECTED) { "state > SELECTED" }
        require(x > searchRegion && x + searchRegion < image.width) { "outside of search region" }
        require(y > searchRegion && y + searchRegion < image.height) { "outside of search region" }

        if (currentPosition(image, Point(x, y))
            && primaryStar.valid
        ) {
            lockPosition(primaryStar)

            if (starCount > 1) {
                clearSecondaryStars()
            }

            if (starCount == 0) {
                guideStars.add(primaryStar)
            }

            listeners.forEach { it.onStarSelected(this, primaryStar) }

            state = GuiderState.SELECTED
        } else {
            throw IllegalArgumentException("no star selected at position: $x, $y")
        }
    }

    override fun updateCurrentPosition(
        image: Image,
        offset: GuiderOffset,
    ): Boolean {
        if (!primaryStar.valid && primaryStar.x == 0.0 && primaryStar.y == 0.0) {
            return false
        }

        try {
            val newStar = GuideStar(primaryStar)

            if (!newStar.find(image, searchRegion, minHFD = minStarHFD)) {
                distanceChecker.activate()
                throw IllegalArgumentException("new star not found")
            }

            // check to see if it seems like the star we just found was the
            // same as the original star by comparing the mass.
            if (massChangeThresholdEnabled) {
                massChecker.exposure(frame.exposure, frame.autoExposure)

                val checkedMass = massChecker.checkMass(newStar.mass, massChangeThreshold)

                if (checkedMass.reject) {
                    // Mass changed!
                    massChecker.add(newStar.mass)
                    distanceChecker.activate()

                    throw IllegalStateException("mass changed")
                }
            }

            var distance = if (lockPosition.valid) {
                if (frame.guidingRAOnly) {
                    abs(newStar.x - lockPosition.x)
                } else {
                    newStar.distance(lockPosition)
                }
            } else {
                0.0
            }

            val tolerance = if (tolerateJumpsEnabled) tolerateJumpsThreshold else Double.MAX_VALUE

            if (!distanceChecker.checkDistance(distance, frame.guidingRAOnly, tolerance)) {
                throw IllegalStateException("distance changed")
            }

            primaryStar = newStar
            massChecker.add(newStar.mass)

            if (lockPosition.valid) {
                offset.camera = primaryStar - lockPosition

                if (multiStar && starCount > 1) {
                    if (refineOffset(image, offset)) {
                        distance = hypot(offset.camera.x, offset.camera.y)
                    }
                } else {
                    starsUsed = 1
                }

                if (mount.calibrated) {
                    mount.transformCameraCoordinatesToMountCoordinates(offset.camera, offset.mount)
                }

                val distanceRA = if (offset.mount.valid) abs(offset.mount.x) else 0.0
                updateCurrentDistance(distance, distanceRA)
            }

            // pFrame->pProfile->UpdateData(pImage, m_primaryStar.X, m_primaryStar.Y)

            frame.adjustAutoExposure(newStar.snr)
        } catch (e: Throwable) {
            frame.resetAutoExposure()
            println(e.message)
            return false
        }

        return true
    }

    override fun isValidLockPosition(point: Point): Boolean {
        val image = currentImage ?: return false

        val region = 1.0 + searchRegion
        return point.x >= region &&
                point.x + region < image.width &&
                point.y >= region &&
                point.y + region < image.height
    }

    companion object {

        const val MIN_SEARCH_REGION = 7f
        const val DEFAULT_SEARCH_REGION = 15f
        const val MAX_SEARCH_REGION = 50f
        const val DEFAULT_MAX_STAR_COUNT = 9
        const val DEFAULT_STABILITY_SIGMAX = 5f

        @JvmStatic
        private fun transformCameraCoordinatesToMountCoordinates(
            camera: Point, mount: Point,
            calibration: Calibration,
            yAngleError: Double,
        ) {
            val hyp = camera.distance
            val cameraTheta = camera.angle
            val xAngle = cameraTheta - calibration.xAngle
            val yAngle = cameraTheta - (calibration.xAngle + yAngleError)

            mount.set(xAngle.cos * hyp, yAngle.sin * hyp)
        }
    }
}
