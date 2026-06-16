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
                <li>Restored full acknowledgement links for CodeGlance Pro, the project that inspired CodeLens Pro.</li>
                <li>Added a clear no-ads and privacy statement: CodeLens Pro does not display ads, sponsored content, affiliate links, tracking, telemetry, or promotional popups.</li>
                <li>Confirmed that the plugin works locally inside the IDE and does not collect, transmit, or store source code, project files, telemetry, or personal data.</li>
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
