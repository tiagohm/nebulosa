package nebulosa.alpaca.indi.device.cameras

import nebulosa.alpaca.api.AlpacaCameraService
import nebulosa.alpaca.api.CameraState
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.api.PulseGuideDirection
import nebulosa.alpaca.api.SensorType
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.alpaca.indi.device.ASCOMDevice
import nebulosa.fits.Bitpix
import nebulosa.fits.Fits
import nebulosa.fits.FitsHeader
import nebulosa.fits.FitsKeyword
import nebulosa.image.algorithms.transformation.CfaPattern
import nebulosa.image.format.BasicImageHdu
import nebulosa.image.format.FloatImageData
import nebulosa.image.format.HeaderCard
import nebulosa.indi.device.Device
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraBinChanged
import nebulosa.indi.device.camera.CameraCanAbortChanged
import nebulosa.indi.device.camera.CameraCanSetTemperatureChanged
import nebulosa.indi.device.camera.CameraCfaChanged
import nebulosa.indi.device.camera.CameraCoolerChanged
import nebulosa.indi.device.camera.CameraCoolerControlChanged
import nebulosa.indi.device.camera.CameraCoolerPowerChanged
import nebulosa.indi.device.camera.CameraExposureAborted
import nebulosa.indi.device.camera.CameraExposureFailed
import nebulosa.indi.device.camera.CameraExposureFinished
import nebulosa.indi.device.camera.CameraExposureMinMaxChanged
import nebulosa.indi.device.camera.CameraExposureProgressChanged
import nebulosa.indi.device.camera.CameraExposureStateChanged
import nebulosa.indi.device.camera.CameraExposuringChanged
import nebulosa.indi.device.camera.CameraFrameCaptured
import nebulosa.indi.device.camera.CameraFrameChanged
import nebulosa.indi.device.camera.CameraFrameFormatsChanged
import nebulosa.indi.device.camera.CameraGainChanged
import nebulosa.indi.device.camera.CameraGainMinMaxChanged
import nebulosa.indi.device.camera.CameraHasCoolerChanged
import nebulosa.indi.device.camera.CameraOffsetChanged
import nebulosa.indi.device.camera.CameraOffsetMinMaxChanged
import nebulosa.indi.device.camera.CameraPixelSizeChanged
import nebulosa.indi.device.camera.CameraTemperatureChanged
import nebulosa.indi.device.camera.FrameType
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.guider.GuideOutputPulsingChanged
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.PropertyState
import nebulosa.log.d
import nebulosa.log.loggerFor
import nebulosa.math.AngleFormatter
import nebulosa.math.format
import nebulosa.math.normalized
import nebulosa.math.toDegrees
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.time.CurrentTime
import nebulosa.util.concurrency.latch.CountUpDownLatch
import okio.buffer
import okio.source
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

