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
        // 阿里云镜像（国内加速）- 优先使用
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        // Maven Central
        mavenCentral()
        google()
        // TapSDK Maven仓库
        maven { url = uri("https://nexus.tapsvc.com/repository/releases/") }
        // JitPack作为备用仓库
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Yjcy"
include(":app")
