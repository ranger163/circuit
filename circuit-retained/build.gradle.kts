// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0
plugins {
  id("com.android.library")
  kotlin("multiplatform")
  id("com.vanniktech.maven.publish")
  `java-test-fixtures`
}

kotlin {
  // region KMP Targets
  android { publishLibraryVariants("release") }
  jvm()
  // endregion

  sourceSets {
    commonMain {
      dependencies {
        api(libs.compose.runtime)
        api(libs.coroutines)
      }
    }
    val androidMain =
      maybeCreate("androidMain").apply {
        dependencies {
          api(libs.androidx.lifecycle.viewModel.compose)
          api(libs.androidx.lifecycle.viewModel)
          api(libs.androidx.compose.runtime)
          implementation(libs.androidx.compose.ui.ui)
        }
      }
    maybeCreate("commonTest").apply { dependencies { implementation(libs.kotlin.test) } }
    val commonJvmTest =
      maybeCreate("commonJvmTest").apply {
        dependencies {
          implementation(libs.junit)
          implementation(libs.truth)
        }
      }
    maybeCreate("jvmTest").apply { dependsOn(commonJvmTest) }
    // TODO export this in Android too when it's supported in kotlin projects
    maybeCreate("jvmMain").apply { dependencies.add("testFixturesApi", projects.circuitTest) }
    maybeCreate("androidInstrumentedTest").apply {
      dependsOn(androidMain)
      dependsOn(commonJvmTest)
      dependencies {
        implementation(libs.coroutines)
        implementation(libs.coroutines.android)
        implementation(projects.circuitRetained)
        implementation(libs.androidx.compose.integration.activity)
        implementation(libs.androidx.compose.ui.testing.junit)
        implementation(libs.androidx.compose.foundation)
        implementation(libs.androidx.compose.ui.ui)
        implementation(libs.androidx.compose.material.material)
        implementation(libs.leakcanary.android.instrumentation)
        implementation(libs.junit)
        implementation(libs.truth)
      }
    }
  }
}

android {
  namespace = "com.slack.circuit.retained"

  defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }

  testOptions { unitTests.isIncludeAndroidResources = true }
  testBuildType = "release"
}
