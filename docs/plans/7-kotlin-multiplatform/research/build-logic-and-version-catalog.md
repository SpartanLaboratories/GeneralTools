# Build logic and the version catalog for core, desktop and the umbrella

- **Date read:** 2026-11-21 (local). Branch `feat/issue-7-kotlin-multiplatform`, build files as at head `29dd4ff`.
- **Versions this report is pinned to:** Gradle 9.0.0 (the wrapper), Kotlin Gradle plugin (KGP) 2.4.21, Dokka 2.3.0,
  vanniktech `com.vanniktech.maven.publish` 0.37.0, foojay toolchain resolver 1.0.0. Latest releases on the
  read date, from `gh release list`: Dokka `2.3.0` (2026-11-05), vanniktech `0.37.0` (2026-06-21), Kotlin `2.4.21`
  (2026-10-08; `2.5.0-RC` is a pre-release), Gradle `9.9.2` (2026-11-19).
- **Method:** documents and sources read at pinned tags; the comparable `build-logic` builds in WebTools and
  MyGameTools read in their checkouts; and a **scratch build** of the target layout (three modules, a catalog, five
  convention plugins) built outside the repository with Gradle 9.0.0, KGP 2.4.21, Dokka 2.3.0 and vanniktech 0.37.0.
  The scratch results are marked *observed*; nothing was published, signed or uploaded, and the JS and Wasm test
  runs were not executed (see *Gaps*).

## 1. Question

How is the build of `GeneralTools-core` (multiplatform: `jvm`, `js { nodejs() }`, `wasmJs { nodejs() }`),
`GeneralTools-desktop` (JVM only) and the source-less `GeneralTools` umbrella organised into convention plugins in an
included `build-logic` build, with a `gradle/libs.versions.toml` version catalog, under KGP 2.4.21 and Gradle 9.0.0, so
that the three modules share one configuration and everything that is built today keeps working?

An answer has to meet these requirements:

- Shared configuration lives in convention plugins in a `build-logic` build the settings include; versions of every
  dependency and plugin live in `gradle/libs.versions.toml`; publishing goes through `com.vanniktech.maven.publish` with
  every publication signed; the wrapper stays at 9 or later.
- Dokka 2 in V2 mode is applied by the convention plugin for published modules; the modules are aggregated at the
  root with `dokka(project(":module"))`; the Kotlin plugins sit on the root plugin classpath with `apply false`; every
  Dokka configuration sets `failOnWarning`; every convention plugin that configures publishing sets
  `JavadocJar.Dokka("dokkaGeneratePublicationHtml")`, a multiplatform one included.
- Unchanged behaviour: the language and API pins at 2.2, `kotlin-stdlib:2.2.0` in `commonMain` and the web stdlib at
  the plugin's version, `explicitApi()`, `abiValidation()` with a committed dump per module, `-PtestLevel=<set>[,<set>]`
  on every test task, the `jvm`, `js` and `wasmJs` targets on Node, and the committed `kotlin-js-store/yarn.lock`.
- Each restructuring step leaves the published output identical except where it is meant to change, provably.
- The Java floor (17) is read from one place in `build-logic`; its mechanics belong elsewhere, this report only leaves
  the place.
- New Kotlin files follow the organization's import grouping.

## 2. Bottom line

- **Five script-based convention plugins plus a few plain Kotlin classes, in one `build-logic` build** (cut in section
  6.1): `generaltools.published-library` (vanniktech, signing, POM, Dokka, `failOnWarning`, platform-neutral),
  `generaltools.kotlin-multiplatform-library` (core), `generaltools.kotlin-jvm-library` (desktop),
  `generaltools.test-levels` (`-PtestLevel`, applied by both library plugins) and a settings plugin
  `generaltools.toolchains` (the foojay resolver). The umbrella applies `java-library` and
  `generaltools.published-library` and configures its own `JavaLibrary(...)` platform, because it has no Kotlin and
  must carry no ABI dump. Each library plugin calls `configure(<Platform>(javadocJar = JavadocJar.Dokka(...)))` itself,
  because that call is platform-specific. Constants (`JAVA_FLOOR`, the Dokka task name) and the pure `-PtestLevel`
  parsing sit in `src/main/kotlin` classes.
- **One catalog, read two ways.** `build-logic/settings.gradle.kts` imports `../gradle/libs.versions.toml` as `libs`.
  `build-logic/build.gradle.kts` turns the catalog's `[plugins]` aliases into marker dependencies (Gradle's documented
  helper), so the plugin versions exist once and the root script's `alias(libs.plugins...) apply false` cannot disagree
  with `build-logic`. Convention plugins read dependency and version entries through the string API
  (`VersionCatalogsExtension`), wrapped in a small typed helper; module scripts use the generated `libs.` accessors.
  The `files(libs.javaClass.superclass.protectionDomain.codeSource.location)` workaround is not adopted. The foojay
  version also lives in the catalog, through a precompiled settings plugin in `build-logic` (observed to configure the
  toolchain repository on Gradle 9.0.0); the fallback is one literal version in `settings.gradle.kts`.
- **`pluginManagement { includeBuild("build-logic") }`** (needed for the settings plugin; a top-level `includeBuild`
  also works for project plugins, observed). The root script applies
  `alias(libs.plugins.kotlin.multiplatform) apply false`, `alias(libs.plugins.kotlin.jvm) apply false` and
  `alias(libs.plugins.dokka)`. Observed: leaving the two `apply false` lines out makes Dokka warn and render two pages
  instead of 190.
- **Dokka 2.3.0, V2.** `org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled` in `gradle.properties`. Observed:
  the same `JavadocJar.Dokka("dokkaGeneratePublicationHtml")` gives real javadoc jars for the multiplatform module's four
  publications (262 files each), the JVM module (74) and the source-less umbrella (278: both modules' pages, through
  `dokka(project(...))` in the umbrella), and a broken KDoc link fails the module's and the root's tasks.
- **Per-module proofs and placements.** `abiValidation()` in both library plugins; dumps are committed per module as
  `<module>/api/<project name>.api` (+ `.klib.api` for core), none for the umbrella. `-PtestLevel` is one plugin over
  `AbstractTestTask`. `kotlin-js-store/` stays at the repository root whichever module has the web targets.
- **Proof for each step** is a byte comparison of the generated POM and Gradle module files of every publication, built
  at the same absolute path (observed: moving the single module into `build-logic` plus the catalog left all eight
  files, jar and klib hashes included, byte-identical; the module split leaves the union of the JVM class entries and of
  the JVM ABI dump identical).

## 3. Findings

Tiers: **Consensus** = official documentation or several independent credible sources; **Practice** = what mature
libraries visibly do; **Observed** = seen in the scratch build (Windows 11, Gradle JVM 23, JDK 17 toolchain, Gradle
9.0.0, `--configuration-cache`); **Source-read** = read from source at the pinned tag.

### 3.1 Included build, precompiled script plugins, external plugins (Consensus; Gradle 9.0.0)

