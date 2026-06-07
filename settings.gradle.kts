pluginManagement {
    repositories {
        google()
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

rootProject.name = "Tang"
include(":app")
include(":core:domain")
include(":core:data")
include(":core:ui")
include(":engine:divination")
include(":feature:divination")
include(":feature:remember")
include(":feature:people")
include(":feature:events")
include(":feature:chat")
include(":feature:gifts")
include(":feature:reflect")
include(":feature:subscription")
include(":feature:profile")
include(":feature:circle")
include(":feature:home")
