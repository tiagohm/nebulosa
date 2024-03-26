package nebulosa.alpaca.indi.device.cameras

import nebulosa.alpaca.api.AlpacaCameraService
import nebulosa.alpaca.api.CameraState
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.api.PulseGuideDirection
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.alpaca.indi.device.ASCOMDevice
import nebulosa.common.concurrency.latch.CountUpDownLatch
import nebulosa.fits.Bitpix
import nebulosa.fits.Fits
import nebulosa.fits.FitsHeader
import nebulosa.fits.FitsKeywordDictionary
import nebulosa.image.algorithms.transformation.CfaPattern
import nebulosa.image.format.BasicImageHdu
import nebulosa.image.format.FloatImageData
import nebulosa.indi.device.Device
import nebulosa.indi.device.camera.*
import nebulosa.indi.device.camera.Camera.Companion.NANO_SECONDS
import nebulosa.indi.device.guide.GuideOutputPulsingChanged
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.PropertyState
import nebulosa.io.readDoubleLe
import nebulosa.io.readFloatLe
import nebulosa.math.formatHMS
import nebulosa.math.formatSignedDMS
import nebulosa.math.normalized
import nebulosa.math.toDegrees
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.time.CurrentTime
import okio.buffer
import okio.source
import java.nio.ByteBuffer
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

