plugins {
    `kotlin-dsl`
    `java-library`
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "io.github.spartanlaboratories"

repositories {
    mavenCentral()
}

dependencies {
    testApi(kotlin("test"))
    api("ch.qos.logback:logback-classic:1.5.13")
    implementation("org.apache.directory.studio:org.apache.commons.io:2.4")          // Files Utility
}

kotlin {
    jvmToolchain(23)
    sourceSets.all {
        languageSettings.enableLanguageFeature("UnionTypes")
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.spartanlaboratories", "GeneralTools", "1.0.6")

    pom {
        name.set("General Tools")
        description.set("A set of generic functions.")
        inceptionYear.set("2026")
        url.set("https://github.com/SpartanLaboratories/GeneralTools")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("SpaSinghOut")
                name.set("Spartak Singh")
                url.set("https://github.com/SpaSinghOut")
            }
        }
        scm {
            url.set("https://github.com/SpartanLaboratories/GeneralTools/")
            connection.set("scm:git:git://github.com/SpartanLaboratories/GeneralTools.git")
            developerConnection.set("scm:git:ssh://git@github.com/SpartanLaboratories/GeneralTools.git")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}