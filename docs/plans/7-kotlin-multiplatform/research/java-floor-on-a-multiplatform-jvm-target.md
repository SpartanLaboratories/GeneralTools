# Java floor on a multiplatform JVM target

How does a multiplatform library's JVM target set its Java floor apart from the toolchain, and guard
it in `check`, under Kotlin Gradle plugin (KGP) 2.4.21 and Gradle 9.0.0? The answer has to hold for
three kinds of module: the core multiplatform module (`jvm`, `js`, `wasmJs`), the JVM-only desktop
module (Kotlin/JVM), and the source-less umbrella. Requirements it must meet: the floor is Java 17
(class-file major 61) on every JVM artifact; it is set by one constant, apart from the toolchain that
runs the build; a guard in `check` reads the built bytes; `js` and `wasmJs` are unaffected; the
runtime classpath (kotlin-stdlib 2.2.0, kotlin-logging 8.0.7, slf4j-api 2.0.16, commons-io for the
desktop module) holds the floor.

## Bottom line

- **Compile to the floor with `jvmTarget` plus `-Xjdk-release`, both from one constant, and run the
  build on no toolchain.** `jvmTarget` alone sets only the class-file version; the flag also hides
  every JDK API newer than the floor, whatever JDK runs Gradle. Set both inside the `jvm { compilerOptions }`
  block of the multiplatform module (not at `kotlin { compilerOptions }`, where `js` and `wasmJs`
  would warn about an unsupported flag) and in `kotlin { compilerOptions }` of the Kotlin/JVM module.
  Drop `jvmToolchain`: it ties the floor to the toolchain, and Gradle 9.0.0 needs JDK 17 to 24 to run
  anyway, so CI on Temurin 17 (the floor) runs the tests on a real 17 and a developer's JDK 23 still
  builds 17 bytecode against the 17 API. There is no typed DSL for the flag in 2.4.21
  (`freeCompilerArgs.add("-Xjdk-release=17")` is the form).
- **Publish the floor as `org.gradle.jvm.version` 17 on every JVM variant.** For the multiplatform
  module keep `jvm { attributes { attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, floor) } }`:
  KGP 2.4.21 never sets it, and it lands on the root module's and the `-jvm` module's API, runtime and
  sources variants. For the Kotlin/JVM desktop module and the umbrella, set
  `tasks.withType<JavaCompile> { options.release.set(floor) }`: Gradle derives the attribute from it,
  and KGP's Kotlin/Java target check accepts it.
- **Guard with a `verifyJavaFloor` task ported from WebTools, with one change.** Inputs: the module's
  jar (`jvmJar` for the multiplatform `jvm` target, `jar` for Kotlin/JVM and the umbrella), every
  `GenerateModuleMetadata` output, and the main runtime classpath
  (`kotlin.jvm().compilations.getByName("main").runtimeDependencyFiles` for multiplatform,
  `configurations.runtimeClasspath` for Kotlin/JVM), wired into `check`. WebTools' task fails on a
  module whose jar holds no class ("no class to verify"), which is exactly the umbrella; add a
  `requireOwnClasses` switch for it and require instead that the runtime classpath held classes.
- **Run on the real dependencies, the guard holds.** Built in a scratch copy on KGP 2.4.21, Gradle
  9.0.0, once on JDK 23 and once on JDK 17: every class in kotlin-stdlib 2.2.0 and 2.4.21,
  kotlin-logging-jvm 8.0.7, slf4j-api 2.0.16 and commons-io 2.4 is at most major 61 outside
  `META-INF/versions/`. The only class at exactly 61 is kotlin-logging's `module-info.class`, so the
  floor cannot drop below 17 without that dependency changing.
- **Risks are small and named below:** `-Xjdk-release` is an unstable `-X` flag that Kotlin says is
  "not guaranteed to be effective for each JDK distribution" (it needs a `ct.sym` in the compile
  JDK); the flag at the wrong level warns on `js`; `jvmTarget` must accompany it; `explicitApi` and
  ABI validation are unaffected (verified).

## Findings

Evidence tiers: **consensus** (documentation or several independent sources), **practice** (what
mature libraries visibly do), **opinion** (one source), **observed** (run in a scratch copy for this
question; the scratch builds are described in *Gaps*).

### F1. `jvmTarget` fixes the class-file version; `-Xjdk-release` fixes the visible JDK API (consensus)