- Gradle documents a dedicated composite build "typically named `build-logic`" as a way to share build logic
  ([Sharing build logic](https://docs.gradle.org/9.0.0/userguide/sharing_build_logic_between_subprojects.html);
  claim 1). Plugin builds "should be included using the `includeBuild` statement inside the `pluginManagement {}`
  block", and such a build "may also contribute a settings plugin that can be applied in the settings file itself"
  ([Composite builds](https://docs.gradle.org/9.0.0/userguide/composite_builds.html); claim 2).
- A plugin an `.gradle.kts` convention plugin applies by id with no version must be on the `build-logic` project's
  `implementation` classpath, and "the plugin version in this case is defined in the dependency declaration"
  ([Precompiled script plugins](https://docs.gradle.org/9.0.0/userguide/implementing_gradle_plugins_precompiled.html);
  claim 3). So the plugin versions of `build-logic` are a build-logic dependency, not a `plugins {}` version.
- Observed: one `*.gradle.kts` plugin can apply another (`id("generaltools.published-library")` inside the library
  plugins), and the accessor for `mavenPublishing {}` was generated in the library plugins without listing
  `com.vanniktech.maven.publish` a second time. WebTools' library plugins list it again "so this script's own
  `mavenPublishing {}` accessor is generated"; that line was not needed here.

### 3.2 The catalog and `build-logic` (Consensus + Practice)

- `build-logic` is a separate build and does not see the root catalog: the documented import is
  `dependencyResolutionManagement { versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } } }`
  in its own `settings.gradle.kts` ([Version catalogs](https://docs.gradle.org/9.0.0/userguide/version_catalogs.html);
  claim 4). The root build reads `gradle/libs.versions.toml` by default (claim 4).
- The generated typed accessors do not reach precompiled script plugins, and a catalog plugin alias cannot be used in
  the plugin's `plugins {}` block or in settings (claim 4). The tracking issue,
  [gradle/gradle#15383](https://github.com/gradle/gradle/issues/15383), is still **open** (last update 2026-06-22;
  claim 5).
- The two answers in use:
  - **String API.** Now in Android's `ProjectExtensions.kt` defines
    `val Project.libs get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")` and its
    plugins read entries with `findLibrary(...)` ([nowinandroid @ 2982e63](https://github.com/android/nowinandroid/blob/2982e63cb691ad49cd009aab43dc206fee6699c2/build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/ProjectExtensions.kt);
    claim 7). Gradle's own page shows `extensions.getByType(VersionCatalogsExtension)` for this (claim 4). Practice.
  - **Class-path hack.** Ktor 3.6.0's `build-logic/build.gradle.kts` adds
    `implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))` under the comment "A hack to
    make version catalogs accessible from buildSrc sources" so scripts can use `the<LibrariesForLibs>()`
    ([ktor @ 3.6.0](https://github.com/ktorio/ktor/blob/3.6.0/build-logic/build.gradle.kts); claim 6). It relies on an
    internal generated class, and a precompiled plugin applied from another precompiled plugin needs a further guard
    (comments in #15383). Practice (one large project).
- **Plugin aliases as `build-logic` dependencies.** Gradle's catalog page gives the helper
  `plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }` (claim 4). Observed with
  `it.version.requiredVersion` in place of `it.version`: `implementation(libs.plugins.kotlin.multiplatform.marker())`
  and the same for `kotlin.jvm`, `dokka` and `maven.publish` resolve. Okio 3.18.2 instead lists libraries
  (`implementation(libs.dokka)`, `libs.kotlin.gradle.plugin`) from the same catalog
  ([okio @ parent-3.18.2](https://github.com/square/okio/blob/parent-3.18.2/build-support/build.gradle.kts); claim 8).
  Aliases under `[plugins]` have the advantage that the root script and the modules use the same alias.
- **Where the version of a settings plugin lives.** A catalog entry cannot be used in settings (claim 4). Ktor 3.6.0 keeps
  the foojay version literal in `settings.gradle.kts` (`id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"`)
  and puts its other settings logic in a precompiled settings plugin of an included build,
  `pluginManagement { includeBuild("build-settings-logic") }` ([ktor @ 3.6.0](https://github.com/ktorio/ktor/blob/3.6.0/settings.gradle.kts);
  claim 6). Observed on Gradle 9.0.0: a precompiled settings plugin
  (`generaltools.toolchains.settings.gradle.kts`: `plugins { id("org.gradle.toolchains.foojay-resolver-convention") }`)
  in `build-logic`, whose `build.gradle.kts` depends on the catalog's foojay marker, configures the toolchain download
  repository. A project requesting a JDK that is not installed reported "No matching toolchain could be found in the
  configured toolchain download repositories" with the plugin and "Toolchain download repositories have not been
  configured" without it. So the foojay version can live in the catalog.

### 3.3 `pluginManagement.includeBuild` or top-level `includeBuild` (Practice; Observed)

- Gradle's page calls for `pluginManagement` for plugin builds and the settings-plugin case (claim 2). Okio 3.18.2 uses a
  top-level `includeBuild("build-support")`; Ktor 3.6.0 and Now in Android use `pluginManagement { includeBuild(...) }`
  (claims 6, 8). Observed: with the top-level form, project plugins resolve and Dokka renders 190 pages; with the
  `pluginManagement` form identically. Only the `pluginManagement` form can supply the settings plugin.
- Observed: with `alias(libs.plugins.kotlin.multiplatform) apply false` and `alias(libs.plugins.kotlin.jvm) apply false`
  in the root script, no version list is needed in `settings.gradle.kts` (WebTools needs a `pluginManagement { plugins {} }`
  list plus a test that its versions equal the `build-logic` marker versions). Both come from one catalog alias here.

### 3.4 Dokka 2.3.0, V2 mode, aggregation, classloader (Consensus + Source-read + Observed)

- **V2 mode.** "Starting from Dokka 2.1.0, DGP v2 is enabled by default." The default in source is
  `val Default: PluginMode = V2EnabledWithHelpers` (v2.3.0, `PluginFeaturesService.kt`), which keeps disabled V1 task
  stubs (`dokkaJavadoc - [V1 tasks disabled]` was listed, observed). The migration page says to set
  `org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled` "after successful migration" (claim 9). MyGameTools
  sets `V2Enabled` and `.noWarn=true` in `gradle.properties`; WebTools sets neither.
- **Aggregation.** "Aggregation in DGP v2 uses the `dependencies {}` block instead of tasks and can be added in any
  `build.gradle.kts` file" (claim 11). Observed: the root's
  `dependencies { dokka(project(":GeneralTools-core")); dokka(project(":GeneralTools-desktop")) }` renders both modules
  (190 HTML files) and `dokkaGeneratePublicationHtml` output goes to `build/dokka/html`.
- **Classloader.** Dokka's own warning, v2.3.0 `KotlinAdapter.kt`: "Apply the Dokka and Kotlin plugins to the root
  project using the `plugins {}` DSL. (If the root project does not need the plugins, use 'apply false')", citing
  [gradle/gradle#25616](https://github.com/gradle/gradle/issues/25616) and
  [#35117](https://github.com/gradle/gradle/issues/35117) (claim 10; both issues open on the read date). Observed in the
  target layout: without the two `apply false` aliases the build prints "Dokka could not load KotlinBasePlugin in project
  ':GeneralTools-core'" and the root publication holds 2 HTML files; with them, 190. The convention plugins' own copy of
  KGP and Dokka (from `build-logic`) is loaded fine once the root carries the same versions.
- **The javadoc jar.** vanniktech 0.37.0: `JavadocJar.Dokka(taskName: String)` is "a javadoc jar using Dokka's output. The
  argument is the name of the dokka task that should be used for that purpose" (claim 12). For a multiplatform module
  it registers one jar task per publication (observed: `jvmDokkaJavadocJar`, `jsDokkaJavadocJar`,
  `wasmJsDokkaJavadocJar`, `kotlinMultiplatformDokkaJavadocJar`, each 262 files, the same Dokka output); for the JVM
  module `dokkaJavadocJar`. Every jar contains the pages of all targets, because the Dokka publication is per module.
- **`failOnWarning`.** Observed: `dokka { dokkaPublications.configureEach { failOnWarning.set(true) } }` in
  `generaltools.published-library` makes `:GeneralTools-core:dokkaGeneratePublicationHtml` fail ("Failed with
  warningCount=2") on an injected unresolved KDoc link, and the root's `dokkaGeneratePublicationHtml` fails through the
  module's `dokkaGeneratePublicationHtml` and `dokkaGenerateModuleHtml`. The repository's present KDoc generated with no
  warning in the target layout (190 pages). The root script needs its own `failOnWarning` line (organization rule), as
  does any other Dokka block.
- **The source-less umbrella.** Observed: a project with only `java-library`, `generaltools.published-library`,
  `api(project(...))` for both modules, `dokka(project(...))` for both modules and
  `mavenPublishing { configure(JavaLibrary(javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"), sourcesJar = SourcesJar.Sources())) }`
  produces a javadoc jar of 278 files holding both modules' pages, a manifest-only class jar and a manifest-only sources
  jar. No `suppress` is needed without a Kotlin plugin (MyGameTools' umbrella, which applies Kotlin/JVM, suppresses its own
  empty source set). The umbrella is not listed in the root's `dokka(...)` dependencies, or both modules would appear twice.

### 3.5 vanniktech 0.37.0 in a convention plugin (Source-read + Observed)

- `configure(platform)` requires the platform's Kotlin or Java plugin to be applied at call time
  (`check(project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform"))` and the equivalents), so the call belongs in
  the library plugin that applies KGP, after its `plugins {}` block, not in a platform-neutral plugin.
  `configureBasedOnAppliedPlugins(javadocJar, sourcesJar)` picks the platform itself, but it is `@Incubating` and
  silently warns "No compatible plugin found" if called before the Kotlin plugin is applied (claim 12). Practice: WebTools
  and MyGameTools both call `configure(...)` in the library plugin.
- The artifact id of each publication is the Gradle project name, and the vanniktech `coordinates(...)` call is optional
  (observed: with project names `GeneralTools-core`, `GeneralTools-desktop` and `GeneralTools` the artifacts were
  `GeneralTools-core`, `-core-jvm`, `-core-js`, `-core-wasm-js`, `GeneralTools-desktop` and `GeneralTools`). A Gradle
  subproject named `GeneralTools` under a root project named `GeneralTools` was accepted. `group` and `version` taken from
  `gradle.properties` (`group=io.github.spartanlaboratories`, `version=3.0.0`) reached the POMs (observed).
- Minimum Gradle 9.0.0 and tested up to KGP 2.4.0 for 0.37.0 (claim 13); KGP 2.4.21 and Gradle 9.0.0 worked together with
  it in the scratch build (publication tasks, jar and javadoc tasks, no publish).

### 3.6 KGP 2.4.21 ABI validation across modules (Consensus + Source-read + Observed)

- "If your project has multiple modules where you want to check for binary compatibility, configure each module
  separately", and `check` runs `checkKotlinAbi` once it is enabled (claim 14). The DSL is still experimental:
  `@RequiresOptIn("The ABI Validation DSL is experimental and may be changed or removed in any future release.", ERROR)`
  (claim 14), so each convention plugin keeps `@OptIn(ExperimentalAbiValidation::class)`.
- Source, v2.4.21 `Configs.kt` / `AbiValidationPaths.kt`: the reference dump directory defaults to
  `<project dir>/api` (`LEGACY_DEFAULT_REFERENCE_DUMP_DIR = "api"`) and the file names are the **Gradle project name**
  plus `.api` (JVM) or `.klib.api` (klib targets) (claim 14). Observed: `GeneralTools-core/api/GeneralTools-core.api`,
  `GeneralTools-core/api/GeneralTools-core.klib.api` and `GeneralTools-desktop/api/GeneralTools-desktop.api`. Today's
  `api/GeneralTools.api` and `api/GeneralTools.klib.api` therefore move with the module and are renamed.
- Observed: applying the Kotlin/JVM library plugin to the umbrella wrote an empty `GeneralTools/api/GeneralTools.api`
  (0 bytes). The umbrella must not apply `abiValidation()` or `explicitApi()`.
- Observed after the split of the scratch copy: the sorted lines of the core and desktop JVM dumps together equal the
  sorted lines of the single module's JVM dump, and the klib dump differs from the old one only in
  `// Library unique name: <io.github.spartanlaboratories:GeneralTools-core>`.

### 3.7 `-PtestLevel` and `kotlin-js-store` with several modules (Source-read + Observed)

- `-PtestLevel` is a project property read at configuration time. One precompiled plugin applied by both library plugins
  carries `tasks.withType<AbstractTestTask>().configureEach { filter... }`; the pure part (the level list, parsing and the
  package pattern) is a plain Kotlin object in `build-logic/src/main/kotlin`, so it can be unit-tested. Observed:
  `-PtestLevel=integration` on core's `jvmTest` ran 27 tests with no failure and on desktop's `test` passed; `-PtestLevel=bogus`
  failed configuration with "Unknown -PtestLevel [bogus]; expected a comma-separated subset of [gating, component, ...]". A
  module without tests at a level is a green no-op because the filter sets `isFailOnNoMatchingTests = false`. The web test
  tasks are covered by [`kmp-test-levels-and-names.md`](kmp-test-levels-and-names.md) (source-read there; not run here).
- KGP applies its Node and Yarn root plugins to the **root project** from whichever module declares a `js` or `wasmJs`
  target: `NodeJsRootPlugin.apply(project.rootProject)` and `WasmNodeJsRootPlugin.apply(project.rootProject)` in
  `KotlinJsIrTarget`, and the lock directory is `lockFileDirectory(project.rootDir)` with
  `const val KOTLIN_JS_STORE = "kotlin-js-store"` (claim 15). The committed `kotlin-js-store/yarn.lock` (and
  `kotlin-js-store/wasm/`) therefore stays at the repository root with several modules, and the lock tasks
  (`kotlinUpgradeYarnLock`, `kotlinWasmUpgradeYarnLock`) run at the root.

### 3.8 Configuration cache and the build-logic compile (Observed)

- All scratch builds ran with `--configuration-cache`: entries were stored and reused (`Reusing configuration cache`),
  including with the `-PtestLevel` plugin and the Dokka aggregation. No "Unsupported Kotlin plugin version" warning was
  printed for KGP 2.4.21 on the `build-logic` classpath under Gradle 9.0.0's embedded Kotlin.
- The build script compiled with Gradle's embedded Kotlin against KGP 2.4.21 types (`KotlinVersion`, `JvmTarget`,
  `ExperimentalAbiValidation`, `abiValidation()`), so `kotlin-dsl` and KGP 2.4.21 coexist in `build-logic`.
  The comment in today's `build.gradle.kts` (lines 13-15) that `kotlin-dsl` conflicts with logback applies to a library
  module that applies `kotlin-dsl`; in `build-logic` it never reaches a module's classpath (observed: core's `jvmTest` and
  desktop's `test` ran with logback).

## 4. How comparable projects do it

| Project @ pin | Build logic | Catalog access | Included how | Dokka / publishing |
|---|---|---|---|---|
| **Ktor** [3.6.0](https://github.com/ktorio/ktor/tree/3.6.0/build-logic) | Precompiled scripts (`ktorbuild.kmp.gradle.kts` ...) and a separate `build-settings-logic` of precompiled settings plugins | `implementation(libs.kotlin.gradlePlugin)` etc. plus the class-path hack | `pluginManagement { includeBuild("build-settings-logic") }`, top-level `includeBuild("build-logic")` | Dokka and `mavenPublishing` as `build-logic` dependencies |
| **Okio** [parent-3.18.2](https://github.com/square/okio/tree/parent-3.18.2/build-support) | One plugin class `BuildSupport` | `from(files("../gradle/libs.versions.toml"))`, libraries (`libs.dokka`, `libs.kotlin.gradle.plugin`) | top-level `includeBuild("build-support")` | tapmoc for the floors |
| **Now in Android** [2982e63](https://github.com/android/nowinandroid/tree/2982e63cb691ad49cd009aab43dc206fee6699c2/build-logic) | Plugin classes (`*ConventionPlugin`) | String API, `Project.libs: VersionCatalog`; `compileOnly(libs.kotlin.gradlePlugin)` | `pluginManagement { includeBuild("build-logic") }` | n/a |
| **WebTools** (checkout) | Precompiled scripts `webtools.kotlin-jvm-library`, `webtools.kotlin-multiplatform-library`, `webtools.published-library`, `webtools.kotlin-common-check`; object `WebToolsBuild` | None: literal marker coordinates in `build-logic/build.gradle.kts`, the same versions repeated in `pluginManagement { plugins {} }` of the root settings, kept equal by a gating test | `pluginManagement { includeBuild("build-logic") }` | `webtools.published-library` applies Dokka, sets `failOnWarning`, theme and source links; each library plugin calls `configure(KotlinJvm/KotlinMultiplatform(javadocJar = JavadocJar.Dokka(WebToolsBuild.DOKKA_PUBLICATION_TASK), ...))`; the root applies both Kotlin plugins `apply false`, lists `dokka(project(...))` per module and sets `failOnWarning` |
| **MyGameTools** (checkout) | `gametools.kotlin-library`, `gametools.published-library`; the umbrella `gametools/build.gradle.kts` | None: literals; versions repeated in the root `pluginManagement { plugins {} }` | top-level `includeBuild("build-logic")` | `published-library` applies Dokka and `configure(JavaLibrary(javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml")))`; the umbrella re-lists `dokka(project(...))`, suppresses its own source sets and checks its javadoc jar has at least 20 HTML files; `gradle.properties` sets `pluginMode=V2Enabled` |

What a catalog changes for the two organization builds: the repeated plugin versions (build-logic markers, root
`pluginManagement`, root `plugins {}`) collapse into one alias, and WebTools' agreement test
(`RootScriptsAgreementGatingTest`) has nothing left to compare for versions. Neither build keeps dependency versions
in a catalog today; both keep them as literals in the convention plugins.

## 5. Pitfalls and regrets

1. **Kotlin plugins missing from the root plugin classpath.** Dokka renders empty modules and only warns (observed: 2
   pages instead of 190). `failOnWarning` does not catch it, because the message is a Gradle-level `logger.warn` at
   configuration, not a Dokka generation warning. Keep a check that the root's aggregated publication has more than the
   chrome pages (WebTools has an end-to-end test; MyGameTools' umbrella counts HTML files).
2. **Byte comparison of klibs depends on the checkout path.** Observed: the same sources and build logic built in two
   directories with different names gave different `js` and `wasmJs` klib hashes (the klib's string table and
   fingerprints hold the absolute source paths), hence different `module.json` hashes. Built at one path in sequence,
   all eight files were identical.
3. **An umbrella that applies the Kotlin JVM plugin writes an empty ABI dump** and, with `explicitApi()`, imposes API
   rules on an empty module. Keep the umbrella outside the Kotlin library plugins.
4. **`.kotlin_module` entries are named after the Gradle project.** After the split the JVM jar entries are the same except
   `META-INF/io.github.spartanlaboratories_GeneralTools.kotlin_module`, which becomes one file per module (observed). A
   check that compares entry lists has to expect it.
5. **Typed catalog accessors in convention scripts.** The class-path hack ties the build to an internal generated class and
   to one catalog name; a second catalog file breaks it (comment in #15383). A third-party plugin exists for the same
   purpose ([typesafe-conventions](https://github.com/radoslaw-panuszewski/typesafe-conventions-gradle-plugin)); a young
   plugin in the build path is more risk than four string lookups.
6. **A missing catalog alias fails late and unclearly.** `findLibrary(alias).get()` throws "No value present". Wrap the
   lookup once and name the alias and catalog in the message.
7. **`afterEvaluate` and eager task APIs in convention plugins.** vanniktech and KGP use `afterEvaluate` internally;
   convention plugins add `configureEach`/`named`/`register` only (the scratch plugins use no `afterEvaluate`).
   `registerVerifyJavaFloor` in WebTools reads `tasks.withType(GenerateModuleMetadata)` lazily for the same reason.
8. **The Node root plugin lands on the root project.** `nodejs()` in the core module adds Node and Yarn tasks (and the
   base lifecycle tasks) to the root project. A root that also applies Dokka is unaffected (observed); a future
   Project Isolation switch would not be, and KGP's own tests disable it for JS and Wasm (KT-75899, see
   [`kmp-test-levels-and-names.md`](kmp-test-levels-and-names.md)).
9. **Dokka default mode is `V2EnabledWithHelpers`.** It is V2, but it leaves V1 task stubs around; the organization rule
   says V2 mode, and `V2Enabled` states it. Dokka's own migration page marks `V2Enabled` as the end state.
10. **The documented Gradle helper prints `it.version` for a plugin version.** For a plain `"x.y.z"` entry that is the
    version; `it.version.requiredVersion` is explicit and stays correct if an entry ever becomes a range or `strictly`.

## 6. Recommendation for this repo

### 6.1 Plugin set and composition (decision 1)

Use **precompiled script plugins for the entry points, plain Kotlin classes for logic that deserves a test or a name**
(WebTools' shape). Plugin classes (Okio, Now in Android) give typed code and unit tests but cost a `gradlePlugin {}`
registration and `extensions.configure<...>` indirection for every extension, and Gradle's documentation presents script
plugins as the ordinary route. A single mega-plugin (Okio) would force the umbrella to carry Kotlin.

```text
build-logic/
  settings.gradle.kts                 # repositories + the libs catalog import
  build.gradle.kts                    # kotlin-dsl; marker dependencies from the catalog
  src/main/kotlin/
    generaltools.toolchains.settings.gradle.kts        # settings plugin: foojay resolver
    generaltools.published-library.gradle.kts          # vanniktech, signing, POM, Dokka base
    generaltools.kotlin-multiplatform-library.gradle.kts
    generaltools.kotlin-jvm-library.gradle.kts
    generaltools.test-levels.gradle.kts                # -PtestLevel
    Catalog.kt                        # Project.catalog, VersionCatalog.library(alias), .version(alias)
    GeneralToolsBuild.kt              # JAVA_FLOOR, DOKKA_PUBLICATION_TASK
    TestLevels.kt                     # level list, parse(): Result<List<String>>, package pattern
```

Composition:

| Plugin | Applies | Sets |
|---|---|---|
| `published-library` | `org.jetbrains.dokka`, `com.vanniktech.maven.publish` | `publishToMavenCentral()`, `signAllPublications()`, the shared POM (license, developer, scm, inception year, URL), `repositories { mavenCentral() }`, `dokka { dokkaPublications.configureEach { failOnWarning.set(true) } }`. Per-module `pom { name; description }` in each module script. No platform. |
| `kotlin-multiplatform-library` | `org.jetbrains.kotlin.multiplatform`, `published-library`, `test-levels` | pins from the catalog, `explicitApi()`, `jvmToolchain`, `jvm`/`js`/`wasmJs` targets, `abiValidation()`, stdlib split, `kotlin("test")` and the JUnit platform, `configure(KotlinMultiplatform(JavadocJar.Dokka(...), SourcesJar.Sources()))`. The Java-floor attribute and `verifyJavaFloor` register here. |
| `kotlin-jvm-library` | `org.jetbrains.kotlin.jvm`, `published-library`, `test-levels` | the same pins, `explicitApi()`, `abiValidation()`, test dependencies, `configure(KotlinJvm(...))`. Java floor and `verifyJavaFloor` register here. |
| `test-levels` | none | `-PtestLevel` over `AbstractTestTask`. |
| `toolchains` (settings) | foojay resolver | nothing else. |

The umbrella's build script applies `` `java-library` `` and `published-library`, declares `api(project(":GeneralTools-core"))` and
`api(project(":GeneralTools-desktop"))`, the two `dokka(project(...))` entries, and calls
`configure(JavaLibrary(javadocJar = JavadocJar.Dokka(GeneralToolsBuild.DOKKA_PUBLICATION_TASK), sourcesJar = SourcesJar.Sources()))`.
Its shape (a `java-library` here, a Kotlin/JVM module, or metadata only) is the published-shape question; this plan fits
the first two without change to the three plugins above (a Kotlin/JVM umbrella adds `dokkaSourceSets.configureEach { suppress.set(true) }`
and applies no ABI or explicit-API setting). A separate `umbrella-library` plugin pays off only with a second umbrella.

`configureBasedOnAppliedPlugins` is not used: it is `@Incubating` and order-dependent, and it would hide the one line the
organization rule asks every publishing plugin to state.

Shared per-module dependencies the convention plugins add: `kotlin-stdlib` at the floor to `commonMain`/`main`
(`api`), the web stdlib at `coreLibrariesVersion` to `jsMain` and `wasmJsMain`, `kotlin("test")`, and logback for the
JVM tests. Dependencies that belong to one module (kotlin-logging, slf4j, commons-io) stay in that module's script and
use `libs.` accessors.

Sketch of the multiplatform plugin (imports in the organization's grouping; Gradle API imports are classed as third
party, an assumption):

```kotlin
//region 2. Intended Function
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SourcesJar
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
//endregion

//region 3. Utility / Catch-all
//region 3.3 Third party
import org.gradle.api.attributes.java.TargetJvmVersion
//endregion
//endregion

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("generaltools.published-library")
    id("generaltools.test-levels")
}

val floor = catalog.version("kotlin-floor")                                   // "2.2.0"
val languageFloor = KotlinVersion.fromVersion(floor.substringBeforeLast('.')) // 2.2

kotlin {
    jvmToolchain(GeneralToolsBuild.JAVA_FLOOR)
    explicitApi()
    compilerOptions { languageVersion.set(languageFloor); apiVersion.set(languageFloor) }
    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.fromTarget(GeneralToolsBuild.JAVA_FLOOR.toString())) }
        attributes { attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, GeneralToolsBuild.JAVA_FLOOR) }
    }
    js { nodejs() }
    @OptIn(ExperimentalWasmDsl::class) wasmJs { nodejs() }
    @OptIn(ExperimentalAbiValidation::class) abiValidation()
    sourceSets {
        val webStdlib = "org.jetbrains.kotlin:kotlin-stdlib:$coreLibrariesVersion"
        commonMain.dependencies { api(catalog.library("kotlin-stdlib-floor").get()) }
        jsMain.dependencies { api(webStdlib) }
        wasmJsMain.dependencies { api(webStdlib) }
        commonTest.dependencies { implementation(kotlin("test")) }
        jvmTest.dependencies { implementation(catalog.library("logback-classic").get()) }
    }
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }

mavenPublishing {
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Dokka(GeneralToolsBuild.DOKKA_PUBLICATION_TASK), sourcesJar = SourcesJar.Sources()))
}
```

(This is the form that built in the scratch copy, with the stdlib floor read from the catalog's `kotlin-floor` version, so
the 2.2 language pin and the `kotlin-stdlib:2.2.0` requirement have one source.)

### 6.2 Catalog and its readers (decision 2)

`gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.4.21"
kotlin-floor = "2.2.0"          # language/API pin (2.2) and the published stdlib requirement
dokka = "2.3.0"
vanniktech-publish = "0.37.0"
foojay = "1.0.0"
kotlin-logging = "8.0.7"
slf4j = "2.0.16"
logback = "1.5.13"
commons-io = "2.4"

[libraries]
kotlin-stdlib-floor = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin-floor" }
kotlin-logging = { module = "io.github.oshai:kotlin-logging", version.ref = "kotlin-logging" }
slf4j-api = { module = "org.slf4j:slf4j-api", version.ref = "slf4j" }
logback-classic = { module = "ch.qos.logback:logback-classic", version.ref = "logback" }
commons-io = { module = "org.apache.directory.studio:org.apache.commons.io", version.ref = "commons-io" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
dokka = { id = "org.jetbrains.dokka", version.ref = "dokka" }
maven-publish = { id = "com.vanniktech.maven.publish", version.ref = "vanniktech-publish" }
foojay-resolver = { id = "org.gradle.toolchains.foojay-resolver-convention", version.ref = "foojay" }
```

- `build-logic/settings.gradle.kts`: `dependencyResolutionManagement { repositories { mavenCentral(); gradlePluginPortal() }; versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } } }`
  and `rootProject.name = "build-logic"`.
- `build-logic/build.gradle.kts`: `plugins { `kotlin-dsl` }`, the documented helper
  `fun Provider<PluginDependency>.marker() = map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}" }`,
  and `implementation(libs.plugins.kotlin.multiplatform.marker())` for `kotlin.multiplatform`, `kotlin.jvm`, `dokka`,
  `maven.publish` and `foojay.resolver`.
- Convention plugins: `catalog.library("alias")` / `catalog.version("alias")` (the string API behind a typed helper
  that names the missing alias). Module and root scripts: the generated `libs.` accessors.
- Settings: `pluginManagement { includeBuild("build-logic") }`, `plugins { id("generaltools.toolchains") }`,
  `rootProject.name = "GeneralTools"`, `include(":GeneralTools-core", ":GeneralTools-desktop", ":GeneralTools")`. **If the settings-plugin
  indirection proves awkward in an IDE or on CI, fall back to** `plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }`
  in `settings.gradle.kts` (Ktor 3.6.0's choice) and drop the `foojay` catalog entry; that is then the only plugin version
  outside the catalog, forced by Gradle's rule that settings cannot use catalog aliases.
- `gradle.properties` carries `group=io.github.spartanlaboratories` and `version=3.0.0`, so no `allprojects {}` block is
  needed (observed to reach every POM); the alternative is `allprojects { group = ...; version = ... }` in the root script
  as WebTools has it.
- Rejected: `files(libs.javaClass.superclass...)` (internal class, one catalog name), `typesafe-conventions` (young
  third-party plugin), and `buildSrc` (organization rule requires a `build-logic` build; a `buildSrc` edit also
  invalidates the whole build).

### 6.3 Where `build-logic` is included, and the root script (decision 3)

`pluginManagement { includeBuild("build-logic") }`: the settings plugin needs it, project plugins resolve identically
(observed), and it matches WebTools, Ktor and Now in Android. The root `build.gradle.kts` holds no source:

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka)
}
repositories { mavenCentral() }
dependencies {
    dokka(project(":GeneralTools-core"))
    dokka(project(":GeneralTools-desktop"))
}
dokka {
    moduleName.set("GeneralTools")
    dokkaPublications.configureEach { failOnWarning.set(true) }
}
```

Both Kotlin aliases stay, although either would put the one KGP jar on the classpath, so the plugin set the modules apply
(KMP in core, JVM in desktop) is the set the root declares. The `pluginManagement { plugins {} }` version list and its
agreement test are not needed.

### 6.4 Dokka and the javadoc jars (decision 4)

- `gradle.properties`: `org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled` and `...pluginMode.noWarn=true`.
- `published-library` applies Dokka and sets `failOnWarning`; each library plugin sets
  `JavadocJar.Dokka(GeneralToolsBuild.DOKKA_PUBLICATION_TASK)` in its `configure(...)`; the umbrella does the same in its
  own script with `JavaLibrary`. Branding and per-module source links are not part of this change (the repository has no
  `docs/brand` or `docs/dokka` yet); `published-library` is where they would go, as in WebTools.
- CI adds `./gradlew dokkaGeneratePublicationHtml` (organization rule); the repository's present KDoc passes
  `failOnWarning` (observed, 190 pages).

### 6.5 ABI dumps, test levels, yarn lock (decision 5)

- Both library plugins call `abiValidation()`; dumps are committed under each module's `api/`, named after the project:
  `GeneralTools-core/api/GeneralTools-core.api`, `GeneralTools-core/api/GeneralTools-core.klib.api`,
  `GeneralTools-desktop/api/GeneralTools-desktop.api`. The umbrella has none.
- `test-levels` is applied by both library plugins. Invoke JVM-only levels per module (`:GeneralTools-core:jvmTest`,
  `:GeneralTools-desktop:test`) because a filtered-to-nothing web task still builds and starts Node (see the earlier
  findings file).
- `kotlin-js-store/` stays where it is (root). Nothing in `build-logic` sets `lockFileDirectory`.

### 6.6 Proving each step unchanged (decision 6)

Procedure, one working tree, one absolute path, builds in sequence (the path matters for klib hashes):

1. At the "before" commit: `./gradlew clean`, then every publication's POM and module task, unqualified so a multi-project
   build runs it in every module that has it:
   `generatePomFileForKotlinMultiplatformPublication generatePomFileForJvmPublication generatePomFileForJsPublication generatePomFileForWasmJsPublication generatePomFileForMavenPublication`
   and the same names with `generateMetadataFileFor`. Copy each project's `build/publications/` and
   `build/libs/*.jar|*.klib` hashes aside. These tasks need no signing key and no network beyond dependency resolution
   (about 20 s on the scratch module).
2. At the "after" commit: the same, then `cmp` every file, and `./gradlew checkKotlinAbi`.

What each step is expected to show (observed where stated):

| Step | Expected | Result in the scratch build |
|---|---|---|
| 1. Catalog, `build-logic`, convention plugins; still one module | all POMs, `module.json` files (their jar and klib sizes and hashes included) byte-identical; dumps unchanged | observed: 8 of 8 files identical; `checkKotlinAbi` green |
| 2. Dokka javadoc jars | POMs and `module.json` identical (javadoc jars are not listed in the module metadata); only the `*-javadoc.jar` files change, from empty to the Dokka output | `module.json` lists no javadoc jar (observed); jars of 262 files |
| 3. The split | POMs differ by artifact id (and by intended dependency changes); `jvm` class entries of core plus desktop equal the old jar's entries except `META-INF/*.kotlin_module`; JVM dump lines of the two modules sorted equal the old dump; klib dump differs only in the library unique name | all three observed |

A step-3 check is also the place to confirm `Misc` facade classes and the JVM class set (core jar plus desktop jar) equal
the old jar's, as listed above. The Kotlin 2.2.0 consumer compile and the consumer smoke matrix stay in the release
rehearsal.

Where the build-logic itself deserves tests: the pure `TestLevels.parse` (a unit test in `build-logic/src/test`, which on
Gradle 9 needs `testRuntimeOnly("org.junit.platform:junit-platform-launcher")`, as WebTools' `build-logic/build.gradle.kts`
notes) is worth having; WebTools' TestKit-driven checks of the plugins are far heavier than a three-module repository
needs, and the publication comparison above covers what they would.

## 7. Load-bearing claims

1. **A `build-logic` composite build is a documented way to share build logic.** "In addition to buildSrc, another
   powerful way to share build logic across subprojects is by using a dedicated composite build, typically named
   build-logic." — [Sharing build logic between subprojects](https://docs.gradle.org/9.0.0/userguide/sharing_build_logic_between_subprojects.html), Gradle 9.0.0.
2. **Plugin builds are included through `pluginManagement`, and may contribute settings plugins.** "These builds should be
   included using the includeBuild statement inside the pluginManagement {} block of the settings file." and "Using this
   mechanism, the included build may also contribute a settings plugin that can be applied in the settings file itself" —
   [Composite builds](https://docs.gradle.org/9.0.0/userguide/composite_builds.html), Gradle 9.0.0.
3. **A convention plugin's external plugins are `build-logic` dependencies, versioned there.** "In order to apply an external
   plugin in a precompiled script plugin, it has to be added to the plugin project’s implementation classpath in the
   plugin’s build file" and "The plugin version in this case is defined in the dependency declaration." —
   [Precompiled script plugins](https://docs.gradle.org/9.0.0/userguide/implementing_gradle_plugins_precompiled.html), Gradle 9.0.0.
4. **Catalog access in `build-logic`.** "does not automatically inherit the version catalog from the main project, so
   additional configuration is required."; "the plugins block in the precompiled script plugin cannot access the version
   catalog."; "You cannot use a plugin declared in a version catalog in your settings file or settings plugin."; "In
   precompiled script plugins inside buildSrc, the version catalog can be accessed using
   extensions.getByType(VersionCatalogsExtension)"; "Gradle automatically imports a catalog in the gradle directory named
   libs.versions.toml."; the import `create("libs") { from(files("../gradle/libs.versions.toml")) }`; and the helper
   `plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }` — [Version catalogs](https://docs.gradle.org/9.0.0/userguide/version_catalogs.html), Gradle 9.0.0.
5. **The typed accessors are still not available to precompiled script plugins.** Issue title "Make generated type-safe
   version catalogs accessors accessible from precompiled script plugins", state OPEN, last updated 2026-06-22 —
   [gradle/gradle#15383](https://github.com/gradle/gradle/issues/15383), read 2026-11-21.
6. **Ktor 3.6.0 uses the class-path hack, precompiled scripts, a settings-plugin included build and a literal foojay version.**
   "// A hack to make version catalogs accessible from buildSrc sources" and
   "implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))"
   ([build-logic/build.gradle.kts](https://github.com/ktorio/ktor/blob/3.6.0/build-logic/build.gradle.kts)); "id(\"org.gradle.toolchains.foojay-resolver-convention\") version \"1.0.0\"" and
   "includeBuild(\"build-settings-logic\")" inside `pluginManagement`
   ([settings.gradle.kts](https://github.com/ktorio/ktor/blob/3.6.0/settings.gradle.kts)) — Ktor 3.6.0.
7. **Now in Android reads the catalog through the string API.** "val Project.libs" / "get(): VersionCatalog =
   extensions.getByType<VersionCatalogsExtension>().named(\"libs\")" —
   [ProjectExtensions.kt @ 2982e63](https://github.com/android/nowinandroid/blob/2982e63cb691ad49cd009aab43dc206fee6699c2/build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/ProjectExtensions.kt).
8. **Okio 3.18.2 shares the catalog with a top-level included build.** "from(files(\"../gradle/libs.versions.toml\"))"
   ([build-support/settings.gradle.kts](https://github.com/square/okio/blob/parent-3.18.2/build-support/settings.gradle.kts)),
   "implementation(libs.dokka)" ([build-support/build.gradle.kts](https://github.com/square/okio/blob/parent-3.18.2/build-support/build.gradle.kts)) and
   `includeBuild("build-support")` ([settings.gradle.kts](https://github.com/square/okio/blob/parent-3.18.2/settings.gradle.kts)) — parent-3.18.2.
9. **Dokka 2.3.0: V2 is the default, in `V2EnabledWithHelpers`; `V2Enabled` is the documented end state.** "Starting from
   Dokka 2.1.0, DGP v2 is enabled by default." and "After successful migration, set the following opt-in flag without
   helpers in the project's gradle.properties file: org.jetbrains.dokka.experimental.gradle.pluginMode=V2Enabled"
   ([Migrate to Dokka Gradle plugin v2](https://kotlinlang.org/docs/dokka-migration.html), read 2026-11-21); "val Default: PluginMode =
   V2EnabledWithHelpers" ([PluginFeaturesService.kt @ v2.3.0](https://github.com/Kotlin/dokka/blob/v2.3.0/dokka-runners/dokka-gradle-plugin/src/main/kotlin/internal/PluginFeaturesService.kt)).
10. **Dokka needs the Kotlin plugins on the root plugin classpath.** "|1. Apply the Dokka and Kotlin plugins to the root project
    using the `plugins {}` DSL." and "|   (If the root project does not need the plugins, use 'apply false')" —
    [KotlinAdapter.kt @ v2.3.0](https://github.com/Kotlin/dokka/blob/v2.3.0/dokka-runners/dokka-gradle-plugin/src/main/kotlin/adapters/KotlinAdapter.kt).
11. **Aggregation is a `dependencies {}` block.** "Aggregation in DGP v2 uses the `dependencies {}` block instead of tasks and
    can be added in any `build.gradle.kts` file." — [Migrate to Dokka Gradle plugin v2](https://kotlinlang.org/docs/dokka-migration.html), read 2026-11-21.
12. **`JavadocJar.Dokka` and `configure` in vanniktech 0.37.0.** "Creates a javadoc jar using Dokka's output. The argument is
    the name of the dokka task that should be used for that purpose." (`JavadocJar.Dokka`) and, on
    `configureBasedOnAppliedPlugins`, "Calls [configure] with a [Platform] chosen based on other applied Gradle plugins."
    annotated `@Incubating`; `check(project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform"))` in
    `KotlinMultiplatform.configure` — [Platform.kt](https://github.com/vanniktech/gradle-maven-publish-plugin/blob/0.37.0/plugin/src/main/kotlin/com/vanniktech/maven/publish/Platform.kt) and
    [MavenPublishBaseExtension.kt](https://github.com/vanniktech/gradle-maven-publish-plugin/blob/0.37.0/plugin/src/main/kotlin/com/vanniktech/maven/publish/MavenPublishBaseExtension.kt), 0.37.0.
13. **vanniktech 0.37.0 compatibility.** In the `## [0.37.0]` section, under "Minimum supported versions": "Gradle 9.0.0"; under "Compatibility tested up to": "Kotlin Gradle Plugin 2.4.0" —
    [CHANGELOG.md](https://github.com/vanniktech/gradle-maven-publish-plugin/blob/0.37.0/CHANGELOG.md), 0.37.0.
14. **KGP 2.4.21 ABI validation.** "If your project has multiple modules where you want to check for binary compatibility,
    configure each module separately." and "By default, when binary compatibility validation is enabled in your project and
    you run the `check` task, Gradle also runs the `checkKotlinAbi` task." —
    [Binary compatibility validation](https://kotlinlang.org/docs/gradle-binary-compatibility-validation.html), read 2026-11-21;
    "The ABI Validation DSL is experimental and may be changed or removed in any future release." —
    [ExperimentalAbiValidation.kt @ v2.4.21](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin-api/src/common/kotlin/org/jetbrains/kotlin/gradle/dsl/abi/ExperimentalAbiValidation.kt);
    `internal const val LEGACY_DEFAULT_REFERENCE_DUMP_DIR = "api"` ([AbiValidationPaths.kt](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/plugin/abi/internal/AbiValidationPaths.kt));
    `val klibFileName = "$projectName$LEGACY_KLIB_DUMP_EXTENSION"` ([Configs.kt](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/plugin/abi/internal/Configs.kt));
    `project.layout.projectDirectory.dir(AbiValidationPaths.LEGACY_DEFAULT_REFERENCE_DUMP_DIR)` (Configs.kt); the extension is built
    with the project's `name` (`AbiValidationExtensionImpl()` in [AbiValidationExtension.kt](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/plugin/abi/internal/AbiValidationExtension.kt)) — v2.4.21.
15. **The yarn lock lives at the root project.** "{ NodeJsRootPlugin.apply(project.rootProject) }" and
    "WasmNodeJsRootPlugin.apply(project.rootProject)" ([KotlinJsIrTarget.kt](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/targets/js/ir/KotlinJsIrTarget.kt));
    "lockFileDirectory(project.rootDir)" ([YarnPluginApplier.kt](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/targets/js/yarn/YarnPluginApplier.kt));
    `const val KOTLIN_JS_STORE = "kotlin-js-store"` ([LockCopyTask.kt](https://github.com/JetBrains/kotlin/blob/v2.4.21/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/targets/js/npm/LockCopyTask.kt)) — v2.4.21.
16. **The pins are current.** `gh release list` on 2026-11-21: Dokka "2.3.0 Latest v2.3.0 2026-11-05T17:15:03Z"; vanniktech
    "0.37.0 Latest 0.37.0 2026-06-21T17:17:21Z"; Kotlin "Kotlin 2.4.21 Latest v2.4.21 2026-10-08T09:25:15Z"; Gradle
    "9.9.2 Latest v9.9.2 2026-11-19T20:59:54Z".
17. **KGP 2.4.21 supports Gradle 9.0.0.** The compatibility row for KGP 2.4.20 to 2.4.21 (Gradle 7.6.3 to 9.7.0) is verified in
    [`kmp-build-and-kotlin-compat.md`](kmp-build-and-kotlin-compat.md) §F6 (claim 15 there), not re-quoted here; the scratch build ran
    KGP 2.4.21 on Gradle 9.0.0.

## 8. Assumptions

1. **Project names equal artifact ids** (`GeneralTools-core`, `GeneralTools-desktop`, `GeneralTools`), so no
   `coordinates(...)` call is needed. Observed to hold, including a subproject named like the root project. If a tool
   objects to the repeated name, add `coordinates(artifactId = ...)` in the module, or rename the root project (which
   changes only unpublished npm package names).
2. **`group` and `version` come from `gradle.properties`.** Observed to reach every POM; if a later Gradle rejects it, use
   `allprojects {}` in the root script.
3. **The umbrella is `java-library` plus the publishing plugin.** The published-shape question decides it; a Kotlin/JVM
   umbrella changes only the umbrella's build script (suppress its source sets, no ABI or explicit API).
4. **Import-group classification.** The grouping policy has no example for build scripts; Gradle API imports are placed in
   3.3, vanniktech and KGP imports in 2. If the policy owner classes them differently, only the import comments change.
5. **The settings plugin is acceptable.** If it is not, one literal foojay version in `settings.gradle.kts` is the fallback and
   the only plugin version outside the catalog.
6. **The Gradle wrapper stays 9.0.0**; everything observed ran there. KGP 2.4.21 is documented up to Gradle 9.7.0, so a
   wrapper bump needs no build-logic change; vanniktech 0.37.0 needs 9.0.0 or newer.
7. **Configuration cache stays enabled** on maintainers' machines; the conventions are cache-clean in the scratch build.
8. **No branding or site work** is part of this change (the repository has no `docs/brand`, `docs/dokka` or `website/`).

## 9. Gaps

- **Web test tasks were not run.** The scratch build compiled and published-metadata-checked `js` and `wasmJs`, ran
  `jvmTest` and desktop's `test`, but did not run `jsNodeTest`/`wasmJsNodeTest` (they download Node, Yarn and npm). The
  `-PtestLevel` behaviour on them rests on the source reading in `kmp-test-levels-and-names.md`. The first real run needs
  `kotlinUpgradeYarnLock` if the lock moved.
- **Signing and upload were not exercised.** `signAllPublications()` is present in the plugin; no signing task, file
  repository or Central upload ran. `generatePomFile*` and `generateMetadataFile*` need no key.
- **Only Windows 11 with a Gradle JVM of 23 and a JDK 17 toolchain** was used; CI (Ubuntu, JDK 17) was not run. The klib
  path dependence (pitfall 2) is the one place a platform difference could matter for the comparison, and the procedure
  avoids it by using one path.
- **The Java-floor mechanics** (`-Xjdk-release`, `verifyJavaFloor`, `TargetJvmVersion` on the desktop and umbrella
  variants) are out of scope; the scratch plugins set `jvmToolchain`, `jvmTarget` and the multiplatform `TargetJvmVersion`
  attribute exactly as the current build does and leave `GeneralToolsBuild.JAVA_FLOOR` as the single constant. The desktop
  and umbrella variants' `org.gradle.jvm.version` was not inspected.
- **The real source-less umbrella's Maven Central acceptance** (a manifest-only class jar and sources jar) and its consumer
  resolution are the published-shape question's; only its javadoc jar and POM were checked here.
- **Dokka warnings on future KDoc.** The present KDoc passes `failOnWarning`; `reportUndocumented` and source links
  (WebTools sets both) were not evaluated for this repository.
- **No unit tests of `build-logic` were written or run**; `TestLevels.parse` is the logic that would carry one.
- **The scratch build is not kept.** The observations are reproducible from the layout in section 6; the layout itself is a
  sketch, not the final build files.
