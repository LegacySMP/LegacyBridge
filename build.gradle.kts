plugins {
    id("java")
    id("checkstyle")
    id("io.freefair.lombok") version "6.6.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

java.sourceCompatibility = JavaVersion.VERSION_17

group = "me.allinkdev"
version = "1.1.1"
description = "Discord for your legacy server."

repositories {
    maven("https://maven.allink.esixtwo.one/releases")
    mavenLocal()
    mavenCentral()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.6.4-R2.1-SNAPSHOT")
    implementation("net.dv8tion:JDA:5.0.0-beta.8") {
        exclude("opus-java")
    }
    implementation("pro.nocom.legacysmp:LegacyLib:1.1.0")
}

tasks {
    assemble {
        dependsOn(shadowJar, checkstyleMain)
    }
}

bukkit {
    main = "me.allinkdev.legacylib.Main"
    author = "Allink"
    depend = listOf("LegacyLib")
}