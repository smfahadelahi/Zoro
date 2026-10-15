plugins { id("com.android.application") }

android {
    namespace = "app.zoro.prototype"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.zoro.prototype"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.0"
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets.getByName("main").assets.srcDir(layout.buildDirectory.dir("generated/zoroAssets"))
}

// The root HTML is the single source of truth, not a second maintained copy.
val bundlePrototype by tasks.registering(Copy::class) {
    from(rootProject.file("index.html"))
    into(layout.buildDirectory.dir("generated/zoroAssets"))
}
tasks.named("preBuild").configure { dependsOn(bundlePrototype) }
