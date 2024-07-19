@file:Suppress("NOTHING_TO_INLINE")
@file:JvmName("Paths")

package nebulosa.test

import java.nio.file.Path

val homeDirectory: Path
    get() = Path.of(System.getProperty("user.home"))

val rootDirectory: Path
    get() = Path.of(System.getProperty("root.dir"))

val projectDirectory: Path
    get() = Path.of(System.getProperty("project.dir"))

val dataDirectory: Path
    get() = Path.of("$rootDirectory", "data")

val cacheDirectory: Path
    get() = Path.of("$rootDirectory", ".cache")

inline fun Path.concat(path: String): Path = Path.of("$this", path)

inline fun Path.concat(vararg path: String): Path = Path.of("$this", *path)
