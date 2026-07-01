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
            <h3>1.0.6</h3>
            <ul>
                <li>Fixed mouse wheel scrolling when the original editor scrollbar is hidden.</li>
                <li>Added minimap mouse wheel scrolling.</li>
                <li>Improved error and warning marker accuracy and refresh behavior.</li>
                <li>Simplified minimap highlights and settings.</li>
            </ul>

            <h3>1.0.5.1</h3>
            <ul>
                <li>Fixed a Marketplace compatibility verification issue where the plugin archive could not be extracted.</li>
            </ul>

            <h3>1.0.5</h3>
            <ul>
                <li>Improved minimap placement across the IDE.</li>
                <li>Removed the minimap from small dialog input fields while keeping editor, diff, terminal, console, and run log support.</li>
            </ul>

            <h3>1.0.4.1</h3>
            <ul>
                <li>Updated plugin description, acknowledgements, vendor, and privacy information.</li>
            </ul>

            <h3>1.0.4</h3>
            <ul>
                <li>Refined Marketplace description and release metadata.</li>
            </ul>

            <h3>1.0.3</h3>
            <ul>
                <li>Updated compatibility metadata for IntelliJ Platform 2026.1+.</li>
            </ul>

            <h3>1.0.2</h3>
            <ul>
                <li>Reduced package size by removing unused code and placeholder features.</li>
                <li>Updated plugin icons.</li>
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