The Kotlin compiler's own description of the flag: "Specify the target version of the generated JVM
bytecode. Limit the API of the JDK in the classpath to the specified Java version. Automatically sets
`-jvm-target version`." ([compiler reference](https://kotlinlang.org/docs/compiler-reference.html),
read 2026-11-22.) The compiler enforces the pairing: a `-jvm-target` that differs from the release is
an error, "'-Xjdk-release=$releaseTargetArg' option conflicts with '-jvm-target $jvmTargetArg'"
(`configureJdkRelease` region of
[`jvmArguments.kt`](https://github.com/JetBrains/kotlin/blob/v2.4.21/compiler/cli/cli-jvm/src/org/jetbrains/kotlin/cli/jvm/jvmArguments.kt),
tag `v2.4.21`). KGP always passes its own `-jvm-target` (derived from the toolchain or the Gradle JVM),
so the flag at the Gradle layer needs `jvmTarget` set to the same value
([KT-61986](https://youtrack.jetbrains.com/issue/KT-61986), Backlog; tapmoc's comment in
[`KgpImpl.kt`](https://github.com/GradleUp/tapmoc/blob/v0.5.0/tapmoc-gradle-plugin/src/kgp/kotlin/com/gradleup/tapmoc/internal/KgpImpl.kt),
`v0.5.0`).

Observed (KGP 2.4.21, Gradle 9.0.0, JDK 23, multiplatform `jvm` with `jvmTarget` 17 and
`-Xjdk-release=17`): `"a,b".splitWithDelimiters(",", 0)` (a JDK 21 method) fails with
`Unresolved reference 'splitWithDelimiters'`; with neither setting the same module compiled to class-file
major 67.

### F2. Under KGP 2.4.21 there is no typed DSL for the flag (practice)

[KT-61648](https://youtrack.jetbrains.com/issue/KT-61648) "Add Gradle compiler option for jdk-release"
and [KT-49746](https://youtrack.jetbrains.com/issue/KT-49746) "Support -Xjdk-release in Gradle toolchain"
are both State Backlog (REST read 2026-11-22). KT-84705 "Make Xjdk-release to type-safe" was fixed in
2.4.0-Beta1, but it concerns the Build Tools API argument type, not `compilerOptions`; the Kotlin
Gradle documentation page ([compiler options](https://kotlinlang.org/docs/gradle-compiler-options.html))
lists no `jdkRelease` and tells users to use `freeCompilerArgs` for options it lacks. The flag is a
`-X` option, which the compiler reference calls "currently unstable: their names and behavior may be
changed without notice".

### F3. A toolchain sets the same things, but ties the floor to the toolchain (consensus)

Kotlin's documentation: a Java toolchain "Sets the `-jdk-home` option available for JVM targets",
"Sets the `compilerOptions.jvmTarget` to the toolchain's JDK version if the user doesn't set the
`jvmTarget` option explicitly", and "Sets the toolchain to be used by any Java compile, test, and
javadoc tasks" ([Gradle configure project](https://kotlinlang.org/docs/gradle-configure-project.html),
read 2026-11-22). `jvmToolchain(17)` therefore gives a 17 floor only while the toolchain is 17: the
floor and the toolchain are one setting, which is what the policy rules out, and raising the toolchain
to 21 would silently raise the bytecode unless `jvmTarget` is also pinned. Gradle's own guidance is
that `--release` "does not control which JDK is used—only how the compiler treats source code" and
"can be combined with toolchains" ([toolchains](https://docs.gradle.org/9.0.0/userguide/toolchains.html)).
Gradle 9.0.0 needs a JVM of its own: "A JVM version between 17 and 24 is required to execute Gradle."
([compatibility](https://docs.gradle.org/9.0.0/userguide/compatibility.html)). With a floor of 17 the
build JVM is therefore never below the floor, and a toolchain buys no extra coverage. One source's
opinion, for the other direction: "Gradle toolchains are rarely a good idea"
([Jake Wharton](https://jakewharton.com/kotlins-jdk-release-compatibility-flag/)).

The compiler skips the release machinery when it would do nothing: in `jvmArguments.kt` the release is
only recorded "if (value != getJavaVersion() || arguments.jdkHome != null)". On CI's JDK 17 the flag is
a no-op by design; on JDK 23 it does the work.

### F4. KGP 2.4.21 never sets `org.gradle.jvm.version` on a multiplatform JVM target; the target `attributes` block reaches every variant (observed, matches source)

No occurrence of `TargetJvmVersion` or `TARGET_JVM_VERSION` exists anywhere in the
`kotlin-gradle-plugin` sources at tag
[`v2.4.21`](https://github.com/JetBrains/kotlin/tree/v2.4.21/libraries/tools/kotlin-gradle-plugin)
(searched in a sparse checkout). User-defined target attributes are propagated by
[`applyUserDefinedAttributes.kt`](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/plugin/mpp/applyUserDefinedAttributes.kt):
"Output configurations of each target need the corresponding compilation's attributes (and, indirectly,
the target's attributes)".

Observed in the scratch module (`jvm { attributes { attribute(TARGET_JVM_VERSION_ATTRIBUTE, 17) } }`),
published to a file repository:

| module file | variant | `org.gradle.usage` | `org.gradle.jvm.version` |
|---|---|---|---|
| `core` (root) | `jvmApiElements-published` | `java-api` | 17 |
| `core` (root) | `jvmRuntimeElements-published` | `java-runtime` | 17 |
| `core` (root) | `jvmSourcesElements-published` | `java-runtime`, category `documentation` | 17 |
| `core-jvm` | the same three | | 17 |
| `core` and `core-js` | `js*Elements-published`, `metadataApiElements` | `kotlin-api`, `kotlin-runtime`, `kotlin-metadata` | none |

With the attribute line removed, both module files declared nothing for the `jvm*` variants and the guard
failed with "variant 'jvmApiElements-published' declares org.gradle.jvm.version nothing, not 17". Gradle's
attribute rule is "Defaults to the JVM version used by Gradle, lower is compatible with higher, prefers
highest compatible" ([variant attributes](https://docs.gradle.org/9.0.0/userguide/variant_attributes.html)),
so a consumer on a JDK below 17 fails at resolution with the attribute and with
`UnsupportedClassVersionError` at run time without it. The `js` and `wasmJs` variants never carry the
attribute, so one attribute on `jvm` leaves them alone.

### F5. For Java-plugin modules Gradle derives the attribute from the Java compile's release (consensus, Gradle source)

In Gradle `v9.0.0`,
[`DefaultJvmLanguageUtilities.getMaxTargetJvmVersion`](https://github.com/gradle/gradle/blob/v9.0.0/platforms/jvm/plugins-java-base/src/main/java/org/gradle/api/plugins/jvm/internal/DefaultJvmLanguageUtilities.java)
returns the compile task's `options.release` when present, then a `--release` compiler argument, then
the task's `targetCompatibility`; that value becomes `org.gradle.jvm.version` on `apiElements` and
`runtimeElements`. The Kotlin/JVM plugin applies the Java plugin, so a Kotlin/JVM module and a
`java-library` umbrella take their attribute from `compileJava`, not from Kotlin's `jvmTarget`.

KGP's check that Kotlin and Java agree (`validateKotlinAndJavaHasSameTargetCompatibility` in
[`KotlinCompile.kt`](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/tasks/KotlinCompile.kt))
is skipped in multiplatform projects without Java sources, which is why the multiplatform `jvm()`
target needs no Java settings, and applies to a Kotlin/JVM module. Observed on a Kotlin/JVM module with
no Java sources, KGP 2.4.21, JDK 23, `jvmTarget` 17:

- no Java setting: `compileKotlin` fails with "Inconsistent JVM-target compatibility detected for tasks
  'compileJava' (23) and 'compileKotlin' (17)";
- `java { sourceCompatibility = 17; targetCompatibility = 17 }` (WebTools' form): passes, attribute 17;
- `tasks.withType<JavaCompile> { options.release.set(17) }` (tapmoc's form, see
  [`compatibility.kt`](https://github.com/GradleUp/tapmoc/blob/v0.5.0/tapmoc-gradle-plugin/src/main/kotlin/com/gradleup/tapmoc/compatibility.kt)):
  passes, attribute 17, and the guard's metadata check passes.

### F6. `-Xjdk-release` and the multiplatform module's other pieces (observed)

In the scratch core module (KGP 2.4.21, `explicitApi()`, `abiValidation()`, `js()`, `languageVersion` and
`apiVersion` 2.2, kotlin-stdlib 2.2.0):

- `explicitApi()` and `abiValidation()` were unaffected: `updateKotlinAbi` wrote `api/core.api` and
  `api/core.klib.api`, and `check` passed with the flag set on the `jvm` target. The flag does not change
  the declarations either tool reads.
- `compileKotlinJs` was unaffected with the flag inside `jvm { compilerOptions }`. With the flag at
  `kotlin { compilerOptions { freeCompilerArgs.add(...) } }` the same task printed `w: Flag is not supported
  by this version of the compiler: -Xjdk-release=17`. Under `allWarningsAsErrors` that would fail the web
  targets. Compiler options set inside the `jvm` target apply to all its compilations (main and test) as the
  convention (`useTargetJvmCompilerOptionsAsConvention` in KGP's
  [`KotlinCompilationCompilerOptionsConfigurator.kt`](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/plugin/mpp/compilationImpl/KotlinCompilationCompilerOptionsConfigurator.kt)).
- The same build passed `check` on JDK 17 (Temurin 17.0.20) and on JDK 23; the JVM classes were major 61
  both times.

### F7. Known issues with the flag (practice; issue tracker, read 2026-11-22)

| issue | what | state |
|---|---|---|
| [KT-72880](https://youtrack.jetbrains.com/issue/KT-72880) | `VarHandle` signature-polymorphic calls compiled as varargs under a release below the compile JDK (`WrongMethodTypeException`) | Fixed in 2.4.0-Beta1, so absent in 2.4.21 |
| [KT-52823](https://youtrack.jetbrains.com/issue/KT-52823), [KT-52815](https://youtrack.jetbrains.com/issue/KT-52815) | `Thread.State` not accessible; mixed Java/Kotlin projects failing | Fixed (1.8.x) |
| [KT-60868](https://youtrack.jetbrains.com/issue/KT-60868) | JPMS "Symbol is declared in module 'kotlin.stdlib' which current module does not depend on" with `-Xjdk-release=1.8` in a module with a `module-info` | Open; concerns JPMS builds, which the 3.0.0 modules do not use |
| [KT-66560](https://youtrack.jetbrains.com/issue/KT-66560) | confusing interaction of `-jvm-target` and a release of 1.8/8 | Open; a release of 17 spells the same in both |
| [KT-81606](https://youtrack.jetbrains.com/issue/KT-81606) | `com.android.kotlin.multiplatform.library` ignores the flag | Submitted; Android targets are not part of this module |
| [KT-29974](https://youtrack.jetbrains.com/issue/KT-29974) | the original feature; the docs say the flag "is not guaranteed to be effective for each JDK distribution" | Fixed |

The distribution caveat has a concrete cause: the compiler reads the API of an older release from the
compile JDK's `lib/ct.sym` and reports "This JDK does not have the 'ct.sym' file required for the
'-Xjdk-release=$jdkRelease' option" when it is missing
([`CliJavaModuleFinder.kt`](https://github.com/JetBrains/kotlin/blob/v2.4.21/compiler/cli/cli-base/src/org/jetbrains/kotlin/cli/jvm/modules/CliJavaModuleFinder.kt),
`v2.4.21`). Temurin and the Oracle JDK ship it; a build on a JRE-only or stripped JDK would fail loudly,
not silently.

### F8. The runtime classpath holds the floor (observed)

Class-file majors read from the cached artifacts (main entries, then `META-INF/versions/` entries):

| artifact | main classes | multi-release entries |
|---|---|---|
| `kotlin-stdlib` 2.2.0 | 969 at major 52 | 1 at 53 |
| `kotlin-logging-jvm` 8.0.7 | 64 at 52, 1 at 61 (`module-info.class`) | none |
| `slf4j-api` 2.0.16 | 55 at 52 | 1 at 53 |
| `commons-io` 2.4 (`commons-io:commons-io`) | 110 at 50 | none |
| `org.apache.directory.studio:org.apache.commons.io` 2.4 | none: a wrapper bundle holding `lib/commons-io-2.4.jar` | none |

`org.apache.directory.studio:org.apache.commons.io:2.4` resolves `commons-io:commons-io:2.4`
transitively, so the guard sees the real classes in that second jar; it never needs to open a nested
jar. The `kotlin("jvm")` plugin also adds `kotlin-stdlib` at the plugin's own version (2.4.21 in the
scratch module, `2.2.0 -> 2.4.21` in the resolved tree), which the guard also passed. The guard must
ignore `META-INF/versions/` (the slf4j and stdlib entries at 53 would otherwise fail a floor below 9).

### F9. The ported guard on the three module shapes (observed)

WebTools' `VerifyJavaFloor` and `JavaFloorScan` copied unchanged (apart from the object name they call)
into a scratch build ran as follows (KGP 2.4.21, Gradle 9.0.0, configuration cache on):

| module | jar | runtime classpath | result |
|---|---|---|---|
| core (multiplatform `jvm`) | `jvmJar` | `jvm` main `runtimeDependencyFiles` | pass: 2 classes, 4 library variants in 3 metadata files, 1121 classes |
| desktop (Kotlin/JVM) | `jar` | `runtimeClasspath` | pass: 1 class, 2 variants in 1 file, 1258 classes |
| umbrella (`java-library`, no sources) | `jar` | `runtimeClasspath` | **fails**: `no class to verify in the module's jars: [umbrella.jar]` |
| umbrella with `requireOwnClasses = false` | `jar` | `runtimeClasspath` | pass: 0 classes, 2 variants, 1259 classes (core and desktop jars among them) |
| core with `jvmTarget`, flag and attribute removed | `jvmJar` | | **fails** with six lines: two classes at "class-file major 67 (Java 23)", four variants declaring `nothing` |

The root module's two `jvm*` variants and the `-jvm` module's two are the four counted variants of the
core module; the `js` module's variants use `kotlin-api` and `kotlin-runtime` usages and are correctly
not counted. The umbrella's own variants carry 17 only because of its Java compile setting: without one,
`java-library` publishes the Gradle JVM's version, which is what 2.2.0 did (major 67, jvm.version 23).

## How comparable projects do it

| project | JVM floor | how it is set |
|---|---|---|
| Kotlin stdlib build, `v2.4.21` ([`LibrariesCommon.kt`](https://github.com/JetBrains/kotlin/blob/v2.4.21/repo/gradle-build-conventions/buildsrc-compat/src/main/kotlin/LibrariesCommon.kt)) | 9 (Java 9 source set) | `compilerOptions.jvmTarget.set(...)` and `compilerOptions.freeCompilerArgs.add("-Xjdk-release=...")` on a task run with a JDK 17 toolchain |
| kotlinx.serialization (commit [`99172ab`](https://github.com/Kotlin/kotlinx.serialization/blob/99172ab9ff063e16466226bbeb9a5d00703bbf41/buildSrc/src/main/kotlin/CompilerOptions.kt)) | 8 | `KotlinJvmCompilerOptions.setJava8Compatible()`: `jvmTarget = JvmTarget.JVM_1_8` plus `freeCompilerArgs.addAll("-Xjdk-release=1.8")` |
| tapmoc `v0.5.0` (used by Okio) | any | `tapmoc { java(17) }` sets `jvmTarget` and `-Xjdk-release`, and `JavaCompile.options.release`; `checkDependencies()` adds a `check` task over "Java class files versions" of API dependencies |
| kotlinx.coroutines (commit [`7f4d6cf`](https://github.com/Kotlin/kotlinx.coroutines/blob/7f4d6cf2414937b50291df6169a69afbaf9c5a20/buildSrc/src/main/kotlin/kotlin-multiplatform-conventions.gradle.kts)) | 8 | `compilerOptions.jvmTarget = JvmTarget.JVM_1_8` plus `jvmToolchain(jdkToolchainVersion)` (11 in `gradle.properties`); no `-Xjdk-release` |
| kotlinx-datetime (commit [`b9e4f49`](https://github.com/Kotlin/kotlinx-datetime/blob/b9e4f4965e04ce6f8231bc8ddc6fee337c9de687/core/build.gradle.kts)) | 8 | `jvm { attributes { attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8) } }` by hand |
| WebTools (this organization, `build-logic`) | 11 | `jvmTarget` and `-Xjdk-release` from `WebToolsBuild.JAVA_FLOOR`, the same attribute line, `verifyJavaFloor` in `check` |

The pattern is consistent: the libraries that care about the API as well as the bytecode use the flag;
the others lean on an old toolchain; the multiplatform ones with a published JVM floor write the
attribute by hand because KGP does not. tapmoc's `checkDependencies()` is the only other class-version
check of dependencies found; WebTools' guard additionally reads the published module metadata and the
jar itself.

## Pitfalls and regrets

- **The flag at extension level.** `kotlin { compilerOptions { freeCompilerArgs.add("-Xjdk-release=17") } }`
  reaches the `js` and `wasmJs` compilations, which warn that the flag is unsupported. Put it in
  `jvm { compilerOptions }`.
- **`jvmTarget` omitted.** The compiler errors on a conflict with the default, and KGP always passes a
  `-jvm-target`; the pair is one logical setting.
- **A forgotten attribute.** A multiplatform `jvm` publication then declares no JVM version and a Java 11
  consumer fails at run time. The guard's metadata check is what notices.
- **Source-less umbrella against a guard that insists on classes.** Reported above; WebTools' task fails
  on it.
- **Checking only compiler settings.** A dependency can compile to a newer class version than the floor
  (a library's `module-info.class` at 61 here); only scanning the files catches that, which is why the
  guard reads bytes, not options.
- **A toolchain as the floor.** It couples the two and forces a download on a clean machine; the guard
  and the flag make it unnecessary.
- **Build on a stripped JDK.** The flag needs `lib/ct.sym` (F7).
- **Gradle's JVM above 24.** Gradle 9.0.0 does not run there; unrelated to the floor, but it bounds the
  "newer than the floor" JDKs a developer can build on.

## Recommendation for this repo

1. **One constant, `JAVA_FLOOR = 17`, in `build-logic`** (an `object` the convention plugins read, as
   WebTools' `WebToolsBuild` does); `classFileMajor(floor) = floor + 44`. Today `build.gradle.kts:31`
   defines `val jvmFloor = 17` and line 35 feeds it to `jvmToolchain`; the new build keeps the constant
   and removes the toolchain call.
2. **Compile settings.**
   - Multiplatform convention plugin:
     `kotlin { jvm { compilerOptions { jvmTarget.set(JvmTarget.fromTarget("$floor")); freeCompilerArgs.add("-Xjdk-release=$floor") } } }`.
     `js` and `wasmJs` get nothing.
   - Kotlin/JVM convention plugin (desktop): the same two lines at `kotlin { compilerOptions { ... } }`,
     plus `tasks.withType<JavaCompile>().configureEach { options.release.set(floor) }`.
   - Umbrella (`java-library`, no Kotlin): `options.release.set(floor)` on `JavaCompile`.
   - No `jvmToolchain` anywhere. CI stays on Temurin 17; developers may use any JDK Gradle supports.
     If a floor-JDK test run is wanted on a machine with a newer JDK, a `javaLauncher` on the `jvmTest`
     task is the narrow, separate setting for it, not a toolchain on compilation.
3. **Attribute.** In the multiplatform plugin keep the by-hand attribute, now from the constant:
   `jvm { attributes { attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, floor) } }`. The
   Kotlin/JVM and umbrella variants take it from step 2's `options.release`; their published
   metadata is what the guard asserts.
4. **Guard.** Port `VerifyJavaFloor`, `JavaFloorScan` and `registerVerifyJavaFloor` from WebTools
   (`WebToolsBuild.kt`, `VerifyJavaFloor.kt`, `JavaFloorScan.kt`) with `requireOwnClasses` added
   (default `true`; `false` for the umbrella, whose zero-class jar is correct) and the check
   "something was scanned" moved to the runtime classpath for that case. Register it from the three
   convention plugins and wire it with `tasks.named("check") { dependsOn(verifyJavaFloor) }`.
   - Multiplatform: jars `files(tasks.named<Jar>("jvmJar").flatMap { it.archiveFile })`;
     runtime classpath `checkNotNull(kotlin.jvm().compilations.getByName("main").runtimeDependencyFiles)`.
   - Kotlin/JVM and umbrella: jars `tasks.named<Jar>("jar")`; runtime classpath
     `configurations.getByName("runtimeClasspath")`.
   - Metadata: every `GenerateModuleMetadata` task of the project (WebTools' `moduleMetadata.from(project.tasks.withType(...))`),
     so a vanniktech or Kotlin-created publication is included whenever it is created.
5. **What I reject.** `jvmToolchain(17)` alone: it works today but equates floor and toolchain, so the
   next toolchain bump changes shipped bytecode without anyone deciding it. A guard without the metadata
   check: it would miss the one thing KGP leaves to the build script, the attribute.

## Load-bearing claims

1. **The flag's contract and its caveat.** "Specify the target version of the generated JVM bytecode.
   Limit the API of the JDK in the classpath to the specified Java version. Automatically sets
   `-jvm-target version`." and "This option is not guaranteed to be effective for each JDK distribution."
   Source: https://kotlinlang.org/docs/compiler-reference.html (read 2026-11-22).
2. **`-X` options are unstable.** "These options are currently unstable: their names and behavior may be
   changed without notice." Source: https://kotlinlang.org/docs/compiler-reference.html (read 2026-11-22).
3. **A conflicting `-jvm-target` is an error.** `"'-Xjdk-release=$releaseTargetArg' option conflicts with
   '-jvm-target $jvmTargetArg'. $suggestion"`. Source:
   https://github.com/JetBrains/kotlin/blob/v2.4.21/compiler/cli/cli-jvm/src/org/jetbrains/kotlin/cli/jvm/jvmArguments.kt
   (tag `v2.4.21`).
4. **The release is skipped when it equals the compile JDK and no JDK home is passed.** "//don't use
   release flag if it equals to compilation JDK version / if (value != getJavaVersion() || arguments.jdkHome != null)".
   Source: same file and tag as claim 3.
5. **The flag needs `ct.sym`.** "This JDK does not have the 'ct.sym' file required for the
   '-Xjdk-release=$jdkRelease' option". Source:
   https://github.com/JetBrains/kotlin/blob/v2.4.21/compiler/cli/cli-base/src/org/jetbrains/kotlin/cli/jvm/modules/CliJavaModuleFinder.kt
   (tag `v2.4.21`).
6. **No typed Gradle option for the flag.** KT-61648 title "Add Gradle compiler option for jdk-release",
   State Backlog; KT-49746 title "Support -Xjdk-release in Gradle toolchain", State Backlog. Source:
   https://youtrack.jetbrains.com/issue/KT-61648 and https://youtrack.jetbrains.com/issue/KT-49746
   (YouTrack REST, read 2026-11-22).
7. **`freeCompilerArgs` is the documented place for options without a DSL, and is available at target
   level.** "A list of additional compiler arguments. You can use experimental `-X` arguments here too."
   and "The `freeCompilerArgs` attribute is available at the extension, target, and compilation unit
   (task) levels." Source: https://kotlinlang.org/docs/gradle-compiler-options.html (read 2026-11-22).
8. **Extension-level options are the default for every target; target-level overrides.** "Compiler options
   set at the extension level are the default for target-level options" and "In Kotlin Multiplatform
   projects, you can configure compiler options inside the specific target. For example,
   `jvm { compilerOptions {}}`." Source: https://kotlinlang.org/docs/gradle-compiler-options.html
   (read 2026-11-22).
9. **What a toolchain does.** "Sets the `-jdk-home` option available for JVM targets." and "Sets the
   `compilerOptions.jvmTarget` to the toolchain's JDK version if the user doesn't set the `jvmTarget`
   option explicitly." Source: https://kotlinlang.org/docs/gradle-configure-project.html (read
   2026-11-22).
10. **Gradle 9.0.0 needs a JVM of 17 to 24.** "A JVM version between 17 and 24 is required to execute
    Gradle." Source: https://docs.gradle.org/9.0.0/userguide/compatibility.html (Gradle 9.0.0).
11. **`org.gradle.jvm.version` semantics.** "Defaults to the JVM version used by Gradle, lower is
    compatible with higher, prefers highest compatible." Source:
    https://docs.gradle.org/9.0.0/userguide/variant_attributes.html (Gradle 9.0.0).
12. **A Java-plugin module's attribute comes from release, then `--release`, then `targetCompatibility`.**
    `if (compileTask.getOptions().getRelease().isPresent()) { return compileTask.getOptions().getRelease(); }`
    then `compilerArgs.indexOf("--release")`, else `JavaVersionParser.parseMajorVersion(compileTask.getTargetCompatibility())`.
    Source: https://github.com/gradle/gradle/blob/v9.0.0/platforms/jvm/plugins-java-base/src/main/java/org/gradle/api/plugins/jvm/internal/DefaultJvmLanguageUtilities.java
    (tag `v9.0.0`).
13. **KGP 2.4.21 does not set the attribute; it propagates user-set target attributes.** No match for
    `TargetJvmVersion` in the plugin sources at the tag (a negative: the check is to search
    https://github.com/JetBrains/kotlin/tree/v2.4.21/libraries/tools/kotlin-gradle-plugin for it), and
    "Output configurations of each target need the corresponding compilation's attributes (and,
    indirectly, the target's attributes)". Source:
    https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/plugin/mpp/applyUserDefinedAttributes.kt
    (tag `v2.4.21`).
14. **KGP's Kotlin/Java target check is skipped for multiplatform without Java sources.** "// Skip check in
    KMP projects and no Java sources". Source:
    https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/tasks/KotlinCompile.kt
    (tag `v2.4.21`).
15. **KT-72880 (VarHandle under a lower release) is fixed in 2.4.0-Beta1.** Issue "Calls with incorrect
    VarHandle method signatures are generated with -Xjdk-release being used", State Fixed, Available in
    2.4.0-Beta1. Source: https://youtrack.jetbrains.com/issue/KT-72880 (YouTrack REST, read 2026-11-22).
16. **Prior art for the flag with `jvmTarget`.** `freeCompilerArgs.addAll("-Xjdk-release=1.8")` next to
    `jvmTarget = JvmTarget.JVM_1_8`; and in the Kotlin build
    `compilerOptions.freeCompilerArgs.add("-Xjdk-release=${JdkMajorVersion.JDK_9_0.targetName}")`.
    Sources: https://github.com/Kotlin/kotlinx.serialization/blob/99172ab9ff063e16466226bbeb9a5d00703bbf41/buildSrc/src/main/kotlin/CompilerOptions.kt
    and https://github.com/JetBrains/kotlin/blob/v2.4.21/repo/gradle-build-conventions/buildsrc-compat/src/main/kotlin/LibrariesCommon.kt.
17. **Prior art for the by-hand attribute.** `attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)`
    inside `jvm { attributes { ... } }`. Source:
    https://github.com/Kotlin/kotlinx-datetime/blob/b9e4f4965e04ce6f8231bc8ddc6fee337c9de687/core/build.gradle.kts
    (commit `b9e4f49`).
18. **Prior art for a dependency class-version check.** "Runs all the Tapmoc tasks that check dependencies
    (Kotlin stdlib, Kotlin metadata and Java class files versions)". Source:
    https://github.com/GradleUp/tapmoc/blob/v0.5.0/tapmoc-gradle-plugin/src/main/kotlin/com/gradleup/tapmoc/internal/TapmocExtensionImpl.kt
    (tag `v0.5.0`).
19. **The flag at extension level reaches Kotlin/JS and warns.** Observed output: `w: Flag is not
    supported by this version of the compiler: -Xjdk-release=17` from `:core:compileKotlinJs`. This is a
    result of a run, not a quotation; re-check by adding the flag at `kotlin { compilerOptions }` of a
    multiplatform module with `js()` on KGP 2.4.21 and running `compileKotlinJs`.

## Gaps

- **Scratch builds, not the real repository.** Claims marked *observed* come from a scratch project
  (core multiplatform with `jvm` and `js`; Kotlin/JVM desktop; `java-library` umbrella) built with
  Gradle 9.0.0 on JDK 23 and JDK 17 against KGP 2.4.21, with the real dependency versions; it had no
  `wasmJs` target, no vanniktech plugin and no tests. The `wasmJs` claim rests on the flag being
  `jvm`-scoped and on the `js` result; `wasmJs` was not compiled.
- **How `build-logic` itself depends on KGP 2.4.21.** The scratch used `buildSrc` for the task classes,
  which need no KGP types, and put the KGP-typed lines in module scripts. How a precompiled script plugin
  in `build-logic` compiles against KGP 2.4.21 under Gradle 9.0.0's embedded Kotlin was not tested.
- **The umbrella's shape is not fixed.** The guard design covers it as a JVM module (`java-library` or
  Kotlin/JVM, no sources). If it becomes a multiplatform module with a `jvm` target, the
  multiplatform plugin applies and the zero-class case is the same.
- **`jvmTest` on JDK 17 locally** was not run; the scratch had no tests. Tests run on Gradle's JVM, so a
  developer on JDK 23 tests on 23 and CI on 17 tests on 17.
- **Page quotes are fetcher-extracted.** The quotes from the Kotlin and Gradle documentation pages (claims 1, 2, 7 to 11) were returned by a page-reading tool, not copied by hand from the rendered page; the source-code quotes (claims 3 to 5, 12, 14, 18) were read from the files at the pinned tags.
- **JDKs without `ct.sym`** were not exercised; F7 rests on the compiler source.
- **A consumer on JDK 11 or 8** resolving the 17 attribute was not run; the claim rests on Gradle's
  documented attribute rule.
- **Assumptions and what changes if false.** (a) CI keeps a JDK of 17 to 24 for Gradle 9.0.0: if CI
  moves to 25 or later, Gradle must be upgraded first. (b) The floor stays 17: lowering it breaks the
  guard on kotlin-logging 8.0.7's major-61 `module-info.class`, so a lower floor needs a dependency
  change or an exclusion. (c) No Java sources enter the multiplatform module: if some do (through
  `withJava()`), KGP's Kotlin/Java target check applies and `options.release` is added there too.
