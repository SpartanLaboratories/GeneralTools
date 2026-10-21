package com.spartanlabs.generaltools

/**
 * Marks a public surface as a Supported Extension: a seam for a likely but non-core need, one a fair number of
 * consumers will want but that is not what this library is fundamentally for.
 *
 * The tier marks purpose, not a weaker promise: a Supported Extension carries the same compatibility
 * guarantee as the Stable Core, and changes only in a major release. The shipped implementations double as
 * worked examples of how to extend that seam, so they are written to be read.
 *
 * This is not an opt-in and gates nothing: it does not need `@OptIn`, produces no warning, and is not
 * checked by the compiler. It documents the tier, and consumers' IDEs show it from the compiled library.
 *
 * Surfaces that carry it: the value-returning arithmetic in `com.spartanlabs.geometry.arithmetic`, and the
 * `TwoDoubles` subclassing seam once it graduates from Experimental
 * ([ExperimentalForInheritanceGeneralToolsApi]).
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
)
public annotation class SupportedExtension
