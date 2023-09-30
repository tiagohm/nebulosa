import io.kotest.core.annotation.EnabledCondition
import io.kotest.core.spec.Spec
import kotlin.reflect.KClass

class NonGitHubOnlyCondition : EnabledCondition {

    override fun enabled(kclass: KClass<out Spec>): Boolean {
        return System.getProperty("github", "false") == "false"
    }
}
