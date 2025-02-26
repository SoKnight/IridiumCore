plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.iridium"
version = "1.6.6"
description = "IridiumCore"

allprojects {
    apply(plugin = "java")

    java.sourceCompatibility = JavaVersion.VERSION_1_8

    repositories {
        mavenCentral()
        maven {
            credentials {
                username = project.property("nexusUsername").toString()
                password = project.property("nexusPassword").toString()
            }
            url = uri("https://repo.soknight.me/repository/releases/")
        }
        maven("https://nexus.iridiumdevelopment.net/repository/maven-releases/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    dependencies {
        // Dependencies that we want to shade in
        implementation("org.jetbrains:annotations:23.0.0")
        implementation("com.github.cryptomorin:XSeries:8.7.0")

        // Other dependencies that are not required or already available at runtime
        compileOnly("me.soknight.advancedskins:api:2.1.0")
        compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
        compileOnly("org.projectlombok:lombok:1.18.22")

        // Enable lombok annotation processing
        annotationProcessor("org.projectlombok:lombok:1.18.22")
    }
}

dependencies {
    // Shade all the subprojects into the jar
    subprojects.forEach { implementation(it) }
}

tasks {
    jar {
        dependsOn("shadowJar")
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")
        relocate("de.tr7zw.changeme.nbtapi", "com.iridium.iridiumcore.dependencies.nbtapi")
        relocate("com.iridium.iridiumcolorapi", "com.iridium.iridiumcore.dependencies.iridiumcolorapi")
        relocate("org.yaml.snakeyaml", "com.iridium.iridiumcore.dependencies.snakeyaml")
        relocate("io.papermc.lib", "com.iridium.iridiumcore.dependencies.paperlib")
        relocate("com.cryptomorin.xseries", "com.iridium.iridiumcore.dependencies.xseries")
        relocate("com.fasterxml.jackson", "com.iridium.iridiumcore.dependencies.fasterxml")
    }

    compileJava {
        options.encoding = "UTF-8"
    }
}

// Maven publishing to SoKnight's Nexus repository
publishing {
    publications.create<MavenPublication>("mavenJava") {
        artifactId = "iridiumcore"

        // Using compiled JARs instead of new publication creating
        artifact(tasks["shadowJar"])
    }

    repositories {
        maven {
            name = "nexus"
            url = uri("https://repo.soknight.me/repository/releases/")
            credentials {
                username = project.property("nexusUsername").toString()
                password = project.property("nexusPassword").toString()
            }
        }
    }
}