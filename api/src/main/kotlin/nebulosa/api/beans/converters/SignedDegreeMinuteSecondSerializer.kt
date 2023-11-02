package nebulosa.api.beans.converters

import nebulosa.math.AngleFormatter

class SignedDegreeMinuteSecondSerializer : AngleSerializer(AngleFormatter.SIGNED_DMS)
