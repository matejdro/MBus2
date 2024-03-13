A work-in-progress application for the bus schedules in the city of Maribor

## Updating versions

Process for updating all library versions::

1. ~~Run `./gradlew dependencyUpdates :buildSrc:dependencyUpdates --no-configuration-cache > /dev/null
   && ./gradlew :app:updateLibsToml`.
   This will check for new versions of all libraries and automatically bump versions inside `libs.toml`.~~
   Until [AGP issue](https://issuetracker.google.com/issues/315689874) is resolved, auto update
   is broken. You must open libs.toml, and manually update all versions
   that are marked in yellow (Lint warning)
2. Check the output of the above task and manually update any libraries that task said it couldn't update automatically.
2. Sync the project, build the app and run all tests to make sure everything still works.
3. Run `detektDebug` gradle task to find any new deprecated code
4. For any new deprecated code found by the search:
    * If the fix is trivial, fix it immediately as part of the update commit
    * Otherwise, make a tech debt ticket and add a @Suppress with the ticket number
      (and/or discuss with the team how to address it)

## Creating a new module

1. Copy module template from `config/templates` folder to root of the project and rename it to your module
2. If new module is an android library and if it needs android resources, add `buildFeatures { androidResources = true }`
   and namespace declaration to module's `build.gradle.kts`
3. Add module to `settings.gradle.kts`
4. Add module to app's `build.gradle.kts` as `implementation(projects.newModule)`)

## Hierarchy of the feature modules

Every feature should contain following modules:

* `feature-name`
    * `data` - data module with all non-UI logic (such as repositories).
      No other module (except for top level `app`) should depend on this.
    * `api` - interfaces and data models exposed to other modules.
      This module should generally contain no logic to speed up builds.
    * `ui` - Module containing feature's ui (Screens / Fragments / ViewModels)
    * `test` (optional) - Module containing test helpers for all other modules in this group

## Running integration tests

`./gradlew :app:connectedAndroidTest -PtestAppWithProguard`

# Creating screenshot tests

To create screenshot tests for your compose screen:

1. Make preview functions public
2. Add `showkase` plugin to the module of the screen you want to test
2. Add `@ShowkaseComposable(group = "Test")` annotation to the preview of the screen you want to test
3. run `recordPaparazziDebug` gradle task to record golden screenshot
   (double check that only previews for your screen were generated)
