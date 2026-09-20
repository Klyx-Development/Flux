import org.gradle.kotlin.dsl.*

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.paperweight.userdev)
}

java {
    disableAutoTargetJvm()
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

dependencies {
    api(project(":common"))
    api(libs.exo.paper)
    compileOnly(libs.paper.api)
    paperweight.paperDevBundle(libs.versions.paper.api.get())
}

tasks {
    test {
        useJUnitPlatform()
    }

    jar {
        archiveFileName = "${rootProject.name}-${project.version}.jar"

        manifest {
            attributes["Implementation-Version"] = rootProject.version
        }
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
    }

    withType<Javadoc>() {
        options.encoding = Charsets.UTF_8.name()
    }

    defaultTasks("build")
}

publishing {
    repositories {
        maven {
            name = "klyxReleases"
            url = uri("https://repo.klyx.org/releases")
            credentials {
                username = System.getProperty("KLYX_PRIVATE_USERNAME") ?: System.getenv("KLYX_PRIVATE_USERNAME")
                password = System.getProperty("KLYX_PRIVATE_PASSWORD") ?: System.getenv("KLYX_PRIVATE_PASSWORD")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.klyx.flux"
            artifactId = "paper"
            version = rootProject.version.toString()
            from(components["java"])
        }
    }
}
