plugins {
    `kotlin-dsl`
    `java-library`
    `maven-publish`
}
group = "com.spartanlabs"
version = "1.2.0b"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    api("ch.qos.logback:logback-classic:1.5.13")
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