// box-model — C4 model as code (Structurizr DSL).
// Source of truth for the structure shown on docs/dashboards/box-model/. The board's C4 tab is rendered from this file.
// As-is at GeneralTools feat/issue-7-kotlin-multiplatform a500e44 (2026-11-22), box model 3.0.0 (unreleased; Maven Central latest 2.2.0). Code is authoritative on any conflict.
workspace "GeneralTools" "A Kotlin Multiplatform library of generic helpers: Color, 2D geometry and small utilities in common code for the JVM, JS and Wasm/JS." {

    !identifiers hierarchical

    model {
        consumer = person "Library consumer" "Kotlin code on the JVM, JS or Wasm/JS that builds axis-aligned boxes, tests points against them, hands them out and implements its own."

        generaltools = softwareSystem "GeneralTools" "One Kotlin Multiplatform library module, published as GeneralTools with its jvm, js and wasm-js artifacts." {
            boxModel = container "Box model" "One shape contract for axis-aligned rectangles, AxisAlignedBox with its primitive accessors and operator contains, in three storage forms, the mutable Rectangle, the ImmutableRectangle copy and CenteredBox, and the conversions between them." "Kotlin common code · 3.0.0" {

                // ---- public surface ----
                axisAlignedBox     = component "AxisAlignedBox" "The shape contract: min, max, center and size; six primitive accessors with default bodies; operator contains. A shape contract, not a read-only guarantee." "interface" "Public"
                rectangle          = component "Rectangle" "The mutable form: a top-left location and its dimensions behind val references, the parts live and mutable; toImmutable()." "final data class" "Public"
                immutableRectangle = component "ImmutableRectangle" "The immutable copy: x, y, width and height fixed at construction; every corner, centre or size read a new value; value equality; copy(...)." "final class" "Public"
                centeredBox        = component "CenteredBox" "The centre-origin form: center and halfExtents; fromCorners in any order; treated as immutable." "final data class" "Public"
                conversions        = component "Conversions" "Rectangle.toCenteredBox() and AxisAlignedBox.toRectangle(), an independent mutable copy; facade CenteredBoxKt." "extension functions" "Public"
            }
            valueCore = container "Geometry value core" "TwoDoubles, Point and Dimensions with their equality contract and its internal helpers." "Kotlin common code · 3.0.0"
            queries   = container "Geometry queries" "Vector algebra, Segment, Ray and the segment and ray intersection tests against boxes." "Kotlin common code · 3.0.0"
        }

        // People and systems
        consumer -> generaltools.boxModel "Builds boxes, tests points against them, hands them out"
        consumer -> generaltools.boxModel.rectangle "Builds; moves and resizes through location and dimensions"
        consumer -> generaltools.boxModel.axisAlignedBox "Implements its own boxes; p in box"
        consumer -> generaltools.boxModel.immutableRectangle "Hands out a box its callers cannot change"

        // Inside the box model
        generaltools.boxModel.rectangle -> generaltools.boxModel.axisAlignedBox "Implements; min and size are its live parts"
        generaltools.boxModel.immutableRectangle -> generaltools.boxModel.axisAlignedBox "Implements; every read a new value"
        generaltools.boxModel.centeredBox -> generaltools.boxModel.axisAlignedBox "Implements; min, max and size from the accessors"
        generaltools.boxModel.rectangle -> generaltools.boxModel.immutableRectangle "toImmutable(): copies the four numbers"
        generaltools.boxModel.immutableRectangle -> consumer "Returned by toImmutable(); handed out on every read"
        generaltools.boxModel.conversions -> generaltools.boxModel.centeredBox "toCenteredBox(): a new box over the same footprint"
        generaltools.boxModel.conversions -> generaltools.boxModel.rectangle "toRectangle(): a new Rectangle with a new Point and Dimensions"

        // The value core underneath
        generaltools.boxModel.rectangle -> generaltools.valueCore "Holds a live Point and Dimensions"
        generaltools.boxModel.centeredBox -> generaltools.valueCore "Holds a Point and Dimensions; copy() shares them"
        generaltools.boxModel.immutableRectangle -> generaltools.valueCore "equals and hashCode through componentEquals and componentHash"

        // Systems built on it
        generaltools.queries -> generaltools.boxModel.axisAlignedBox "segmentIntersectsBox and rayIntersectsBox read the six accessors"
    }

    views {
        systemContext generaltools "Context" "GeneralTools and who touches it." {
            include *
            autoLayout lr
        }

        container generaltools "Containers" "The box model and the systems it meets." {
            include *
            autoLayout lr
        }

        component generaltools.boxModel "Components" "The box model as built at feat/issue-7-kotlin-multiplatform a500e44." {
            include *
            autoLayout tb
        }

        dynamic generaltools.boxModel "HandOut" "A library copies its box once and hands the copy out, as built." {
            consumer -> generaltools.boxModel.rectangle "Rectangle(location, dimensions), then toImmutable()"
            generaltools.boxModel.rectangle -> generaltools.boxModel.immutableRectangle "ImmutableRectangle(minX, minY, width, height)"
            generaltools.boxModel.immutableRectangle -> consumer "the copy, the same instance on every read"
            consumer -> generaltools.boxModel.rectangle "location.setTo(...): the live part moves; the copy does not"
            consumer -> generaltools.boxModel.immutableRectangle "p in bounds: reads x, y, width, height without allocating"
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
