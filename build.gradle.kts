plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    application
}

group = "pe.upeu.ancajima"
version = "1.0.0"

repositories { mavenCentral() }

val ktorVersion = "3.0.3"
val exposedVersion = "0.57.0"

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.zaxxer:HikariCP:6.2.1")
    runtimeOnly("com.h2database:h2:2.3.232")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("ch.qos.logback:logback-classic:1.5.15")
    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
}

application { mainClass.set("pe.upeu.agrosierra.AplicacionKt") }
kotlin { jvmToolchain(21) }
tasks.test { useJUnitPlatform() }

