plugins {
    java
    application
}

application.mainClass = "top.modpotato.Main"

group = "top.modpotato"
version = "0.3"

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
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.14.0")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
}