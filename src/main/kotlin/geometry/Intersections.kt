package com.spartanlabs.geometry

import org.slf4j.LoggerFactory
import kotlin.math.abs

/** Logger used for diagnostic output from this file's functions. */
private val log = LoggerFactory.getLogger("com.spartanlabs.geometry.Intersections")

//region Result types

/**
 * The outcome of [segmentIntersectsSegment].
 *
 * A sum type rather than `Point?` because the collinear-overlap case (an
 * along-wall ray, a sliding swept-collision contact) is real and cannot be
 * expressed by a single nullable point.
 */
sealed interface SegmentIntersection {
    /** The two segments do not meet anywhere. */
    data object None : SegmentIntersection

    /**
     * The two segments meet at exactly one [point] — a proper crossing, a
     * shared endpoint, a T-junction, or a collinear end-to-end touch.
     *
     * @property point the single intersection point.
     */
    data class Touching(val point: Point) : SegmentIntersection

    /**
     * The two segments are collinear and share the sub-segment [segment], which
     * has non-zero length.
     *
     * @property segment the overlapping portion, oriented along the first
     *   segment.
     */
    data class Overlapping(val segment: Segment) : SegmentIntersection
}

//endregion

//region Segment / segment

/**
 * 2D segment/segment intersection via the perp-dot (cross-product) method.
 *
 * Non-parallel segments yield [SegmentIntersection.None] or a single
 * [SegmentIntersection.Touching] point; collinear segments yield `None`,
 * `Touching` (end-to-end), or [SegmentIntersection.Overlapping]. Two segments
 * whose cross product is within [EPSILON] of zero are treated as parallel /
 * collinear (deterministic; see plan §6).
 *
 * @param s1 the first segment; its parameterisation orients any `Touching` /
 *   `Overlapping` result.
 * @param s2 the second segment.
 * @return `Result.failure(IllegalArgumentException)` if either segment is
 *   degenerate ([Segment.length] `< EPSILON`) or contains `NaN`; otherwise
 *   `Result.success` of one [SegmentIntersection] variant.
 */
fun segmentIntersectsSegment(s1: Segment, s2: Segment): Result<SegmentIntersection> {
    if (s1.a.containsNaN || s1.b.containsNaN || s2.a.containsNaN || s2.b.containsNaN) {
        log.warn("segmentIntersectsSegment: NaN coordinate in {} or {}", s1, s2)
        return Result.failure(IllegalArgumentException("Segments must have finite coordinates"))
    }
    if (s1.length < EPSILON || s2.length < EPSILON) {
        log.warn("segmentIntersectsSegment: degenerate segment ({} or {})", s1, s2)
        return Result.failure(IllegalArgumentException("Segments must be longer than EPSILON"))
    }

    val r = s1.delta
    val s = s2.delta
    val rxs = r cross s
    val qmp = Point(s2.a.x - s1.a.x, s2.a.y - s1.a.y)

    if (abs(rxs) > EPSILON) {
        // Lines are not parallel: a unique line/line intersection exists at
        // s1.pointAt(t). It is a segment intersection only when both parameters
        // fall inside their own [0, 1] range.
        val t = (qmp cross s) / rxs
        val u = (qmp cross r) / rxs
        return if (t in 0.0..1.0 && u in 0.0..1.0) {
            Result.success(SegmentIntersection.Touching(s1.pointAt(t)))
        } else {
            Result.success(SegmentIntersection.None)
        }
    }

    // rxs ~ 0 => the segments are parallel. If (q - p) x r is also ~ 0 they are
    // collinear; otherwise they lie on distinct parallel lines and cannot meet.
    if (abs(qmp cross r) > EPSILON) {
        return Result.success(SegmentIntersection.None)
    }

    // Collinear: project s2's endpoints onto s1's direction as fractions of r,
    // then intersect [t0, t1] with s1's own [0, 1] range.
    val rr = r dot r
    val t0 = (qmp dot r) / rr
    val t1 = t0 + (s dot r) / rr
    val overlapLo = maxOf(0.0, minOf(t0, t1))
    val overlapHi = minOf(1.0, maxOf(t0, t1))

    return when {
        overlapHi < overlapLo - EPSILON -> Result.success(SegmentIntersection.None)
        overlapHi - overlapLo <= EPSILON ->
            Result.success(SegmentIntersection.Touching(s1.pointAt(overlapLo)))
        else -> Result.success(
            SegmentIntersection.Overlapping(Segment(s1.pointAt(overlapLo), s1.pointAt(overlapHi)))
        )
    }
}

