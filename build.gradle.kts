plugins {
    `java-library`
    `maven-publish`
    jacoco
}

group = "net.lamgc"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()

}

dependencies {
    api("com.google.code.gson:gson:2.10.1")
    implementation("org.slf4j:slf4j-api:2.0.6")

    testImplementation("org.example.testing:testing-named-parameters:1.0.0") {
        repositories {
            maven("https://git.lamgc.me/api/packages/LamGC/maven")
        }
    }
    testImplementation("org.example.testing:testing-not-named-parameters:1.0.0") {
        repositories {
            maven("https://git.lamgc.me/api/packages/LamGC/maven")
        }
    }
    testRuntimeOnly("ch.qos.logback:logback-classic:1.3.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.mockito:mockito-core:5.1.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
    options.release.set(11)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}