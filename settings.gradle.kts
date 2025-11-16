import java.util.Properties

// Lê o arquivo gradle.properties para obter a versão do Loom
val properties = Properties()
file("gradle.properties").inputStream().use { properties.load(it) }
val loomVersion = properties.getProperty("loom_version")

pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
    // Define a versão do plugin aqui, para que o build.gradle.kts a encontre
    plugins {
        id("fabric-loom") version loomVersion
    }
}