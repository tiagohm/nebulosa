package nebulosa.api.system

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("system")
class SystemController(private val systemService: SystemService) {

    @GetMapping("latest-release")
    fun latestRelease() = systemService.latestRelease()
}
