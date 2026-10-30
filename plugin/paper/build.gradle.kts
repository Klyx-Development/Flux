import sun.jvmstat.monitor.MonitoredVmUtil.jvmArgs

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

java {
    disableAutoTargetJvm()
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

dependencies {
    compileOnly(libs.paper.api)
    implementation(project(":platform-paper"))
}

tasks {
    jar {
        enabled = false
    }

    test {
        useJUnitPlatform()
    }

    shadowJar {
        archiveFileName = "${rootProject.name}-test-${project.version}.jar"
        archiveClassifier = null

        from(project(":platform-paper").sourceSets.main.get().output)
        from(project(":common").sourceSets.main.get().output)

        manifest {
            attributes["Implementation-Version"] = rootProject.version
        }
    }

    assemble {
        dependsOn(shadowJar)
    }

    // The example blueprints ship inside the demo and are copied to the plugin folder on first run.
    processResources {
        from(rootProject.file("examples/effects")) {
            into("effects")
        }
        from(rootProject.file("examples/snowstorm")) {
            into("snowstorm")
        }
        from(rootProject.file("examples/emitters")) {
            into("emitters")
        }
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 25
    }

    withType<Javadoc>() {
        options.encoding = Charsets.UTF_8.name()
    }

    val version = "26.2"
    val javaVersion = JavaLanguageVersion.of(25)

    val jvmArgsExternal = listOf(
        "-Dcom.mojang.eula.agree=true"
    )

    runServer {
        minecraftVersion(version)
        runDirectory = rootDir.resolve("run/paper/$version")

        javaLauncher = project.javaToolchains.launcherFor {
            languageVersion = javaVersion
        }

        jvmArgs = jvmArgsExternal
    }
}
