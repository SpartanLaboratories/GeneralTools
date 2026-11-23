// color — C4 model as code (Structurizr DSL).
// Source of truth for the structure shown on docs/dashboards/color/. The board's C4 tab is rendered from this file.
// As-is at GeneralTools feat/issue-7-kotlin-multiplatform a500e44 (2026-11-22), version 3.0.0, unreleased. Code is authoritative on any conflict.
workspace "GeneralTools" "A Kotlin Multiplatform library of generic helpers: Color, 2D geometry and small utilities in common code for the JVM, JS and Wasm/JS, with file, URL and desktop helpers on the JVM." {

    !identifiers hierarchical

    model {
        consumer = person "Consumer" "A Kotlin project on the JVM, JS or Wasm/JS that builds, transforms and encodes colours."

        generaltools = softwareSystem "GeneralTools" "A Kotlin Multiplatform library of generic helpers, built and published as one artifact." {
            colorValue = container "Color" "The validated RGBA value type Color, every channel in 0..255, with its transforms, its luminance, the clamping factory Color.of and its codecs." "Kotlin Multiplatform · common code · 3.0.0" {

                // ---- public surface ----
                colorType  = component "Color" "public data class Color(red, green, blue, alpha = 255): an immutable value; the constructor and copy validate every channel." "final data class" "Public"
                transforms = component "Transforms" "lightened, darkened, withAlpha, inverted, grayscale, lerp and luminance: each returns a new Color, or a Float for luminance; none throws." "member functions" "Public"
                encoders   = component "Encoders" "toHex (#RRGGBB or #AARRGGBB), toPackedInt (ARGB in one Int) and normalized (four floats in 0..1)." "member functions" "Public"
                named      = component "Named colours" "Fourteen constants on the companion, WHITE to TRANSPARENT, each built through the constructor." "companion vals" "Public"
                factoryOf  = component "Color.of" "Builds a Color with every channel clamped into 0..255: the factory for values that may be out of range." "companion function" "Public"
                decoders   = component "Decoders" "fromHex (#RGB, #RRGGBB, #AARRGGBB, # or 0x optional), fromPackedInt and fromNormalized." "companion functions" "Public"

                // ---- internals ----
                invariant  = component "Channel invariant" "init { require(channel in 0..255) } for red, green, blue, alpha in that order; the message names the first channel out of range and its value." "init block" "Internal"
            }
            testSuite   = container "Test suite" "The tests by named test set, in commonTest and jvmTest." "Kotlin Multiplatform · tests"
            gradleBuild = container "Gradle build" "Builds, tests, checks and publishes the library." "Gradle 9.0.0 · Kotlin 2.4.21" {
                moduleLayout = component "Module layout and published shape" "GeneralTools-core, GeneralTools-desktop and the source-less GeneralTools umbrella." "planned" "Internal,Planned"
            }
        }

        // People and systems
        consumer -> generaltools.colorValue "Builds, transforms and encodes colours with"
        consumer -> generaltools.colorValue.colorType "Constructs; copy"
        consumer -> generaltools.colorValue.factoryOf "Clamps untrusted channels with"
        consumer -> generaltools.colorValue.decoders "Parses hex, packed and normalized values with"

        // Inside Color
        generaltools.colorValue.colorType -> generaltools.colorValue.invariant "Validates through, at construction and copy"
        generaltools.colorValue.transforms -> generaltools.colorValue.invariant "Build each result through the constructor"
        generaltools.colorValue.factoryOf -> generaltools.colorValue.invariant "Clamps, then constructs"
        generaltools.colorValue.decoders -> generaltools.colorValue.invariant "Construct; a negative hex channel is refused"
        generaltools.colorValue.named -> generaltools.colorValue.invariant "Built through the constructor"
        generaltools.colorValue.colorType -> generaltools.colorValue.transforms "Offers"
        generaltools.colorValue.colorType -> generaltools.colorValue.encoders "Offers"
        generaltools.colorValue.decoders -> generaltools.colorValue.encoders "Inverse of toHex and toPackedInt"

        // Tests
        generaltools.testSuite -> generaltools.colorValue "ColorTest, ColorHexEncodingTest, ColorTransformRangeTest"

        // Planned
        generaltools.gradleBuild.moduleLayout -> generaltools.colorValue "Would place Color.kt in GeneralTools-core"
    }

    views {
        systemContext generaltools "Context" "GeneralTools and who touches it." {
            include *
            autoLayout lr
        }

        container generaltools "Containers" "The systems of GeneralTools that touch Color." {
            include *
            autoLayout lr
        }

        component generaltools.colorValue "Components" "Color as built at a500e44." {
            include *
            autoLayout tb
        }

        dynamic generaltools.colorValue "FromHex" "A three-digit hex parsed into a Color, as built." {
            consumer -> generaltools.colorValue.decoders "Color.fromHex(#F53)"
            generaltools.colorValue.decoders -> generaltools.colorValue.invariant "Color(255, 85, 51): each nibble doubled, alpha 255"
            generaltools.colorValue.invariant -> generaltools.colorValue.colorType "every channel in 0..255"
            generaltools.colorValue.colorType -> consumer "the new Color"
            consumer -> generaltools.colorValue.encoders "toHex()"
            generaltools.colorValue.encoders -> consumer "#FF5533"
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
