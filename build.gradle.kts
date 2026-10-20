import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    // One Kotlin Multiplatform module replaces kotlin("jvm") + java-library (java-library is an error in a
    // multiplatform project on Gradle 8.7+). kotlin-dsl is still not used: this is a library, not a Gradle plugin,
    // and kotlin-dsl's gradleApi() bundles an SLF4J provider that conflicts with logback on the test classpath.
    kotlin("multiplatform") version "2.4.21"
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "io.github.spartanlaboratories"
version = "3.0.0"

repositories {
    mavenCentral()
}

//region Compatibility floors
// The oldest consumers: Kotlin 2.2.0 (MyGameTools) and Kotlin 2.1.20 (MyGameClient via gametools) can read language/API 2.2.
val languageFloor = KotlinVersion.KOTLIN_2_2
val stdlibFloor = "2.2.0"
val jvmFloor = 17
//endregion

kotlin {
    jvmToolchain(jvmFloor)
    // Strict explicit API: a declaration is public only on purpose, because this module exports to
    // three targets. Test source sets are not subject to it.
    explicitApi()

    //region Pins
    // Metadata and API checks follow the floor on every target. The js/wasmJs ABI version still follows this compiler (2.4),
    // which is why web consumers need Kotlin 2.4; coreLibrariesVersion is NOT lowered because the web compilers reject an older stdlib klib.
    compilerOptions {
        languageVersion.set(languageFloor)
        apiVersion.set(languageFloor)
    }
    //endregion

    //region Targets
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.fromTarget(jvmFloor.toString())) }
        // A multiplatform JVM publication declares no org.gradle.jvm.version; declare it so a JVM 11 consumer
        // fails at resolution rather than with UnsupportedClassVersionError.
        attributes { attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, jvmFloor) }
    }
    js { nodejs() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    //endregion

    //region API proof
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
    //endregion

    //region Dependencies by source set
    sourceSets {
        val webStdlib = "org.jetbrains.kotlin:kotlin-stdlib:$coreLibrariesVersion"

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib:$stdlibFloor")
                // Runtime-only on the JVM variant; kotlin-logging's jvm artifact does not bring slf4j - jvmMain's api(slf4j-api) supplies it.
                implementation("io.github.oshai:kotlin-logging:8.0.4")   // exact version, no range; internal use only, never api
            }
        }
        val jsMain by getting { dependencies { api(webStdlib) } }
        val wasmJsMain by getting { dependencies { api(webStdlib) } }
        val jvmMain by getting {
            dependencies {
                // The library only needs the slf4j API - consumers choose their own logging backend.
                api("org.slf4j:slf4j-api:2.0.16")
                implementation("org.apache.directory.studio:org.apache.commons.io:2.4")   // Files Utility
            }
        }
        val commonTest by getting { dependencies { implementation(kotlin("test")) } }
        val jvmTest by getting {
            // Logback is only needed to actually see log output while running the JVM tests.
            dependencies { implementation("ch.qos.logback:logback-classic:1.5.13") }
        }
    }
    //endregion
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }   // JVM tests run on the JUnit 5 platform

//region -PtestLevel
val testLevels = listOf("gating", "component", "integration", "deterministic", "e2e", "nonfunctional", "uat")

/** Parses `-PtestLevel`; blank entries are ignored, duplicates dropped, an unknown level is the failure. */
fun parseTestLevels(raw: String?, known: List<String>): Result<List<String>> {
    val requested = raw.orEmpty().split(',').map(String::trim).filter(String::isNotEmpty).distinct()
    val unknown = requested.filterNot(known::contains)
    return if (unknown.isEmpty()) Result.success(requested)
    else Result.failure(IllegalArgumentException("Unknown -PtestLevel $unknown; expected a comma-separated subset of $known"))
}

val selectedTestLevels: List<String> =
    parseTestLevels(providers.gradleProperty("testLevel").orNull, testLevels)
        .getOrElse { throw GradleException(it.message ?: "Invalid -PtestLevel", it) }   // the one place the Result is unwrapped

if (selectedTestLevels.isNotEmpty()) logger.lifecycle("Test levels selected: $selectedTestLevels")

tasks.withType<AbstractTestTask>().configureEach {
    if (selectedTestLevels.isNotEmpty()) {
        filter.isFailOnNoMatchingTests = false   // a level with no tests on a target is a green no-op
        selectedTestLevels.forEach { level -> filter.includeTestsMatching("com.spartanlabs.testing.$level.*") }
    }
}
//endregion

mavenPublishing {
    publishToMavenCentral()                     // upload only; releasing is the maintainer's explicit step
    signAllPublications()
    // The artifactId must equal rootProject.name (settings.gradle.kts): vanniktech rewrites the target artifact ids from it.
    coordinates(project.group.toString(), "GeneralTools", project.version.toString())
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty(), sourcesJar = SourcesJar.Sources()))

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
