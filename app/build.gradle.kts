import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// release 서명 정보는 저장소에 올리지 않는 secrets.properties에서 읽는다 (.gitignore 처리).
// 파일이 없으면 서명 없이 빌드된다. 심사용 서명 APK는 apk/ 폴더에 있다.
val secrets = Properties().apply {
    val file = rootProject.file("secrets.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "io.github.lapcatt.myfirstmile"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "io.github.lapcatt.myfirstmile"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (secrets.containsKey("RELEASE_STORE_FILE")) {
            create("release") {
                storeFile = rootProject.file(secrets.getProperty("RELEASE_STORE_FILE"))
                storePassword = secrets.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = secrets.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = secrets.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            // 코드 축소(R8) 끔: 켜면 kotlin.** 클래스 접근 오류(IllegalAccessError)로 실행 즉시 종료됨 (2026-11-15 확인).
            // 동작이 검증된 debug와 같은 코드로 내보내기 위해 끈다. keep 규칙을 맞춘 뒤 다시 켜는 것은 나중에.
            optimization {
                enable = false
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("ai.pleos.playground:Vehicle:2.3.2")
    implementation("ai.pleos.playground:TextToSpeech:3.3.1")
}