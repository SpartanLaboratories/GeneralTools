package com.spartanlabs.generaltools

/**
 * Opt-in marker for subclassing [com.spartanlabs.geometry.TwoDoubles], an Experimental seam.
 *
 * Subclassing `TwoDoubles` is allowed, but its shape has not yet been proven by a consumer and may change
 * in a minor release. Using `TwoDoubles`, [com.spartanlabs.geometry.Point] and
 * [com.spartanlabs.geometry.Dimensions] (as values, parameters or receivers) needs no opt-in; only a class
 * that extends `TwoDoubles` does:
 *
 *     @OptIn(ExperimentalForInheritanceGeneralToolsApi::class)
 *     class Velocity(dx: Double, dy: Double) : TwoDoubles(dx, dy)
 *
 * The marker has no targets, so it can only be passed to `@OptIn` or `@SubclassOptInRequired`; it gates
 * inheritance and nothing else. The level is ERROR: lowering a level later never breaks a consumer, raising
 * one would. Once a consumer has built against the seam it graduates to a Supported Extension
 * ([SupportedExtension]) and this marker is retired from `TwoDoubles`; the release notes of that release
 * record the promotion.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Subclassing TwoDoubles is Experimental and may change in a minor release. " +
        "Opt in with @OptIn(ExperimentalForInheritanceGeneralToolsApi::class).",
)
@Retention(AnnotationRetention.BINARY)
@Target()
public annotation class ExperimentalForInheritanceGeneralToolsApi
