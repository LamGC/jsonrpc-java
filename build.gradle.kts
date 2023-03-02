plugins {
    `java-library`
    `maven-publish`
    jacoco
    signing
}

group = "net.lamgc"
version = "0.1.0-RC1"

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

tasks.withType<Javadoc> {
    options {
        encoding = "UTF-8"
    }
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.test {
    useJUnitPlatform()
}

// We support "reproducible builds" to ensure that everyone can review and trust this project!
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

publishing {
    repositories {
        maven("https://git.lamgc.me/api/packages/LamGC/maven") {
            credentials {
                username = project.properties["repo.credentials.self-git.username"].toString()
                password = project.properties["repo.credentials.self-git.password"].toString()
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("jsonrpc-java")
                description.set("Simple and flexible JsonRpc library.")
                url.set("https://github.com/LamGC/jsonrpc-java")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("LamGC")
                        name.set("LamGC")
                        email.set("lam827@lamgc.net")
                        url.set("https://github.com/LamGC")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/LamGC/jsonrpc-java.git")
                    developerConnection.set("scm:git:https://github.com/LamGC/jsonrpc-java.git")
                    url.set("https://github.com/LamGC/jsonrpc-java")
                }
                issueManagement {
                    url.set("https://github.com/LamGC/jsonrpc-java/issues")
                    system.set("Github Issues")
                }
            }
        }
    }

}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}