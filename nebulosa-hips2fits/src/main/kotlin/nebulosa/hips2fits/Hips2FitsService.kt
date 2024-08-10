package nebulosa.hips2fits

import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.toDegrees
import nebulosa.retrofit.RetrofitService
import okhttp3.OkHttpClient
import retrofit2.create

/**
 * The CDS hips2fits service offers a way to extract FITS images from HiPS sky maps.
 */
class Hips2FitsService(
    url: String = MAIN_URL,
    httpClient: OkHttpClient? = null,
) : RetrofitService(url, httpClient) {

    private val service by lazy { retrofit.create<Hips2Fits>() }

    /**
     * Extracts a FITS image from a HiPS given the output image pixel size,
     * the center of projection, the type of projection and the field of view.
     */
    fun query(
        id: String,
        ra: Angle, dec: Angle,
        width: Int = 1200, height: Int = 900,
        rotation: Angle = 0.0,
        fov: Angle = 1.0.deg,
        projection: ProjectionType = ProjectionType.TAN,
        coordSystem: CoordinateFrameType = CoordinateFrameType.ICRS,
        format: FormatOutputType = FormatOutputType.FITS,
    ) = service.query(
        id, ra.toDegrees, dec.toDegrees, width, height, projection.name, fov.toDegrees,
        coordSystem.name.lowercase(), rotation.toDegrees, format.name.lowercase(),
    )

    // https://alasky.cds.unistra.fr/MocServer/query?get=record&fmt=json&expr=ID%3DCDS*%20%26%26%20hips_service_url*%3D*alasky*%20%26%26%20dataproduct_type%3Dimage%20%26%26%20moc_sky_fraction%20%3E%3D%200.99%20%26%26%20obs_regime%3DOptical%2CInfrared%2CUV%2CRadio%2CX-ray%2CGamma-ray
    fun availableSurveys() = service
        .availableSurveys("ID=CDS* && hips_service_url*=*alasky* && dataproduct_type=image && moc_sky_fraction >= 0.99 && obs_regime=Optical,Infrared,UV,Radio,X-ray,Gamma-ray")

    companion object {

        const val MAIN_URL = "https://alasky.cds.unistra.fr/"
        const val ALTERNATIVE_URL = "https://alasky.u-strasbg.fr/"
    }
}
