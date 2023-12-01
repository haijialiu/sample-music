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
//        maven("https://www.jitpack.io")
//        maven("https://maven.aliyun.com/repository/jcenter")
//        maven("https://maven.aliyun.com/repository/google")
//        maven("https://maven.aliyun.com/repository/central")
//        maven("https://maven.aliyun.com/repository/gradle-plugin")
        google()
        mavenCentral()
    }
}

rootProject.name = "sample-music"
include(":app")
