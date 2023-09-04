plugins {
    kotlin("jvm") version "1.8.0"
    `java-library`
    `maven-publish`
}

group = "com.spartanlabs"
version = "1.0.12"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    api("ch.qos.logback:logback-classic:1.3.0-alpha13")
    implementation("org.apache.directory.studio:org.apache.commons.io:2.4")          // Files Utility
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
        //create<MavenPublication>("generaltools").from(components["java"])
        create<MavenPublication>("generaltools-snapshot"){
            version = "LATEST"
        }.from(components["java"])
    }
    repositories{
        maven("C:/Users/spartak/Documents/Programming/libraries")
        maven{
            name = "GeneralTools"
            url= uri("https://maven.pkg.github.com/SpartanLabs/GeneralTools")
            credentials{
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}