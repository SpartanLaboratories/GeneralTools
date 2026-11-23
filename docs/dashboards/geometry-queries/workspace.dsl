// geometry-queries — C4 model as code (Structurizr DSL). Source of truth for the structure shown on the board beside it.
// As-is at GeneralTools feat/issue-7-kotlin-multiplatform a500e44 (2026-11-22), geometry-queries 3.0.0. Code is authoritative on any conflict.
workspace "GeneralTools" "A Kotlin Multiplatform library of generic helpers: Color, 2D geometry and small utilities for the JVM, JS and Wasm/JS." {

    !identifiers hierarchical

    model {
        consumer = person "Consumer" "A Kotlin project on the JVM, JS or Wasm/JS that measures vectors and tests segments and rays against segments and boxes."
        generaltools = softwareSystem "GeneralTools" "The repository as a software system: one Kotlin Multiplatform library, published as one artifact." {
            valueCore = container "Geometry value core" "The mutable coordinate-pair value types TwoDoubles, Point and Dimensions with their equality contract." "Kotlin Multiplatform · commonMain"
            boxModel = container "Box model" "AxisAlignedBox with its primitive accessors and operator contains, in three storage forms." "Kotlin Multiplatform · commonMain"
            failureSeam = container "Failure and logging seam" "The internal KLogger.failure helper: one WARN line on the caller's logger and a Result.failure(IllegalArgumentException)." "Kotlin Multiplatform · commonMain · internal" "Internal"
            queries = container "Geometry queries" "Vector algebra on Point, Segment, Ray and the segment and ray intersection tests against segments and boxes." "Kotlin Multiplatform library · commonMain · 3.0.0" {
                // ---- public surface ----
                vectors   = component "Vector algebra" "EPSILON and the Point extensions dot, cross, length, lengthSquared, normalized and projectedOnto." "top-level declarations · Vectors.kt · facade VectorsKt" "Public"
                segment   = component "Segment" "A directed segment from a to b: delta, length, lengthSquared, pointAt and asRay." "data class · Segment.kt" "Public"
                ray       = component "Ray" "A half-line from origin along direction: pointAt and unit." "data class · Ray.kt" "Public"
                outcome   = component "SegmentIntersection" "The three-way result of a segment test: None, Touching(point) or Overlapping(segment)." "sealed interface · Intersections.kt" "Public"
                intersect = component "Intersection tests" "segmentIntersectsSegment, segmentIntersectsBox and rayIntersectsBox, each returning a kotlin.Result." "top-level functions · Intersections.kt · facade IntersectionsKt" "Public"
                // ---- internals ----
                boxCheck  = component "boxProblem" "The first problem that makes a box unusable for a test: a size that is not finite or is negative." "private function · Intersections.kt" "Internal"
            }
        }
        backend = softwareSystem "Logging backend" "The consumer's logging: an slf4j backend on the JVM, the console at one global level on JS and Wasm/JS." "External"

        consumer -> generaltools.queries.intersect "Tests segments and rays against segments and boxes"
        consumer -> generaltools.queries.vectors "Measures, normalises and projects vectors"
        consumer -> generaltools.queries.segment "Builds segments, samples points, turns one into a ray"
        consumer -> generaltools.queries.ray "Builds rays, samples points, takes the unit ray"
        generaltools.queries.intersect -> generaltools.queries.outcome "Answers a segment pair with one variant"
        generaltools.queries.intersect -> generaltools.queries.segment "Reads endpoints, delta, length and pointAt"
        generaltools.queries.intersect -> generaltools.queries.ray "Reads origin and direction"
        generaltools.queries.intersect -> generaltools.queries.vectors "cross, dot and length against EPSILON"
        generaltools.queries.intersect -> generaltools.queries.boxCheck "Validates the box before any arithmetic"
        generaltools.queries.segment -> generaltools.queries.vectors "length of delta against EPSILON"
        generaltools.queries.segment -> generaltools.queries.ray "asRay builds a Ray at a along delta"
        generaltools.queries.ray -> generaltools.queries.vectors "unit through normalized"
        generaltools.queries.vectors -> generaltools.valueCore "Reads x, y and containsNaN; builds new Points"
        generaltools.queries.intersect -> generaltools.boxModel "Reads minX, minY, maxX, maxY; operator contains"
        generaltools.queries.boxCheck -> generaltools.boxModel "Reads width and height"
        generaltools.queries.intersect -> generaltools.failureSeam "failure(message, detail) on an invalid input"
        generaltools.queries.segment -> generaltools.failureSeam "failure(message, detail) on a degenerate segment"
        generaltools.queries.vectors -> generaltools.failureSeam "failure(message, detail) on a zero-length vector"
        generaltools.failureSeam -> backend "One WARN line on the calling file's logger, no throwable"
    }

    views {
        systemContext generaltools "Context" "GeneralTools and who touches it, from the geometry queries' side." {
            include *
            autoLayout lr
        }
        container generaltools "Containers" "The systems the geometry queries use." {
            include *
            autoLayout lr
        }
        component generaltools.queries "Components" "geometry-queries as built at a500e44." {
            include *
            autoLayout tb
        }
        dynamic generaltools.queries "RayAgainstBox" "rayIntersectsBox(r, box), as built: validation first, then the slab test." {
            consumer -> generaltools.queries.intersect "rayIntersectsBox(r, box)"
            generaltools.queries.intersect -> generaltools.queries.boxCheck "boxProblem(box)"
            generaltools.queries.boxCheck -> generaltools.boxModel "width, height"
            generaltools.queries.intersect -> generaltools.boxModel "minX, minY, maxX, maxY, each read once"
            generaltools.queries.intersect -> generaltools.queries.vectors "direction.length against EPSILON"
            generaltools.queries.intersect -> generaltools.failureSeam "failure(message, detail), on an invalid input only"
            generaltools.failureSeam -> backend "WARN message: detail"
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
