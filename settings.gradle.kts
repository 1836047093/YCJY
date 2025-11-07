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
        // Maven Central和Google优先
        mavenCentral()
        google()
        
        // Sonatype仓库（LeanCloud官方推荐，解决Maven CDN缓存问题）⭐
        maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
        
        // 阿里云镜像作为备用
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        
        // TapSDK Maven仓库
        maven { url = uri("https://nexus.tapsvc.com/repository/releases/") }
        // JitPack作为备用仓库
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Yjcy"
include(":app")
