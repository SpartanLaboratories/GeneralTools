// gradle-build — the Gradle build: C4 model as code (Structurizr DSL). Source of truth for the structure shown on docs/dashboards/gradle-build/index.html.
// As-is at GeneralTools feat/issue-7-kotlin-multiplatform a500e44 (2026-11-22): one Kotlin Multiplatform module at the repository root, Gradle 9.0.0, the Kotlin Gradle plugin 2.4.21, vanniktech maven-publish 0.37.0 and the foojay toolchain resolver 1.0.0. Two parts are built; six are planned and drawn dashed. Code is authoritative on any conflict.
workspace "GeneralTools" "A Kotlin Multiplatform library of generic helpers, built, tested and published as one artifact by one Gradle build." {

    !identifiers hierarchical

    model {
        maintainer = person "Maintainer" "Builds and tests with the Gradle wrapper, accepts an intended API change with updateKotlinAbi, and uploads a release by hand."
        actions = softwareSystem "GitHub Actions" "Runs Build.yml on every push: one job on ubuntu-latest with Temurin 17, which runs ./gradlew build and publishes nothing." "External"
        central = softwareSystem "Maven Central" "Serves the library's dependencies and, through the Central Portal, receives the bundle publishToMavenCentral uploads." "External"
        portal = softwareSystem "Gradle Plugin Portal" "Serves the Kotlin Multiplatform, vanniktech maven-publish and foojay plugins the scripts apply at literal versions." "External"
        foojay = softwareSystem "foojay toolchain service" "Finds a JDK 17 download for the toolchain when no installed JDK matches." "External"
        nodejs = softwareSystem "Node.js and Yarn downloads" "nodejs.org serves Node.js and GitHub releases serve Yarn, fetched by kotlinNodeJsSetup and kotlinYarnSetup when missing." "External"
        npm = softwareSystem "npm registry" "registry.yarnpkg.com serves the 85 packages of the Mocha test tooling that kotlin-js-store/yarn.lock pins." "External"

        generaltools = softwareSystem "GeneralTools" "A Kotlin Multiplatform library of generic helpers: Color, 2D geometry and small helpers in common code for the JVM, JS and Wasm/JS, with file, URL and desktop helpers on the JVM only." {
            failureSeam = container "Failure and logging seam" "The internal KLogger.failure helper: one WARN line on the calling file's logger and a Result.failure; the common code's kotlin-logging loggers." "Kotlin Multiplatform · common code" "Internal"
            valueCore = container "Geometry value core" "TwoDoubles, Point and Dimensions with their equality contract, the subclassing opt-in and the import-gated arithmetic." "Kotlin Multiplatform · common code"
            boxModel = container "Box model" "AxisAlignedBox in three storage forms: Rectangle, ImmutableRectangle and CenteredBox." "Kotlin Multiplatform · common code"
            queries = container "Geometry queries" "Vector algebra on Point, Segment, Ray and the intersection tests." "Kotlin Multiplatform · common code"
            colorValue = container "Color" "The validated RGBA value type with its transforms and codecs." "Kotlin Multiplatform · common code"
            misc = container "Misc helpers" "String, list, timing and Result helpers in common code; asURL, read and Logger.time on the JVM, on the facade MiscKt." "Kotlin Multiplatform · common code and jvmMain"
            desktopEdge = container "Desktop edge" "Screenshots, the browser and image files through AWT, ImageIO and commons-io, on the facade UserActionsKt." "Kotlin/JVM · jvmMain"
            testSuite = container "Test suite" "The tests by named test set: commonTest on the JVM, JS and Wasm/JS, jvmTest on the JVM only." "kotlin-test · JUnit Platform · Mocha" "Internal"

            gradleBuild = container "Gradle build" "Builds, tests, checks and publishes the library as one Kotlin Multiplatform module: the wrapper, the settings, the root build script with its two built parts and its publishing block, the committed Yarn lock, and CI." "Gradle 9.0.0 · Kotlin Gradle plugin 2.4.21 · vanniktech 0.37.0" "Internal" {
                // ---- built ----
                targets = component "Target and compatibility configuration" "build.gradle.kts:27-120 and Build.yml: the floors (language and API 2.2, kotlin-stdlib 2.2.0, jvmFloor 17), jvmToolchain(17), the jvm, js and wasmJs targets with TargetJvmVersion 17 declared on jvm, the dependencies of each source set, the JUnit Platform, the -PtestLevel filter on every test task, and CI on Temurin 17." "Gradle Kotlin DSL · GitHub Actions workflow" "Internal"
                apiProof = component "API proof harness" "explicitApi() and abiValidation() (build.gradle.kts:38, :63): checkKotlinAbi, in check, compares the public API of every target with api/GeneralTools.api (JVM, 337 lines) and api/GeneralTools.klib.api (js and wasmJs, 388 lines); updateKotlinAbi rewrites both; .gitattributes keeps them LF." "Kotlin Gradle plugin ABI validation · committed dumps" "Internal"
                // ---- planned, not built (drawn dashed and faded; only Planned is dashed) ----
                catalog = component "Version catalog" "Planned: gradle/libs.versions.toml, the version of every dependency and plugin written once, read by the scripts, the convention plugins and build-logic's own build." "Gradle version catalog" "Internal,Planned"
                buildLogic = component "Build logic" "Planned: the included build build-logic/ with the publishing, multiplatform-library, JVM-library and test-set conventions and the shared constants; the root project an unpublished aggregator." "Gradle included build · convention plugins" "Internal,Planned"
                javaFloor = component "Java floor" "Planned: 17 stated once in build-logic, driving jvmTarget, -Xjdk-release and the published attribute, with verifyJavaFloor in check; no toolchain requested, the foojay resolver gone." "constant · Gradle task" "Internal,Planned"
                moduleLayout = component "Module layout and published shape" "Planned: GeneralTools-core, GeneralTools-desktop and the source-less GeneralTools umbrella at one version; the rewritten root POM, the dependency constraint, the Automatic-Module-Name entries; the root project renamed GeneralTools-build." "Gradle modules · settings" "Internal,Planned"
                apiReference = component "API reference jars" "Planned: each published component's javadoc jar from Dokka 2 with failOnWarning, in place of the empty javadoc jars built today; the root aggregates both library modules; two page-count checks." "Dokka 2" "Internal,Planned"
                releaseSet = component "Publication proof and release set" "Planned: the publication comparison, the rehearsal repository, the release set of three modules uploaded as one deployment, and the component-set assertion." "publishing convention · root tasks" "Internal,Planned"
            }
        }

        // ---- as built ----
        maintainer -> generaltools.gradleBuild.targets "Runs ./gradlew build, or one test task with -PtestLevel"
        maintainer -> generaltools.gradleBuild.apiProof "Runs updateKotlinAbi after an intended API change and commits the dumps"
        actions -> generaltools.gradleBuild.targets "Runs Build.yml: ./gradlew build on every push, on Temurin 17"
        generaltools.gradleBuild.targets -> generaltools.gradleBuild.apiProof "Hands the compiled output of every target to checkKotlinAbi, in check"
        generaltools.gradleBuild.targets -> central "Resolves the dependencies of each source set"
        generaltools.gradleBuild.targets -> foojay "Resolves a JDK 17 when none is installed"
        generaltools.gradleBuild.targets -> nodejs "Downloads Node.js and Yarn for the web test tasks"
        generaltools.gradleBuild.targets -> npm "Installs the Mocha test tooling the Yarn lock pins"
        generaltools.gradleBuild -> portal "Resolves its three plugins at literal versions"
        generaltools.gradleBuild -> central "Uploads one signed bundle: publishToMavenCentral, upload only"
        generaltools.gradleBuild.targets -> generaltools.failureSeam "Declares kotlin-logging 8.0.4: implementation, commonMain"
        generaltools.gradleBuild.targets -> generaltools.misc "Declares slf4j-api 2.0.16: api, jvmMain, for Logger.time"
        generaltools.gradleBuild.targets -> generaltools.desktopEdge "Declares commons-io 2.4: implementation, jvmMain"
        generaltools.gradleBuild.targets -> generaltools.testSuite "Runs its classes on every target; -PtestLevel selects its sets"

        // ---- planned (dashed: one end is Planned) ----
        generaltools.gradleBuild.catalog -> generaltools.gradleBuild.targets "Will hold every version the script writes as a literal today"
        generaltools.gradleBuild.buildLogic -> generaltools.gradleBuild.catalog "Will read the versions through one helper"
        generaltools.gradleBuild.buildLogic -> generaltools.gradleBuild.targets "Will take the shared settings: pins, targets, test sets, explicitApi, abiValidation"
        generaltools.gradleBuild.javaFloor -> generaltools.gradleBuild.targets "Will replace jvmToolchain(17) and the foojay resolver with jvmTarget, -Xjdk-release and verifyJavaFloor"
        generaltools.gradleBuild.moduleLayout -> generaltools.gradleBuild.apiProof "Will give each library module its own dumps"
        generaltools.gradleBuild.moduleLayout -> generaltools.desktopEdge "Will build UserActions.kt in GeneralTools-desktop"
        generaltools.gradleBuild.apiReference -> central "Will ship Dokka pages in every javadoc jar"
        generaltools.gradleBuild.releaseSet -> central "Will upload the six components as one deployment"
    }

    views {
        systemContext generaltools "Context" "GeneralTools, its maintainer, CI and the services the build reaches." {
            include *
            autoLayout lr
        }
        container generaltools "Containers" "The library's systems and the Gradle build that compiles, tests and publishes them." {
            include *
            autoLayout lr
        }
        component generaltools.gradleBuild "Components" "The Gradle build as built at a500e44: two parts built, six planned." {
            include *
            autoLayout tb
        }
        dynamic generaltools.gradleBuild "Build" "./gradlew build on the one module, as built." {
            maintainer -> generaltools.gradleBuild.targets "./gradlew build: configure, compile jvm, js and wasmJs"
            generaltools.gradleBuild.targets -> nodejs "kotlinNodeJsSetup, kotlinYarnSetup: Node.js and Yarn when missing"
            generaltools.gradleBuild.targets -> npm "kotlinNpmInstall, then the Yarn lock compared"
            generaltools.gradleBuild.targets -> generaltools.testSuite "jvmTest, jsNodeTest, wasmJsNodeTest"
            generaltools.gradleBuild.targets -> generaltools.gradleBuild.apiProof "checkKotlinAbi on the compiled output, in check"
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