//endregion

//region Segment / box, ray / box

/**
 * Whether any part of segment [s] lies inside or on [box].
 *
 * Slab test along the segment's own parameterisation clamped to `[0, 1]`, with
 * an endpoint-inside short-circuit for the fully-contained case. [box] is any
 * [AxisAlignedBox] — a [Square] or a [CenteredBox]; only [AxisAlignedBox.min] /
 * [AxisAlignedBox.max] are read, so the origin convention does not matter.
 *
 * @param s the segment to test.
 * @param box the box to test against.
 * @return `Result.failure(IllegalArgumentException)` if [s] is degenerate /
 *   non-finite or [box] has negative or non-finite [AxisAlignedBox.size];
 *   otherwise `Result.success` of the boolean (a segment fully outside the box
 *   yields `success(false)`).
 */
fun segmentIntersectsBox(s: Segment, box: AxisAlignedBox): Result<Boolean> {
    boxProblem(box)?.let {
        log.warn("segmentIntersectsBox: {}", it)
        return Result.failure(IllegalArgumentException(it))
    }
    if (s.a.containsNaN || s.b.containsNaN || s.length < EPSILON) {
        log.warn("segmentIntersectsBox: degenerate or non-finite segment {}", s)
        return Result.failure(
            IllegalArgumentException("Segment must be finite and longer than EPSILON")
        )
    }

    // Cheap path: an endpoint inside (or on) the box guarantees an intersection
    // and covers the fully-contained segment.
    if (box contains s.a || box contains s.b) return Result.success(true)

    val dir = s.delta
    // invD may be +/-Infinity for an axis-parallel segment: intentional under
    // IEEE-754 (see plan §2.1). minOf / maxOf on Double delegate to
    // java.lang.Math.min / max, which PROPAGATE NaN, so a 0 * Infinity that
    // arises when an endpoint sits exactly on a slab plane collapses the
    // interval and the final `tmax >= tmin` test cleanly reports "no overlap".
    val invDx = 1.0 / dir.x
    val invDy = 1.0 / dir.y
    var tmin = 0.0
    var tmax = 1.0

    val tx1 = (box.min.x - s.a.x) * invDx
    val tx2 = (box.max.x - s.a.x) * invDx
    tmin = maxOf(tmin, minOf(tx1, tx2))
    tmax = minOf(tmax, maxOf(tx1, tx2))

    val ty1 = (box.min.y - s.a.y) * invDy
    val ty2 = (box.max.y - s.a.y) * invDy
    tmin = maxOf(tmin, minOf(ty1, ty2))
    tmax = minOf(tmax, maxOf(ty1, ty2))

    // NaN in either bound makes this comparison false -> reported as a miss.
    return Result.success(tmax >= tmin)
}

/**
 * Parametric entry distance of ray [r] into [box] via the NaN-safe slab method.
 *
 * [box] is any [AxisAlignedBox] — a [Square] or a [CenteredBox]; only
 * [AxisAlignedBox.min] / [AxisAlignedBox.max] are read.
 *
 * @param r the ray; [Ray.direction] need not be unit length.
 * @param box the box to test against.
 * @return `Result.failure(IllegalArgumentException)` for a zero-direction or
 *   non-finite ray, or a [box] with negative or non-finite
 *   [AxisAlignedBox.size] or non-finite corners;
 *   `Result.success(null)` on a clean miss or a box entirely behind the origin;
 *   `Result.success(0.0)` when the origin is inside (or on) the box;
 *   otherwise `Result.success(tEntry)` where `tEntry > 0` is measured in units
 *   of `r.direction.length` (use [Ray.pointAt] or [Ray.unit]).
 */
