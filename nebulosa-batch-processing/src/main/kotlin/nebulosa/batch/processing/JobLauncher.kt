package nebulosa.batch.processing

interface JobLauncher : Collection<JobExecution>, Stoppable {

    fun launch(job: Job): JobExecution
}
