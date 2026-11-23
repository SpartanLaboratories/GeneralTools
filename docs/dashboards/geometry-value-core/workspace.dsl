// geometry-value-core — C4 model as code (Structurizr DSL).
// Source of truth for the structure shown on docs/dashboards/geometry-value-core/. The board's C4 tab is rendered from this file.
// As-is at GeneralTools feat/issue-7-kotlin-multiplatform a500e44 (2026-11-22), geometry value core 3.0.0 (unreleased; Maven Central latest 2.2.0). Code is authoritative on any conflict.
workspace "GeneralTools" "A Kotlin Multiplatform library of generic helpers: Color, 2D geometry and small utilities in common code for the JVM, JS and Wasm/JS." {

    !identifiers hierarchical

    model {
        consumer = person "Library consumer" "Kotlin code on the JVM, JS or Wasm/JS that holds points and sizes, moves and compares them in place and, in a file that imports the arithmetic package, adds and scales them."
        backend  = softwareSystem "Logging backend" "The consumer's slf4j backend on the JVM; the console on JS and Wasm/JS." "External"

        generaltools = softwareSystem "GeneralTools" "One Kotlin Multiplatform library module, published as GeneralTools with its jvm, js and wasm-js artifacts." {
            valueCore = container "Geometry value core" "The mutable coordinate-pair value types TwoDoubles, Point and Dimensions with their equality contract, subclassing behind an opt-in marker, and the import-gated arithmetic operators." "Kotlin common code · 3.0.0" {

                // ---- public surface ----
                twoDoubles = component "TwoDoubles" "Abstract base of every coordinate pair: first and second, the in-place verbs, divideBy, the final equality contract and destructuring. No abstract member." "abstract class · protected constructors" "Public"
                point      = component "Point" "A 2D point: x and y over first and second, distanceFrom, unary minus. Final." "final class" "Public"
                dimensions = component "Dimensions" "A 2D size: width and height over first and second. Final." "final class" "Public"
                arithmetic = component "Arithmetic operators" "Twelve value-returning plus, minus, times and div operators on Point and Dimensions, visible only in a file that imports com.spartanlabs.geometry.arithmetic." "12 extension operators · facade ArithmeticKt" "Public"
                optIn      = component "ExperimentalForInheritanceGeneralToolsApi" "The opt-in marker that gates subclassing TwoDoubles at ERROR level; usable only as an argument of @OptIn or @SubclassOptInRequired." "annotation class · @RequiresOptIn" "Public"
                supported  = component "SupportedExtension" "Documents the Supported Extension tier; not an opt-in, gates nothing, kept in the compiled library." "annotation class · BINARY retention" "Public"

                // ---- internals ----
                equality   = component "Component equality" "componentEquals and componentHash: IEEE equality except NaN equals NaN; -0.0 hashes as 0.0, every NaN alike. Shared with the box model's ImmutableRectangle." "internal functions" "Internal"
            }
            failureSeam = container "Failure and logging seam" "The internal KLogger.failure helper: one WARN line on the calling file's logger and a Result.failure carrying an IllegalArgumentException." "Kotlin common code · internal"
            boxModel    = container "Box model" "AxisAlignedBox and its three storage forms, built on Point and Dimensions." "Kotlin common code · 3.0.0"
            queries     = container "Geometry queries" "Vector algebra on Point, Segment, Ray and the intersection tests." "Kotlin common code · 3.0.0"
        }

        // People and systems
        consumer -> generaltools.valueCore "Holds, moves, compares and divides points and sizes"
        consumer -> generaltools.valueCore.point "Constructs; moves in place; compares; distanceFrom"
        consumer -> generaltools.valueCore.twoDoubles "Subclasses after opting in; calls divideBy and the in-place verbs"
        consumer -> generaltools.valueCore.arithmetic "Imports per file; p + q, 2.0 * p, p / k"

        // Inside the value core
        generaltools.valueCore.point -> generaltools.valueCore.twoDoubles "Extends; x and y are first and second"
        generaltools.valueCore.dimensions -> generaltools.valueCore.twoDoubles "Extends; width and height are first and second"
        generaltools.valueCore.twoDoubles -> generaltools.valueCore.equality "equals and hashCode, per component"
        generaltools.valueCore.twoDoubles -> generaltools.valueCore.optIn "Subclassing gated by, through @SubclassOptInRequired"
        generaltools.valueCore.arithmetic -> generaltools.valueCore.point "Builds a new Point; operands unchanged"
        generaltools.valueCore.arithmetic -> generaltools.valueCore.dimensions "Builds a new Dimensions; operands unchanged"
        generaltools.valueCore.arithmetic -> generaltools.valueCore.supported "Marked with, every operator"

        // Failures
        generaltools.valueCore.twoDoubles -> generaltools.failureSeam "divideBy by 0.0 or -0.0: failure(message, detail)"
        generaltools.valueCore.point -> generaltools.failureSeam "distanceFrom with a NaN coordinate: failure(message, detail)"
        generaltools.failureSeam -> backend "One WARN line on the calling file's logger"
        generaltools.failureSeam -> generaltools.valueCore.twoDoubles "Returns Result.failure(IllegalArgumentException(message))"
        generaltools.valueCore.twoDoubles -> consumer "Returns the Result of divideBy; the value unchanged on a failure"

        // Systems built on it
        generaltools.boxModel -> generaltools.valueCore.point "Rectangle's location, CenteredBox's center"
        generaltools.boxModel -> generaltools.valueCore.dimensions "Rectangle's dimensions, CenteredBox's halfExtents"
        generaltools.boxModel -> generaltools.valueCore.equality "ImmutableRectangle's equals and hashCode"
        generaltools.queries -> generaltools.valueCore.point "Vector algebra; Segment ends; Ray origin and direction"
    }

    views {
        systemContext generaltools "Context" "GeneralTools and who touches it." {
            include *
            autoLayout lr
        }

        container generaltools "Containers" "The geometry value core and the systems it meets." {
            include *
            autoLayout lr
        }

        component generaltools.valueCore "Components" "The geometry value core as built at feat/issue-7-kotlin-multiplatform a500e44." {
            include *
            autoLayout tb
        }

        dynamic generaltools.valueCore "ZeroDivisor" "divideBy on a zero divisor, then on a non-zero one, as built." {
            consumer -> generaltools.valueCore.twoDoubles "p divideBy 0.0, on a Point"
            generaltools.valueCore.twoDoubles -> generaltools.failureSeam "magnitude == 0.0: failure(Cannot divide by zero, detail)"
            generaltools.failureSeam -> backend "WARN on com.spartanlabs.geometry.TwoDoubles, detail rendered only when WARN is on"
            generaltools.failureSeam -> generaltools.valueCore.twoDoubles "Result.failure(IllegalArgumentException)"
            generaltools.valueCore.twoDoubles -> consumer "the failure; p unchanged"
            consumer -> generaltools.valueCore.twoDoubles "p divideBy 2.0: both components divided in place, Result.success(Unit)"
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
