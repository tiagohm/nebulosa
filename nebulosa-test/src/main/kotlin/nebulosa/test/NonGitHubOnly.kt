package nebulosa.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty

@Test
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@EnabledIfSystemProperty(named = "github", matches = "false")
annotation class NonGitHubOnly
