package nebulosa.api.preferences

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("preferences")
class PreferenceController(
    private val preferenceService: PreferenceService,
) {

    @GetMapping("{key}/boolean")
    fun getBoolean(@PathVariable key: String): Boolean? {
        return preferenceService.getBoolean(key)
    }

    @GetMapping("{key}/int")
    fun getInt(@PathVariable key: String): Int? {
        return preferenceService.getInt(key)
    }

    @GetMapping("{key}/long")
    fun getLong(@PathVariable key: String): Long? {
        return preferenceService.getLong(key)
    }

    @GetMapping("{key}/double")
    fun getDouble(@PathVariable key: String): Double? {
        return preferenceService.getDouble(key)
    }

    @GetMapping("{key}/text")
    fun getText(@PathVariable key: String): String? {
        return preferenceService.getText(key)
    }

    @GetMapping("{key}")
    fun getJSON(@PathVariable key: String): Any? {
        return preferenceService.getJSON<Any>(key)
    }

    @PutMapping("{key}/boolean")
    fun putBoolean(@PathVariable key: String, @RequestParam value: Boolean) {
        return preferenceService.putBoolean(key, value)
    }

    @PutMapping("{key}/int")
    fun putInt(@PathVariable key: String, @RequestParam value: Int) {
        return preferenceService.putInt(key, value)
    }

    @PutMapping("{key}/long")
    fun putLong(@PathVariable key: String, @RequestParam value: Long) {
        return preferenceService.putLong(key, value)
    }

    @PutMapping("{key}/double")
    fun putDouble(@PathVariable key: String, @RequestParam value: Double) {
        return preferenceService.putDouble(key, value)
    }

    @PutMapping("{key}/text")
    fun putText(@PathVariable key: String, @RequestParam value: String) {
        return preferenceService.putText(key, value)
    }

    @PutMapping("{key}")
    fun putJSON(@PathVariable key: String, @RequestBody body: PreferenceRequestBody?) {
        return preferenceService.putJSON(key, body?.data)
    }

    @DeleteMapping("{key}")
    fun delete(@PathVariable key: String) {
        return preferenceService.delete(key)
    }

    @PutMapping("{key}/exists")
    fun exists(@PathVariable key: String): Boolean {
        return key in preferenceService
    }

    @PutMapping("clear")
    fun clear() {
        return preferenceService.clear()
    }
}
