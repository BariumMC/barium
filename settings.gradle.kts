pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
    
    plugins {
        // A lógica de leitura do arquivo de propriedades foi movida para DENTRO deste bloco.
        val properties = java.util.Properties()
        file("gradle.properties").inputStream().use { properties.load(it) }
        val loomVersion = properties.getProperty("loom_version")

        // Agora, 'loomVersion' está no escopo correto e será encontrado.
        id("fabric-loom") version loomVersion
    }
}