plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.20.1"
}

group = "io.github.tofagerl"
version = providers.gradleProperty("pluginVersion").getOrElse("0.1.0")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        val localIde = providers.gradleProperty("localIde").orNull
        if (localIde != null) local(localIde) else intellijIdea("2026.3")
    }
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(25)
}

intellijPlatform {
    pluginVerification {
        ides {
            val localIde = providers.gradleProperty("localIde").orNull
            if (localIde != null) local(localIde) else create(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdea, "2026.3")
        }
    }
    pluginConfiguration {
        id = "io.github.tofagerl.tasks"
        name = "Tasks"
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "263"
            untilBuild = provider { null }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