data class ASCOMCamera(
    override val device: ConfiguredDevice,
    override val service: AlpacaCameraService,
    override val sender: AlpacaClient,
) : ASCOMDevice(), Camera {

    @Volatile override var exposuring = false
        private set
    @Volatile override var hasCoolerControl = false
        private set
    @Volatile override var coolerPower = 0.0
        private set
    @Volatile override var cooler = false
        private set
    @Volatile override var hasDewHeater = false
        private set
    @Volatile override var dewHeater = false
        private set
    @Volatile override var frameFormats = emptyList<String>()
        private set
    @Volatile override var canAbort = false
        private set
    @Volatile override var cfaOffsetX = 0
        private set
    @Volatile override var cfaOffsetY = 0
        private set
    @Volatile override var cfaType = CfaPattern.RGGB
        private set
    @Volatile override var exposureMin: Duration = Duration.ZERO
        private set
    @Volatile override var exposureMax: Duration = Duration.ZERO
        private set
    @Volatile override var exposureState = PropertyState.IDLE
        private set
    @Volatile override var exposureTime: Duration = Duration.ZERO
        private set
    @Volatile override var hasCooler = false
        private set
    @Volatile override var canSetTemperature = false
        private set
    @Volatile override var canSubFrame = true
        private set
    @Volatile override var x = 0
        private set
    @Volatile override var minX = 0
        private set
    @Volatile override var maxX = 0
        private set
    @Volatile override var y = 0
        private set
    @Volatile override var minY = 0
        private set
    @Volatile override var maxY = 0
        private set
    @Volatile override var width = 0
        private set
    @Volatile override var minWidth = 0
        private set
    @Volatile override var maxWidth = 0
        private set
    @Volatile override var height = 0
        private set
    @Volatile override var minHeight = 0
        private set
    @Volatile override var maxHeight = 0
        private set
    @Volatile override var canBin = true
        private set
    @Volatile override var maxBinX = 1
        private set
    @Volatile override var maxBinY = 1
        private set
    @Volatile override var binX = 1
        private set
    @Volatile override var binY = 1
        private set
    @Volatile override var gain = 0
        private set
    @Volatile override var gainMin = 0
        private set
    @Volatile override var gainMax = 0
        private set
    @Volatile override var offset = 0
        private set
    @Volatile override var offsetMin = 0
        private set
    @Volatile override var offsetMax = 0
        private set
    @Volatile override var hasGuiderHead = false // TODO: ASCOM has guider head?
        private set
    @Volatile override var pixelSizeX = 0.0
        private set
    @Volatile override var pixelSizeY = 0.0
        private set

    @Volatile override var hasThermometer = false
        private set
    @Volatile override var temperature = 0.0
        private set

    @Volatile override var canPulseGuide = false
        private set
    @Volatile override var pulseGuiding = false
        private set

    @Volatile private var cameraState = CameraState.IDLE
    @Volatile private var frameType = FrameType.LIGHT
    @Volatile private var mount: Mount? = null

    private val imageReadyWaiter = ImageReadyWaiter()

    init {
        refresh(0L)
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

    override fun startCapture(exposureTime: Duration) {
        this.exposureTime = exposureTime

        service.startExposure(device.number, exposureTime.toNanos() / NANO_SECONDS, frameType == FrameType.DARK).doRequest {
            imageReadyWaiter.captureStarted()
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
        for (device in devices) {
            if (device is Mount) mount = device
        }
    }

    override fun handleMessage(message: INDIProtocol) {
    }

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

    override fun onDisconnected() {
    }

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
        exposureMin = Duration.ZERO
        exposureMax = Duration.ZERO
        exposureState = PropertyState.IDLE
        exposureTime = Duration.ZERO
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
        hasGuiderHead = false
        pixelSizeX = 0.0
        pixelSizeY = 0.0
        hasThermometer = false
        temperature = 0.0
        canPulseGuide = false
        pulseGuiding = false
        cameraState = CameraState.IDLE
    }

    override fun close() {
        super.close()
        reset()
        imageReadyWaiter.interrupt()
    }

    @Synchronized
    override fun refresh(elapsedTimeInSeconds: Long) {
        super.refresh(elapsedTimeInSeconds)

        if (connected) {
            service.cameraState(device.number).doRequest { processCameraState(it.value) }

            processBin()
            processGain()
            processOffset()
            processCooler()
        }
    }

    private fun processCameraState(value: CameraState) {
        if (cameraState != value) {
            cameraState = value

            val prevExposuring = exposuring
            val prevExposureState = exposureState

            when (value) {
                CameraState.IDLE -> {
                    if (exposuring) {
                        exposuring = false
                        exposureState = PropertyState.IDLE
                    }
                }
                CameraState.WAITING -> {
                    if (exposuring) {
                        exposuring = false
                        exposureState = PropertyState.BUSY
                    }
                }
                CameraState.EXPOSURING -> {
                    if (!exposuring) {
                        exposuring = true
                        exposureState = PropertyState.BUSY
                    }
                }
                CameraState.READING -> {
                    if (exposuring) {
                        exposuring = false
                        exposureState = PropertyState.OK
                    }
                }
                CameraState.DOWNLOAD -> {
                    if (exposuring) {
                        exposuring = false
                        exposureState = PropertyState.OK
                    }
                }
                CameraState.ERROR -> {
                    if (exposuring) {
                        exposuring = false
                        exposureState = PropertyState.ALERT
                    }
                }
            }

            if (prevExposuring != exposuring) sender.fireOnEventReceived(CameraExposuringChanged(this))
            if (prevExposureState != exposureState) sender.fireOnEventReceived(CameraExposureStateChanged(this, prevExposureState))

            if (exposuring) {
                service.percentCompleted(device.number).doRequest {

                }
            }

            if (exposureState == PropertyState.IDLE && (prevExposureState == PropertyState.BUSY || exposuring)) {
                sender.fireOnEventReceived(CameraExposureAborted(this))
            } else if (exposureState == PropertyState.OK && prevExposureState == PropertyState.BUSY) {
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
        service.offset(device.number).doRequest {
            if (it.value != offset) {
                offset = it.value

                sender.fireOnEventReceived(CameraOffsetChanged(this))
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
                exposureMin = Duration.ofNanos((min.value * NANO_SECONDS).toLong())
                exposureMax = Duration.ofNanos((max.value * NANO_SECONDS).toLong())

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
                LOG.info("guide output attached: {}", name)
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
    }

    private fun readImage() {
        service.imageArray(device.number).execute().body()?.use { body ->
            val stream = body.byteStream()
            val metadata = ImageMetadata.from(stream.readNBytes(44))

            if (metadata.errorNumber != 0) {
                LOG.error("failed to read image. device={}, error={}", name, metadata.errorNumber)
                return
            }

            val width = metadata.dimension1
            val height = metadata.dimension2
            val numberOfChannels = max(1, metadata.dimension3)
            val source = stream.source().buffer()
            val data = FloatImageData(width, height, numberOfChannels)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    val idx = y * width + x

                    for (p in 0 until numberOfChannels) {
                        val pixel = when (metadata.imageElementType.bitpix) {
                            Bitpix.BYTE -> (source.readByte().toInt() and 0xFF) / 255f
                            Bitpix.SHORT -> (source.readShortLe().toInt() + 32768) / 65535f
                            Bitpix.INTEGER -> ((source.readIntLe().toLong() + 2147483648) / 4294967295.0).toFloat()
                            Bitpix.FLOAT -> source.readFloatLe()
                            Bitpix.DOUBLE -> source.readDoubleLe().toFloat()
                            Bitpix.LONG -> return
                        }

                        val channel = when (p) {
                            0 -> data.red
                            1 -> data.green
                            else -> data.blue
                        }

                        channel[idx] = pixel
                    }
                }
            }

            source.close()

            val header = FitsHeader()
            header.add(FitsKeywordDictionary.SIMPLE, true)
            header.add(FitsKeywordDictionary.BITPIX, -32)
            header.add(FitsKeywordDictionary.NAXIS, if (numberOfChannels == 3) 3 else 2)
            header.add(FitsKeywordDictionary.NAXIS1, width)
            header.add(FitsKeywordDictionary.NAXIS2, height)
            if (numberOfChannels == 3) header.add(FitsKeywordDictionary.NAXIS3, numberOfChannels)
            header.add(FitsKeywordDictionary.EXTEND, true)
            header.add(FitsKeywordDictionary.INSTRUME, name)
            header.add(FitsKeywordDictionary.EXPTIME, 0.0) // TODO
            header.add(FitsKeywordDictionary.CCD_TEMP, temperature)
            header.add(FitsKeywordDictionary.PIXSIZEn.n(1), pixelSizeX)
            header.add(FitsKeywordDictionary.PIXSIZEn.n(2), pixelSizeY)
            header.add(FitsKeywordDictionary.XBINNING, binX)
            header.add(FitsKeywordDictionary.YBINNING, binY)
            header.add(FitsKeywordDictionary.XPIXSZ, pixelSizeX * binX)
            header.add(FitsKeywordDictionary.YPIXSZ, pixelSizeY * binY)
            header.add("FRAME", frameType.description, "Frame Type")
            header.add(FitsKeywordDictionary.IMAGETYP, "${frameType.description} Frame")

            mount?.also {
                header.add(FitsKeywordDictionary.TELESCOP, it.name)
                header.add(FitsKeywordDictionary.SITELAT, it.latitude.toDegrees)
                header.add(FitsKeywordDictionary.SITELONG, it.longitude.toDegrees)
                val center = Geoid.IERS2010.lonLat(it.longitude, it.latitude, it.elevation)
                val icrf = ICRF.equatorial(it.rightAscension, it.declination, epoch = CurrentTime, center = center)
                val raDec = icrf.equatorial()
                header.add(FitsKeywordDictionary.OBJCTRA, raDec.longitude.normalized.formatHMS())
                header.add(FitsKeywordDictionary.OBJCTDEC, raDec.longitude.formatSignedDMS())
                header.add(FitsKeywordDictionary.RA, raDec.longitude.normalized.toDegrees)
                header.add(FitsKeywordDictionary.DEC, raDec.longitude.toDegrees)
                header.add(FitsKeywordDictionary.PIERSIDE, it.pierSide.name)
                header.add(FitsKeywordDictionary.EQUINOX, 2000)
                header.add(FitsKeywordDictionary.DATE_OBS, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                header.add(FitsKeywordDictionary.COMMENT, "Generated by Nebulosa via ASCOM")
                header.add(FitsKeywordDictionary.GAIN, gain)
                header.add("OFFSET", offset, "Offset")
            }

            val hdu = BasicImageHdu(width, height, numberOfChannels, header, data)

            val fits = Fits()
            fits.add(hdu)

            sender.fireOnEventReceived(CameraFrameCaptured(this, null, fits, false))
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
        " offsetMax=$offsetMax, hasGuiderHead=$hasGuiderHead," +
        " canPulseGuide=$canPulseGuide, pulseGuiding=$pulseGuiding)"

    data class ImageMetadata(
        @JvmField val metadataVersion: Int, // Bytes 0..3 - Metadata version = 1
        @JvmField val errorNumber: Int, // Bytes 4..7 - Alpaca error number or zero for success
        @JvmField val clientTransactionID: Int, // Bytes 8..11 - Client's transaction ID
        @JvmField val serverTransactionID: Int, // Bytes 12..15 - Device's transaction ID
        @JvmField val dataStart: Int, // Bytes 16..19 - Offset of the start of the data bytes
        @JvmField val imageElementType: ImageArrayElementType, // Bytes 20..23 - Element type of the source image array
        @JvmField val transmissionElementType: Int, // Bytes 24..27 - Element type as sent over the network
        @JvmField val rank: Int, // Bytes 28..31 - Image array rank (2 or 3)
        @JvmField val dimension1: Int, // Bytes 32..35 - Length of image array first dimension
        @JvmField val dimension2: Int, // Bytes 36..39 - Length of image array second dimension
        @JvmField val dimension3: Int, // Bytes 40..43 - Length of image array third dimension (0 for 2D array)
    ) {

        companion object {

            @JvmStatic
            fun from(data: ByteBuffer) = ImageMetadata(
                data.getInt(), data.getInt(), data.getInt(), data.getInt(), data.getInt(),
                ImageArrayElementType.entries[data.getInt()], data.getInt(), data.getInt(),
                data.getInt(), data.getInt(), data.getInt()
            )

            @JvmStatic
            fun from(data: ByteArray) = from(ByteBuffer.wrap(data, 0, 44))
        }
    }

    private inner class ImageReadyWaiter : Thread("$name ASCOM Image Ready Waiter") {

        private val latch = CountUpDownLatch(1)

        init {
            isDaemon = true
        }

        fun captureStarted() {
            latch.reset()
        }

        fun captureAborted() {
            latch.countUp()
        }

        override fun run() {
            while (true) {
                latch.await()

                while (latch.get()) {
                    val startTime = System.currentTimeMillis()

                    service.isImageReady(device.number).doRequest {
                        if (it.value) {
                            latch.countUp()
                            readImage()
                        }
                    }

                    if (!latch.get()) {
                        val endTime = System.currentTimeMillis()
                        val delayTime = 1000L - (endTime - startTime)

                        if (delayTime > 1L) {
                            sleep(delayTime)
                        }
                    }
                }
            }
        }
    }
}
