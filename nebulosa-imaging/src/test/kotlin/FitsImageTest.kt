import nebulosa.test.FitsStringSpec

class FitsImageTest : FitsStringSpec() {

    init {
//        "8-bits color full (ICC Profile + Properties + Thumbnail)" {
//            val fits = Fits("src/test/resources/M51.8.Color.Full.fits")
//            fits.read()
//            val image = Image.openFITS(fits)
//            val outputFile = File("src/test/resources/M51.8.Color.Full.png")
//            ImageIO.write(image, "PNG", outputFile)
//            outputFile.md5() shouldBe "5aa19e4d7aa87d3207f9c5f710698b2f"
//        }
//
//        "Grayscale" {
//            val fits = Fits("src/test/resources/M51.8.Color.fits")
//            fits.read()
//            val image = Image.openFITS(fits).transform(Grayscale.BT709)
//            val outputFile = File("src/test/resources/M51.8.Color.Grayscale.BT709.png")
//            ImageIO.write(image, "PNG", outputFile)
//            outputFile.md5() shouldBe "a7313408fafa9c1d743ca34a481051b9"
//        }
//        "Debayer - GRBG" {
//            val fits = Fits("src/test/resources/Debayer.GRBG.fits")
//            fits.read()
//            val image = Image.openFITS(fits)
//            val outputFile = File("src/test/resources/Debayer.GRBG.png")
//            ImageIO.write(image, "PNG", outputFile)
//            outputFile.md5() shouldBe "dad1a430e41e4846f0c6a9c594e5d57d"
//        }
//        "SCNR" {
//            val fits = Fits("src/test/resources/Debayer.GRBG.fits")
//            fits.read()
//            val scnr = SubtractiveChromaticNoiseReduction(ImageChannel.GREEN, 0.5f, ProtectionMethod.AVERAGE_NEUTRAL)
//            val image = Image.openFITS(fits).transform(scnr)
//            val outputFile = File("src/test/resources/Debayer.SCNR.png")
//            ImageIO.write(image, "PNG", outputFile)
//            outputFile.md5() shouldBe "223dd13ec260782e135ff64a5acedb26"
//        }
//        "Salt & Pepper Noise" {
//            val fits = Fits("src/test/resources/Flower.fits")
//            fits.read()
//            val image = Image.openFITS(fits).transform(SaltAndPepperNoise(0.1f, Random(0)))
//            val outputFile = File("src/test/resources/Flower.SaltPepperNoise.png")
//            ImageIO.write(image, "PNG", outputFile)
//            outputFile.md5() shouldBe "7d15259b367ea973be204038f0972159"
//        }
//        "write Color FITS as FITS" {
//            val fits = Fits("src/test/resources/Flower.fits")
//            fits.read()
//            val image1 = Image.openFITS(fits)
//            val outputFile1 = File("src/test/resources/Flower.Color.Fits.1.png")
//            ImageIO.write(image1, "PNG", outputFile1)
//
////            val image2 = Image.openFITS(image1.fits())
////            val outputFile2 = File("src/test/resources/Flower.Color.Fits.2.png")
////            ImageIO.write(image2, "PNG", outputFile2)
////
////            outputFile1.md5() shouldBe outputFile2.md5()
//        }
//        "write Color PNG as FITS" {
//            val fits = Fits("src/test/resources/Flower.fits")
//            fits.read()
//            val image1 = Image.openFITS(fits)
//            val output1 = File("src/test/resources/Flower.Color.PNG.1.png")
//            ImageIO.write(image1, "PNG", output1)
//
//            val image2 = Image.openImage(ImageIO.read(output1))
//            val output2 = File("src/test/resources/Flower.Color.PNG.2.png")
//            ImageIO.write(image2, "PNG", output2)
//
//            output1.md5() shouldBe output2.md5()
//        }
//        "write Mono FITS as FITS" {
//            val fits = Fits("src/test/resources/CCD Simulator.Gray.fits")
//            fits.read()
//            val image1 = Image.openFITS(fits).transform(ScreenTransformFunction(5.8e-5f))
//            val output1 = File("src/test/resources/CCD Simulator.Gray.Mono.Fits.1.png")
//            ImageIO.write(image1, "PNG", output1)
//
////            val image2 = Image.openFITS(image1.fits())
////            val output2 = File("src/test/resources/CCD Simulator.Gray.Mono.Fits.2.png")
////            ImageIO.write(image2, "PNG", output2)
////
////            output1.md5() shouldBe output2.md5()
//        }
//        "write Mono PNG as FITS" {
//            val fits = Fits("src/test/resources/CCD Simulator.Gray.fits")
//            fits.read()
//            val image1 = Image.openFITS(fits).transform(ScreenTransformFunction(5.8e-5f))
//            val output1 = File("src/test/resources/CCD Simulator.Gray.Mono.PNG.1.png")
//            ImageIO.write(image1, "PNG", output1)
//
//            val image2 = Image.openImage(ImageIO.read(output1))
//            val output2 = File("src/test/resources/CCD Simulator.Gray.Mono.PNG.2.png")
//            ImageIO.write(image2, "PNG", output2)
//
//            // TODO: outputFile1.md5() shouldBe outputFile2.md5()
//        }
    }
}
