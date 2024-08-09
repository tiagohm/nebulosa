package nebulosa.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS

@Test
@EnabledOnOs(OS.WINDOWS)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class WindowsOnly
