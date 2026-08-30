plugins {
    // kotlin-dsl is for projects that write Gradle plugins/build logic - it implicitly pulls in
    // gradleApi(), which bundles Gradle's own embedded SLF4J provider and conflicts with
    // logback-classic on the test classpath. This is a general-purpose library, not a Gradle
    // plugin, so it uses the plain Kotlin JVM plugin instead.
    // NOTE: pick the Kotlin version your codebase actually needs (e.g. to support the
    // "UnionTypes" language feature below) - 2.1.0 is a placeholder.
    kotlin("jvm") version "2.2.0"
    `java-library`
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "io.github.spartanlaboratories"

repositories {
    mavenCentral()
}

dependencies {
    // The library only needs the slf4j API - consumers choose their own logging backend.
    api("org.slf4j:slf4j-api:2.0.16")
    implementation("org.apache.directory.studio:org.apache.commons.io:2.4")          // Files Utility

    // Logback is only needed to actually see log output while running the test suite.
    testApi(kotlin("test"))
    testImplementation("ch.qos.logback:logback-classic:1.5.13")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates("io.github.spartanlaboratories", "GeneralTools", "2.0.1")

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