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
        // TapSDK Maven仓库
        maven { url = uri("https://nexus.tapsvc.com/repository/releases/") }
        // JitPack作为备用仓库
        maven { url = uri("https://jitpack.io") }
        // 阿里云Maven镜像作为备用
        maven { url = uri("https://maven.aliyun.com/repository/public") }
    }
}

rootProject.name = "Yjcy"
include(":app")
