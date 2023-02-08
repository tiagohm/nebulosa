package nebulosa.query.simbad

import nebulosa.query.QueryService

class SimbadService : QueryService(""), Simbad {

    private val service = retrofit.create(Simbad::class.java)
}
