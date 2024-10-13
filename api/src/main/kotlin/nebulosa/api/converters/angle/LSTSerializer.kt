package nebulosa.api.converters.angle

import nebulosa.math.AngleFormatter

class LSTSerializer : FormattedAngleSerializer(AngleFormatter.Builder().hours().noSign().noSeconds().build())
