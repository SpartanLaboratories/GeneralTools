// test-suite — C4 model as code (Structurizr DSL). Source of truth for the structure shown on the board beside it.
// As-is at GeneralTools feat/issue-7-kotlin-multiplatform a500e44 (2026-11-22), test-suite, unversioned. Code is authoritative on any conflict.
workspace "GeneralTools" "A Kotlin Multiplatform library of generic helpers: Color, 2D geometry and small utilities for the JVM, JS and Wasm/JS." {

    !identifiers hierarchical

    model {
        maintainer = person "Maintainer" "Works on the library and runs its tests through Gradle, every set or one at a time."
        generaltools = softwareSystem "GeneralTools" "The repository as a software system: one Kotlin Multiplatform library, published as one artifact." {
            gradleBuild = container "Gradle build" "Builds, tests, checks and publishes the library; its -PtestLevel filter selects test sets by package on every test task." "Gradle 9.0.0 · Kotlin Gradle plugin 2.4.21" "Internal"
            failureSeam = container "Failure and logging seam" "The internal KLogger.failure helper: one WARN line on the caller's logger and a Result.failure." "Kotlin Multiplatform · commonMain · internal" "Internal"
            valueCore = container "Geometry value core" "TwoDoubles, Point and Dimensions with their equality contract and the import-gated arithmetic." "Kotlin Multiplatform · commonMain"
            boxModel = container "Box model" "AxisAlignedBox, Rectangle, ImmutableRectangle and CenteredBox." "Kotlin Multiplatform · commonMain"
            queries = container "Geometry queries" "Vector algebra on Point, Segment, Ray and the intersection tests." "Kotlin Multiplatform · commonMain"
            colorValue = container "Color" "The validated RGBA value type with its transforms and codecs." "Kotlin Multiplatform · commonMain"
            misc = container "Misc helpers" "The small helpers on the facade MiscKt: common, and asURL, read and Logger.time on the JVM." "Kotlin Multiplatform · commonMain and jvmMain"
            desktopEdge = container "Desktop edge" "Screenshots, the browser and image files through AWT, ImageIO and commons-io." "Kotlin/JVM · jvmMain"
            testSuite = container "Test suite" "Each system's behaviour proved on every target it ships on, organised by named test set." "kotlin-test · JUnit Platform on the JVM · Node.js on js and wasmJs" "Internal" {
                // ---- commonTest: compiled for, and run on, jvm, js and wasmJs ----
                commonComponent     = component "component · commonTest" "30 classes, 336 tests: each system's behaviour in isolation, pure, nothing mocked." "kotlin-test · com.spartanlabs.testing.component.*" "Internal"
                commonDeterministic = component "deterministic · commonTest" "4 classes, 11 tests: fixed input-to-output tables, identical on every target." "kotlin-test · com.spartanlabs.testing.deterministic.*" "Internal"
                commonE2e           = component "e2e · commonTest" "2 classes, 7 tests: a consumer's journey through the real types." "kotlin-test · com.spartanlabs.testing.e2e.*" "Internal"
                commonSupport       = component "Common test support" "BoxFixtures, one box per implementor, and RecordingKLogger, a KLogger that records what it would emit." "internal test helpers · no tests" "Internal"
                // ---- jvmTest: the JVM only ----
                jvmComponent        = component "component · jvmTest" "2 classes, 3 tests: asURL and the slf4j Logger.time." "kotlin-test on JUnit 5 · com.spartanlabs.testing.component.*" "Internal"
                jvmDeterministic    = component "deterministic · jvmTest" "1 class, 1 test: capitalizeEveryWord under another JVM default locale." "kotlin-test on JUnit 5 · com.spartanlabs.testing.deterministic.*" "Internal"
                jvmIntegration      = component "integration · jvmTest" "6 classes, 32 tests: the real logback backend, real files and, with a display, the real screen." "kotlin-test on JUnit 5 · com.spartanlabs.testing.integration.*" "Internal"
                jvmNonfunctional    = component "nonfunctional · jvmTest" "1 class, 2 tests: equals and hashCode allocate nothing measurable." "kotlin-test on JUnit 5 · com.spartanlabs.testing.nonfunctional.*" "Internal"
                jvmSupport          = component "JVM test support" "LogCapture, a logback ListAppender on one named logger, and logback-test.xml." "internal test helper and resource · no tests" "Internal"
                // ---- planned, not built (drawn dashed and faded; only Planned is dashed) ----
                jvmLogCapture       = component "JVM log-capture tests" "Pin the logger name, level and text of asURL, read and the UserActions functions that run headless." "kotlin-test on JUnit 5 · integration" "Internal,Planned"
                desktopTests        = component "Desktop module tests" "UserActionsTest and the log capture of UserActions, with their own logback-test.xml and log-capture helper, in the GeneralTools-desktop module." "kotlin-test on JUnit 5" "Internal,Planned"
            }
        }
        nodejs = softwareSystem "Node.js" "Runs the js and wasmJs tests; the Kotlin Gradle plugin downloads it on the first run." "External"
        logback = softwareSystem "logback" "The slf4j backend on the JVM test classpath, logback-classic 1.5.13." "External"

        maintainer -> generaltools.gradleBuild "Runs check, allTests or one test task, with or without -PtestLevel"
        generaltools.gradleBuild -> generaltools.testSuite.commonComponent "Runs on jvmTest, jsNodeTest and wasmJsNodeTest"
        generaltools.gradleBuild -> generaltools.testSuite.jvmIntegration "Runs on jvmTest"
        generaltools.gradleBuild -> nodejs "Downloads it and runs the web test tasks on it"
        generaltools.testSuite.commonComponent -> generaltools.valueCore "TwoDoubles, Point, Dimensions and the arithmetic"
        generaltools.testSuite.commonComponent -> generaltools.queries "The vector algebra, Segment, Ray and the intersection tests"
        generaltools.testSuite.commonComponent -> generaltools.boxModel "The three box forms, the accessors and contains"
        generaltools.testSuite.commonComponent -> generaltools.colorValue "Color"
        generaltools.testSuite.commonComponent -> generaltools.misc "The common helpers"
        generaltools.testSuite.commonComponent -> generaltools.failureSeam "failure() against a recording logger"
        generaltools.testSuite.commonComponent -> generaltools.testSuite.commonSupport "Uses BoxFixtures and RecordingKLogger"
        generaltools.testSuite.commonDeterministic -> generaltools.colorValue "Hex encoding and transform ranges"
        generaltools.testSuite.commonDeterministic -> generaltools.boxModel "The accessors' literal IEEE results"
        generaltools.testSuite.commonDeterministic -> generaltools.valueCore "componentEquals and componentHash"
        generaltools.testSuite.commonE2e -> generaltools.boxModel "A library hands out an immutable box copy and queries it"
        generaltools.testSuite.commonE2e -> generaltools.valueCore "A consumer subclasses TwoDoubles behind the opt-in"
        generaltools.testSuite.jvmComponent -> generaltools.misc "asURL and Logger.time"
        generaltools.testSuite.jvmDeterministic -> generaltools.misc "capitalizeEveryWord under a Turkish default locale"
        generaltools.testSuite.jvmIntegration -> generaltools.valueCore "divideBy and the arithmetic div at the logback backend"
        generaltools.testSuite.jvmIntegration -> generaltools.queries "Every failure site's WARN line at the logback backend"
        generaltools.testSuite.jvmIntegration -> generaltools.misc "evaluateList's TRACE lines; read on a real file"
        generaltools.testSuite.jvmIntegration -> generaltools.desktopEdge "Real files and, with a display, the real screen"
        generaltools.testSuite.jvmIntegration -> generaltools.testSuite.jvmSupport "Captures events with LogCapture"
        generaltools.testSuite.jvmNonfunctional -> generaltools.valueCore "Allocated bytes of equals and hashCode, never time"
        generaltools.testSuite.jvmSupport -> logback "Attaches a ListAppender; sets com.spartanlabs to TRACE"
        generaltools.queries -> generaltools.failureSeam "Reports every failure"
        generaltools.failureSeam -> logback "One WARN line per failure, through kotlin-logging and slf4j"
        generaltools.testSuite.jvmLogCapture -> generaltools.misc "Pins the log lines of asURL and read"
        generaltools.testSuite.jvmLogCapture -> generaltools.desktopEdge "Pins the log lines of the headless UserActions functions"
        generaltools.testSuite.desktopTests -> generaltools.desktopEdge "Tests UserActions in its own module"
    }

    views {
        systemContext generaltools "Context" "GeneralTools and who touches it, from the test suite's side." {
            include *
            autoLayout lr
        }
        container generaltools "Containers" "The suite, the build that runs it and the systems it tests." {
            include *
            autoLayout lr
        }
        component generaltools.testSuite "Components" "test-suite as built at a500e44: the named test sets in each source set." {
            include *
            autoLayout tb
        }
        dynamic generaltools.testSuite "FailureSiteCapture" "An integration log-capture test of one failure site, as built." {
            generaltools.gradleBuild -> generaltools.testSuite.jvmIntegration "jvmTest runs FailureSitesLoggingTest"
            generaltools.testSuite.jvmIntegration -> generaltools.testSuite.jvmSupport "LogCapture.attach(the Intersections logger)"
            generaltools.testSuite.jvmSupport -> logback "addAppender(ListAppender)"
            generaltools.testSuite.jvmIntegration -> generaltools.queries "rayIntersectsBox with a zero-direction ray"
            generaltools.queries -> generaltools.failureSeam "failure(message) { detail }"
            generaltools.failureSeam -> logback "WARN message: detail, no throwable"
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
