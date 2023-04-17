plugins {
    id("java")
    id("checkstyle")
    id("io.freefair.lombok") version "6.6.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

java.sourceCompatibility = JavaVersion.VERSION_17

group = "me.allinkdev"
version = "1.0-SNAPSHOT"
description = "Discord for your Beta server."

repositories {
    maven("https://maven.allink.esixtwo.one/snapshots")
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("org.bukkit:asclepius:1.1.7")
    implementation("net.dv8tion:JDA:5.0.0-beta.3") {
        exclude("opus-java")
    }
    implementation("pro.nocom.legacysmp:LegacyLib:1.1.0-SNAPSHOT")
}

tasks {
    assemble {
        dependsOn(shadowJar, checkstyleMain)
    }
}

bukkit {
    main = "me.allinkdev.betabridge.Main"
    author = "Allink"
    depend = listOf("LegacyLib")
}