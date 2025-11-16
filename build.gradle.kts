import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import net.fabricmc.loom.task.RemapJarTask

// 1. Declarar propriedades do gradle.properties de forma segura
val minecraft_version: String by project
val loader_version: String by project
val fabric_version: String by project
val mod_version: String by project
val maven_group: String by project
val archives_base_name: String by project
val modmenu_version: String by project
val cloth_config_version: String by project
val loom_version: String by project

plugins {
    id("fabric-loom")
    `maven-publish`
}

version = mod_version
group = maven_group

base {
    archivesName.set(archives_base_name)
}

repositories {
    maven { url = uri("https://maven.shedaniel.me/") }
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
}

loom {
    splitEnvironmentSourceSets()

    mods {
        create("barium") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }
}

dependencies {
    // Jogo e Mapeamentos
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())

    // Fabric
    modImplementation("net.fabricmc:fabric-loader:$loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    // Dependências do Mod
    modImplementation("com.terraformersmc:modmenu:$modmenu_version")
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:$cloth_config_version")
}

tasks.processResources {
    inputs.property("version", mod_version)
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to mod_version))
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_$archives_base_name" }
    }
}