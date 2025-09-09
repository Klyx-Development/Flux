plugins {
    alias(libs.plugins.paperweight.userdev)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.api.get())
    implementation(project(":api"))
    implementation(project(":common"))
    compileOnly(libs.paper.api)
}