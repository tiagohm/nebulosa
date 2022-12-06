package nebulosa.api.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("scheduler")
class SchedulerController {

    @Autowired
    private lateinit var schedulerService: SchedulerService

    @Synchronized
    @PostMapping("pause")
    fun pause() {
        schedulerService.pause()
    }

    @Synchronized
    @PostMapping("unpause")
    fun unpause() {
        schedulerService.unpause()
    }

    @GetMapping
    fun tasks(): List<ScheduledTask<*>> {
        return schedulerService.tasks()
    }
}
