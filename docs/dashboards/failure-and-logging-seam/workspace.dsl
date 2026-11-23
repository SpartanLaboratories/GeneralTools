// failure-and-logging-seam — C4 model as code (Structurizr DSL).
// Source of truth for the structure shown on docs/dashboards/failure-and-logging-seam/. The board's C4 tab is rendered from this file.
// As-is at GeneralTools feat/issue-7-kotlin-multiplatform a500e44 (2026-11-22), version 3.0.0, unreleased. Code is authoritative on any conflict.
workspace "GeneralTools" "A Kotlin Multiplatform library of generic helpers: Color, 2D geometry and small utilities in common code for the JVM, JS and Wasm/JS, with file, URL and desktop helpers on the JVM." {

    !identifiers hierarchical

    model {
        consumer  = person "Consumer" "A Kotlin project on the JVM, JS or Wasm/JS that calls the library and configures its own logging."
        klogging  = softwareSystem "kotlin-logging" "io.github.oshai:kotlin-logging 8.0.4, the multiplatform logging facade the common code logs through; an implementation dependency." "External"
        backend   = softwareSystem "Logging backend" "The consumer's slf4j binding on the JVM; the console at one global level on JS and Wasm/JS." "External"

        generaltools = softwareSystem "GeneralTools" "A Kotlin Multiplatform library of generic helpers, built and published as one artifact." {
            failureSeam = container "Failure and logging seam" "One logging API for every file and one way to report an expected failure: one WARN line and a Result.failure." "Kotlin Multiplatform · common code · internal" {

                // ---- internals (the seam has no public surface) ----
                helper     = component "KLogger.failure" "internal fun <T> KLogger.failure(message, detail): one lazy WARN line on the receiver logger, then Result.failure(IllegalArgumentException(message))." "internal extension function · Failure.kt" "Internal"
                convention = component "Logger convention" "private val log = KotlinLogging.logger(name) in six common files, each named com.spartanlabs.<package>.<File> as in 2.x." "per-file private val" "Internal"
                pin        = component "kotlin-logging 8.0.4" "implementation(io.github.oshai:kotlin-logging:8.0.4) in commonMain: an exact version, never api." "build dependency · build.gradle.kts" "Internal"

                // ---- planned, not built (drawn dashed and faded; only Planned is dashed) ----
                pinNext    = component "kotlin-logging 8.0.7" "The one kotlin-logging version moved from 8.0.4 to 8.0.7, still implementation only." "planned dependency" "Internal,Planned"
                jvmFiles   = component "JVM files on the convention" "MiscJvm.kt and UserActions.kt obtain kotlin-logging loggers of the same names, each call in the lazy form, levels and texts unchanged." "planned" "Internal,Planned"
            }
            valueCore   = container "Geometry value core" "TwoDoubles, Point and Dimensions." "Kotlin Multiplatform · common code"
            queries     = container "Geometry queries" "Vector algebra, Segment, Ray and the intersection tests." "Kotlin Multiplatform · common code"
            misc        = container "Misc helpers" "The small helpers on the facade MiscKt." "Kotlin Multiplatform · common code and JVM"
            desktopEdge = container "Desktop edge" "Screenshots, the browser and image files, on the JVM." "Kotlin/JVM"
            testSuite   = container "Test suite" "The tests by named test set, in commonTest and jvmTest." "Kotlin Multiplatform · tests"
            gradleBuild = container "Gradle build" "Builds, tests, checks and publishes the library." "Gradle 9.0.0 · Kotlin 2.4.21" {
                moduleLayout = component "Module layout and published shape" "GeneralTools-core, GeneralTools-desktop and the source-less GeneralTools umbrella." "planned" "Internal,Planned"
            }
        }

        // People and systems
        consumer -> generaltools.queries "Calls a geometry function that can fail"
        consumer -> generaltools.valueCore "Calls divideBy or distanceFrom"
        consumer -> backend "Configures levels and sinks"

        // The failure sites
        generaltools.queries -> generaltools.failureSeam.helper "Reports 10 failure sites through"
        generaltools.valueCore -> generaltools.failureSeam.helper "Reports divideBy and distanceFrom through"
        generaltools.queries -> generaltools.failureSeam.convention "Three files hold a logger"
        generaltools.valueCore -> generaltools.failureSeam.convention "TwoDoubles.kt and Point.kt hold a logger"
        generaltools.misc -> generaltools.failureSeam.convention "Misc.kt holds a logger"

        // Inside the seam
        generaltools.failureSeam.helper -> klogging "warn { } on the calling file's KLogger"
        generaltools.failureSeam.convention -> klogging "KotlinLogging.logger(name)"
        generaltools.failureSeam.pin -> klogging "Pins 8.0.4, implementation"
        klogging -> backend "JVM: slf4j under the logger name; JS and Wasm/JS: the console"

        // Tests
        generaltools.testSuite -> generaltools.failureSeam.helper "FailureTest; log capture through logback"

        // Planned
        generaltools.failureSeam.pinNext -> klogging "Would pin 8.0.7"
        generaltools.misc -> generaltools.failureSeam.jvmFiles "MiscJvm.kt would log through it"
        generaltools.desktopEdge -> generaltools.failureSeam.jvmFiles "UserActions.kt would log through it"
        generaltools.gradleBuild.moduleLayout -> generaltools.failureSeam "Would place Failure.kt in GeneralTools-core"
    }

    views {
        systemContext generaltools "Context" "GeneralTools and who touches it." {
            include *
            autoLayout lr
        }

        container generaltools "Containers" "The systems of GeneralTools that touch the seam." {
            include *
            autoLayout lr
        }

        component generaltools.failureSeam "Components" "The failure and logging seam as built at a500e44; its planned parts drawn dashed." {
            include *
            autoLayout tb
        }

        dynamic generaltools.failureSeam "FailureReport" "An expected failure reported, as built." {
            consumer -> generaltools.queries "segmentIntersectsSegment(s1, s2) with a NaN coordinate"
            generaltools.queries -> generaltools.failureSeam.helper "log.failure(message) { detail }: Segments must have finite coordinates"
            generaltools.failureSeam.helper -> klogging "warn { message: detail }, the detail evaluated only when WARN is enabled"
            klogging -> backend "one WARN event on com.spartanlabs.geometry.Intersections, no throwable"
            generaltools.failureSeam.helper -> generaltools.queries "Result.failure(IllegalArgumentException(message))"
            generaltools.queries -> consumer "the failed Result, unchanged"
            autoLayout lr
        }

        styles {
            element "Person"          { shape person  background #08427b color #ffffff }
            element "Software System" { background #1168bd color #ffffff }
            element "External"        { background #999999 color #ffffff }
            element "Container"       { background #438dd5 color #ffffff }
            element "Component"       { background #85bbf0 color #000000 }
            element "Internal"        { background #c6dcf5 color #000000 }
            element "Standalone"      { background #eeeeee color #000000 border dotted }
            element "Planned"         { background #ffffff color #666666 border dashed opacity 60 }
        }
    }
}
