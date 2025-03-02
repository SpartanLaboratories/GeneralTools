plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    `java-library`
    `maven-publish`
}
group = "com.spartanlabs"
version = "1.1.0"

repositories {
    mavenCentral()
}
dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(project(":utils"))
    testImplementation(kotlin("test"))
    api("ch.qos.logback:logback-classic:1.3.0-alpha13")
    implementation("org.apache.directory.studio:org.apache.commons.io:2.4")          // Files Utility
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(23)
}
java{
    withJavadocJar()
    withSourcesJar()
}

publishing{
    publications{
        create<MavenPublication>("generaltools").from(components["java"])
        create<MavenPublication>("generaltools-snapshot"){
            version = "LATEST"
        }.from(components["java"])
    }
    repositories{
        maven("D:/Documents/Programming")
        /*
        maven{
            name = "generaltools"
            url= uri("https://maven.pkg.github.com/Spartan-Laboratories/GeneralTools")
            credentials{
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
         */
    }
}