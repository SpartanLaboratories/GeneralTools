// desktop-edge — C4 model as code (Structurizr DSL).
// Source of truth for the structure shown on docs/dashboards/desktop-edge/. The board's C4 tab is rendered from this file.
// As-is at GeneralTools feat/issue-7-kotlin-multiplatform a500e44 (2026-11-22), version 3.0.0, unreleased. Code is authoritative on any conflict.
workspace "GeneralTools" "A Kotlin Multiplatform library of generic helpers: Color, 2D geometry and small utilities in common code for the JVM, JS and Wasm/JS, with file, URL and desktop helpers on the JVM." {

    !identifiers hierarchical

    model {
        consumer = person "Consumer" "A JVM program on a machine with a display that captures the screen, opens pages and saves images."
        awt      = softwareSystem "AWT desktop" "java.awt: Robot captures the screen, Toolkit gives its size, Desktop hands a page to the browser." "External"
        browser  = softwareSystem "Default browser" "The system's default browser, outside the JVM." "External"
        files    = softwareSystem "File system" "Where images are written and files copied." "External"
        commonsIo = softwareSystem "commons-io" "org.apache.directory.studio:org.apache.commons.io 2.4: FileUtils.copyFile; an implementation dependency." "External"
        slf4j    = softwareSystem "slf4j" "org.slf4j:slf4j-api 2.0.16, which the file logs through directly." "External"

        generaltools = softwareSystem "GeneralTools" "A Kotlin Multiplatform library of generic helpers, built and published as one artifact." {
            desktopEdge = container "Desktop edge" "Drives a desktop from the JVM: screenshots of the screen, an area or a browser page, opening a page in the browser, and cropping and saving images." "Kotlin/JVM · jvmMain · 3.0.0" {

                // ---- public surface ----
                capture   = component "Screen capture" "screenshotFull and screenshotArea(x, y, width, height): a new java.awt.Robot per call, createScreenCapture." "top-level functions" "Public"
                pageShot  = component "screenshotBrowser" "Opens the address, sleeps 1600 ms on the caller's thread, then captures the fixed area 2520 × 1280 at (40, 100)." "top-level function" "Public"
                browse    = component "openInBrowser" "Desktop.getDesktop().browse(URL(address).toURI())." "top-level function" "Public"
                images    = component "Image files" "cropImage into a new ARGB image; BufferedImage.to writes <name>.png with ImageIO; File.saveTo copies a file to <name>.png with commons-io." "top-level functions" "Public"

                // ---- internals ----
                inputs    = component "Input helpers, unused" "click, typeKey, shiftType, typeText, typeCharacters, typeRandomKey and typeRandomKeys: private, and no public function calls them." "private functions" "Internal"
                log       = component "UserActions logger" "private val log = LoggerFactory.getLogger(com.spartanlabs.generaltools.UserActions): DEBUG, INFO and TRACE at ten sites." "private val" "Internal"

                // ---- planned, not built (drawn dashed and faded; only Planned is dashed) ----
                logNext   = component "UserActions logger on kotlin-logging" "The file's logger obtained from kotlin-logging under the same name, each call in the lazy form, levels and texts unchanged." "planned" "Internal,Planned"
            }
            failureSeam = container "Failure and logging seam" "One logging API for every file and one way to report an expected failure." "Kotlin Multiplatform · common code · internal"
            testSuite   = container "Test suite" "The tests by named test set, in commonTest and jvmTest." "Kotlin Multiplatform · tests"
            gradleBuild = container "Gradle build" "Builds, tests, checks and publishes the library." "Gradle 9.0.0 · Kotlin 2.4.21" {
                moduleLayout = component "Module layout and published shape" "GeneralTools-core, GeneralTools-desktop and the source-less GeneralTools umbrella." "planned" "Internal,Planned"
            }
        }

        // People and systems
        consumer -> generaltools.desktopEdge "Captures, opens and saves with"
        consumer -> generaltools.desktopEdge.capture "screenshotFull(), screenshotArea(…)"
        consumer -> generaltools.desktopEdge.pageShot "screenshotBrowser(address)"
        consumer -> generaltools.desktopEdge.browse "openInBrowser(address)"
        consumer -> generaltools.desktopEdge.images "cropImage, image to name, file saveTo name"

        // Inside the file
        generaltools.desktopEdge.pageShot -> generaltools.desktopEdge.browse "Opens the page first"
        generaltools.desktopEdge.pageShot -> generaltools.desktopEdge.capture "Then screenshotArea(40, 100, 2520, 1280)"
        generaltools.desktopEdge.capture -> generaltools.desktopEdge.log "DEBUG per capture"
        generaltools.desktopEdge.images -> generaltools.desktopEdge.log "DEBUG per crop, write and copy"

        // Out of the system
        generaltools.desktopEdge.capture -> awt "Robot().createScreenCapture(area)"
        generaltools.desktopEdge.browse -> awt "Desktop.browse(uri)"
        awt -> browser "Opens the page"
        generaltools.desktopEdge.images -> files "ImageIO.write PNG; copyFile"
        generaltools.desktopEdge.images -> commonsIo "FileUtils.copyFile"
        generaltools.desktopEdge.log -> slf4j "LoggerFactory.getLogger(name)"

        // Tests
        generaltools.testSuite -> generaltools.desktopEdge "UserActionsTest; screenshots need a display"

        // Planned
        generaltools.desktopEdge.logNext -> generaltools.failureSeam "Would follow the seam's convention"
        generaltools.gradleBuild.moduleLayout -> generaltools.desktopEdge "Would place UserActions.kt in GeneralTools-desktop"
    }

    views {
        systemContext generaltools "Context" "GeneralTools and who touches it." {
            include *
            autoLayout lr
        }

        container generaltools "Containers" "The systems of GeneralTools that touch the desktop edge." {
            include *
            autoLayout lr
        }

        component generaltools.desktopEdge "Components" "The desktop edge as built at a500e44; its planned logger drawn dashed." {
            include *
            autoLayout tb
        }

        dynamic generaltools.desktopEdge "BrowserScreenshot" "screenshotBrowser, as built." {
            consumer -> generaltools.desktopEdge.pageShot "screenshotBrowser(address)"
            generaltools.desktopEdge.pageShot -> generaltools.desktopEdge.browse "openInBrowser(address)"
            generaltools.desktopEdge.browse -> awt "Desktop.browse(URL(address).toURI())"
            awt -> browser "the page opens outside the JVM"
            generaltools.desktopEdge.pageShot -> generaltools.desktopEdge.capture "after Thread.sleep(1600): screenshotArea(40, 100, 2520, 1280)"
            generaltools.desktopEdge.capture -> awt "Robot().createScreenCapture(area)"
            generaltools.desktopEdge.capture -> consumer "the BufferedImage"
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