fun rayIntersectsBox(r: Ray, box: AxisAlignedBox): Result<Double?> {
    boxProblem(box)?.let {
        log.warn("rayIntersectsBox: {}", it)
        return Result.failure(IllegalArgumentException(it))
    }
    if (!box.min.x.isFinite() || !box.min.y.isFinite() || !box.max.x.isFinite() || !box.max.y.isFinite()) {
        log.warn("rayIntersectsBox: non-finite box corner ({}, {})", box.min, box.max)
        return Result.failure(IllegalArgumentException("Box corners must be finite"))
    }
    if (r.origin.containsNaN || r.direction.containsNaN ||
        !r.origin.x.isFinite() || !r.origin.y.isFinite() ||
        !r.direction.x.isFinite() || !r.direction.y.isFinite()
    ) {
        log.warn("rayIntersectsBox: non-finite ray {}", r)
        return Result.failure(IllegalArgumentException("Ray must have finite coordinates"))
    }
    if (r.direction.length < EPSILON) {
        log.warn("rayIntersectsBox: zero-direction ray {}", r)
        return Result.failure(IllegalArgumentException("Ray direction must be longer than EPSILON"))
    }

    // invD is +/-Infinity for an axis-parallel ray: intentional under IEEE-754
    // (a parallel-and-outside slab leaves tmin/tmax at -/+Infinity -> miss; a
    // parallel-and-inside slab leaves the constraint inert). The one hazard is
    // 0 * Infinity = NaN when the origin lies exactly on a slab plane; because
    // minOf / maxOf on Double delegate to java.lang.Math.min / max (which
    // PROPAGATE NaN, unlike SSE minps/maxps), that NaN reaches the final test
    // and `tmax >= maxOf(tmin, 0.0)` evaluates false -> a deterministic clean
    // miss. See plan §2.1.
    val invDx = 1.0 / r.direction.x
    val invDy = 1.0 / r.direction.y
    var tmin = Double.NEGATIVE_INFINITY
    var tmax = Double.POSITIVE_INFINITY

    val tx1 = (box.min.x - r.origin.x) * invDx
    val tx2 = (box.max.x - r.origin.x) * invDx
    tmin = maxOf(tmin, minOf(tx1, tx2))
    tmax = minOf(tmax, maxOf(tx1, tx2))

    val ty1 = (box.min.y - r.origin.y) * invDy
    val ty2 = (box.max.y - r.origin.y) * invDy
    tmin = maxOf(tmin, minOf(ty1, ty2))
    tmax = minOf(tmax, maxOf(ty1, ty2))

    // Hit iff the exit parameter is at or beyond both the entry parameter and
    // the origin. Any NaN (from the 0 * Infinity grazing case) makes this false.
    if (!(tmax >= maxOf(tmin, 0.0))) return Result.success(null)
    // Clamp a negative entry to 0.0 -> "origin is inside the box".
    return Result.success(if (tmin < 0.0) 0.0 else tmin)
}

//endregion

//region Shared validation

/**
 * Describes the first problem that makes [box] unusable for an intersection
 * test, or `null` if it is valid. `size == 0` on an axis is allowed (a
 * degenerate line/point box is a valid limit); only negative or non-finite
 * size fails.
 */
private fun boxProblem(box: AxisAlignedBox): String? {
    val size = box.size
    if (size.containsNaN || !size.width.isFinite() || !size.height.isFinite()) {
        return "Box size must be finite"
    }
    if (size.width < 0.0 || size.height < 0.0) {
        return "Box size must be non-negative"
    }
    return null
}

//endregion
