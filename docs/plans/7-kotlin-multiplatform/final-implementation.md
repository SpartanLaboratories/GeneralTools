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