@Suppress("RedundantModalityModifier")
data class ASCOMCamera(
    override val device: ConfiguredDevice,
    override val service: AlpacaCameraService,
    override val sender: AlpacaClient,
) : ASCOMDevice(), Camera {

    @Volatile final override var exposuring = false
    @Volatile final override var hasCoolerControl = false
    @Volatile final override var coolerPower = 0.0
    @Volatile final override var cooler = false
    @Volatile final override var hasDewHeater = false
    @Volatile final override var dewHeater = false
    @Volatile final override var frameFormats = emptyList<String>()
    @Volatile final override var canAbort = false
    @Volatile final override var cfaOffsetX = 0
    @Volatile final override var cfaOffsetY = 0
    @Volatile final override var cfaType = CfaPattern.RGGB
    @Volatile final override var exposureMin = 0L
    @Volatile final override var exposureMax = 0L
    @Volatile final override var exposureState = PropertyState.IDLE
    @Volatile final override var exposureTime = 0L
    @Volatile final override var hasCooler = false
    @Volatile final override var canSetTemperature = false
    @Volatile final override var canSubFrame = true
    @Volatile final override var x = 0
    @Volatile final override var minX = 0
    @Volatile final override var maxX = 0
    @Volatile final override var y = 0
    @Volatile final override var minY = 0
    @Volatile final override var maxY = 0
    @Volatile final override var width = 0
    @Volatile final override var minWidth = 0
    @Volatile final override var maxWidth = 0
    @Volatile final override var height = 0
    @Volatile final override var minHeight = 0
    @Volatile final override var maxHeight = 0
    @Volatile final override var canBin = true
    @Volatile final override var maxBinX = 1
    @Volatile final override var maxBinY = 1
    @Volatile final override var binX = 1
    @Volatile final override var binY = 1
    @Volatile final override var gain = 0
    @Volatile final override var gainMin = 0
    @Volatile final override var gainMax = 0
    @Volatile final override var offset = 0
    @Volatile final override var offsetMin = 0
    @Volatile final override var offsetMax = 0
    @Volatile final override var pixelSizeX = 0.0
    @Volatile final override var pixelSizeY = 0.0

    @Volatile final override var hasThermometer = false
    @Volatile final override var temperature = 0.0

    @Volatile final override var canPulseGuide = false
    @Volatile final override var pulseGuiding = false

    final override val guideHead = null // TODO: ASCOM has guide head?

    @Volatile private var cameraState = CameraState.IDLE
    @Volatile private var frameType = FrameType.LIGHT
    @Volatile private var fitsKeywords: Array<out HeaderCard> = emptyArray()
    @Volatile private var canDebayer = false

    private val imageReadyWaiter = ImageReadyWaiter()

    override val snoopedDevices = ArrayList<Device>(4)

    override fun initialize() {
        super.initialize()
        imageReadyWaiter.start()
    }

    override fun cooler(enabled: Boolean) {
        service.cooler(device.number, enabled).doRequest()
    }

    override fun dewHeater(enabled: Boolean) {
        // TODO
    }

    override fun temperature(value: Double) {
        service.setpointCCDTemperature(device.number, value).doRequest()
    }

    override fun frameFormat(format: String?) {
        val index = frameFormats.indexOf(format)

        if (index >= 0) {
            service.readoutMode(device.number, index).doRequest()
        }
    }

    override fun frameType(type: FrameType) {
        frameType = type
    }

    override fun frame(x: Int, y: Int, width: Int, height: Int) {
        service.startX(device.number, x).doRequest() ?: return
        service.startY(device.number, y).doRequest() ?: return
        service.numX(device.number, width).doRequest() ?: return
        service.numY(device.number, height).doRequest()
    }

    override fun bin(x: Int, y: Int) {
        service.binX(device.number, x).doRequest() ?: return
        service.binY(device.number, y).doRequest()
    }

    override fun gain(value: Int) {
        service.gain(device.number, value).doRequest()
    }

    override fun offset(value: Int) {
        service.offset(device.number, value).doRequest()
    }

    override fun startCapture(exposureTime: Long) {
        if (!exposuring) {
            this.exposureTime = exposureTime

            service.startExposure(device.number, exposureTime.toDouble() / MICROS_TO_SECONDS, frameType == FrameType.LIGHT).doRequest {
                imageReadyWaiter.captureStarted(exposureTime)
            }
        }
    }

    override fun abortCapture() {
        service.abortExposure(device.number).doRequest()
        imageReadyWaiter.captureAborted()
    }

    private fun pulseGuide(direction: PulseGuideDirection, duration: Duration) {
        if (canPulseGuide) {
            val durationInMilliseconds = duration.toMillis()

            service.pulseGuide(device.number, direction, durationInMilliseconds).doRequest() ?: return

            if (durationInMilliseconds > 0) {
                pulseGuiding = true
                sender.fireOnEventReceived(GuideOutputPulsingChanged(this))
            }
        }
    }

    override fun guideNorth(duration: Duration) {
        pulseGuide(PulseGuideDirection.NORTH, duration)
    }

    override fun guideSouth(duration: Duration) {
        pulseGuide(PulseGuideDirection.SOUTH, duration)
    }

    override fun guideEast(duration: Duration) {
        pulseGuide(PulseGuideDirection.EAST, duration)
    }

    override fun guideWest(duration: Duration) {
        pulseGuide(PulseGuideDirection.WEST, duration)
    }

    override fun snoop(devices: Iterable<Device?>) {
        snoopedDevices.clear()

        for (device in devices) {
            device?.also(snoopedDevices::add)
        }
    }

    override fun fitsKeywords(vararg cards: HeaderCard) {
        fitsKeywords = cards
    }

    override fun handleMessage(message: INDIProtocol) = Unit

    override fun onConnected() {
        processExposureMinMax()
        processFrameMinMax()
        processGainMinMax()
        processOffsetMinMax()
        processCapabilities()
        processPixelSize()
        processCfaOffset()
        processReadoutModes()
    }

    override fun onDisconnected() = Unit

    override fun reset() {
        super.reset()

        exposuring = false
        hasCoolerControl = false
        coolerPower = 0.0
        cooler = false
        hasDewHeater = false
        dewHeater = false
        frameFormats = emptyList()
        canAbort = false
        cfaOffsetX = 0
        cfaOffsetY = 0
        cfaType = CfaPattern.RGGB
        exposureMin = 0L
        exposureMax = 0L
        exposureState = PropertyState.IDLE
        exposureTime = 0L
        hasCooler = false
        canSetTemperature = false
        canSubFrame = true
        x = 0
        minX = 0
        maxX = 0
        y = 0
        minY = 0
        maxY = 0
        width = 0
        minWidth = 0
        maxWidth = 0
        height = 0
        minHeight = 0
        maxHeight = 0
        canBin = true
        maxBinX = 1
        maxBinY = 1
        binX = 1
        binY = 1
        gain = 0
        gainMin = 0
        gainMax = 0
        offset = 0
        offsetMin = 0
        offsetMax = 0
        pixelSizeX = 0.0
        pixelSizeY = 0.0
        hasThermometer = false
        temperature = 0.0
        canPulseGuide = false
        pulseGuiding = false
        cameraState = CameraState.IDLE
    }

    override fun close() {
        if (hasThermometer) {
            hasThermometer = false
            sender.unregisterThermometer(this)
        }

        if (canPulseGuide) {
            canPulseGuide = false
            sender.unregisterGuideOutput(this)
        }

        super.close()
        reset()
        imageReadyWaiter.interrupt()
    }

    @Synchronized
    override fun refresh(elapsedTimeInSeconds: Long) {
        super.refresh(elapsedTimeInSeconds)

        if (connected) {
            processCameraState()
            processBin()
            processGain()
            processOffset()
            processCooler()
            processTemperature(false)
        }
    }

    private fun processCameraState() {
        if (!service.cameraState(device.number).doRequest { processCameraState(it.value) }) {
            sender.fireOnEventReceived(CameraExposureFailed(this))
        }
    }

    private fun processCameraState(value: CameraState) {
        if (cameraState != value) {
            cameraState = value

            val prevExposuring = exposuring
            val prevExposureState = exposureState

            when (value) {
                CameraState.IDLE -> {
                    exposuring = false
                    exposureState = PropertyState.IDLE
                }
                CameraState.WAITING -> {
                    exposuring = false
                    exposureState = PropertyState.BUSY
                }
                CameraState.EXPOSURING -> {
                    exposuring = true
                    exposureState = PropertyState.BUSY
                }
                CameraState.READING -> {
                    exposuring = false
                    exposureState = PropertyState.OK
                }
                CameraState.DOWNLOAD -> {
                    exposuring = false
                    exposureState = PropertyState.OK
                }
                CameraState.ERROR -> {
                    exposuring = false
                    exposureState = PropertyState.ALERT
                }
            }

            if (prevExposuring != exposuring) sender.fireOnEventReceived(CameraExposuringChanged(this))
            if (prevExposureState != exposureState) sender.fireOnEventReceived(CameraExposureStateChanged(this))

            if (exposuring) {
                service.percentCompleted(device.number).doRequest {
                    val progressedExposureTime = (imageReadyWaiter.exposureTime * it.value) / 100
                    this.exposureTime = imageReadyWaiter.exposureTime - progressedExposureTime

                    sender.fireOnEventReceived(CameraExposureProgressChanged(this))
                }
            }

            if (exposureState == PropertyState.OK && prevExposureState == PropertyState.BUSY) {
                sender.fireOnEventReceived(CameraExposureFinished(this))
            } else if (exposureState == PropertyState.ALERT && prevExposureState != PropertyState.ALERT) {
                sender.fireOnEventReceived(CameraExposureFailed(this))
            }
        }
    }

    private fun processBin() {
        service.binX(device.number).doRequest { x ->
            service.binY(device.number).doRequest { y ->
                if (x.value != binX || y.value != binY) {
                    binX = x.value
                    binY = y.value

                    sender.fireOnEventReceived(CameraBinChanged(this))
                }
            }
        }
    }

    private fun processGainMinMax() {
        service.gainMin(device.number).doRequest { min ->
            service.gainMax(device.number).doRequest { max ->
                gainMin = min.value
                gainMax = max.value
                gain = max(gainMin, min(gain, gainMax))

                sender.fireOnEventReceived(CameraGainMinMaxChanged(this))
            }
        }
    }

    private fun processGain() {
        service.gain(device.number).doRequest {
            if (it.value != gain) {
                gain = it.value

                sender.fireOnEventReceived(CameraGainChanged(this))
            }
        }
    }

    private fun processOffsetMinMax() {
        service.offsetMin(device.number).doRequest { min ->
            service.offsetMax(device.number).doRequest { max ->
                offsetMin = min.value
                offsetMax = max.value
                offset = max(offsetMin, min(offset, offsetMax))

                sender.fireOnEventReceived(CameraOffsetMinMaxChanged(this))
            }
        }
    }

    private fun processOffset() {
        if (offsetMax > 0) {
            service.offset(device.number).doRequest {
                if (it.value != offset) {
                    offset = it.value

                    sender.fireOnEventReceived(CameraOffsetChanged(this))
                }
            }
        }
    }

    private fun processFrameMinMax() {
        service.x(device.number).doRequest { w ->
            service.y(device.number).doRequest { h ->
                width = w.value
                height = h.value
                minWidth = 0
                maxWidth = width
                minHeight = 0
                maxHeight = height
                x = 0
                minX = 0
                maxX = width - 1
                y = 0
                minY = 0
                maxY = height - 1

                if (!processFrame()) {
                    sender.fireOnEventReceived(CameraFrameChanged(this))
                }
            }
        }
    }

    private fun processFrame(): Boolean {
        service.numX(device.number).doRequest { w ->
            service.numY(device.number).doRequest { h ->
                service.startX(device.number).doRequest { x ->
                    service.startY(device.number).doRequest { y ->
                        if (w.value != width || h.value != height || x.value != this.x || y.value != this.y) {
                            width = w.value
                            height = h.value
                            this.x = x.value
                            this.y = y.value

                            sender.fireOnEventReceived(CameraFrameChanged(this))

                            return true
                        }
                    }
                }
            }
        }

        return false
    }

    private fun processCooler() {
        if (hasCoolerControl) {
            service.coolerPower(device.number).doRequest {
                if (coolerPower != it.value) {
                    coolerPower = it.value

                    sender.fireOnEventReceived(CameraCoolerPowerChanged(this))
                }
            }
        }

        if (hasCooler) {
            service.isCoolerOn(device.number).doRequest {
                if (cooler != it.value) {
                    cooler = it.value

                    sender.fireOnEventReceived(CameraCoolerChanged(this))
                }
            }
        }
    }

    private fun processPixelSize() {
        service.pixelSizeX(device.number).doRequest { x ->
            service.pixelSizeY(device.number).doRequest { y ->
                if (pixelSizeX != x.value || pixelSizeY != y.value) {
                    pixelSizeX = x.value
                    pixelSizeY = y.value

                    sender.fireOnEventReceived(CameraPixelSizeChanged(this))
                }
            }
        }
    }

    private fun processCfaOffset() {
        service.bayerOffsetX(device.number).doRequest { x ->
            service.bayerOffsetY(device.number).doRequest { y ->
                if (cfaOffsetX != x.value || cfaOffsetY != y.value) {
                    cfaOffsetX = x.value
                    cfaOffsetY = y.value

                    sender.fireOnEventReceived(CameraCfaChanged(this))
                }
            }
        }
    }

    private fun processReadoutModes() {
        service.readoutModes(device.number).doRequest {
            frameFormats = it.value.toList()

            sender.fireOnEventReceived(CameraFrameFormatsChanged(this))
        }
    }

    private fun processExposureMinMax() {
        service.exposureMin(device.number).doRequest { min ->
            service.exposureMax(device.number).doRequest { max ->
                exposureMin = (min.value * MICROS_TO_SECONDS).toLong()
                exposureMax = (max.value * MICROS_TO_SECONDS).toLong()

                sender.fireOnEventReceived(CameraExposureMinMaxChanged(this))
            }
        }
    }

    private fun processCapabilities() {
        service.canAbortExposure(device.number).doRequest {
            if (it.value) {
                canAbort = true
                sender.fireOnEventReceived(CameraCanAbortChanged(this))
            }
        }

        service.canCoolerPower(device.number).doRequest {
            if (it.value) {
                hasCoolerControl = true
                sender.fireOnEventReceived(CameraCoolerControlChanged(this))
            }
        }

        service.canPulseGuide(device.number).doRequest {
            if (it.value) {
                canPulseGuide = true
                sender.registerGuideOutput(this)
            }
        }

        service.canSetCCDTemperature(device.number).doRequest {
            if (it.value) {
                canSetTemperature = true
                hasCooler = true

                sender.fireOnEventReceived(CameraHasCoolerChanged(this))
                sender.fireOnEventReceived(CameraCanSetTemperatureChanged(this))
            }
        }

        service.sensorType(device.number).doRequest {
            if (it.value == SensorType.RGGB) {
                canDebayer = true
            }
        }

        processTemperature(true)
    }

    private fun processTemperature(init: Boolean) {
        if (hasThermometer || init) {
            service.ccdTemperature(device.number).doRequest {
                if (!hasThermometer) {
                    hasThermometer = true
                    sender.registerThermometer(this)
                }

                if (it.value != temperature) {
                    temperature = it.value
                    sender.fireOnEventReceived(CameraTemperatureChanged(this))
                }
            }
        }
    }

    private fun readImage(exposureTime: Long) {
        service.imageArray(device.number).execute().body()?.use { body ->
            val stream = body.byteStream()
            val metadata = ImageMetadata.from(stream.readNBytes(44))

            if (metadata.errorNumber != 0) {
                LOG.error("failed to read image. device={}, error={}", name, metadata.errorNumber)
                return
            } else {
                LOG.d { debug("image read. metadata={}", metadata) }
            }

            val width = metadata.dimension1
            val height = metadata.dimension2
            val numberOfChannels = max(1, metadata.dimension3)
            val data = FloatImageData(width, height, numberOfChannels)

            stream.source().buffer().use { source ->
                val channels = arrayOf(data.red, data.green, data.blue)

                for (x in 0 until width) {
                    for (y in 0 until height) {
                        val idx = y * width + x

                        for (p in 0 until numberOfChannels) {
                            channels[p][idx] = when (metadata.transmissionElementType.bitpix) {
                                Bitpix.BYTE -> (source.readByte().toLong() and 0xFF) / 255f
                                Bitpix.SHORT -> (source.readShortLe().toLong() and 0xFFFF) / 65535f
                                Bitpix.INTEGER -> ((source.readIntLe().toLong() and 0xFFFFFFFF) / 4294967295.0).toFloat()
                                else -> return LOG.d { warn("invalid transmission element type: ${metadata.transmissionElementType}") }
                            }
                        }
                    }
                }
            }

            val header = FitsHeader()
            header.add(FitsKeyword.SIMPLE, true)
            header.add(Bitpix.FLOAT)
            header.add(FitsKeyword.NAXIS, if (numberOfChannels == 3) 3 else 2)
            header.add(FitsKeyword.NAXIS1, width)
            header.add(FitsKeyword.NAXIS2, height)
            if (numberOfChannels == 3) header.add(FitsKeyword.NAXIS3, numberOfChannels)
            header.add(FitsKeyword.EXTEND, true)
            header.add(FitsKeyword.INSTRUME, name)
            val exposureTimeInSeconds = exposureTime.toDouble() / MICROS_TO_SECONDS
            header.add(FitsKeyword.EXPTIME, exposureTimeInSeconds)
            header.add(FitsKeyword.EXPOSURE, exposureTimeInSeconds)
            if (hasThermometer) header.add(FitsKeyword.CCD_TEMP, temperature)
            header.add(FitsKeyword.PIXSIZEn.n(1), pixelSizeX)
            header.add(FitsKeyword.PIXSIZEn.n(2), pixelSizeY)
            header.add(FitsKeyword.XBINNING, binX)
            header.add(FitsKeyword.YBINNING, binY)
            header.add(FitsKeyword.XPIXSZ, pixelSizeX * binX)
            header.add(FitsKeyword.YPIXSZ, pixelSizeY * binY)
            header.add("FRAME", frameType.description, "Frame Type")
            header.add(FitsKeyword.IMAGETYP, "${frameType.description} Frame")
            header.add(FitsKeyword.DATE_OBS, LocalDateTime.now(ZoneOffset.UTC).format(DATE_OBS_FORMAT))
            header.add(FitsKeyword.COMMENT, "Generated by Nebulosa via ASCOM")
            header.add(FitsKeyword.GAIN, gain)
            header.add("OFFSET", offset, "Offset")
            if (canDebayer) header.add(cfaType)

            val mount = snoopedDevices.firstOrNull { it is Mount && it.connected } as? Mount

            mount?.also {
                header.add(FitsKeyword.TELESCOP, it.name)
                header.add(FitsKeyword.SITELONG, it.longitude.format(DEC_FORMAT))
                header.add(FitsKeyword.SITELAT, it.latitude.format(DEC_FORMAT))
                val center = Geoid.IERS2010.lonLat(it.longitude, it.latitude, it.elevation)
                val icrf = ICRF.equatorial(it.rightAscension, it.declination, epoch = CurrentTime, center = center)
                val raDec = icrf.equatorial()
                header.add(FitsKeyword.OBJCTRA, raDec.longitude.normalized.format(RA_FORMAT))
                header.add(FitsKeyword.OBJCTDEC, raDec.latitude.format(DEC_FORMAT))
                header.add(FitsKeyword.RA, raDec.longitude.normalized.format(RA_FORMAT))
                header.add(FitsKeyword.DEC, raDec.latitude.format(DEC_FORMAT))
                val altAz = icrf.horizontal()
                header.add(FitsKeyword.OBJCTAZ, altAz.longitude.toDegrees)
                header.add(FitsKeyword.OBJCTALT, altAz.latitude.toDegrees)
                // header.add(FitsKeyword.PIERSIDE, it.pierSide.name)
                header.add(FitsKeyword.EQUINOX, 2000)
            }

            val focuser = snoopedDevices.firstOrNull { it is Focuser && it.connected } as? Focuser

            focuser?.also {
                header.add(FitsKeyword.FOCUSPOS, it.position)
            }

            val wheel = snoopedDevices.firstOrNull { it is FilterWheel && it.connected } as? FilterWheel

            wheel?.also {
                header.add(FitsKeyword.FILTER, it.names.getOrNull(it.position) ?: "Filter #${it.position}")
            }

            val rotator = snoopedDevices.firstOrNull { it is Rotator && it.connected } as? Rotator

            rotator?.also {
                header.add(FitsKeyword.ROTATANG, rotator.angle.toDegrees)
            }

            fitsKeywords.forEach(header::add)

            val hdu = BasicImageHdu(width, height, numberOfChannels, header, data)
            val image = Fits()
            image.add(hdu)

            sender.fireOnEventReceived(CameraFrameCaptured(this, image = image))
        } ?: LOG.error("image body is null. device={}", name)
    }

    override fun toString() = "Camera(name=$name, connected=$connected, exposuring=$exposuring," +
            " hasCoolerControl=$hasCoolerControl, cooler=$cooler," +
            " hasDewHeater=$hasDewHeater, dewHeater=$dewHeater," +
            " frameFormats=$frameFormats, canAbort=$canAbort," +
            " cfaOffsetX=$cfaOffsetX, cfaOffsetY=$cfaOffsetY, cfaType=$cfaType," +
            " exposureMin=$exposureMin, exposureMax=$exposureMax," +
            " exposureState=$exposureState, exposureTime=$exposureTime," +
            " hasCooler=$hasCooler, hasThermometer=$hasThermometer, canSetTemperature=$canSetTemperature," +
            " temperature=$temperature, canSubFrame=$canSubFrame," +
            " x=$x, minX=$minX, maxX=$maxX, y=$y, minY=$minY, maxY=$maxY," +
            " width=$width, minWidth=$minWidth, maxWidth=$maxWidth, height=$height," +
            " minHeight=$minHeight, maxHeight=$maxHeight," +
            " canBin=$canBin, maxBinX=$maxBinX, maxBinY=$maxBinY," +
            " binX=$binX, binY=$binY, gain=$gain, gainMin=$gainMin," +
            " gainMax=$gainMax, offset=$offset, offsetMin=$offsetMin," +
            " offsetMax=$offsetMax, canPulseGuide=$canPulseGuide, pulseGuiding=$pulseGuiding)"

    data class ImageMetadata(
        @JvmField val metadataVersion: Int, // Bytes 0..3 - Metadata version = 1
        @JvmField val errorNumber: Int, // Bytes 4..7 - Alpaca error number or zero for success
        @JvmField val clientTransactionID: Int, // Bytes 8..11 - Client's transaction ID
        @JvmField val serverTransactionID: Int, // Bytes 12..15 - Device's transaction ID
        @JvmField val dataStart: Int, // Bytes 16..19 - Offset of the start of the data bytes
        @JvmField val imageElementType: ImageArrayElementType, // Bytes 20..23 - Element type of the source image array
        @JvmField val transmissionElementType: ImageArrayElementType, // Bytes 24..27 - Element type as sent over the network
        @JvmField val rank: Int, // Bytes 28..31 - Image array rank (2 or 3)
        @JvmField val dimension1: Int, // Bytes 32..35 - Length of image array first dimension
        @JvmField val dimension2: Int, // Bytes 36..39 - Length of image array second dimension
        @JvmField val dimension3: Int, // Bytes 40..43 - Length of image array third dimension (0 for 2D array)
    ) {

        companion object {

            @JvmStatic
            fun from(data: ByteBuffer) = ImageMetadata(
                data.getInt(), data.getInt(), data.getInt(), data.getInt(), data.getInt(),
                ImageArrayElementType.entries[data.getInt()], ImageArrayElementType.entries[data.getInt()],
                data.getInt(), data.getInt(), data.getInt(), data.getInt()
            )

            @JvmStatic
            fun from(data: ByteArray) = from(ByteBuffer.wrap(data, 0, 44).order(ByteOrder.LITTLE_ENDIAN))
        }
    }

    private inner class ImageReadyWaiter : Thread("$name ASCOM Image Ready Waiter") {

        private val latch = CountUpDownLatch(1)
        private val aborted = AtomicBoolean()

        @Volatile @JvmField var exposureTime = 0L

        init {
            isDaemon = true
        }

        fun captureStarted(exposureTime: Long) {
            this.exposureTime = exposureTime
            aborted.set(false)
            latch.reset()
        }

        fun captureAborted() {
            aborted.set(true)
            latch.countUp()
        }

        override fun run() {
            while (true) {
                latch.await()

                while (latch.get()) {
                    val startTime = System.currentTimeMillis()

                    processCameraState()

                    service.isImageReady(device.number).doRequest {
                        if (it.value && !aborted.get()) {
                            latch.countUp()

                            try {
                                readImage(exposureTime)
                            } catch (e: Throwable) {
                                LOG.error("failed to read image", e)
                            }
                        }
                    }

                    if (aborted.get()) {
                        break
                    } else if (!latch.get()) {
                        val endTime = System.currentTimeMillis()
                        val delayTime = 1000L - (endTime - startTime)

                        if (delayTime > 1L) {
                            sleep(delayTime)
                        }
                    }
                }

                if (aborted.get()) {
                    sender.fireOnEventReceived(CameraExposureAborted(this@ASCOMCamera))
                } else {
                    sender.fireOnEventReceived(CameraExposureFinished(this@ASCOMCamera))
                }

                processCameraState(CameraState.IDLE)
            }
        }
    }

    companion object {

        private const val MICROS_TO_SECONDS = 1_000_000L

        private val LOG = loggerFor<ASCOMCamera>()
        private val DATE_OBS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        private val RA_FORMAT = AngleFormatter.HMS.newBuilder().secondsDecimalPlaces(3).separators(" ").build()
        private val DEC_FORMAT = AngleFormatter.SIGNED_DMS.newBuilder().secondsDecimalPlaces(3).separators(" ").build()
    }
}
