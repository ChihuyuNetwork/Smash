plugins {
    kotlin("jvm") version "1.8.10"
    id("com.github.johnrengelman.shadow") version "8.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    `maven-publish`
    `kotlin-dsl`
}

group = "love.chihuyu"
version = "1.0.0"
val pluginVersion: String by project.ext

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.github.paperspigot:paperspigot-api:1.8.8-R0.1-SNAPSHOT")
    implementation("org.yaml:snakeyaml:2.0")
    implementation(kotlin("stdlib"))
}

ktlint {
    ignoreFailures.set(true)
    disabledRules.add("no-wildcard-imports")
}

tasks {
    test {
        useJUnitPlatform()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(sourceSets.main.get().resources.srcDirs) {
            filter(org.apache.tools.ant.filters.ReplaceTokens::class, mapOf("tokens" to mapOf(
                "version" to project.version.toString(),
                "name" to project.name,
                "mainPackage" to "love.chihuyu.${project.name.lowercase()}.${project.name}Plugin"
            )))
            filteringCharset = "UTF-8"
        }
    }

    shadowJar {
        val loweredProject = project.name.lowercase()
        exclude("org/slf4j/**")
        relocate("org.snakeyaml", "love.chihuyu.$loweredProject.lib.org.snakeyaml")
        relocate("kotlin", "love.chihuyu.$loweredProject.lib.kotlin")
    }
}

publishing {
    repositories {
        maven {
            name = "repo"
            credentials(PasswordCredentials::class)
            url = uri("https://repo.hirosuke.me/repository/maven-central/")
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

kotlin {
    jvmToolchain(8)
}

open class SetupTask : DefaultTask() {

    @TaskAction
    fun action() {
        val projectDir = project.projectDir
        projectDir.resolve("renovate.json").deleteOnExit()
        val srcDir = projectDir.resolve("src/main/kotlin/love/chihuyu/${project.name.lowercase()}").apply(File::mkdirs)
        srcDir.resolve("${project.name}Plugin.kt").writeText(
            """
                package love.chihuyu.${project.name.lowercase()}
                
                import org.bukkit.plugin.java.JavaPlugin

                class ${project.name}Plugin: JavaPlugin() {
                    companion object {
                        lateinit var ${project.name}Plugin: JavaPlugin
                    }
                
                    init {
                        ${project.name}Plugin = this
                    }
                }
            """.trimIndent()
        )
    }
}

task<SetupTask>("setup")