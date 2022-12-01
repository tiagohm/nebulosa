package nebulosa.api.scheduler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("scheduler")
class SchedulerController {

    @Autowired
    private lateinit var schedulerService: SchedulerService

    @GetMapping
    fun list(): List<ScheduledTaskRes> {
        return schedulerService.tasks().map(ScheduledTaskRes::from)
    }
}
