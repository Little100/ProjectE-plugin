plugins {
    java
    id("com.diffplug.spotless") version "6.25.0"
}

group = "org.Little_100"
version = properties["version"]!!

repositories {
    mavenCentral()
    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.opencollab.dev/main/")
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.geysermc.geyser:api:2.8.3-SNAPSHOT")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
}

spotless {
    java {
        palantirJavaFormat()
        removeUnusedImports()
    }

    isEnforceCheck = false
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"

        options.release.set(21)
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(sourceSets.main.get().resources.srcDirs) {
            include("**/*.yml")
            include("*.yml")
            include("pack/*.zip")

            filesMatching("plugin.yml") {
                expand(
                    "version" to version
                )
            }
        }
    }

    register("verifyResourcesExist") {
        doLast {
            val buildDirPath = layout.buildDirectory.dir("resources/main").get().asFile
            val requiredFiles = listOf(
                "lang/en_us.yml",
                "lang/zh_cn.yml",
                "config.yml",
                "plugin.yml",
                "pack/ProjectE Resourcepack.zip"
            )

            requiredFiles.forEach { fileName ->
                val resourceFile = File(buildDirPath, fileName)
                if (!resourceFile.exists()) {
                    throw GradleException("Required file '$fileName' does not exist in the output resources!")
                } else {
                    println("$fileName exists in the output resources")
                }
            }
        }
    }

    processResources {
        finalizedBy("verifyResourcesExist")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}