// GeneralTools — C4 model as code (Structurizr DSL): the repository's systems and how they interact. Source of truth for docs/dashboards/index.html.
// As-is at GeneralTools feat/issue-7-kotlin-multiplatform a500e44 (2026-11-22): one Kotlin Multiplatform module at the repository root, published as one artifact, 3.0.0 unreleased (Maven Central's latest 2.2.0), for the jvm, js and wasmJs targets; the Gradle build (Gradle 9.0.0, the Kotlin Gradle plugin 2.4.21) with its two built parts. Code is authoritative on any conflict.
// The Gradle build's six planned parts are tagged Planned and drawn dashed; they are not built. The build's own pieces (the wrapper, the settings, the publishing block, the Yarn lock) are the container itself, not components.
workspace "GeneralTools" "A Kotlin Multiplatform library of generic helpers, built, tested and published as one artifact by one Gradle build." {

    !identifiers hierarchical

    model {
        consumer   = person "Consumer" "A Kotlin project on the JVM, JS or Wasm/JS that depends on io.github.spartanlaboratories:GeneralTools."
        maintainer = person "Maintainer" "Builds and tests with the Gradle wrapper, accepts an intended API change with updateKotlinAbi, and uploads a release by hand."
        actions  = softwareSystem "GitHub Actions" "Runs Build.yml on every push: one job on ubuntu-latest with Temurin 17, which runs ./gradlew build and publishes nothing." "External"
        central  = softwareSystem "Maven Central" "Serves the library's dependencies and, through the Central Portal, receives the bundle publishToMavenCentral uploads; its latest GeneralTools is 2.2.0." "External"
        nodejs   = softwareSystem "Node.js and Yarn downloads" "Node.js and Yarn, fetched by the Kotlin Gradle plugin when missing, run the js and wasmJs tests." "External"
        klogging = softwareSystem "kotlin-logging" "io.github.oshai:kotlin-logging 8.0.4, the multiplatform logging facade the common code logs through; an implementation dependency." "External"
        backend  = softwareSystem "Logging backend" "The consumer's slf4j binding on the JVM; the console at one global level on JS and Wasm/JS." "External"
        logback  = softwareSystem "logback" "logback-classic 1.5.13, the slf4j backend on the JVM test classpath only." "External"
        awt      = softwareSystem "AWT desktop" "java.awt: Robot captures the screen, Toolkit gives its size, Desktop hands a page to the default browser." "External"
        browser  = softwareSystem "Default browser" "The system's default browser, outside the JVM." "External"
        files    = softwareSystem "File system" "The local files read reads, images are written to and files are copied between." "External"

        generaltools = softwareSystem "GeneralTools" "A Kotlin Multiplatform library of generic helpers: Color, 2D geometry and small string, list and timing utilities in common code for the JVM, JS and Wasm/JS, with file, URL and desktop-automation helpers on the JVM only, built and published as one artifact by one Gradle build." {
            // ---- the library: one artifact, every system in it ----
            failureSeam = container "Failure and logging seam" "The internal KLogger.failure helper: one WARN line on the calling file's logger and a Result.failure carrying an IllegalArgumentException; the common code's kotlin-logging loggers under their 2.x names." "Kotlin Multiplatform · commonMain · internal" "Internal"
            valueCore   = container "Geometry value core" "TwoDoubles, Point and Dimensions with their equality contract, subclassing behind an opt-in marker, and the import-gated arithmetic operators." "Kotlin Multiplatform · commonMain · 3.0.0"
            boxModel    = container "Box model" "AxisAlignedBox with its primitive accessors and operator contains, in three storage forms: Rectangle, ImmutableRectangle and CenteredBox." "Kotlin Multiplatform · commonMain · 3.0.0"
            queries     = container "Geometry queries" "Vector algebra on Point, Segment, Ray and the segment and ray intersection tests against segments and boxes." "Kotlin Multiplatform · commonMain · 3.0.0"
            colorValue  = container "Color" "The validated RGBA value type with its transforms, the clamping factory Color.of and its codecs." "Kotlin Multiplatform · commonMain · 3.0.0"
            misc        = container "Misc helpers" "String, list, timing and Result helpers in common code; asURL, read and Logger.time on the JVM; one JVM facade, MiscKt." "Kotlin Multiplatform · commonMain and jvmMain · 3.0.0"
            desktopEdge = container "Desktop edge" "Screenshots, the browser and image files through AWT, ImageIO and commons-io, on the facade UserActionsKt." "Kotlin/JVM · jvmMain · 3.0.0"

            // ---- inside the repository only ----
            testSuite   = container "Test suite" "The tests by named test set: commonTest on the JVM, JS and Wasm/JS, jvmTest on the JVM only; 46 classes." "kotlin-test · JUnit Platform · Mocha" "Internal"
            gradleBuild = container "Gradle build" "Builds, tests, checks and publishes the library as one Kotlin Multiplatform module: the wrapper, the settings, the root build script with its two built parts and its publishing block, the committed Yarn lock, and CI." "Gradle 9.0.0 · Kotlin Gradle plugin 2.4.21 · vanniktech 0.37.0" "Internal" {
                // ---- built ----
                targets  = component "Target and compatibility configuration" "The jvm, js and wasmJs targets, language and API 2.2 with kotlin-stdlib 2.2.0, jvmToolchain(17), the dependencies of each source set, the -PtestLevel filter on every test task, and CI on Temurin 17." "Gradle Kotlin DSL · GitHub Actions workflow" "Internal"
                apiProof = component "API proof harness" "explicitApi() and abiValidation(): checkKotlinAbi, in check, compares every target's public API with the committed JVM and klib dumps under api/; updateKotlinAbi rewrites them." "Kotlin Gradle plugin ABI validation · committed dumps" "Internal"
                // ---- planned, not built (drawn dashed and faded; only Planned is dashed) ----
                catalog      = component "Version catalog" "Planned: gradle/libs.versions.toml, the version of every dependency and plugin written once." "Gradle version catalog" "Internal,Planned"
                buildLogic   = component "Build logic" "Planned: the included build build-logic/ with the publishing, multiplatform-library, JVM-library and test-set conventions and the shared constants." "Gradle included build · convention plugins" "Internal,Planned"
                javaFloor    = component "Java floor" "Planned: 17 stated once, driving jvmTarget, -Xjdk-release and the published attribute, with verifyJavaFloor in check and no toolchain requested." "constant · Gradle task" "Internal,Planned"
                moduleLayout = component "Module layout and published shape" "Planned: GeneralTools-core, GeneralTools-desktop and the source-less GeneralTools umbrella at one version, the rewritten root POM and the dependency constraint." "Gradle modules · settings" "Internal,Planned"
                apiReference = component "API reference jars" "Planned: each published component's javadoc jar from Dokka 2 with failOnWarning, in place of the empty javadoc jars built today." "Dokka 2" "Internal,Planned"
                releaseSet   = component "Publication proof and release set" "Planned: the publication comparison, the rehearsal repository and the release set of three modules uploaded as one deployment." "publishing convention · root tasks" "Internal,Planned"
            }
        }

        // ---- as built: the consumer ----
        consumer -> generaltools.valueCore "Holds, moves, compares and divides points and sizes"
        consumer -> generaltools.boxModel "Builds boxes, tests points against them, hands them out"
        consumer -> generaltools.queries "Measures vectors; tests segments and rays against segments and boxes"
        consumer -> generaltools.colorValue "Builds, transforms and encodes colours"
        consumer -> generaltools.misc "Calls the helpers on MiscKt"
        consumer -> generaltools.desktopEdge "Captures, opens and saves, on the JVM"
        consumer -> central "Resolves the artifact; Gradle selects the variant of its target"
        consumer -> backend "Configures levels and sinks"

        // ---- as built: between the library's systems ----
        generaltools.queries -> generaltools.valueCore "Vector algebra on Point; Segment and Ray hold Points"
        generaltools.queries -> generaltools.boxModel "The box tests read the six primitive accessors and operator contains"
        generaltools.queries -> generaltools.failureSeam "Reports every failure: ten sites in five functions"
        generaltools.boxModel -> generaltools.valueCore "Built on Point and Dimensions; ImmutableRectangle reuses the equality helpers"
        generaltools.valueCore -> generaltools.failureSeam "divideBy and distanceFrom report their failures through it"
        generaltools.misc -> generaltools.failureSeam "Misc.kt obtains its logger under the seam's convention"

        // ---- as built: what the library reaches outside ----
        generaltools.failureSeam -> klogging "warn { } on the calling file's KLogger"
        klogging -> backend "JVM: slf4j under the logger name; JS and Wasm/JS: the console"
        generaltools.misc -> backend "MiscJvm.kt logs through slf4j; Logger.time logs on the caller's Logger"
        generaltools.misc -> files "read reads the whole file"
        generaltools.desktopEdge -> awt "Robot captures the screen; Desktop.browse opens a page"
        awt -> browser "Opens the page"
        generaltools.desktopEdge -> files "Writes PNGs with ImageIO; copies files with commons-io"
        generaltools.desktopEdge -> backend "Logs through slf4j"

        // ---- as built: the tests ----
        generaltools.testSuite -> generaltools.failureSeam "Tests failure() in isolation"
        generaltools.testSuite -> generaltools.valueCore "Tests TwoDoubles, Point, Dimensions and the arithmetic"
        generaltools.testSuite -> generaltools.boxModel "Tests the box forms and their accessors"
        generaltools.testSuite -> generaltools.queries "Tests the vector algebra, Segment, Ray, the intersection tests and their WARN lines"
        generaltools.testSuite -> generaltools.colorValue "Tests Color"
        generaltools.testSuite -> generaltools.misc "Tests the common and JVM helpers"
        generaltools.testSuite -> generaltools.desktopEdge "Tests UserActions on real files and the real screen"
        generaltools.testSuite -> logback "Captures the library's log events at the slf4j backend on the JVM"

        // ---- as built: the build ----
        maintainer -> generaltools.gradleBuild.targets "Runs ./gradlew build, or one test task with -PtestLevel"
        maintainer -> generaltools.gradleBuild.apiProof "Runs updateKotlinAbi after an intended API change and commits the dumps"
        actions -> generaltools.gradleBuild.targets "Runs Build.yml: ./gradlew build on every push, on Temurin 17"
        generaltools.gradleBuild.targets -> generaltools.gradleBuild.apiProof "Hands the compiled output of every target to checkKotlinAbi, in check"
        generaltools.gradleBuild.targets -> central "Resolves the dependencies of each source set"
        generaltools.gradleBuild.targets -> nodejs "Downloads Node.js and Yarn for the web test tasks"
        generaltools.gradleBuild.targets -> generaltools.failureSeam "Declares kotlin-logging 8.0.4: implementation, commonMain"
        generaltools.gradleBuild.targets -> generaltools.misc "Declares slf4j-api 2.0.16: api, jvmMain, for Logger.time"
        generaltools.gradleBuild.targets -> generaltools.desktopEdge "Declares commons-io 2.4: implementation, jvmMain"
        generaltools.gradleBuild.targets -> generaltools.testSuite "Runs its classes on every target; -PtestLevel selects its sets"
        generaltools.gradleBuild -> central "Uploads one signed bundle: publishToMavenCentral, upload only"

        // ---- planned (dashed: one end is Planned) ----
        generaltools.gradleBuild.catalog -> generaltools.gradleBuild.targets "Will hold every version the script writes as a literal today"
        generaltools.gradleBuild.buildLogic -> generaltools.gradleBuild.catalog "Will read the versions through one helper"
        generaltools.gradleBuild.buildLogic -> generaltools.gradleBuild.targets "Will take the shared settings: pins, targets, test sets, explicitApi, abiValidation"
        generaltools.gradleBuild.javaFloor -> generaltools.gradleBuild.targets "Will replace jvmToolchain(17) and the foojay resolver"
        generaltools.gradleBuild.moduleLayout -> generaltools.gradleBuild.apiProof "Will give each library module its own dumps"
        generaltools.gradleBuild.moduleLayout -> generaltools.failureSeam "Will place Failure.kt in GeneralTools-core"
        generaltools.gradleBuild.moduleLayout -> generaltools.colorValue "Will place Color.kt in GeneralTools-core"
        generaltools.gradleBuild.moduleLayout -> generaltools.misc "Will place both files in GeneralTools-core"
        generaltools.gradleBuild.moduleLayout -> generaltools.desktopEdge "Will build UserActions.kt in GeneralTools-desktop"
        generaltools.gradleBuild.apiReference -> central "Will ship Dokka pages in every javadoc jar"
        generaltools.gradleBuild.releaseSet -> central "Will upload the six components as one deployment"
    }

    views {
        systemContext generaltools "Context" "GeneralTools, who uses and builds it, and what it reaches." {
            include *
            autoLayout lr
        }
        container generaltools "Containers" "The library's seven systems, the test suite and the Gradle build that compiles, tests and publishes them." {
            include *
            autoLayout lr
        }
        component generaltools.gradleBuild "Build" "The Gradle build as built at a500e44: two parts built, six planned (dashed)." {
            include *
            autoLayout tb
        }
        dynamic generaltools "BoxQuery" "segmentIntersectsBox on a box with a negative width, as built: four systems and the logging backend." {
            consumer -> generaltools.queries "segmentIntersectsBox(s, box)"
            generaltools.queries -> generaltools.boxModel "boxProblem reads width and height"
            generaltools.queries -> generaltools.failureSeam "log.failure with Box size must be non-negative and its detail"
            generaltools.failureSeam -> klogging "warn { } on com.spartanlabs.geometry.Intersections"
            klogging -> backend "One WARN line, no throwable"
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
