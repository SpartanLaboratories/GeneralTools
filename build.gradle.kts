plugins {
    kotlin("jvm") version "1.8.0"
    `java-library`
    `maven-publish`
}

group = "com.spartanlabs"
version = "1.0.7"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
java{
    withJavadocJar()
    withSourcesJar()
}

publishing{
    publications{
        create<MavenPublication>("generaltools").from(components["java"])
    }
    repositories{
        maven("C:/Users/spartak/Documents/Programming/libraries")
    }
}