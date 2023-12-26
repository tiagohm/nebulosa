package nebulosa.api.beans.converters.angle

import nebulosa.math.AngleFormatter

class DeclinationSerializer : AngleSerializer(AngleFormatter.SIGNED_DMS)
