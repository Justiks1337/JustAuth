plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21
java.toolchain.languageVersion = JavaLanguageVersion.of(21)
group = "org.galerka_auth.justauth"
description = ":pig:"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.32")
    compileOnly("fr.xephi:authme:5.6.0-SNAPSHOT")
    
    implementation("org.telegram:telegrambots-longpolling:7.2.1")
    implementation("org.telegram:telegrambots-client:7.2.1")
    implementation("org.telegram:telegrambots-meta:7.2.1")

    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-g", "-parameters"))
    options.release = 21
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    filesMatching("plugin.yml") {
        expand(inputs.properties)
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    destinationDirectory = file("/home/max/Рабочий стол/")
}

tasks.runServer {
    minecraftVersion("1.20.2")
    jvmArgs("-Xms512M -Xmx512M --add-modules=jdk.incubator.vector -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20".split(" "))
}
