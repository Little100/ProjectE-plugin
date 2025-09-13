plugins {
    java
    id("com.diffplug.spotless") version "7.2.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "org.Little_100"
version = "1.0.3-SNAPSHOT"

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
    runServer {
        minecraftVersion("1.21")
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"

        if (JavaVersion.current().isJava10Compatible) {
            options.release.set(21)
        }
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(sourceSets.main.get().resources.srcDirs) {
            include("**/*.yml")
            expand(
                "version" to version
            )
        }
        from(sourceSets.main.get().resources.srcDirs) {
            include("**/*.zip")
        }
        from(sourceSets.main.get().resources.srcDirs) {
            include("**/plugin.yml")
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
    val javaVersion = JavaVersion.toVersion(21)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}