/* plan.js: plan.json as a script, written by plan/scripts/embed.js beside the board for its live view (plan/RULES.md §1). Do not edit: edit plan.json and re-embed. */
window.dashboardLive&&window.dashboardLive("plan",
{
  "schema": "plan-board/1",
  "repo": "SpartanLaboratories/GeneralTools",
  "issue": 7,
  "slug": "7-kotlin-multiplatform",
  "folder": "docs/plans/7-kotlin-multiplatform",
  "kind": "tracking",
  "title": "Kotlin Multiplatform",
  "issueTitle": "Make GeneralTools Kotlin Multiplatform (Color and geometry usable from common code)",
  "function": "GeneralTools ships as a JVM-only jar, so the common code of new Kotlin Multiplatform libraries cannot use its `Color` and geometry types. The issue asks for a multiplatform build with `Color` and the geometry package in common code, the JVM-only helpers kept on the JVM, and a release to Maven Central. The design makes it release `3.0.0`: Kotlin Multiplatform with `jvm`, `js` and `wasmJs` targets, a fixed set of ten API breaks proved against the `2.2.0` API dump, and an immutable box copy that also resolves #8. Seven units of it, from the multiplatform build to the `Misc` and `Color` breaks, are already on the feature branch, built and not yet merged; the board's baseline is the branch as it stands, so they are part of it and are not Rows of this board. This run amended the architecture for six issues that belong on the branch before it merges (#20, #21, #23, #24, #25, #26, with #17 folded into the module split): three modules published at one version, `GeneralTools-core`, `GeneralTools-desktop` and a source-less `GeneralTools` umbrella over both; a version catalog; convention plugins with Dokka javadoc jars; and a Java floor of 17 guarded in `check`. The board's Rows are the eight units of work still to do, in landing order: those six issues, a review of the seven built units, and the release. Each lands into the feature branch, and one merge to master at the end is the 3.0.0 landing.",
  "status": "planned",
  "sized": "2026-11-22T02:23:17Z",
  "parent": null,
  "fixOf": null,
  "branch": "feat/issue-7-kotlin-multiplatform",
  "baseline": {
    "ref": "feat/issue-7-kotlin-multiplatform",
    "sha": "29dd4ff020ea865798c9d1d11d083b2e2a118f7c",
    "date": "2026-11-21"
  },
  "asBuilt": null,
  "systems": [
    {
      "slug": "failure-and-logging-seam",
      "name": "Failure and logging seam",
      "slot": 2,
      "top": "Failure and logging seam",
      "status": "built",
      "board": "docs/dashboards/failure-and-logging-seam/index.html"
    },
    {
      "slug": "version-catalog",
      "name": "Version catalog",
      "slot": 1,
      "top": "Gradle build",
      "status": "planned",
      "board": null
    },
    {
      "slug": "build-logic",
      "name": "Build logic",
      "slot": 1,
      "top": "Gradle build",
      "status": "planned",
      "board": null
    },
    {
      "slug": "java-floor",
      "name": "Java floor",
      "slot": 1,
      "top": "Gradle build",
      "status": "planned",
      "board": null
    },
    {
      "slug": "module-layout-and-published-shape",
      "name": "Module layout and published shape",
      "slot": 1,
      "top": "Gradle build",
      "status": "planned",
      "board": null
    },
    {
      "slug": "test-suite",
      "name": "Test suite",
      "slot": 9,
      "top": "Test suite",
      "status": "built",
      "board": "docs/dashboards/test-suite/index.html"
    },
    {
      "slug": "publication-proof-and-release-set",
      "name": "Publication proof and release set",
      "slot": 1,
      "top": "Gradle build",
      "status": "planned",
      "board": null
    }
  ],
  "wip": {
    "step": "document",
    "since": "2026-11-22T23:02:14Z",
    "updated": "2026-11-23T00:13:01Z",
    "detail": "Document: 3 of 3 tasks done; every task done.",
    "steps": [
      "sizing",
      "interview",
      "research",
      "architecture",
      "document",
      "landing",
      "merge",
      "sub-issues",
      "implementation",
      "landed"
    ],
    "current": [
      "built"
    ],
    "questions": [],
    "research": null,
    "interview": [
      {
        "text": "Where do the remaining units land?",
        "options": [
          "The feature branch is the base: each remaining unit lands into it, and one merge to master at the end is the 3.0.0 landing.",
          "The branch merges to master first with the version held, and each remaining unit then lands on master.",
          "The branch merges to master as built, at 3.0.0, before the module split."
        ],
        "answer": "The first option: the remaining units land into the feature branch, and one merge to master at the end is the 3.0.0 landing.",
        "asked": "2026-11-22T02:47:08Z",
        "answered": "2026-11-22T03:04:14Z"
      },
      {
        "text": "How is 3.0.0 released?",
        "options": [
          "Staged and by hand, for 3.0.0 only: upload, checks against the staged deployment, the Publish click, then the tag.",
          "The standing automatic release at the final landing, with the pre-publish checks run before it against a file-repository publication of the same tree."
        ],
        "answer": "The second option: the standing automatic release at the final landing, after the pre-publish checks have run against a file-repository publication of the same tree.",
        "asked": "2026-11-22T02:47:08Z",
        "answered": "2026-11-22T03:04:14Z"
      },
      {
        "text": "Do the seven units already built go through today's quality and documentation steps?",
        "options": [
          "As a review unit of its own, after the module split and before the release.",
          "Folded into the release unit, over the whole branch.",
          "Taken as built, with only the CHANGELOG entries and the boards added."
        ],
        "answer": "The first option: a review unit of their own, after the module split and before the release.",
        "asked": "2026-11-22T02:47:08Z",
        "answered": "2026-11-22T03:04:14Z"
      },
      {
        "text": "After the module split, what are the artifacts, and what does io.github.spartanlaboratories:GeneralTools resolve to in 3.0.0?",
        "options": [
          "GeneralTools-core and GeneralTools-desktop, with a source-less GeneralTools artifact depending on both.",
          "generaltools-core and generaltools-desktop, with a source-less GeneralTools artifact depending on both.",
          "generaltools-core and generaltools-desktop, and no GeneralTools 3.0.0."
        ],
        "answer": "The first option: GeneralTools-core and GeneralTools-desktop, with a source-less GeneralTools artifact depending on both.",
        "asked": "2026-11-22T02:47:08Z",
        "answered": "2026-11-22T03:04:14Z"
      },
      {
        "text": "Do the artifacts share one version?",
        "options": [
          "One 3.0.0 for every artifact, with a bare 3.0.0 tag.",
          "A version per module, with a tag per module."
        ],
        "answer": "The first option: one 3.0.0 for every artifact, with a bare 3.0.0 tag.",
        "asked": "2026-11-22T02:47:08Z",
        "answered": "2026-11-22T03:04:14Z"
      },
      {
        "text": "How much of #19 (Dokka) joins 3.0.0?",
        "options": [
          "The Dokka javadoc jar only; the CI job, the site and its branding follow 3.0.0 on master.",
          "All of #19, as a unit before the release.",
          "None of it: 3.0.0 ships an empty javadoc jar."
        ],
        "answer": "The first option: the Dokka javadoc jar only; the CI job, the site and its branding follow 3.0.0 on master.",
        "asked": "2026-11-22T02:47:08Z",
        "answered": "2026-11-22T03:04:14Z"
      },
      {
        "text": "When is #22 (import grouping) done?",
        "options": [
          "After 3.0.0 lands on master; the remaining units write their new files to the grouping from the start.",
          "As a unit on the branch, after the module split."
        ],
        "answer": "The first option: after 3.0.0 lands on master, the remaining units writing their new files to the grouping from the start.",
        "asked": "2026-11-22T02:47:08Z",
        "answered": "2026-11-22T03:04:14Z"
      },
      {
        "text": "Are the fourteen recommended changes to the documents made as recommended?",
        "options": [
          "All of them, with the as-built record reworded when it is reconciled at the final landing.",
          "All of them, with the as-built record reworded now."
        ],
        "answer": "The first option: all of them, with the as-built record reworded when it is reconciled at the final landing.",
        "asked": "2026-11-22T02:47:08Z",
        "answered": "2026-11-22T03:04:14Z"
      }
    ],
    "plan": null,
    "stages": [],
    "remediation": null,
    "open": [],
    "event": {
      "name": "document",
      "at": "2026-11-23T00:13:01Z",
      "note": "3 of 3 tasks done"
    },
    "history": {
      "sizing": [
        {
          "since": null,
          "ended": "2026-11-22T02:23:17Z",
          "endedBy": "sized",
          "to": "interview"
        }
      ],
      "interview": [
        {
          "since": "2026-11-22T02:23:17Z",
          "ended": "2026-11-22T02:47:08Z",
          "endedBy": "halted",
          "to": "interview",
          "event": {
            "name": "sized",
            "at": "2026-11-22T02:23:17Z"
          },
          "detail": "Amending the architecture, `architecture.md`, for the six issues that belong on the branch before it merges (#20, #21, #23, #24, #25, #26), after checking it against the branch as built. The remaining units and their order are settled with the release last.",
          "interview": [
            {
              "text": "Where do the remaining units land?",
              "options": [
                "The feature branch is the base: each remaining unit lands into it, and one merge to master at the end is the 3.0.0 landing.",
                "The branch merges to master first with the version held, and each remaining unit then lands on master.",
                "The branch merges to master as built, at 3.0.0, before the module split."
              ],
              "answer": "The first option: the remaining units land into the feature branch, and one merge to master at the end is the 3.0.0 landing.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "How is 3.0.0 released?",
              "options": [
                "Staged and by hand, for 3.0.0 only: upload, checks against the staged deployment, the Publish click, then the tag.",
                "The standing automatic release at the final landing, with the pre-publish checks run before it against a file-repository publication of the same tree."
              ],
              "answer": "The second option: the standing automatic release at the final landing, after the pre-publish checks have run against a file-repository publication of the same tree.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "Do the seven units already built go through today's quality and documentation steps?",
              "options": [
                "As a review unit of its own, after the module split and before the release.",
                "Folded into the release unit, over the whole branch.",
                "Taken as built, with only the CHANGELOG entries and the boards added."
              ],
              "answer": "The first option: a review unit of their own, after the module split and before the release.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "After the module split, what are the artifacts, and what does io.github.spartanlaboratories:GeneralTools resolve to in 3.0.0?",
              "options": [
                "GeneralTools-core and GeneralTools-desktop, with a source-less GeneralTools artifact depending on both.",
                "generaltools-core and generaltools-desktop, with a source-less GeneralTools artifact depending on both.",
                "generaltools-core and generaltools-desktop, and no GeneralTools 3.0.0."
              ],
              "answer": "The first option: GeneralTools-core and GeneralTools-desktop, with a source-less GeneralTools artifact depending on both.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "Do the artifacts share one version?",
              "options": [
                "One 3.0.0 for every artifact, with a bare 3.0.0 tag.",
                "A version per module, with a tag per module."
              ],
              "answer": "The first option: one 3.0.0 for every artifact, with a bare 3.0.0 tag.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "How much of #19 (Dokka) joins 3.0.0?",
              "options": [
                "The Dokka javadoc jar only; the CI job, the site and its branding follow 3.0.0 on master.",
                "All of #19, as a unit before the release.",
                "None of it: 3.0.0 ships an empty javadoc jar."
              ],
              "answer": "The first option: the Dokka javadoc jar only; the CI job, the site and its branding follow 3.0.0 on master.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "When is #22 (import grouping) done?",
              "options": [
                "After 3.0.0 lands on master; the remaining units write their new files to the grouping from the start.",
                "As a unit on the branch, after the module split."
              ],
              "answer": "The first option: after 3.0.0 lands on master, the remaining units writing their new files to the grouping from the start.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "Are the fourteen recommended changes to the documents made as recommended?",
              "options": [
                "All of them, with the as-built record reworded when it is reconciled at the final landing.",
                "All of them, with the as-built record reworded now."
              ],
              "answer": "The first option: all of them, with the as-built record reworded when it is reconciled at the final landing.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            }
          ],
          "held": true
        },
        {
          "since": "2026-11-22T03:04:14Z",
          "ended": "2026-11-22T03:07:46Z",
          "endedBy": "research-planning",
          "to": "research",
          "event": {
            "name": "resumed",
            "at": "2026-11-22T03:04:14Z"
          },
          "detail": "The remaining units land into the feature branch, and one merge to master at the end is the 3.0.0 landing. 3.0.0 is released by the standing automatic release at that landing, after the pre-publish checks have run against a file-repository publication of the same tree. The seven units already built get a review unit of their own, after the module split and before the release. The artifacts are GeneralTools-core and GeneralTools-desktop with a source-less GeneralTools artifact depending on both, all at one version. Of #19 only the Dokka javadoc jar joins 3.0.0, and #22 follows it on master. The fourteen recommended document changes are made, the as-built record being reworded when it is reconciled at the final landing.",
          "interview": [
            {
              "text": "Where do the remaining units land?",
              "options": [
                "The feature branch is the base: each remaining unit lands into it, and one merge to master at the end is the 3.0.0 landing.",
                "The branch merges to master first with the version held, and each remaining unit then lands on master.",
                "The branch merges to master as built, at 3.0.0, before the module split."
              ],
              "answer": "The first option: the remaining units land into the feature branch, and one merge to master at the end is the 3.0.0 landing.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "How is 3.0.0 released?",
              "options": [
                "Staged and by hand, for 3.0.0 only: upload, checks against the staged deployment, the Publish click, then the tag.",
                "The standing automatic release at the final landing, with the pre-publish checks run before it against a file-repository publication of the same tree."
              ],
              "answer": "The second option: the standing automatic release at the final landing, after the pre-publish checks have run against a file-repository publication of the same tree.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "Do the seven units already built go through today's quality and documentation steps?",
              "options": [
                "As a review unit of its own, after the module split and before the release.",
                "Folded into the release unit, over the whole branch.",
                "Taken as built, with only the CHANGELOG entries and the boards added."
              ],
              "answer": "The first option: a review unit of their own, after the module split and before the release.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "After the module split, what are the artifacts, and what does io.github.spartanlaboratories:GeneralTools resolve to in 3.0.0?",
              "options": [
                "GeneralTools-core and GeneralTools-desktop, with a source-less GeneralTools artifact depending on both.",
                "generaltools-core and generaltools-desktop, with a source-less GeneralTools artifact depending on both.",
                "generaltools-core and generaltools-desktop, and no GeneralTools 3.0.0."
              ],
              "answer": "The first option: GeneralTools-core and GeneralTools-desktop, with a source-less GeneralTools artifact depending on both.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "Do the artifacts share one version?",
              "options": [
                "One 3.0.0 for every artifact, with a bare 3.0.0 tag.",
                "A version per module, with a tag per module."
              ],
              "answer": "The first option: one 3.0.0 for every artifact, with a bare 3.0.0 tag.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "How much of #19 (Dokka) joins 3.0.0?",
              "options": [
                "The Dokka javadoc jar only; the CI job, the site and its branding follow 3.0.0 on master.",
                "All of #19, as a unit before the release.",
                "None of it: 3.0.0 ships an empty javadoc jar."
              ],
              "answer": "The first option: the Dokka javadoc jar only; the CI job, the site and its branding follow 3.0.0 on master.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "When is #22 (import grouping) done?",
              "options": [
                "After 3.0.0 lands on master; the remaining units write their new files to the grouping from the start.",
                "As a unit on the branch, after the module split."
              ],
              "answer": "The first option: after 3.0.0 lands on master, the remaining units writing their new files to the grouping from the start.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            },
            {
              "text": "Are the fourteen recommended changes to the documents made as recommended?",
              "options": [
                "All of them, with the as-built record reworded when it is reconciled at the final landing.",
                "All of them, with the as-built record reworded now."
              ],
              "answer": "The first option: all of them, with the as-built record reworded when it is reconciled at the final landing.",
              "asked": "2026-11-22T02:47:08Z",
              "answered": "2026-11-22T03:04:14Z"
            }
          ]
        }
      ],
      "research": [
        {
          "since": "2026-11-22T03:07:46Z",
          "ended": "2026-11-22T03:49:09Z",
          "endedBy": "researched",
          "to": "architecture",
          "event": {
            "name": "verification-started",
            "at": "2026-11-22T03:40:04Z"
          },
          "detail": "Research: verifying 3 findings.",
          "research": {
            "phase": "complete",
            "drafted": 0,
            "since": {
              "planning": "2026-11-22T03:07:46Z",
              "research": "2026-11-22T03:09:41Z",
              "verification": "2026-11-22T03:40:04Z",
              "complete": "2026-11-22T03:49:09Z"
            }
          },
          "questions": [
            {
              "question": "published-shape-after-the-split",
              "rows": [],
              "findings": "research/published-shape-after-the-split.md",
              "state": "complete",
              "started": "2026-11-22T03:09:41Z",
              "returned": "2026-11-22T03:37:59Z"
            },
            {
              "question": "build-logic-and-version-catalog",
              "rows": [],
              "findings": "research/build-logic-and-version-catalog.md",
              "state": "complete",
              "started": "2026-11-22T03:09:41Z",
              "returned": "2026-11-22T03:37:59Z"
            },
            {
              "question": "java-floor-on-a-multiplatform-jvm-target",
              "rows": [],
              "findings": "research/java-floor-on-a-multiplatform-jvm-target.md",
              "state": "complete",
              "started": "2026-11-22T03:09:41Z",
              "returned": "2026-11-22T03:37:59Z"
            }
          ]
        }
      ],
      "architecture": [
        {
          "since": "2026-11-22T03:49:09Z",
          "ended": "2026-11-22T04:22:01Z",
          "endedBy": "halted",
          "to": "architecture",
          "event": {
            "name": "plan-stage",
            "at": "2026-11-22T04:20:30Z"
          },
          "detail": "The design was accepted after four corrections made in place: the test-set wording, a release-set wiring that cannot form a task cycle, an unsupported claim about other libraries removed, and the record of the built units reconciled by the release unit rather than the review.",
          "architecture": {
            "stages": [
              {
                "id": "write",
                "state": "done",
                "started": "2026-11-22T03:49:09Z",
                "finished": "2026-11-22T04:16:50Z",
                "report": "The amended design keeps the seven built units as the baseline and adds a version catalog, convention plugins, a Java floor guarded in check, and three published modules (a multiplatform core, a desktop module and a source-less GeneralTools umbrella), with eight units left to land, the review of the built units and the release last."
              },
              {
                "id": "check",
                "state": "done",
                "started": "2026-11-22T04:16:50Z",
                "finished": "2026-11-22T04:20:30Z",
                "report": "The design was accepted after four corrections made in place: the test-set wording, a release-set wiring that cannot form a task cycle, an unsupported claim about other libraries removed, and the record of the built units reconciled by the release unit rather than the review.",
                "result": "accepted"
              },
              {
                "id": "report",
                "state": "current",
                "started": "2026-11-22T04:20:30Z"
              },
              {
                "id": "consequences",
                "state": "todo"
              }
            ]
          },
          "held": true,
          "asked": [
            {
              "text": "Is the amended architecture approved?",
              "options": [
                "As proposed.",
                "With changes.",
                "Reconsidered."
              ],
              "answer": null,
              "asked": "2026-11-22T04:22:01Z",
              "answered": "2026-11-22T22:49:01Z"
            },
            {
              "text": "Are the eight remaining units approved as the sub-issues of #7, six of them existing issues linked and two filed new?",
              "options": [
                "As listed.",
                "With changes."
              ],
              "answer": null,
              "asked": "2026-11-22T04:22:01Z",
              "answered": "2026-11-22T22:49:01Z"
            },
            {
              "text": "How do this issue's planning documents reach master?",
              "options": [
                "On the integration branch, reaching master with the release landing; the sub-issues are filed and linked without a documents landing first.",
                "On a documents-only branch cut from master, reconciled with the integration branch at the release landing."
              ],
              "answer": null,
              "asked": "2026-11-22T04:22:01Z",
              "answered": "2026-11-22T22:49:01Z"
            },
            {
              "text": "When the existing issues are linked, are any of them amended?",
              "options": [
                "A comment on #25 saying the unit also brings the Dokka javadoc jar and the rehearsal repository.",
                "#23 retitled, since its floor lands on the integration branch rather than straight on master."
              ],
              "answer": null,
              "asked": "2026-11-22T04:22:01Z",
              "answered": "2026-11-22T22:49:01Z"
            }
          ]
        },
        {
          "since": "2026-11-22T22:49:01Z",
          "ended": "2026-11-22T23:02:14Z",
          "endedBy": "plan-accepted",
          "to": "document",
          "event": {
            "name": "architecture-signed-off",
            "at": "2026-11-22T22:49:01Z"
          },
          "detail": "The amended architecture is approved as proposed, with its eight remaining units in landing order: the kotlin-logging version, JVM logging, the version catalog, convention plugins, the Java floor, the module split, the review of the built units, and the release.",
          "architecture": {
            "stages": [
              {
                "id": "write",
                "state": "done",
                "started": "2026-11-22T03:49:09Z",
                "finished": "2026-11-22T04:16:50Z",
                "report": "The amended design keeps the seven built units as the baseline and adds a version catalog, convention plugins, a Java floor guarded in check, and three published modules (a multiplatform core, a desktop module and a source-less GeneralTools umbrella), with eight units left to land, the review of the built units and the release last."
              },
              {
                "id": "check",
                "state": "done",
                "started": "2026-11-22T04:16:50Z",
                "finished": "2026-11-22T04:20:30Z",
                "report": "The design was accepted after four corrections made in place: the test-set wording, a release-set wiring that cannot form a task cycle, an unsupported claim about other libraries removed, and the record of the built units reconciled by the release unit rather than the review.",
                "result": "accepted"
              },
              {
                "id": "report",
                "state": "done",
                "started": "2026-11-22T04:20:30Z",
                "finished": "2026-11-22T22:49:01Z",
                "report": "The amended architecture is approved as proposed, with its eight remaining units in landing order: the kotlin-logging version, JVM logging, the version catalog, convention plugins, the Java floor, the module split, the review of the built units, and the release."
              },
              {
                "id": "consequences",
                "state": "done",
                "started": "2026-11-22T22:49:01Z",
                "finished": "2026-11-22T23:02:14Z",
                "report": "The consequences of the amended design are written in impact.md: the published coordinates and artifacts, the blast radius on consumers, the adoption of the new build pieces, and the follow-ups left after 3.0.0."
              }
            ]
          }
        }
      ]
    },
    "next": "kotlin-logging-version",
    "qa": [],
    "qaMax": 3,
    "waitingOn": null,
    "resume": null,
    "blockers": {
      "waitsOn": [],
      "blocks": []
    },
    "landing": null,
    "waits": [
      {
        "step": "interview",
        "since": "2026-11-22T02:47:08Z",
        "ended": "2026-11-22T03:04:14Z",
        "by": "halted"
      },
      {
        "step": "architecture",
        "since": "2026-11-22T04:22:01Z",
        "ended": "2026-11-22T22:49:01Z",
        "by": "halted"
      }
    ],
    "architecture": null,
    "asked": [
      {
        "step": "architecture",
        "text": "Is the amended architecture approved?",
        "options": [
          "As proposed.",
          "With changes.",
          "Reconsidered."
        ],
        "answer": null,
        "asked": "2026-11-22T04:22:01Z",
        "answered": "2026-11-22T22:49:01Z"
      },
      {
        "step": "architecture",
        "text": "Are the eight remaining units approved as the sub-issues of #7, six of them existing issues linked and two filed new?",
        "options": [
          "As listed.",
          "With changes."
        ],
        "answer": null,
        "asked": "2026-11-22T04:22:01Z",
        "answered": "2026-11-22T22:49:01Z"
      },
      {
        "step": "architecture",
        "text": "How do this issue's planning documents reach master?",
        "options": [
          "On the integration branch, reaching master with the release landing; the sub-issues are filed and linked without a documents landing first.",
          "On a documents-only branch cut from master, reconciled with the integration branch at the release landing."
        ],
        "answer": null,
        "asked": "2026-11-22T04:22:01Z",
        "answered": "2026-11-22T22:49:01Z"
      },
      {
        "step": "architecture",
        "text": "When the existing issues are linked, are any of them amended?",
        "options": [
          "A comment on #25 saying the unit also brings the Dokka javadoc jar and the rehearsal repository.",
          "#23 retitled, since its floor lands on the integration branch rather than straight on master."
        ],
        "answer": null,
        "asked": "2026-11-22T04:22:01Z",
        "answered": "2026-11-22T22:49:01Z"
      }
    ],
    "document": {
      "tasks": [
        {
          "id": "reconcile-plan",
          "name": "Reconcile plan",
          "state": "done",
          "started": "2026-11-22T23:02:50Z",
          "finished": "2026-11-22T23:04:31Z",
          "report": "The architecture and impact.md are confirmed final; one sentence of impact.md on the Dokka task was reworded to agree with the architecture, which keeps that task out of check."
        },
        {
          "id": "state-boards",
          "name": "State boards",
          "state": "done",
          "started": "2026-11-22T23:06:07Z",
          "finished": "2026-11-23T00:09:59Z"
        },
        {
          "id": "document-landing",
          "name": "Document commit",
          "state": "done",
          "started": "2026-11-23T00:09:59Z",
          "finished": "2026-11-23T00:13:01Z"
        }
      ]
    }
  },
  "milestones": [
    {
      "column": "gap",
      "label": "Research verified",
      "date": "2026-11-22",
      "ref": "research/README.md#verification-results",
      "note": "Questions 11 to 13, asked for the amendment: the published shape after the module split, the build's convention plugins and version catalog, and the Java floor of the JVM target; their 58 outside claims re-checked at their sources, all holding. Questions 1 to 10 were verified on 2026-10-18."
    },
    {
      "column": "considered",
      "label": "Architecture signed off",
      "date": "2026-11-22",
      "ref": "architecture.md#11-decomposition",
      "note": "The amended architecture, with the eight remaining units in landing order; the seven built units are its baseline."
    },
    {
      "column": "verdict",
      "label": "Architecture final",
      "date": "2026-11-22",
      "ref": "architecture.md",
      "note": "Each of the eight units' verdicts, with the consequences of the amended design in `impact.md`."
    }
  ],
  "trail": [
    {
      "date": "2026-11-22",
      "column": "gap",
      "label": "Sized; the interview starts"
    },
    {
      "date": "2026-11-22",
      "column": "gap",
      "label": "Halted at the interview: waiting on you (The answers to seven questions about the rest of 3.0.0, and a decision on the changes recommended for the architecture.)"
    },
    {
      "date": "2026-11-22",
      "column": "gap",
      "label": "Resumed at the interview"
    },
    {
      "date": "2026-11-22",
      "column": "gap",
      "label": "Research planning: 0 questions drafted"
    },
    {
      "date": "2026-11-22",
      "column": "gap",
      "label": "Research started: `published-shape-after-the-split`, `build-logic-and-version-catalog`, `java-floor-on-a-multiplatform-jvm-target`"
    },
    {
      "date": "2026-11-22",
      "column": "gap",
      "label": "Research returned: `published-shape-after-the-split`",
      "ref": "research/published-shape-after-the-split.md"
    },
    {
      "date": "2026-11-22",
      "column": "gap",
      "label": "Research returned: `java-floor-on-a-multiplatform-jvm-target`",
      "ref": "research/java-floor-on-a-multiplatform-jvm-target.md"
    },
    {
      "date": "2026-11-22",
      "column": "gap",
      "label": "Research returned: `build-logic-and-version-catalog`",
      "ref": "research/build-logic-and-version-catalog.md"
    },
    {
      "date": "2026-11-22",
      "column": "gap",
      "label": "Verification started: 3 findings"
    },
    {
      "date": "2026-11-22",
      "column": "gap",
      "label": "Research verified; the architecture next",
      "ref": "research/README.md"
    },
    {
      "date": "2026-11-22",
      "column": "considered",
      "label": "Architecture stage: check/accept · write: done — The amended design keeps the seven built units as the baseline and adds a version catalog, convention plugins, a Java floor guarded in check, and three published modules (a multiplatform core, a desktop module and a source-less GeneralTools umbrella), with eight units left to land, the review of the built units and the release last."
    },
    {
      "date": "2026-11-22",
      "column": "considered",
      "label": "Architecture stage: report · check/accept: accepted — The design was accepted after four corrections made in place: the test-set wording, a release-set wiring that cannot form a task cycle, an unsupported claim about other libraries removed, and the record of the built units reconciled by the release unit rather than the review."
    },
    {
      "date": "2026-11-22",
      "column": "considered",
      "label": "Halted at the architecture: waiting on you (Sign-off of the amended architecture and its eight remaining units, and the four rule changes the design depends on.)"
    },
    {
      "date": "2026-11-22",
      "column": "considered",
      "label": "Resumed at the architecture"
    },
    {
      "date": "2026-11-22",
      "column": "considered",
      "label": "Architecture signed off",
      "ref": "architecture.md"
    },
    {
      "date": "2026-11-22",
      "column": "verdict",
      "label": "Architecture final, with its units",
      "ref": "architecture.md"
    },
    {
      "date": "2026-11-22",
      "column": "verdict",
      "label": "Planning branch `feat/issue-7-kotlin-multiplatform` confirmed"
    },
    {
      "date": "2026-11-22",
      "column": "built",
      "label": "Document: Reconcile plan current"
    },
    {
      "date": "2026-11-22",
      "column": "built",
      "label": "Document: 1 of 3 tasks done"
    },
    {
      "date": "2026-11-22",
      "column": "built",
      "label": "Document: State boards current"
    },
    {
      "date": "2026-11-23",
      "column": "built",
      "label": "Document: Document commit current"
    },
    {
      "date": "2026-11-23",
      "column": "built",
      "label": "Document: every task done"
    }
  ],
  "rows": [
    {
      "id": "kotlin-logging-version",
      "group": "Remaining units",
      "name": "kotlin-logging 8.0.7",
      "system": "failure-and-logging-seam",
      "where": "build.gradle.kts",
      "unit": {
        "n": 1,
        "slug": "kotlin-logging-version",
        "blockedBy": []
      },
      "gap": {
        "kind": "debt",
        "summary": "The library depends on kotlin-logging 8.0.4, not 8.0.7, the latest release and the organization's one version (#26). The README's Logging section names 8.0.4, and the repository has no `CHANGELOG.md` for the remaining units to record their changes in.",
        "evidence": [
          "`build.gradle.kts:74` — `implementation(\"io.github.oshai:kotlin-logging:8.0.4\")`",
          "`architecture.md` §3.4 — 8.0.7 is the latest release and requires kotlin-stdlib 2.1.21, as 8.0.4 does",
          "`CHANGELOG.md` — absent at the baseline"
        ],
        "source": "architecture.md",
        "link": "architecture.md#31-this-repository-at-the-baseline"
      },
      "options": [
        {
          "id": "first-unit",
          "name": "Move to 8.0.7 as the first unit, a runtime-only dependency as before",
          "verdict": "accepted",
          "why": "Everything after it then logs through, and is compared against, the version that ships; the publication comparison shows only the kotlin-logging version changing.",
          "source": "architecture.md §11.2",
          "link": "architecture.md#112-remaining-units-and-landing-order"
        },
        {
          "id": "own-logging",
          "name": "Per-platform `expect`/`actual` logging or an in-house reporter instead of kotlin-logging",
          "verdict": "declined",
          "why": "More code per platform, and the JVM side would have to re-implement the slf4j bridge so consumers' backends keep working.",
          "wins": "If kotlin-logging's startup line or maintenance became unacceptable.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        }
      ],
      "asBuilt": {
        "state": "pending",
        "summary": "Not started; maps to the existing issue #26."
      },
      "verdict": {
        "kind": "build",
        "statements": [
          {
            "text": "kotlin-logging moves from 8.0.4 to 8.0.7, the one version the library depends on, and stays an `implementation` dependency.",
            "ref": "architecture.md#s3-failure-and-logging-seam",
            "excerpt": {
              "ref": "architecture.md#s3-failure-and-logging-seam",
              "heading": "S3. Failure and logging seam",
              "paragraph": "Responsibility: give every file one logging API, and report every expected failure the same way - one WARN line on the file's own logger and a `Result.failure(IllegalArgumentException)` carrying the same message. · Placement: the helper is in the core module's `commonMain`, package `com.spartanlabs.generaltools`, in a file of `internal` declarations (`Failure.kt`; its facade has no public member and is not dumped). The logger convention applies to every Kotlin source file of every module. · Owns: · `internal fun <T> KLogger.failure(message: String, detail: (() -> Any?)? = null): Result<T>`; · …",
              "hash": "0a2f79721215"
            }
          },
          {
            "text": "The unit creates `CHANGELOG.md` in the Keep a Changelog layout, with an `[Unreleased]` section that each later landing adds to.",
            "ref": "architecture.md#81-this-repository",
            "excerpt": {
              "ref": "architecture.md#81-this-repository",
              "heading": "8.1 This repository",
              "paragraph": "Build files. The one root script becomes a catalog (S11), a `build-logic` build (S12), a root aggregator script, and one script per module that holds only what is the module's own. `gradle.properties` gains the group, the one version and Dokka's plugin mode. `settings.gradle.kts` includes `build-logic` and the three modules, gives the root project its own name, and loses the toolchain resolver. The Gradle wrapper is untouched. · CI (`.github/workflows/Build.yml`). No change. `./gradlew build` already means `check` on every project, so it runs each module's tests, `checkKotlinAbi`, …",
              "hash": "a2260274536c"
            }
          },
          {
            "text": "Its proof is a publication comparison in which only the kotlin-logging version differs and both API dumps stay unchanged.",
            "ref": "architecture.md#112-remaining-units-and-landing-order",
            "excerpt": {
              "ref": "architecture.md#112-remaining-units-and-landing-order",
              "heading": "11.2 Remaining units and landing order",
              "paragraph": "Eight units remain. Each is a sub-issue of #7, branches from the integration branch and lands into it through its own pull request (A13), and is planned in its own folder, `docs/plans/7-kotlin-multiplatform/<number>-<slug>/`. Six map to an existing issue; the review and the release need an issue each before they are planned.",
              "hash": "84ef95ae1d23"
            }
          }
        ],
        "defense": "A dependency bump with no source change, landed first so that every later unit logs through, and is compared against, the version that ships. The `CHANGELOG.md` it creates collects every later landing's entry under `[Unreleased]`.",
        "risks": [
          "kotlin-logging 8 prints one startup line per process unless `-Dkotlin-logging.logStartupMessage=false` is set; the migration notes say so"
        ],
        "source": "architecture.md §11.2, unit 1",
        "link": "architecture.md#112-remaining-units-and-landing-order"
      }
    },
    {
      "id": "jvm-logging",
      "group": "Remaining units",
      "name": "kotlin-logging in the JVM sources",
      "system": "failure-and-logging-seam",
      "where": "src/jvmMain/kotlin/generaltools/",
      "unit": {
        "n": 2,
        "slug": "jvm-logging",
        "blockedBy": [
          {
            "unit": 1
          }
        ]
      },
      "gap": {
        "kind": "debt",
        "summary": "Two JVM files still obtain their logger from slf4j while the common code logs through kotlin-logging, so the library has two logging APIs (#20). Nothing pins their logger names, levels and texts, which the module split will move unchanged.",
        "evidence": [
          "`src/jvmMain/kotlin/generaltools/MiscJvm.kt:18` — `LoggerFactory.getLogger(\"com.spartanlabs.generaltools.Misc\")`",
          "`src/jvmMain/kotlin/generaltools/UserActions.kt:17` — `LoggerFactory.getLogger(\"com.spartanlabs.generaltools.UserActions\")`, used at ten sites"
        ],
        "source": "architecture.md",
        "link": "architecture.md#31-this-repository-at-the-baseline"
      },
      "options": [
        {
          "id": "same-names-lazy",
          "name": "kotlin-logging loggers of the same names, each call in the lazy form with the same level and text, pinned by log-capture tests",
          "verdict": "accepted",
          "why": "One logging API in every file; consumers' logback rules keep matching the 2.x names, and the tests fix what the split must not change.",
          "source": "architecture.md S3",
          "link": "architecture.md#s3-failure-and-logging-seam"
        },
        {
          "id": "slf4j-direct",
          "name": "slf4j called directly in the JVM-only files",
          "verdict": "declined",
          "why": "Two logging APIs in one library, and a function that later moves into common code would need its logging rewritten.",
          "wins": "If the JVM files could never move and kotlin-logging were not already a dependency of both modules.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        }
      ],
      "asBuilt": {
        "state": "pending",
        "summary": "Not started; maps to the existing issue #20."
      },
      "verdict": {
        "kind": "build",
        "statements": [
          {
            "text": "`MiscJvm.kt` and `UserActions.kt` log through kotlin-logging loggers with their 2.x names, levels and message texts, in lazy form.",
            "ref": "architecture.md#s3-failure-and-logging-seam",
            "excerpt": {
              "ref": "architecture.md#s3-failure-and-logging-seam",
              "heading": "S3. Failure and logging seam",
              "paragraph": "Responsibility: give every file one logging API, and report every expected failure the same way - one WARN line on the file's own logger and a `Result.failure(IllegalArgumentException)` carrying the same message. · Placement: the helper is in the core module's `commonMain`, package `com.spartanlabs.generaltools`, in a file of `internal` declarations (`Failure.kt`; its facade has no public member and is not dumped). The logger convention applies to every Kotlin source file of every module. · Owns: · `internal fun <T> KLogger.failure(message: String, detail: (() -> Any?)? = null): Result<T>`; · …",
              "hash": "0a2f79721215"
            }
          },
          {
            "text": "`Logger.time` keeps its slf4j receiver, the public JVM contract.",
            "ref": "architecture.md#s8-misc-helpers",
            "excerpt": {
              "ref": "architecture.md#s8-misc-helpers",
              "heading": "S8. Misc helpers",
              "paragraph": "Responsibility: the small general helpers, split by what they need from the platform. · Placement: the core module: `Misc.kt` (common) and `MiscJvm.kt` (JVM source set), both starting with `@file:JvmMultifileClass` and `@file:JvmName(\"MiscKt\")` (explicit `kotlin.jvm` imports), so every function stays on the facade `MiscKt`: · common: `capitalizeEveryWord`, `evaluateList(list: Iterable<T>, validator: (T) -> Boolean)`, `profile`, `onException` (public `inline`), `forEveryIndexed`, `forEvery`, `trimIfLongerThan`; · JVM: `String.asURL()`, `read(textFile)`, `Logger.time(...)`. · Owns: the …",
              "hash": "27f79dfdb605"
            }
          },
          {
            "text": "New log-capture tests pin the logger name, level and text of `asURL`, `read` and the `UserActions` functions that run headless.",
            "ref": "architecture.md#s10-test-suite",
            "excerpt": {
              "ref": "architecture.md#s10-test-suite",
              "heading": "S10. Test suite",
              "paragraph": "Responsibility: prove each system's behaviour on every target it ships on, organised by test set. · Placement: every class is in `com.spartanlabs.testing.<set>.<package>`, one class per file. · Core module: `commonTest` holds the pure tests, which run on all three targets; `jvmTest` holds the tests of `asURL`, `read` and `Logger.time`, the log-capture tests that pin logger names, levels and messages at the logback backend, and the allocation test. · Desktop module (after `module-split`): its `test` source set holds `UserActionsTest` and the log-capture test of `UserActions`. · Each module's …",
              "hash": "1f7add5e702d"
            }
          }
        ],
        "defense": "A source change in two JVM files, made where they stand so that the module split later moves them unchanged. What a consumer's logging backend sees, the logger names, levels and texts, stays as in 2.x, and the JVM dump does not change.",
        "tests": [
          "`jvmTest`: log-capture tests of `asURL`, `read` and the `UserActions` functions that run headless, read at the logback backend"
        ],
        "source": "architecture.md §11.2, unit 2",
        "link": "architecture.md#112-remaining-units-and-landing-order"
      }
    },
    {
      "id": "version-catalog",
      "group": "Remaining units",
      "name": "Version catalog",
      "system": "version-catalog",
      "where": "gradle/libs.versions.toml",
      "unit": {
        "n": 3,
        "slug": "version-catalog",
        "blockedBy": [
          {
            "unit": 1
          }
        ]
      },
      "gap": {
        "kind": "debt",
        "summary": "Every plugin and dependency version is a literal in the build script, and there is no version catalog (#24). `gradle/` holds only the wrapper.",
        "evidence": [
          "`build.gradle.kts:16-17` — the Kotlin and publishing plugins, each with a literal version",
          "`build.gradle.kts:67-91` — the dependencies by source set, each with a literal version",
          "`gradle/` — the wrapper only"
        ],
        "source": "architecture.md",
        "link": "architecture.md#31-this-repository-at-the-baseline"
      },
      "options": [
        {
          "id": "catalog-no-value-change",
          "name": "`gradle/libs.versions.toml` holding every dependency and plugin version, read by the build, with no value changed",
          "verdict": "accepted",
          "why": "Each version exists once, and the publication files stay byte-identical, so the unit proves itself by comparison; the convention plugins that follow read it.",
          "source": "architecture.md S11",
          "link": "architecture.md#s11-version-catalog"
        }
      ],
      "asBuilt": {
        "state": "pending",
        "summary": "Not started; maps to the existing issue #24."
      },
      "verdict": {
        "kind": "build",
        "statements": [
          {
            "text": "`gradle/libs.versions.toml` holds the version of every dependency and plugin, and the build reads every version from it.",
            "ref": "architecture.md#s11-version-catalog",
            "excerpt": {
              "ref": "architecture.md#s11-version-catalog",
              "heading": "S11. Version catalog",
              "paragraph": "Responsibility: hold the version of every dependency and every plugin once. · Placement: `gradle/libs.versions.toml` (`version-catalog`). · Owns: the versions: Kotlin 2.4.21; the Kotlin floor 2.2.0, which is both the language and API pin and the published `kotlin-stdlib` requirement; Dokka 2.3.0; vanniktech 0.37.0; kotlin-logging 8.0.7; slf4j 2.0.16; logback 1.5.13; commons-io 2.4. It owns the library entries and the plugin aliases built from them. · Read by: module and root scripts through the generated `libs` accessors; the convention plugins through the catalog's string API behind one …",
              "hash": "1d80d83917fc"
            }
          },
          {
            "text": "No version value changes, so every generated POM and module file stays byte-identical.",
            "ref": "architecture.md#112-remaining-units-and-landing-order",
            "excerpt": {
              "ref": "architecture.md#112-remaining-units-and-landing-order",
              "heading": "11.2 Remaining units and landing order",
              "paragraph": "Eight units remain. Each is a sub-issue of #7, branches from the integration branch and lands into it through its own pull request (A13), and is planned in its own folder, `docs/plans/7-kotlin-multiplatform/<number>-<slug>/`. Six map to an existing issue; the review and the release need an issue each before they are planned.",
              "hash": "84ef95ae1d23"
            }
          }
        ],
        "defense": "Build-only, with no value changed: its proof is that every generated POM and module file stays byte-identical and both API dumps are unchanged. It comes before the convention plugins, which read it.",
        "source": "architecture.md §11.2, unit 3",
        "link": "architecture.md#112-remaining-units-and-landing-order"
      }
    },
    {
      "id": "convention-plugins",
      "group": "Remaining units",
      "name": "Convention plugins and Dokka javadoc jars",
      "system": "build-logic",
      "where": "build-logic/ (included build)",
      "unit": {
        "n": 4,
        "slug": "convention-plugins",
        "blockedBy": [
          {
            "unit": 3
          }
        ]
      },
      "gap": {
        "kind": "missing",
        "summary": "All build configuration lives in one script, with nothing a second module could share (#25). The published javadoc jars are empty, and there is no file repository to rehearse a publication against.",
        "evidence": [
          "`build.gradle.kts:16-153` — targets, pins, tests and publishing in one script",
          "`build.gradle.kts:127` — `KotlinMultiplatform(javadocJar = JavadocJar.Empty(), ...)`"
        ],
        "source": "architecture.md",
        "link": "architecture.md#31-this-repository-at-the-baseline"
      },
      "options": [
        {
          "id": "included-build-logic",
          "name": "An included `build-logic` build with publishing, multiplatform-library and test-set conventions; then Dokka output in the javadoc jars, with `failOnWarning`, and the rehearsal repository",
          "verdict": "accepted",
          "why": "The organization's convention names a `build-logic` build; each step leaves the publication files identical or changes exactly one thing in them.",
          "source": "architecture.md S12",
          "link": "architecture.md#s12-build-logic"
        },
        {
          "id": "buildsrc-or-one-plugin",
          "name": "`buildSrc`, or one plugin class for every module",
          "verdict": "declined",
          "why": "A change to `buildSrc` invalidates the whole build, and one plugin for every module would force the Kotlin plugin onto the umbrella.",
          "wins": "If the build had one kind of module.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "typed-accessors",
          "name": "Typed catalog accessors inside the convention plugins, through the generated class or a third-party plugin",
          "verdict": "declined",
          "why": "The first relies on an internal generated class and on one catalog name; the second puts a young plugin in the build path to save a handful of string lookups.",
          "wins": "If Gradle made the accessors available to precompiled script plugins.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "dokka-in-check",
          "name": "A Dokka job in `check` for 3.0.0",
          "verdict": "declined",
          "why": "It costs a Dokka run on every build, and it is the CI job #19 defines with the site and the workflow around it.",
          "wins": "If #19 landed before the release.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        }
      ],
      "asBuilt": {
        "state": "pending",
        "summary": "Not started; maps to the existing issue #25."
      },
      "verdict": {
        "kind": "build",
        "statements": [
          {
            "text": "An included `build-logic` build holds the publishing, multiplatform-library and test-set conventions, and the one module applies them.",
            "ref": "architecture.md#s12-build-logic",
            "excerpt": {
              "ref": "architecture.md#s12-build-logic",
              "heading": "S12. Build logic",
              "paragraph": "Responsibility: hold every piece of build configuration that more than one module needs, once, as convention plugins the modules apply. · Placement: an included build, `build-logic/`, brought in through `pluginManagement { includeBuild(\"build-logic\") }`. Precompiled script plugins are the entry points; logic that deserves a name or a test is a plain Kotlin class beside them (`convention-plugins`; the JVM-library convention arrives with its first module in `module-split`). · Owns: · a publishing convention: the publishing plugin, signing of every publication, the shared POM metadata, Dokka …",
              "hash": "09415a245863"
            }
          },
          {
            "text": "The group and the version move to `gradle.properties`, written once for every module.",
            "ref": "architecture.md#s12-build-logic",
            "excerpt": {
              "ref": "architecture.md#s12-build-logic",
              "heading": "S12. Build logic",
              "paragraph": "Responsibility: hold every piece of build configuration that more than one module needs, once, as convention plugins the modules apply. · Placement: an included build, `build-logic/`, brought in through `pluginManagement { includeBuild(\"build-logic\") }`. Precompiled script plugins are the entry points; logic that deserves a name or a test is a plain Kotlin class beside them (`convention-plugins`; the JVM-library convention arrives with its first module in `module-split`). · Owns: · a publishing convention: the publishing plugin, signing of every publication, the shared POM metadata, Dokka …",
              "hash": "09415a245863"
            }
          },
          {
            "text": "Each published component's javadoc jar becomes its Dokka output, generated in Dokka's V2 mode with `failOnWarning`.",
            "ref": "architecture.md#s15-api-reference-jars",
            "excerpt": {
              "ref": "architecture.md#s15-api-reference-jars",
              "heading": "S15. API reference jars",
              "paragraph": "Responsibility: ship each published component's API reference as its javadoc jar. · Placement: the publishing convention applies Dokka 2 in V2 mode and sets `failOnWarning`; each library convention, and the umbrella's script, names the Dokka publication task as the source of its javadoc jar; the root script aggregates the library modules and sets `failOnWarning` too (`convention-plugins`; the aggregation of two modules and the umbrella's jar arrive with `module-split`). · Owns: the javadoc jars of all six components. Each of the core module's four carries the same Dokka output, with the pages …",
              "hash": "a31a59bb0382"
            }
          },
          {
            "text": "The publishing convention defines the file repository that the release rehearsal publishes to.",
            "ref": "architecture.md#s16-publication-proof-and-release-set",
            "excerpt": {
              "ref": "architecture.md#s16-publication-proof-and-release-set",
              "heading": "S16. Publication proof and release set",
              "paragraph": "Responsibility: prove the published shape before anything is uploaded, and make an upload hold the whole set or nothing. · Placement: the publishing convention and the root script. The comparison is a procedure, not a task, and every remaining unit that touches the build runs it, from `kotlin-logging-version` on. `convention-plugins` adds the rehearsal repository; `module-split` adds the release set and the component-set assertion; `release` runs the whole (section 11.4). · Owns: · the publication comparison: the generated POM and Gradle module file of every publication, built at one checkout …",
              "hash": "47a4d7c418e6"
            }
          }
        ],
        "defense": "Build restructuring in two steps, each checked by the publication comparison: after the conventions every POM and module file is identical, jar and klib hashes included; after Dokka only the javadoc jars differ, from empty to Dokka pages.",
        "tests": [
          "`jsNodeTest` and `wasmJsNodeTest` still run 354 tests each, and `-PtestLevel` still selects on every test task"
        ],
        "risks": [
          "Dokka's `failOnWarning` runs on the existing KDoc for real here; from then on a broken KDoc link fails every task that builds a javadoc jar, and CI does not run Dokka until #19, so the unit's own Dokka run or the rehearsal catches it",
          "The publication comparison depends on the checkout path, since a klib records absolute source paths: both sides are built at one path, in sequence"
        ],
        "source": "architecture.md §11.2, unit 4",
        "link": "architecture.md#112-remaining-units-and-landing-order"
      }
    },
    {
      "id": "java-floor",
      "group": "Remaining units",
      "name": "Java floor 17, guarded in check",
      "system": "java-floor",
      "where": "build-logic/, settings.gradle.kts",
      "unit": {
        "n": 5,
        "slug": "java-floor",
        "blockedBy": [
          {
            "unit": 4
          }
        ]
      },
      "gap": {
        "kind": "risk",
        "summary": "The Java floor and the build's toolchain are one setting, with no `-Xjdk-release` and no guard in `check` (#23). The next toolchain bump would raise the shipped bytecode unnoticed, as 2.2.0 shipped at class-file major 67, usable only from JDK 23.",
        "evidence": [
          "`build.gradle.kts:31` — `val jvmFloor = 17`, feeding `jvmToolchain` (:35), `jvmTarget` (:51) and the by-hand attribute (:54)",
          "`settings.gradle.kts:2` — the foojay toolchain resolver 1.0.0",
          "`architecture.md` §3.2 — the released 2.2.0 is class-file major 67 and declares `org.gradle.jvm.version` 23"
        ],
        "source": "architecture.md",
        "link": "architecture.md#31-this-repository-at-the-baseline"
      },
      "options": [
        {
          "id": "release-flag-and-guard",
          "name": "One constant driving `jvmTarget` and `-Xjdk-release` and the published attribute, no toolchain, and `verifyJavaFloor` in `check`",
          "verdict": "accepted",
          "why": "The floor becomes a property of the output, stated once and checked on the classes and the dependencies, whatever JDK runs the build.",
          "source": "architecture.md S13",
          "link": "architecture.md#s13-java-floor"
        },
        {
          "id": "toolchain-floor",
          "name": "`jvmToolchain(17)` as the Java floor",
          "verdict": "declined",
          "why": "One setting for two things: the next toolchain bump would raise the shipped bytecode without anyone deciding it, a clean machine must download a JDK, and the dependencies' floor stays unchecked.",
          "wins": "If the build JDK were fixed at the floor for good.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "jvmtarget-alone",
          "name": "`jvmTarget` alone, without `-Xjdk-release`",
          "verdict": "declined",
          "why": "It fixes the class-file version but leaves every newer JDK API callable from a newer build JDK, a failure that shows only at run time on Java 17.",
          "wins": "If the build always ran on JDK 17.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        }
      ],
      "asBuilt": {
        "state": "pending",
        "summary": "Not started; maps to the existing issue #23."
      },
      "verdict": {
        "kind": "build",
        "statements": [
          {
            "text": "One floor constant, 17, drives `jvmTarget` and `-Xjdk-release` inside the `jvm` target and the published `TargetJvmVersion` attribute.",
            "ref": "architecture.md#s13-java-floor",
            "excerpt": {
              "ref": "architecture.md#s13-java-floor",
              "heading": "S13. Java floor",
              "paragraph": "Responsibility: make 17 the oldest Java release every JVM artifact runs on, state it once, and fail `check` when the built output or its dependencies break it. · Placement: `build-logic` (`java-floor`): one constant, one registration entry point that every module publishing a JVM variant calls, and the guard task. · Owns: · compile settings, both from the constant: `jvmTarget` and `-Xjdk-release` at the same value. In the multiplatform module they sit inside `jvm { compilerOptions }`, so the flag does not reach the web compilations; in the Kotlin/JVM module on the `kotlin {}` extension. The …",
              "hash": "677ff2ca441b"
            }
          },
          {
            "text": "The build requests no toolchain, so the toolchain resolver leaves the settings.",
            "ref": "architecture.md#s13-java-floor",
            "excerpt": {
              "ref": "architecture.md#s13-java-floor",
              "heading": "S13. Java floor",
              "paragraph": "Responsibility: make 17 the oldest Java release every JVM artifact runs on, state it once, and fail `check` when the built output or its dependencies break it. · Placement: `build-logic` (`java-floor`): one constant, one registration entry point that every module publishing a JVM variant calls, and the guard task. · Owns: · compile settings, both from the constant: `jvmTarget` and `-Xjdk-release` at the same value. In the multiplatform module they sit inside `jvm { compilerOptions }`, so the flag does not reach the web compilations; in the Kotlin/JVM module on the `kotlin {}` extension. The …",
              "hash": "677ff2ca441b"
            }
          },
          {
            "text": "`verifyJavaFloor` runs in `check`, reading the class-file versions of the jar and its runtime classpath and the attribute of every published JVM variant.",
            "ref": "architecture.md#s13-java-floor",
            "excerpt": {
              "ref": "architecture.md#s13-java-floor",
              "heading": "S13. Java floor",
              "paragraph": "Responsibility: make 17 the oldest Java release every JVM artifact runs on, state it once, and fail `check` when the built output or its dependencies break it. · Placement: `build-logic` (`java-floor`): one constant, one registration entry point that every module publishing a JVM variant calls, and the guard task. · Owns: · compile settings, both from the constant: `jvmTarget` and `-Xjdk-release` at the same value. In the multiplatform module they sit inside `jvm { compilerOptions }`, so the flag does not reach the web compilations; in the Kotlin/JVM module on the `kotlin {}` extension. The …",
              "hash": "677ff2ca441b"
            }
          }
        ],
        "defense": "Build-only: the floor becomes a property of the output, stated once and checked on the classes and their runtime classpath, whatever JDK runs the build. It comes before the split, so the two new modules are created at the floor.",
        "tests": [
          "`check`: `verifyJavaFloor`, which fails when the target or the attribute is taken away; a probe calling a JDK 21 API does not compile on a newer JDK"
        ],
        "risks": [
          "`-Xjdk-release` is an unstable compiler flag that needs the compile JDK's `ct.sym`; if a later Kotlin drops or renames it, the guard still holds the class-file version and the published attribute"
        ],
        "source": "architecture.md §11.2, unit 5",
        "link": "architecture.md#112-remaining-units-and-landing-order"
      }
    },
    {
      "id": "module-split",
      "group": "Remaining units",
      "name": "Module split: core, desktop and the umbrella",
      "system": "module-layout-and-published-shape",
      "where": "GeneralTools-core/, GeneralTools-desktop/, GeneralTools/",
      "unit": {
        "n": 6,
        "slug": "module-split",
        "blockedBy": [
          {
            "unit": 2
          },
          {
            "unit": 5
          }
        ]
      },
      "gap": {
        "kind": "missing",
        "summary": "The library is one module, so every JVM consumer gets the AWT and commons-io desktop code with the shared types, and module and artifact names cannot change after 3.0.0 without a break (#21). A plain Maven consumer of the root artifact gets no classes and must depend on `GeneralTools-jvm` (#17).",
        "evidence": [
          "`build.gradle.kts:126` — one coordinate, `GeneralTools`",
          "`src/jvmMain/kotlin/generaltools/UserActions.kt:3-14` — commons-io and AWT imports, the only file that uses them",
          "`README.md` — Maven consumers depend on `GeneralTools-jvm`"
        ],
        "source": "architecture.md",
        "link": "architecture.md#31-this-repository-at-the-baseline"
      },
      "options": [
        {
          "id": "three-modules",
          "name": "`GeneralTools-core` (multiplatform), `GeneralTools-desktop` (JVM, depending on the core) and a source-less `GeneralTools` umbrella over both, at one version, with a constraint against a stale 2.2.0 and the root POM rewritten for plain Maven",
          "verdict": "accepted",
          "why": "2.x consumers keep their coordinate, plain-Maven consumers get real classes, and no build ends up with two copies of the same classes; the split is cheapest before the first multiplatform release.",
          "source": "architecture.md S14",
          "link": "architecture.md#s14-module-layout-and-published-shape"
        },
        {
          "id": "one-module",
          "name": "One module for everything, with the desktop code in its JVM source set",
          "verdict": "declined",
          "why": "Every JVM consumer gets the desktop code, and a later Android family could not leave it out of the one `jvm` variant.",
          "wins": "If no second device family were ever planned and the desktop code had no dependency of its own.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "miscjvm-in-desktop",
          "name": "`MiscJvm.kt` in the desktop module",
          "verdict": "declined",
          "why": "Its functions compile into the facade class `MiscKt`, which cannot span two jars; another module would move them to another class, a binary break outside the break set.",
          "wins": "If that binary break were acceptable and the three functions drove a desktop.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "desktop-package",
          "name": "A package of its own for the desktop code",
          "verdict": "declined",
          "why": "It renames `UserActionsKt`'s package, a source and binary break outside the break set, to help a way of consuming the library no consumer uses.",
          "wins": "If a consumer needed core and desktop together on the module path.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "desktop-without-core",
          "name": "The desktop module without a dependency on the core module",
          "verdict": "declined",
          "why": "An application declaring only its family's artifact would get no `Color` or geometry, and adding the dependency later would change what its consumers resolve.",
          "wins": "If the desktop code were a standalone tool whose consumers must not receive the core.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "version-per-module",
          "name": "A version per module",
          "verdict": "declined",
          "why": "The modules are one library cut by device family: the umbrella would need a compatibility table, and one tag, release and CHANGELOG section would stop describing what was published.",
          "wins": "If the modules had different release cadences and consumers of only one of them.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "lowercase-names",
          "name": "Lowercase artifact names (`generaltools-core`)",
          "verdict": "declined",
          "why": "Beside the mixed-case `GeneralTools` umbrella the library would have two spellings, permanently, since coordinates are case-sensitive and cannot be withdrawn.",
          "wins": "If the umbrella's coordinate were changing too.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "no-umbrella",
          "name": "No `GeneralTools` 3.0.0 at all: consumers move to the new artifacts",
          "verdict": "declined",
          "why": "Every 2.x consumer would change its coordinate, and a build still holding `GeneralTools:2.2.0` beside the new artifacts would have two copies of every class with nothing to align them.",
          "wins": "If no 2.x consumer existed.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "multiplatform-umbrella",
          "name": "A multiplatform umbrella",
          "verdict": "declined",
          "why": "Three more Central components and two empty klibs that tie the umbrella to Kotlin 2.4 on the web, for no prior art; every 2.x consumer is a JVM build.",
          "wins": "If a project with web targets had to declare the one line `GeneralTools` in its common code.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "metadata-only-umbrella",
          "name": "A metadata-only umbrella (a POM with no jar)",
          "verdict": "declined",
          "why": "Maven's dependency type defaults to `jar`, so a consumer keeping its 2.x declaration would fail; an empty jar costs nothing.",
          "wins": "If Maven consumers declared `<type>pom</type>`.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "kotlin-umbrella",
          "name": "The umbrella as a Kotlin/JVM module",
          "verdict": "declined",
          "why": "The Kotlin plugin on a module with no source writes an empty ABI dump and puts explicit-API rules on it; `java-library` publishes the same files.",
          "wins": "If the umbrella held Kotlin source.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "capabilities",
          "name": "Capabilities for duplicate protection",
          "verdict": "declined",
          "why": "Core and desktop would both declare the capability and collide with each other and with the umbrella in every ordinary build; capabilities have no Maven form.",
          "wins": "If one module alone replaced the old artifact.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "desktop-multiplatform",
          "name": "The desktop module as a multiplatform module with only a `jvm()` target",
          "verdict": "declined",
          "why": "It adds an empty root artifact and a `-jvm` artifact and brings the empty-root problem back for Maven consumers, for no gain while the desktop family has one target.",
          "wins": "If the desktop family gained a second target.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        }
      ],
      "asBuilt": {
        "state": "pending",
        "summary": "Not started; maps to the existing issue #21, with #17 folded in."
      },
      "verdict": {
        "kind": "build",
        "statements": [
          {
            "text": "The library becomes three modules at one version: the multiplatform `GeneralTools-core`, the Kotlin/JVM `GeneralTools-desktop` and the source-less `java-library` umbrella `GeneralTools`.",
            "ref": "architecture.md#s14-module-layout-and-published-shape",
            "excerpt": {
              "ref": "architecture.md#s14-module-layout-and-published-shape",
              "heading": "S14. Module layout and published shape",
              "paragraph": "Responsibility: decide which code ships in which artifact, how the artifacts depend on each other, and what a consumer's build resolves. · Placement: three Gradle modules, each in a folder named after it, each project named after its artifact; the settings file; each module's own script (`module-split`).",
              "hash": "cebcde00f4d4"
            }
          },
          {
            "text": "`MiscJvm.kt` stays in the core module's JVM source set, on the facade class it shares with the common helpers.",
            "ref": "architecture.md#s8-misc-helpers",
            "excerpt": {
              "ref": "architecture.md#s8-misc-helpers",
              "heading": "S8. Misc helpers",
              "paragraph": "Responsibility: the small general helpers, split by what they need from the platform. · Placement: the core module: `Misc.kt` (common) and `MiscJvm.kt` (JVM source set), both starting with `@file:JvmMultifileClass` and `@file:JvmName(\"MiscKt\")` (explicit `kotlin.jvm` imports), so every function stays on the facade `MiscKt`: · common: `capitalizeEveryWord`, `evaluateList(list: Iterable<T>, validator: (T) -> Boolean)`, `profile`, `onException` (public `inline`), `forEveryIndexed`, `forEvery`, `trimIfLongerThan`; · JVM: `String.asURL()`, `read(textFile)`, `Logger.time(...)`. · Owns: the …",
              "hash": "27f79dfdb605"
            }
          },
          {
            "text": "`UserActions.kt` and its commons-io dependency move to the desktop module, in the same package.",
            "ref": "architecture.md#s9-desktop-edge",
            "excerpt": {
              "ref": "architecture.md#s9-desktop-edge",
              "heading": "S9. Desktop edge",
              "paragraph": "Responsibility: everything that drives a desktop: the screen, the mouse and keyboard, the browser and image files, through AWT and commons-io. · Placement: built in the single module's JVM source set (`UserActions.kt`, facade `UserActionsKt`). `module-split` moves it to the `GeneralTools-desktop` module (S14), in the unchanged package `com.spartanlabs.generaltools`, so its facade keeps its name and every caller's import and every linked call site stay valid. · Owns: `implementation(commons-io)`, the only place that dependency is visible, and the explicit `java.awt.Rectangle` import, which …",
              "hash": "b0b544c5e6ac"
            }
          },
          {
            "text": "The desktop module depends on the core module in API scope.",
            "ref": "architecture.md#s14-module-layout-and-published-shape",
            "excerpt": {
              "ref": "architecture.md#s14-module-layout-and-published-shape",
              "heading": "S14. Module layout and published shape",
              "paragraph": "Responsibility: decide which code ships in which artifact, how the artifacts depend on each other, and what a consumer's build resolves. · Placement: three Gradle modules, each in a folder named after it, each project named after its artifact; the settings file; each module's own script (`module-split`).",
              "hash": "cebcde00f4d4"
            }
          },
          {
            "text": "The core module's root POM depends on `GeneralTools-core-jvm`, so a plain Maven consumer of `GeneralTools` gets real classes.",
            "ref": "architecture.md#s14-module-layout-and-published-shape",
            "excerpt": {
              "ref": "architecture.md#s14-module-layout-and-published-shape",
              "heading": "S14. Module layout and published shape",
              "paragraph": "Responsibility: decide which code ships in which artifact, how the artifacts depend on each other, and what a consumer's build resolves. · Placement: three Gradle modules, each in a folder named after it, each project named after its artifact; the settings file; each module's own script (`module-split`).",
              "hash": "cebcde00f4d4"
            }
          },
          {
            "text": "The core JVM variant and the desktop module publish a constraint that raises any `GeneralTools` in a Gradle build to their own version.",
            "ref": "architecture.md#s14-module-layout-and-published-shape",
            "excerpt": {
              "ref": "architecture.md#s14-module-layout-and-published-shape",
              "heading": "S14. Module layout and published shape",
              "paragraph": "Responsibility: decide which code ships in which artifact, how the artifacts depend on each other, and what a consumer's build resolves. · Placement: three Gradle modules, each in a folder named after it, each project named after its artifact; the settings file; each module's own script (`module-split`).",
              "hash": "cebcde00f4d4"
            }
          },
          {
            "text": "The core JVM jar and the desktop jar carry the `Automatic-Module-Name` entries `com.spartanlabs.generaltools.core` and `com.spartanlabs.generaltools.desktop`.",
            "ref": "architecture.md#s14-module-layout-and-published-shape",
            "excerpt": {
              "ref": "architecture.md#s14-module-layout-and-published-shape",
              "heading": "S14. Module layout and published shape",
              "paragraph": "Responsibility: decide which code ships in which artifact, how the artifacts depend on each other, and what a consumer's build resolves. · Placement: three Gradle modules, each in a folder named after it, each project named after its artifact; the settings file; each module's own script (`module-split`).",
              "hash": "cebcde00f4d4"
            }
          },
          {
            "text": "The root project takes its own name, `GeneralTools-build`, so the umbrella is the only project named `GeneralTools`.",
            "ref": "architecture.md#s14-module-layout-and-published-shape",
            "excerpt": {
              "ref": "architecture.md#s14-module-layout-and-published-shape",
              "heading": "S14. Module layout and published shape",
              "paragraph": "Responsibility: decide which code ships in which artifact, how the artifacts depend on each other, and what a consumer's build resolves. · Placement: three Gradle modules, each in a folder named after it, each project named after its artifact; the settings file; each module's own script (`module-split`).",
              "hash": "cebcde00f4d4"
            }
          },
          {
            "text": "Each module commits its own API dumps, and their union is compared with the 2.2.0 baseline.",
            "ref": "architecture.md#113-break-ledger-and-proof",
            "excerpt": {
              "ref": "architecture.md#113-break-ledger-and-proof",
              "heading": "11.3 Break ledger and proof",
              "paragraph": "The ledger. Rows are the differences from the committed 2.2.0 baseline in the plugin's listing, the built unit that landed them, and the proof for what the dump cannot show. The build confirmed them: at `29dd4ff` the JVM dump differs from the baseline by exactly these rows.",
              "hash": "c0522f426e0a"
            }
          },
          {
            "text": "A Central-bound task of any one module uploads all three modules, and a check fails an invocation that would leave one out.",
            "ref": "architecture.md#s16-publication-proof-and-release-set",
            "excerpt": {
              "ref": "architecture.md#s16-publication-proof-and-release-set",
              "heading": "S16. Publication proof and release set",
              "paragraph": "Responsibility: prove the published shape before anything is uploaded, and make an upload hold the whole set or nothing. · Placement: the publishing convention and the root script. The comparison is a procedure, not a task, and every remaining unit that touches the build runs it, from `kotlin-logging-version` on. `convention-plugins` adds the rehearsal repository; `module-split` adds the release set and the component-set assertion; `release` runs the whole (section 11.4). · Owns: · the publication comparison: the generated POM and Gradle module file of every publication, built at one checkout …",
              "hash": "47a4d7c418e6"
            }
          },
          {
            "text": "The `.gitattributes` rule moves to the module dump folders, and its comment says that `checkKotlinAbi` compares lines.",
            "ref": "architecture.md#81-this-repository",
            "excerpt": {
              "ref": "architecture.md#81-this-repository",
              "heading": "8.1 This repository",
              "paragraph": "Build files. The one root script becomes a catalog (S11), a `build-logic` build (S12), a root aggregator script, and one script per module that holds only what is the module's own. `gradle.properties` gains the group, the one version and Dokka's plugin mode. `settings.gradle.kts` includes `build-logic` and the three modules, gives the root project its own name, and loses the toolchain resolver. The Gradle wrapper is untouched. · CI (`.github/workflows/Build.yml`). No change. `./gradlew build` already means `check` on every project, so it runs each module's tests, `checkKotlinAbi`, …",
              "hash": "a2260274536c"
            }
          }
        ],
        "defense": "The one unit that changes coordinates. It starts from a build whose versions, conventions and floor are already in place, so its comparison shows the split and nothing else: the same classes and the same dump lines, in two jars, and a rehearsal publication that passes the component-set assertion.",
        "tests": [
          "The desktop tests move to the desktop module, with their own `logback-test.xml` and capture helper; `verifyJavaFloor` passes on all three modules"
        ],
        "risks": [
          "The split package: the core JVM jar and the desktop jar cannot be resolved together on the Java module path; each works alone under its `Automatic-Module-Name`, and no such consumer is known",
          "The desktop module's Kotlin floor: a 2.4 stdlib requirement in its published files would stop Kotlin 2.2.0 and 2.1.20 consumers, so the generated files are read for it",
          "Mixed graphs with `GeneralTools:2.2.0`: a published `gametools` fails at run time beside 3.0.0, and a Maven build that declares only a new artifact holds two copies of every class until it excludes the old one",
          "The publishing plugin's behaviour for several modules in one deployment and for a source-less module is read from its source"
        ],
        "source": "architecture.md §11.2, unit 6",
        "link": "architecture.md#112-remaining-units-and-landing-order"
      }
    },
    {
      "id": "built-units-review",
      "group": "Remaining units",
      "name": "Review of the seven built units",
      "system": "test-suite",
      "where": "the built units' code, tests and KDoc at their final paths",
      "unit": {
        "n": 7,
        "slug": "built-units-review",
        "blockedBy": [
          {
            "unit": 6
          }
        ]
      },
      "gap": {
        "kind": "risk",
        "summary": "The design requires the seven built units to be reviewed before the release, and that review is still to do: their tests against the named test sets, their code against their plans, and their KDoc and comments against the documentation standard. No change record names their ten breaks and additions.",
        "evidence": [
          "`architecture.md` §2, requirement 16 — the review of the built units, after the module split and before the release",
          "`CHANGELOG.md` — absent at the baseline"
        ],
        "source": "architecture.md",
        "link": "architecture.md#2-requirements"
      },
      "options": [
        {
          "id": "review-unit",
          "name": "A review unit of its own, after the module split and before the release, fixing what it finds and adding the built units' CHANGELOG entries",
          "verdict": "accepted",
          "why": "The review reads the code at the paths it ships from, and its fixes land before anything is rehearsed; a fix that would need a break outside the break set becomes a follow-up.",
          "source": "architecture.md §11.2",
          "link": "architecture.md#112-remaining-units-and-landing-order"
        }
      ],
      "asBuilt": {
        "state": "pending",
        "summary": "Not started; needs an issue of its own before it is planned."
      },
      "verdict": {
        "kind": "build",
        "statements": [
          {
            "text": "The seven built units are reviewed at their final paths: their tests against the named test sets, their code against their plans, and their documentation against the documentation standard.",
            "ref": "architecture.md#112-remaining-units-and-landing-order",
            "excerpt": {
              "ref": "architecture.md#112-remaining-units-and-landing-order",
              "heading": "11.2 Remaining units and landing order",
              "paragraph": "Eight units remain. Each is a sub-issue of #7, branches from the integration branch and lands into it through its own pull request (A13), and is planned in its own folder, `docs/plans/7-kotlin-multiplatform/<number>-<slug>/`. Six map to an existing issue; the review and the release need an issue each before they are planned.",
              "hash": "84ef95ae1d23"
            }
          },
          {
            "text": "What the review finds is fixed before the release.",
            "ref": "architecture.md#112-remaining-units-and-landing-order",
            "excerpt": {
              "ref": "architecture.md#112-remaining-units-and-landing-order",
              "heading": "11.2 Remaining units and landing order",
              "paragraph": "Eight units remain. Each is a sub-issue of #7, branches from the integration branch and lands into it through its own pull request (A13), and is planned in its own folder, `docs/plans/7-kotlin-multiplatform/<number>-<slug>/`. Six map to an existing issue; the review and the release need an issue each before they are planned.",
              "hash": "84ef95ae1d23"
            }
          },
          {
            "text": "A fix that would need a break outside the 3.0.0 break set is not made in 3.0.0 and becomes a follow-up issue.",
            "ref": "architecture.md#112-remaining-units-and-landing-order",
            "excerpt": {
              "ref": "architecture.md#112-remaining-units-and-landing-order",
              "heading": "11.2 Remaining units and landing order",
              "paragraph": "Eight units remain. Each is a sub-issue of #7, branches from the integration branch and lands into it through its own pull request (A13), and is planned in its own folder, `docs/plans/7-kotlin-multiplatform/<number>-<slug>/`. Six map to an existing issue; the review and the release need an issue each before they are planned.",
              "hash": "84ef95ae1d23"
            }
          },
          {
            "text": "The unit adds the CHANGELOG entries for the ten breaks and the additions.",
            "ref": "architecture.md#81-this-repository",
            "excerpt": {
              "ref": "architecture.md#81-this-repository",
              "heading": "8.1 This repository",
              "paragraph": "Build files. The one root script becomes a catalog (S11), a `build-logic` build (S12), a root aggregator script, and one script per module that holds only what is the module's own. `gradle.properties` gains the group, the one version and Dokka's plugin mode. `settings.gradle.kts` includes `build-logic` and the three modules, gives the root project its own name, and loses the toolchain resolver. The Gradle wrapper is untouched. · CI (`.github/workflows/Build.yml`). No change. `./gradlew build` already means `check` on every project, so it runs each module's tests, `checkKotlinAbi`, …",
              "hash": "a2260274536c"
            }
          }
        ],
        "defense": "Review and fixes of code already built, read at the paths it ships from, with no new feature; its fixes land before anything is rehearsed.",
        "tests": [
          "The whole suite audited against the named test sets"
        ],
        "risks": [
          "A fix that changes a dump line becomes a ledger row with a migration note; one that would need a break outside the break set waits for a later release as a follow-up issue"
        ],
        "source": "architecture.md §11.2, unit 7",
        "link": "architecture.md#112-remaining-units-and-landing-order"
      }
    },
    {
      "id": "release",
      "group": "Release",
      "name": "Release 3.0.0",
      "system": "publication-proof-and-release-set",
      "where": "master, Maven Central",
      "unit": {
        "n": 8,
        "slug": "release",
        "blockedBy": [
          {
            "unit": 1
          },
          {
            "unit": 2
          },
          {
            "unit": 3
          },
          {
            "unit": 4
          },
          {
            "unit": 5
          },
          {
            "unit": 6
          },
          {
            "unit": 7
          }
        ]
      },
      "gap": {
        "kind": "missing",
        "summary": "3.0.0 is not released: `master` stays at the released 2.2.0 until the release landing. The README describes a hand-run, staged release that this repository no longer uses.",
        "evidence": [
          "`architecture.md`, Baseline — `master` stays at the released `2.2.0` until the release landing",
          "`README.md:103-119` — the manual, staged release"
        ],
        "source": "architecture.md",
        "link": "architecture.md#114-release-in-detail"
      },
      "options": [
        {
          "id": "standing-release",
          "name": "The standing automatic release at the release landing, after a rehearsal publication of the exact tree, the component-set assertion and the smoke matrix",
          "verdict": "accepted",
          "why": "Everything that can fail runs before the landing, against the tree that lands; one invocation uploads and publishes the six components as one deployment.",
          "source": "architecture.md §11.4",
          "link": "architecture.md#114-release-in-detail"
        },
        {
          "id": "staged-upload",
          "name": "A staged upload, tested through the Portal before pressing Publish",
          "verdict": "declined",
          "why": "It needs a hand-run release, and this repository's release is the standing automatic one; a failed validation publishes nothing, and the rehearsal of the same tree covers a valid but wrong artifact.",
          "wins": "If the repository released by hand.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        },
        {
          "id": "pre-release",
          "name": "A `0.x` or pre-release version for the smoke test",
          "verdict": "declined",
          "why": "A published pre-release is permanent and its bytes differ from the release.",
          "wins": "If Central allowed deleting a version.",
          "source": "architecture.md §10",
          "link": "architecture.md#10-alternatives-considered"
        }
      ],
      "asBuilt": {
        "state": "pending",
        "summary": "Not started; needs an issue of its own before it is planned."
      },
      "verdict": {
        "kind": "build",
        "list": "numbered",
        "statements": [
          {
            "text": "The MyGameTools migration builds against this repository in a scratch clone before anything is published.",
            "ref": "architecture.md#114-release-in-detail",
            "excerpt": {
              "ref": "architecture.md#114-release-in-detail",
              "heading": "11.4 `release` in detail",
              "paragraph": "The unit has no feature code. It proves the tree that will land, finishes the documents that describe it, and then lands it. The smoke project and the MyGameTools clone live outside the repository and are discarded afterwards.",
              "hash": "05f2169dfd2f"
            }
          },
          {
            "text": "A file-repository publication of the exact tree that lands is checked to hold exactly the six components, with their published shape.",
            "ref": "architecture.md#114-release-in-detail",
            "excerpt": {
              "ref": "architecture.md#114-release-in-detail",
              "heading": "11.4 `release` in detail",
              "paragraph": "The unit has no feature code. It proves the tree that will land, finishes the documents that describe it, and then lands it. The smoke project and the MyGameTools clone live outside the repository and are discarded afterwards.",
              "hash": "05f2169dfd2f"
            }
          },
          {
            "text": "The consumer smoke matrix resolves and compiles against that publication, mixed builds with `gametools` 5.1.0 included.",
            "ref": "architecture.md#114-release-in-detail",
            "excerpt": {
              "ref": "architecture.md#114-release-in-detail",
              "heading": "11.4 `release` in detail",
              "paragraph": "The unit has no feature code. It proves the tree that will land, finishes the documents that describe it, and then lands it. The smoke project and the MyGameTools clone live outside the repository and are discarded afterwards.",
              "hash": "05f2169dfd2f"
            }
          },
          {
            "text": "The README's Releasing section, the migration notes and the CHANGELOG's `[3.0.0]` section are finished in the last landing into the integration branch.",
            "ref": "architecture.md#114-release-in-detail",
            "excerpt": {
              "ref": "architecture.md#114-release-in-detail",
              "heading": "11.4 `release` in detail",
              "paragraph": "The unit has no feature code. It proves the tree that will land, finishes the documents that describe it, and then lands it. The smoke project and the MyGameTools clone live outside the repository and are discarded afterwards.",
              "hash": "05f2169dfd2f"
            }
          },
          {
            "text": "`final-implementation.md` is reconciled into the as-built record of the seven built units, and `impact.md` is brought up to date with the build.",
            "ref": "architecture.md#114-release-in-detail",
            "excerpt": {
              "ref": "architecture.md#114-release-in-detail",
              "heading": "11.4 `release` in detail",
              "paragraph": "The unit has no feature code. It proves the tree that will land, finishes the documents that describe it, and then lands it. The smoke project and the MyGameTools clone live outside the repository and are discarded afterwards.",
              "hash": "05f2169dfd2f"
            }
          },
          {
            "text": "The integration branch merges into `master` as the release landing, which the standing automatic release publishes as one release event tagged `3.0.0`.",
            "ref": "architecture.md#114-release-in-detail",
            "excerpt": {
              "ref": "architecture.md#114-release-in-detail",
              "heading": "11.4 `release` in detail",
              "paragraph": "The unit has no feature code. It proves the tree that will land, finishes the documents that describe it, and then lands it. The smoke project and the MyGameTools clone live outside the repository and are discarded afterwards.",
              "hash": "05f2169dfd2f"
            }
          },
          {
            "text": "Issue #8 is closed once 3.0.0 is published, with a comment that states its scope.",
            "ref": "architecture.md#114-release-in-detail",
            "excerpt": {
              "ref": "architecture.md#114-release-in-detail",
              "heading": "11.4 `release` in detail",
              "paragraph": "The unit has no feature code. It proves the tree that will land, finishes the documents that describe it, and then lands it. The smoke project and the MyGameTools clone live outside the repository and are discarded afterwards.",
              "hash": "05f2169dfd2f"
            }
          }
        ],
        "defense": "Operational, with no feature code. The release is irreversible, so everything that can fail runs before the release landing, against the tree that lands, and what cannot run before it, the Portal, fails closed.",
        "tests": [
          "The smoke matrix, resolved from the rehearsal repository: Kotlin 2.2.0 and 2.1.20 JVM projects on JDK 17, a Kotlin 2.4 multiplatform project, a plain Maven project, mixed builds with `gametools` 5.1.0, and the smoke sources for what no dump shows"
        ],
        "risks": [
          "Release-set completeness: an upload holding the umbrella without the modules it depends on could never be withdrawn; the release set, the dry run and the component-set assertion guard it",
          "The Portal's validation of the five artifact names new in the namespace is not rehearsed; a failed validation publishes nothing, so the cost is a second attempt",
          "Release timing: Central releases are budgeted per calendar month in the shared namespace; the gate checks for room before the landing",
          "A long-lived integration branch: a change on `master` that touches the build or the sources reopens the rehearsal"
        ],
        "source": "architecture.md §11.4",
        "link": "architecture.md#114-release-in-detail"
      }
    }
  ],
  "impact": {
    "basis": "plan",
    "summary": "3.0.0 is a major release of GeneralTools as three artifacts at one version: the multiplatform `GeneralTools-core`, the JVM `GeneralTools-desktop` and the `GeneralTools` umbrella over both, which keeps 2.x consumers' coordinate. It breaks source and binary compatibility by the ten listed breaks: MyGameTools must migrate before it builds on 3.0.0, and no published `gametools` runs beside it. It lowers the JDK a consumer needs to 17 and opens the library to `js` and `wasmJs`.",
    "release": {
      "bump": "none",
      "version": null,
      "note": "The tracking plan moves no version itself: `3.0.0`, a major, is already set on the integration branch, and the release unit publishes it once, at the release landing."
    },
    "none": null,
    "downstream": [
      {
        "who": "MyGameTools",
        "kind": "consumer",
        "effect": "breaking",
        "summary": "Keeps its coordinate: `GeneralTools:3.0.0` resolves to the umbrella and through it to the core JVM jar and the desktop jar. It does not compile until migrated: 39 lines in 14 files name `Square`, and `VisibleObject` assigns `area.dimensions`. Its in-place `Point` and `Dimensions` mutations and its `Color(...)` calls keep compiling.",
        "system": null,
        "link": "impact.md#1-who-is-affected"
      },
      {
        "who": "MyGameClient",
        "kind": "consumer",
        "effect": "none",
        "compat": "notable",
        "summary": "Unaffected until it moves to a MyGameTools release built on 3.0.0; then 27 lines in 8 files follow the rename.",
        "system": null,
        "link": "impact.md#1-who-is-affected"
      },
      {
        "who": "Builds that resolve a published `gametools` beside a 3.0.0 artifact",
        "kind": "consumer",
        "effect": "breaking",
        "summary": "Every published `gametools` references `Square`: with one copy of the classes it fails with `NoClassDefFoundError` at the first `new Square`. A Maven build that declares only `GeneralTools-core` or `GeneralTools-desktop` beside `GeneralTools` 2.2.0 holds two copies of every class until it excludes the old one.",
        "system": null,
        "link": "impact.md#33-consumers-binary"
      },
      {
        "who": "Plain Maven consumers of `GeneralTools`",
        "kind": "consumer",
        "effect": "additive",
        "summary": "They keep their declaration and get real classes: the umbrella depends on the core and desktop modules, and the core's root POM points at `GeneralTools-core-jvm` (#17).",
        "system": null,
        "link": "impact.md#1-who-is-affected"
      },
      {
        "who": "Consumers on JDK 17 to 22",
        "kind": "consumer",
        "effect": "additive",
        "summary": "They can resolve and run 3.0.0: every JVM artifact is bytecode 17 and declares `org.gradle.jvm.version` 17, where 2.2.0 needed JDK 23.",
        "system": null,
        "link": "impact.md#34-platform-floors"
      },
      {
        "who": "JS and Wasm consumers",
        "kind": "consumer",
        "effect": "additive",
        "summary": "The library is usable from `js` and `wasmJs` through `GeneralTools-core`, on Kotlin 2.4 or newer; Kotlin/Wasm is Beta, so the web artifacts carry a weaker practical promise than the JVM ones. The umbrella is JVM-only.",
        "system": null,
        "link": "impact.md#34-platform-floors"
      },
      {
        "who": "Consumers on the Java module path",
        "kind": "consumer",
        "effect": "behaviour",
        "summary": "A new limit: the core JVM jar and the desktop jar share a package, so they cannot be resolved together there; each works alone under its `Automatic-Module-Name`. No such consumer is known.",
        "system": null,
        "link": "impact.md#1-who-is-affected"
      },
      {
        "who": "`GeneralTools-core`, its three target artifacts and `GeneralTools-desktop`",
        "kind": "artifact",
        "effect": "additive",
        "summary": "Five components new in the namespace, published with the umbrella as one deployment of six at `3.0.0`.",
        "system": null,
        "link": "architecture.md#113-break-ledger-and-proof"
      },
      {
        "who": "MyGameServer, WebTools and SpartanGraphics",
        "kind": "repository",
        "effect": "none",
        "summary": "Nothing changes for them. SpartanGraphics would declare `GeneralTools-core` in its common code if it adopts the library.",
        "system": null,
        "link": "impact.md#1-who-is-affected"
      }
    ],
    "adaptation": [
      {
        "who": "MyGameTools",
        "need": "required",
        "compat": "breaking",
        "summary": "About 17 files and 45 lines: the rename in 14 files, `area.dimensions = value` becoming `area.dimensions.setTo(value)`, the dependency line with its unchanged coordinate. `Square` is in its public API, so the migration is a major release for its own consumers.",
        "system": null,
        "link": "impact.md#32-consumers-source"
      },
      {
        "who": "MyGameClient",
        "need": "required",
        "compat": "breaking",
        "summary": "After MyGameTools releases on 3.0.0, its 27 lines in 8 files follow the rename, and its `Portrait` builds the subject's `Color` with `Color.of`, so a bad network value is clamped instead of throwing.",
        "system": null,
        "link": "impact.md#32-consumers-source"
      },
      {
        "who": "Maven builds that declare a new artifact beside `GeneralTools` 2.2.0",
        "need": "required",
        "compat": "breaking",
        "summary": "They exclude `GeneralTools` 2.2.0, as the migration notes show, or they hold two copies of every class.",
        "system": null,
        "link": "impact.md#33-consumers-binary"
      },
      {
        "who": "Common code and web consumers",
        "need": "optional",
        "summary": "They declare `GeneralTools-core`; the `GeneralTools` umbrella serves JVM builds only.",
        "system": null,
        "link": "impact.md#1-who-is-affected"
      },
      {
        "who": "Files that import the arithmetic operators",
        "need": "optional",
        "summary": "Importing `com.spartanlabs.geometry.arithmetic` opts a file in, at the accepted costs: compound assignments on its `var` holders stop compiling there. A file that does not import it behaves as in 2.x.",
        "system": null,
        "link": "architecture.md#12-risks-at-the-systems-level"
      }
    ],
    "api": [
      {
        "surface": "The published artifacts",
        "change": "changed",
        "tier": "stable-core",
        "compat": "breaking",
        "summary": "The library is multiplatform and split into `GeneralTools-core` and `GeneralTools-desktop`; `GeneralTools` 3.0.0 is an umbrella over both, so its consumers keep their coordinate (break 1).",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`TwoDoubles` subclassing",
        "change": "changed",
        "tier": "experimental",
        "compat": "breaking",
        "summary": "`TwoDoubles` is no longer `sealed`; subclassing it is Experimental behind an ERROR-level opt-in, and an exhaustive `when` over it needs an `else` (break 2).",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`TwoDoubles.divideBy`",
        "change": "changed",
        "tier": "stable-core",
        "compat": "breaking",
        "summary": "Divides in place and returns `Result<Unit>` instead of throwing; a zero divisor leaves the value unchanged and is reported at WARN (break 3).",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`evaluateList`",
        "change": "changed",
        "tier": "stable-core",
        "compat": "breaking",
        "summary": "Takes `(T) -> Boolean`, is not `inline`, and lives in common code (break 4).",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`com.spartanlabs.logging`",
        "change": "removed",
        "tier": null,
        "summary": "The package is deleted; no repository uses it (break 5).",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`Square`",
        "change": "removed",
        "tier": "stable-core",
        "summary": "Renamed `Rectangle`, with `val` references; `toSquare()` becomes `toRectangle()` (break 6).",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`TwoDoubles` equality",
        "change": "changed",
        "tier": "stable-core",
        "compat": "breaking",
        "summary": "Equality becomes lawful: NaN equals NaN, `-0.0` hashes like `0.0`, and `equals` and `hashCode` are final (break 7).",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`Color` constructor",
        "change": "changed",
        "tier": "stable-core",
        "compat": "breaking",
        "summary": "The public constructor validates its channels; `Color.of` clamps (break 8).",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`trimIfLongerThan`",
        "change": "changed",
        "tier": "stable-core",
        "compat": "breaking",
        "summary": "Throws `IndexOutOfBoundsException` for a negative length on every platform (break 9).",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`capitalizeEveryWord`",
        "change": "changed",
        "tier": "stable-core",
        "compat": "breaking",
        "summary": "Stops dropping commas that are part of its input (break 10).",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`ImmutableRectangle` and `Rectangle.toImmutable()`",
        "change": "added",
        "tier": "stable-core",
        "summary": "An immutable box copy, so a library can hand out a box its callers cannot mutate (#8).",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`AxisAlignedBox` accessors and operators",
        "change": "added",
        "tier": "stable-core",
        "summary": "Six allocation-free primitive accessors with default bodies, `contains` as an operator, destructuring and `Point.unaryMinus`.",
        "system": null,
        "link": "architecture.md#2-requirements"
      },
      {
        "surface": "`com.spartanlabs.geometry.arithmetic`",
        "change": "added",
        "tier": "supported-extension",
        "summary": "`plus`, `minus`, `times` and `div` for `Point` and `Dimensions`, available to a file that imports them, with the `SupportedExtension` annotation that marks the tier.",
        "system": null,
        "link": "architecture.md#9-extension-and-stability"
      }
    ],
    "conversion": [
      {
        "what": "Keep the dependency on `GeneralTools` and move it to `3.0.0`; common code and web targets declare `GeneralTools-core` instead.",
        "link": "impact.md#1-who-is-affected"
      },
      {
        "what": "Rename `Square` to `Rectangle` and `toSquare()` to `toRectangle()`.",
        "link": "impact.md#32-consumers-source"
      },
      {
        "what": "Replace an assignment of a box's `dimensions` with `setTo(...)`, which copies the values rather than adopting the instance.",
        "link": "impact.md#32-consumers-source"
      },
      {
        "what": "Build a `Color` from untrusted values with `Color.of`, which clamps, since the constructor now throws.",
        "link": "impact.md#32-consumers-source"
      },
      {
        "what": "In a Maven build that declares a new artifact beside a published `gametools`, exclude `GeneralTools` 2.2.0.",
        "link": "impact.md#33-consumers-binary"
      }
    ],
    "source": "impact.md",
    "link": "impact.md"
  },
  "tests": null,
  "roadmap": null,
  "followUps": [
    {
      "issue": 11,
      "title": "Make EPSILON a parameter of the geometry functions that use it",
      "note": "Additive overloads defaulting to `EPSILON`, in a minor release after 3.0.0."
    },
    {
      "issue": 12,
      "title": "Add a compile-fail test for TwoDoubles' subclassing opt-in",
      "note": "A Gradle TestKit compile-fail test, in a minor release after 3.0.0; the same harness can hold the arithmetic package's `+=` ambiguity, which only the release's smoke project checks."
    },
    {
      "issue": 13,
      "title": "Add toImmutable() for every AxisAlignedBox",
      "note": "So a consumer can hand out a `CenteredBox` without converting it first; 3.0.0 gives `toImmutable()` to `Rectangle` only."
    },
    {
      "issue": 14,
      "title": "Add a screenshotArea overload that takes an AxisAlignedBox",
      "note": "In `UserActions`, in `GeneralTools-desktop`, whose API dependency on the core already brings that type."
    },
    {
      "issue": 15,
      "title": "Add immutable counterparts for Point and Segment",
      "note": "The part of #8 that 3.0.0 leaves to a later release: `Point` and `Dimensions` stay mutable."
    },
    {
      "issue": 16,
      "title": "Add a Color factory that returns a Result",
      "note": "Beside the validating constructor that 3.0.0 introduces."
    },
    {
      "issue": 18,
      "title": "Cache Node and Yarn downloads in CI",
      "note": "CI downloads both on every run since the web targets were added."
    },
    {
      "issue": 19,
      "title": "Generate API docs with Dokka: javadoc jar and GitHub Pages site",
      "note": "After 3.0.0 on `master`: the Dokka job in CI, the project site, the Pages workflow and the branding. 3.0.0 ships the Dokka javadoc jars only."
    },
    {
      "issue": 22,
      "title": "Group imports as the Commenting page sets out",
      "note": "After 3.0.0 on `master`, as one change with no other edit; the files the remaining units add follow the grouping from the start."
    },
    {
      "name": "Device-family modules",
      "note": "`-android` and `-web` modules join beside `GeneralTools-desktop` when those families exist, each a new artifact depending on the core in API scope; neither joins the JVM-only umbrella."
    },
    {
      "name": "The TwoDoubles subclassing seam graduates",
      "note": "From Experimental to Supported Extension once a consumer builds on it; that release's notes record the promotion."
    }
  ],
  "documents": [
    {
      "label": "Issue #7",
      "path": "https://github.com/SpartanLaboratories/GeneralTools/issues/7",
      "note": "the request: Color and geometry usable from common code, published as a multiplatform library"
    },
    {
      "label": "Research",
      "path": "research/README.md",
      "note": "the topic-by-topic summary and its verification; thirteen findings files beside it, among them the build and Kotlin compatibility, Maven Central publishing, kotlin-logging, cross-platform parity, the geometry models and the downstream impact, and, for the amendment, the published shape after the module split, the build's convention plugins and version catalog, and the Java floor of the JVM target"
    },
    {
      "label": "Architecture",
      "path": "architecture.md",
      "note": "the amended design: requirements (§2), the baseline (§3), the sixteen systems (§6), alternatives (§10), the seven built units and the eight remaining units in landing order (§11), the release in detail (§11.4), risks (§12)"
    },
    {
      "label": "plan-kmp-build.md",
      "path": "plan-kmp-build.md",
      "note": "built unit 1: the multiplatform build, the baseline API dump and the publication"
    },
    {
      "label": "plan-explicit-api.md",
      "path": "plan-explicit-api.md",
      "note": "built unit 2: strict explicit API mode, no API change"
    },
    {
      "label": "plan-logging.md",
      "path": "plan-logging.md",
      "note": "built unit 3: kotlin-logging, the internal failure helper, and the old logging package removed"
    },
    {
      "label": "plan-commonize.md",
      "path": "plan-commonize.md",
      "note": "built unit 4: everything that can be common moved to common code, the tests sorted into their sets"
    },
    {
      "label": "plan-two-doubles.md",
      "path": "plan-two-doubles.md",
      "note": "built unit 5: the value core's breaks and the import-gated arithmetic"
    },
    {
      "label": "plan-box-model.md",
      "path": "plan-box-model.md",
      "note": "built unit 6: `Square` renamed `Rectangle`, the box accessors and `ImmutableRectangle` (#8)"
    },
    {
      "label": "plan-generaltools-breaks.md",
      "path": "plan-generaltools-breaks.md",
      "note": "built unit 7: the `Misc` and `Color` breaks"
    },
    {
      "label": "final-implementation.md",
      "path": "final-implementation.md",
      "note": "the deviations and observations recorded unit by unit as the seven built units were built; reconciled into the as-built record in the release unit"
    },
    {
      "label": "impact.md",
      "path": "impact.md",
      "note": "the consequences as planned: who is affected, adoption in this repository, the blast radius, the platform floors and the follow-ups"
    },
    {
      "label": "Superseded architecture",
      "path": "superseded/architecture-2026-11-22.md",
      "note": "the design the amended architecture replaces"
    },
    {
      "label": "Superseded release plan",
      "path": "superseded/plan-release-2026-11-22.md",
      "note": "the earlier plan of the release unit, a staged hand-run release, replaced by the amended architecture's release unit"
    }
  ],
  "notes": {
    "corrections": [
      {
        "what": "The halt of 2026-11-22 02:47:08 UTC is reworded in its trail entry: The halt put seven questions and one decision on the recommended changes, eight entries in all; its text said five. The passage now reads \"The answers to seven questions about the rest of 3.0.0\".",
        "when": "2026-11-22",
        "reword": {
          "event": "halted",
          "at": "2026-11-22T02:47:08Z",
          "from": "sha256:f7a3787195f2",
          "to": "sha256:702627191854"
        }
      }
    ]
  },
  "links": {
    "workBoard": "../index.html",
    "repoBoard": "../../dashboards/index.html",
    "boards": {
      "failure-and-logging-seam": "../../dashboards/failure-and-logging-seam/index.html",
      "test-suite": "../../dashboards/test-suite/index.html"
    }
  }
}
);
