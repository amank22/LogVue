import com.voxfinite.logvue.Dependencies
plugins {
    kotlin("kapt")
}

dependencies {
    compileOnly(Dependencies.LogVueApi)
    compileOnly(kotlin("stdlib"))

    compileOnly(Dependencies.Pf4j)
    kapt(Dependencies.Pf4j)
//    implementation("org.apache.commons:commons-lang3:3.5") // this is an example for an external library included
}
