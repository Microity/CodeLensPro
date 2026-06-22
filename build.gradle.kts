plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        local(
            providers.gradleProperty("localIdePath")
                .orElse(providers.environmentVariable("CODELENS_PRO_IDE_PATH"))
        )
        bundledPlugin("com.intellij.java")
    }
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    pluginConfiguration {
        id = "com.codelens.pro"
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        changeNotes = """
            <ul>
                <li>Limited the minimap to appropriate full editor surfaces so it no longer appears in dialog-backed input fields such as new branch name fields.</li>
                <li>Kept minimap support for main editors, diff editors, terminals, consoles, and run output logs.</li>
            </ul>
        """.trimIndent()
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
