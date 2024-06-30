package nebulosa.api.beans.converters.angle

import nebulosa.math.AngleFormatter

class DeclinationSerializer : FormattedAngleSerializer(AngleFormatter.SIGNED_DMS)
