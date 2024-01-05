package nebulosa.api.beans.converters.angle

import nebulosa.math.AngleFormatter

class LSTSerializer : AngleSerializer(AngleFormatter.Builder().hours().noSign().noSeconds().build())
