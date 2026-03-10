pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SkillTracker"
include(":app")
include(":core:ui", ":core:domain", ":core:di", ":core:navigation", ":core:network", ":core:common")
include(":feature:auth", ":feature:main")
include(":data")
project(":core:ui").projectDir = file("core-ui")
project(":core:domain").projectDir = file("core-domain")
project(":core:navigation").projectDir = file("core-navigation")
project(":core:network").projectDir = file("core-network")
project(":core:common").projectDir = file("core-common")

project(":feature:auth").projectDir = file("feature-auth")
project(":feature:main").projectDir = file("feature-main")

project(":data").projectDir = file("data")
