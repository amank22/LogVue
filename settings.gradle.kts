pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
rootProject.name = "logvue"

include("api")
include("app")
include("plugins")
include("plugins:pdt")
