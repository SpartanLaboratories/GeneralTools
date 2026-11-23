// misc-helpers — C4 model as code (Structurizr DSL).
// Source of truth for the structure shown on docs/dashboards/misc-helpers/. The board's C4 tab is rendered from this file.
// As-is at GeneralTools feat/issue-7-kotlin-multiplatform a500e44 (2026-11-22), version 3.0.0, unreleased. Code is authoritative on any conflict.
workspace "GeneralTools" "A Kotlin Multiplatform library of generic helpers: Color, 2D geometry and small utilities in common code for the JVM, JS and Wasm/JS, with file, URL and desktop helpers on the JVM." {

    !identifiers hierarchical

    model {
        consumer = person "Consumer" "A Kotlin project that calls the helpers: from common code on the JVM, JS or Wasm/JS, the JVM-only ones from a JVM source set."
        slf4j    = softwareSystem "slf4j" "org.slf4j:slf4j-api 2.0.16, an api dependency of the JVM variant; the consumer binds the backend." "External"
        files    = softwareSystem "File system" "The local files read reads." "External"

        generaltools = softwareSystem "GeneralTools" "A Kotlin Multiplatform library of generic helpers, built and published as one artifact." {
            misc = container "Misc helpers" "The small general helpers on the one JVM facade MiscKt, split by what they need from the platform." "Kotlin Multiplatform · common code and JVM · 3.0.0" {

                // ---- public surface, common code (Misc.kt) ----
                strings = component "String helpers" "capitalizeEveryWord, locale-free; trimIfLongerThan, a negative length refused on every platform." "top-level functions · Misc.kt" "Public"
                lists   = component "Collection helpers" "evaluateList(list, validator), stopping at the first match; forEvery and forEveryIndexed, infix forms of forEach." "top-level functions · Misc.kt" "Public"
                timing  = component "profile" "Runs an action and returns <name> took <n>ms, measured with measureTime." "top-level function · Misc.kt" "Public"
                results = component "onException" "Result.onException(vararg exceptions, transform): recovers a failure whose exception's class is one listed." "public inline function · Misc.kt" "Public"

                // ---- public surface, JVM (MiscJvm.kt) ----
                asUrl   = component "asURL" "String.asURL(): java.net.URL; parses only, opens no connection; MalformedURLException on bad text." "top-level function · MiscJvm.kt" "Public"
                readFn  = component "read" "read(textFile): every line of a text file, the file closed before it returns." "top-level function · MiscJvm.kt" "Public"
                time    = component "Logger.time" "Logger.time(actionName, action): logs profile's text at INFO on the caller's slf4j Logger." "extension function · MiscJvm.kt" "Public"

                // ---- internals ----
                commonLog = component "Misc logger, common" "private val log = KotlinLogging.logger(com.spartanlabs.generaltools.Misc): TRACE in evaluateList." "private val · Misc.kt" "Internal"
                jvmLog    = component "Misc logger, JVM" "private val log = LoggerFactory.getLogger(com.spartanlabs.generaltools.Misc): DEBUG in asURL and read." "private val · MiscJvm.kt" "Internal"

                // ---- planned, not built (drawn dashed and faded; only Planned is dashed) ----
                jvmLogNext = component "Misc logger, JVM, on kotlin-logging" "The JVM file's logger obtained from kotlin-logging under the same name, each call in the lazy form; Logger.time keeps its slf4j receiver." "planned" "Internal,Planned"
            }
            failureSeam = container "Failure and logging seam" "One logging API for every file and one way to report an expected failure." "Kotlin Multiplatform · common code · internal"
            testSuite   = container "Test suite" "The tests by named test set, in commonTest and jvmTest." "Kotlin Multiplatform · tests"
            gradleBuild = container "Gradle build" "Builds, tests, checks and publishes the library." "Gradle 9.0.0 · Kotlin 2.4.21" {
                moduleLayout = component "Module layout and published shape" "GeneralTools-core, GeneralTools-desktop and the source-less GeneralTools umbrella." "planned" "Internal,Planned"
            }
        }

        // People and systems
        consumer -> generaltools.misc "Calls the helpers on MiscKt"
        consumer -> generaltools.misc.strings "capitalizeEveryWord, trimIfLongerThan"
        consumer -> generaltools.misc.lists "evaluateList with a lambda"
        consumer -> generaltools.misc.results "runCatching { } .onException(…)"
        consumer -> generaltools.misc.time "logger.time(name) { action }"
        consumer -> generaltools.misc.readFn "read(path)"
        consumer -> generaltools.misc.asUrl "text.asURL()"

        // Inside the facade
        generaltools.misc.time -> generaltools.misc.timing "Calls the common profile"
        generaltools.misc.lists -> generaltools.misc.commonLog "TRACE on a match or a miss"
        generaltools.misc.readFn -> generaltools.misc.jvmLog "DEBUG before and after reading"
        generaltools.misc.asUrl -> generaltools.misc.jvmLog "DEBUG before parsing"

        // Out of the system
        generaltools.misc.commonLog -> generaltools.failureSeam "Obtained under the seam's logger convention"
        generaltools.misc.jvmLog -> slf4j "LoggerFactory.getLogger(name)"
        generaltools.misc.time -> slf4j "info(text) on the caller's Logger"
        generaltools.misc.readFn -> files "Reads the whole file"

        // Tests
        generaltools.testSuite -> generaltools.misc "Six test classes in three sets"

        // Planned
        generaltools.misc.jvmLogNext -> generaltools.failureSeam "Would follow the seam's convention"
        generaltools.gradleBuild.moduleLayout -> generaltools.misc "Would place both files in GeneralTools-core"
    }

    views {
        systemContext generaltools "Context" "GeneralTools and who touches it." {
            include *
            autoLayout lr
        }

        container generaltools "Containers" "The systems of GeneralTools that touch the misc helpers." {
            include *
            autoLayout lr
        }

        component generaltools.misc "Components" "The misc helpers as built at a500e44; the planned JVM logger drawn dashed." {
            include *
            autoLayout tb
        }

        dynamic generaltools.misc "LoggerTime" "Logger.time, the JVM file calling the common profile, as built." {
            consumer -> generaltools.misc.time "logger.time(name) { action }"
            generaltools.misc.time -> generaltools.misc.timing "profile(name, action)"
            generaltools.misc.timing -> consumer "action(), measured with measureTime"
            generaltools.misc.timing -> generaltools.misc.time "<name> took <n>ms"
            generaltools.misc.time -> slf4j "info(text) on the caller's Logger"
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
