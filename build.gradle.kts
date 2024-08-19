import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val bcryptVersion = "0.10.2"
val discord4JVersion = "3.2.6"
val mockitoVersion = "5.5.0"
val kotlinMockitoVersion = "5.0.0"

plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("org.sonarqube") version "4.4.1.3373"
    jacoco
}

sonar {
    properties {
        property("sonar.projectKey", "ShockJake_kotlin-library-backend")
        property("sonar.organization", "shockjake")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

group = "university.com"
version = "0.0.1"

application {
    mainClass.set("university.com.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ktor {
    fatJar {
        archiveFileName.set("kotlin-library.jar")
    }
}

repositories {
    mavenCentral()
}

configurations {
    val integrationTestImplementation by creating {
        extendsFrom(configurations["testImplementation"])
    }
    val integrationTestRuntimeOnly by creating {
        extendsFrom(configurations["testRuntimeOnly"])
    }
}

dependencies {
    implementation("at.favre.lib:bcrypt:$bcryptVersion")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-apache-jvm")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-host-common-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-jackson-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.discord4j:discord4j-core:$discord4JVersion")
    implementation("io.ktor:ktor-client-cio-jvm")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$kotlinMockitoVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
    testImplementation("io.ktor:ktor-client-mock")
}

sourceSets {
    val integrationTest by creating {
        kotlin.srcDir("src/integrationTest/kotlin")
        resources.srcDir("src/integrationTest/resources")
        compileClasspath += sourceSets["main"].output + sourceSets["test"].output
        runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath

    shouldRunAfter("test")
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.named("integrationTest"))
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test, tasks.named("integrationTest"))
    reports {
        xml.required = true
        html.required = true
    }
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.JSON)
        reporter(ReporterType.HTML)
    }
}
