plugins {
    java
    application
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

application.mainClass = "top.modpotato.Main"

group = project.property("group").toString()
version = project.property("version").toString()
description = project.property("description").toString()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${project.property("paperVersion")}")
    
    // compileOnly("dev.folia:folia-api:${project.property("foliaVersion")}")
    
    compileOnly("net.kyori:adventure-api:${project.property("adventureVersion")}")
}

tasks {
    jar {
        from("LICENSE") {
            rename { "${it}_${project.name}" }
        }
    }
    
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }
    
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("plugin.yml") {
            expand(
                "version" to project.version,
                "name" to project.name,
                "description" to project.description,
                "author" to project.property("author")
            )
        }
    }
    
    runServer {
        minecraftVersion("1.21.4")
    }
}