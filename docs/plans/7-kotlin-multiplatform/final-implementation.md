# Final implementation: issue #7 — GeneralTools 3.0.0 on Kotlin Multiplatform

The as-built record for
[SpartanLaboratories/GeneralTools#7](https://github.com/SpartanLaboratories/GeneralTools/issues/7).
The design is [architecture.md](architecture.md); each unit's plan is a `plan-<unit>.md` beside it.
The implementer appends deviations here as they happen; the planner reconciles the file into the
as-built record once implementation ends.

## Deviations

### `kmp-build` — C1 baseline dump: `apiDump` run on JDK 17, not the default JDK 23

- **Plan:** [plan-kmp-build.md](plan-kmp-build.md) §7 "C1 - baseline dump", step 3: "`./gradlew apiDump`
  (Gradle runs on the installed JDK 17-24 ...)".
- **What differs:** the first run on the machine's default JDK (`JAVA_HOME` = Oracle JDK 23.0.2) failed in
  `:apiBuild` with `Unsupported class file major version 67`: tag `2.2.0` sets no toolchain or `jvmTarget`,
  so `kotlin("jvm") 2.2.0` emits bytecode for the running JDK (23), which binary-compatibility-validator
  `0.18.2`'s class reader cannot read. The dump was produced by re-running the same command with
  `JAVA_HOME` pointed at the Gradle-provisioned Temurin 17.0.20 (`~/.gradle/jdks/eclipse_adoptium-17-amd64-windows.2`),
  from a clean scratch (`build/`, `.gradle/`, `.kotlin/` removed).
- **Why:** JDK 17 is inside the plan's stated 17-24 range and matches the JDK CI builds `2.2.0` with
  (`.github/workflows/Build.yml`, Java 17). The plan's "JDK 17-24" is therefore too wide for this step: at least
  JDK 23 does not work. No file content was edited; the dump is copied byte-for-byte from the scratch.

### `kmp-build` — C1 carried eight files; deviation entries ride with the commit they describe

- **Plan:** [plan-kmp-build.md](plan-kmp-build.md) §11 (the C1 row lists seven paths) and §9 Level 1 ("Common to
  C2-C7: ... no `docs/plans/` file is staged").
- **What differs:** C1 (`8099065`) carries eight files: the seven of the §11 C1 row plus this file,
  `final-implementation.md`, whose first entry describes C1's work. From C2 on, each entry appended here is committed
  with the commit whose work it describes, so `docs/plans/7-kotlin-multiplatform/final-implementation.md` is the one
  `docs/plans/` path that may be staged in C2-C7; §9's check holds for every other planning document.
- **Why:** the user's planning rule commits documents under `docs/plans/` with the work they describe, and the
  implementer logs each deviation at the moment it is made, so the entry belongs to the same commit as the change.

### `kmp-build` — spike procedures S3 and S2 run with four mechanical changes

- **Plan:** [plan-kmp-build.md](plan-kmp-build.md) §9 Level 3, S3 and S2 steps 1, 2, 4 and 5 (Scratch A, built fresh
  from `3d25a7e` with the C2 moves and the 6.2 build script).
- **What differs:**
  - S3: the single invocation `./gradlew checkKotlinAbi updateKotlinAbi` fails both tasks in Gradle 9.0.0 task
    validation ("Task ':checkKotlinAbi' uses this output of task ':updateKotlinAbi' without declaring an explicit or
    implicit dependency"). The two tasks were run as separate invocations: `checkKotlinAbi`, `updateKotlinAbi`,
    `checkKotlinAbi`.
  - S2 step 1: `--info` prints no compiler arguments under the Kotlin daemon (the `grep` matched nothing). The step was
    re-run as `./gradlew compileKotlinJs compileKotlinWasmJs compileCommonMainKotlinMetadata --rerun-tasks --debug`,
    whose `[KOTLIN]` argument lines carry `-api-version 2.2 -language-version 2.2` for all three compilations.
  - S2 steps 2 and 5: step 2's `check` fails in `checkKotlinAbi`, because the spike's public `halve` changes both dumps.
    Step 5 (`updateKotlinAbi` with the spike present) was therefore run before step 2.
  - S2 step 4: `kotlin-js-store/` was copied to the session scratchpad (`kotlin-js-store-for-C6/`), not into the
    repository. A copy in the working tree before C6 would break the §9 Level 1 check for C2-C5b that
    `git status --short` lists only that commit's paths.
- **Why:** each change keeps the question the step asks and the evidence it records; none touches the repository or
  the committed design.

### `explicit-api` — `TwoDoubles` line 13 keeps `second:Double` without a space

- **Plan:** [plan-explicit-api.md](plan-explicit-api.md) §7.9, row for line 13 (`TwoDoubles` and its two constructor
  properties), whose "resulting text" reads `public var second: Double = 0.0`.
- **What differs:** the line reads `public sealed class TwoDoubles(public var first: Double = 0.0, public var
  second:Double = 0.0){`. Only the two `public` tokens and the class's `public` were inserted; the existing
  `second:Double` (no space after the colon) was left as it was.
- **Why:** §4 ("No KDoc, comment or formatting change beyond the inserted tokens") and §5 ("four mechanical forms,
  and nothing else") forbid the extra space; every other "resulting text" row in §7 keeps the original spacing
  (e.g. `IJColor(public val colorCode:String)`, `setTo(new: TwoDoubles): Unit =    setTo(...)`). The compiled class
  is unaffected either way.

### `logging` — commit 1 replaces `kmp-build`'s placeholder comment in the `commonMain` block

- **Plan:** [plan-logging.md](plan-logging.md) §6 "Commit 1", `build.gradle.kts`: "add one line" (the
  kotlin-logging `implementation`) "with the comment that it is runtime-only on the JVM variant and that `jvmMain`'s
  `api(slf4j-api)` is what supplies slf4j ... Nothing else in the build changes."
- **What differs:** besides the added dependency line and its comment, the `commonMain` block's placeholder comment
  ``// implementation(kotlin-logging) is added here by the `logging` unit.`` (left by `kmp-build`) is removed; the
  required comment takes its place on the line above the dependency.
- **Why:** the placeholder marks where this unit's line goes and states it is still to come; kept, it would describe
  a dependency that is now present. No other line of the build script changes.

### `logging` — commit 2 ends `Point.distanceFrom`'s KDoc failure sentence with a full stop

- **Plan:** [plan-logging.md](plan-logging.md) §6 "Commit 2" ("The KDoc on the public function gets one added
  sentence") and §8 "KDoc" (one added sentence on `Point.distanceFrom`).
- **What differs:** besides the added sentence, the existing last line of `Point.distanceFrom`'s KDoc,
  "Returns a failed [Result] if either point contains a NaN value", gains a closing full stop.
- **Why:** the line had no terminal punctuation, so KDoc would run it and the added sentence together as
  "... a NaN value On failure also logs ...". The other five KDoc blocks already end their sentence with a full stop
  and are changed only by the added line.

### `logging` — commit 2 drops the "was" comment on `divideBy`'s ERROR line

- **Plan:** [plan-logging.md](plan-logging.md) §6 "Commit 2", `TwoDoubles.kt`, whose code shows
  ``log.error { ... }   // was log.error("... {} ...", simpleName)``.
- **What differs:** the line carries no trailing comment: `log.error { "Attempted to divide a ${this::class.simpleName} by zero" }`.
- **Why:** the plan contradicted itself. Its own §9 Level 1 grep #2 (`log\.(warn|error|info|debug|trace)\("` over
  `src/jvmMain/kotlin/geometry`, "expect no output") matched that comment. The user chose (2026-10-20) to drop the
  comment so the grep prints nothing.

### `logging` — commit 2 adds the `Ray.unit` KDoc sentence, a KDoc-only change to `Ray.kt`

- **Plan:** [plan-logging.md](plan-logging.md) §8 "KDoc" asks for a sentence on `Ray.unit` ("failure is reported by
  [Point.normalized], which logs one WARN line"). §2 ("`Ray.kt` is unchanged by this unit"), §4 (non-goal "Any change to
  `Ray.kt`"), §6 "Commit 2" (five geometry files) and §11 row 2 ("five geometry files") leave `Ray.kt` out.
- **What differs:** `Ray.unit`'s KDoc gains the line "Failure is reported by [Point.normalized], which logs one WARN
  line." at the end of its description, where the other six functions got their sentence. No code in `Ray.kt` changes,
  and `Ray.kt` is part of commit 2.
- **Why:** §8 contradicted the "Ray.kt unchanged" statements. The user chose (2026-10-20) to add the sentence as a
  KDoc-only change.

### `logging` — commit 2 puts the README quick-reference note inside the parentheses

- **Plan:** [plan-logging.md](plan-logging.md) §8, "Geometry quick reference": in the sentence that begins
  "Intersection functions (all return `Result`; ...)" *append* "Each failure also logs one WARN line (see Logging)."
- **What differs:** the note sits inside the parenthetical, before the colon:
  "... negative / non-finite `box.size`; each failure also logs one WARN line, see Logging):".
- **Why:** appending literally after the sentence's closing `):` broke the colon that leads into the table. The user
  chose (2026-10-20) the in-parentheses wording, which keeps that lead-in.

### `logging` — commit 3 has no README intro clause to remove

- **Plan:** [plan-logging.md](plan-logging.md) §6 "Commit 3", `README.md`: "remove the clause naming a 'lightweight
  logging wrapper' from the intro, in whatever form `kmp-build` left it" (assumption L6).
- **What differs:** commit 3 does not edit the README intro. The commit removes only the `com.spartanlabs.logging`
  Modules bullet and appends the removal migration row.
- **Why:** the clause was already gone. `kmp-build`'s `0f6ddde` rewrote the intro (`git log -S "logging wrapper"
  -- README.md` lists it as the last commit to touch the text), and the intro at `978c4a6` names no logging wrapper
  in any form. L6's "remove whichever form is present" therefore removes nothing.

### `commonize` — commit 1 names the other file in `MiscJvm.kt`'s facade comment

- **Plan:** [plan-commonize.md](plan-commonize.md) §5.4, `MiscJvm.kt`: "The same line comment about the facade sits under
  its annotations", where the comment for `Misc.kt` reads `// Shares the JVM facade class MiscKt with MiscJvm.kt; keep the
  two file annotations in step.`
- **What differs:** in `MiscJvm.kt` the comment reads `// Shares the JVM facade class MiscKt with Misc.kt; keep the two
  file annotations in step.` Only the file name differs.
- **Why:** copied literally, the comment in `MiscJvm.kt` would say the file shares the facade with itself. Naming the
  other half keeps the purpose of the comment: each file points at its partner.

### `commonize` — commit 1 checks the `MiscKt` facade on its public members (`javap -public -s`)

- **Plan:** [plan-commonize.md](plan-commonize.md) R3 ("`javap -s` of `MiscKt` before and after the split is identical")
  and §9 "Per-commit extra checks", commit 1 and 2 (`javap -s ... | sort`, "expected: no output", with the note that
  `javap` without `-p` lists public members only).
- **What differs:** the facade criterion is "public members identical": `javap -public -s` of `MiscKt` before and after
  the split, compared, plus the JVM dump check (`git diff --exit-code HEAD -- api/`). The plan's `javap -s` comparison
  is not empty after the split: it loses the `static {};` initializer (with its `descriptor: ()V`) and the
  `Compiled from "Misc.kt"` line.
- **Why:** the plan assumed plain `javap` lists only public members. It also prints the class initializer and the
  SourceFile line. After the split the private slf4j logger and its class initializer live in the part class
  `MiscKt__MiscJvmKt`, and a multifile facade has no single source file. Neither line is API. The JVM dump and all ten
  public signatures and descriptors on `MiscKt` are unchanged (assumption A7 as the architecture defines it), so the user
  accepted the split (2026-10-20) on the public-members criterion.

### `commonize` — commit 3 keeps `TwoDoubles.equals`'s original spacing around the new expression

- **Plan:** [plan-commonize.md](plan-commonize.md) §5.3 "`TwoDoubles.equals` and `hashCode`", whose code block shows
  `if (other == null || other::class != this::class) return false` (and the signature as `equals(other: Any?): Boolean`).
- **What differs:** the line reads `if(other==null || other::class != this::class) return false`. Only the two
  `.javaClass` expressions were replaced; the existing `if(other==null` spacing was left as it was. The signature line
  `override operator fun equals(other: Any?):Boolean {` is also unchanged.
- **Why:** §5.3 states "Only the expression changes; the two members' visibility modifiers and KDoc stay as
  `explicit-api` and `logging` left them". Re-spacing the line would be a formatting change beyond the expression. This
  follows the `explicit-api` precedent above (`TwoDoubles` line 13 keeps `second:Double`). The compiled code is the same
  either way.
