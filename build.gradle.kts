val envFile: File = rootProject.file(".env")
if (envFile.exists()) {
    envFile.forEachLine { line ->
        val trimmed = line.trim()
        if (trimmed.isBlank() || trimmed.startsWith("#")) return@forEachLine

        val parts = trimmed.split("=", limit = 2)
        if (parts.size != 2) return@forEachLine

        val key = parts[0].trim()
        val value = parts[1].trim()

        if (!System.getenv().containsKey(key)) {
            System.setProperty(key, value)
        }
    }
}

allprojects {
    group = "org.klyx.flux"
    version = "1.0.2"

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

description = "Klyx's particle API"
