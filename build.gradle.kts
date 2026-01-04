plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.axpx.algotrading"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    // LMAX Disruptor
    implementation("com.lmax:disruptor:4.0.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Metrics (optional but recommended for algo trading)
    implementation("io.dropwizard.metrics:metrics-core:4.2.21")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
}

application {
    mainClass.set("com.algotrading.Main")
}

tasks.test {
    useJUnitPlatform()

    // Performance testing needs more heap
    maxHeapSize = "2g"

    // JVM args for low-latency testing
    jvmArgs = listOf(
        "-XX:+UseZGC",              // ZGC for low pause times
        "-XX:+AlwaysPreTouch",      // Touch memory upfront
        "-Xms1g",
        "-Xmx2g"
    )
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf(
        "-Xlint:unchecked",
        "-Xlint:deprecation"
    ))
}

// Shadow JAR for deployment
tasks.shadowJar {
    archiveBaseName.set("algo-trading-platform")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())

    manifest {
        attributes["Main-Class"] = "io.github.axpx.algotrading.Main"
    }
}