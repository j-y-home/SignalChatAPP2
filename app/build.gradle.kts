import org.gradle.kotlin.dsl.annotationProcessor
import org.gradle.kotlin.dsl.compileOnly
import kotlin.math.sign

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")

}

android {
    namespace = "com.example.endtoendencryptionsystem"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.endtoendencryptionsystem"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters+=listOf("x86", "armeabi-v7a", "arm64-v8a")
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    sourceSets {
        named("main") {
            assets.srcDirs("src/main/assets")
        }
    }
    aaptOptions {
        //additionalParameters = "--auto-add-overlay"
        ignoreAssetsPattern ="!.svn:!.git:.*:!CVS:!thumbs.db:!picasa.ini:!*.scc:*~"
    }


}

dependencies {


    implementation(libs.fragment.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.refresh.layout.kernel)
    implementation(libs.refresh.header.classics)
    implementation(libs.circleimageview)
    implementation(libs.android.auto.size)
    implementation(libs.adapter)
    
    implementation(libs.autodispose)
    implementation(libs.autodispose.lifecycle)
    implementation(libs.autodispose.android)
    implementation(libs.autodispose.androidx.lifecycle)
    implementation(libs.glide)
    implementation(libs.glide.compiler)
    implementation(libs.pictureselector)
    implementation(libs.live.event.bus)
    //Material_Spinner
    implementation(libs.smartmaterialspinner)
    implementation(libs.powerspinner)
    implementation(libs.material.spinner)
    implementation(libs.rxbinding)
    implementation(libs.rxbinding.core)
    implementation(libs.rxbinding.appcompat)
    implementation(libs.rxbinding.material)
    implementation(libs.status.bar)
    implementation(libs.x.popup)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
//    androidTestImplementation(libs.espresso.core)
    ksp(libs.room.compiler)
    implementation(libs.brv)
    testImplementation("junit:junit:")
    //App升级
    implementation ("com.github.xuexiangjys:XUpdate:2.1.4")
    //网络请求的实现一
    implementation ("com.zhy:okhttputils:2.6.2")
    implementation("com.github.loper7:DateTimePicker:0.6.3")
    implementation("io.github.razerdp:BasePopup:3.2.1")
    implementation("com.kongzue.dialogx:DialogX:0.0.49")
    implementation("com.kongzue.dialogx.style:DialogXMIUIStyle:0.0.49")
    implementation ("org.java-websocket:Java-WebSocket:1.5.7")

    // 以下为uniMP-Android必须导入的SDK相关依赖arr
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar")))) //导入libs目录下的所有arr
    implementation(fileTree(mapOf("include" to arrayOf("*.jar"), "dir" to "libs")))
    implementation ("androidx.appcompat:appcompat:1.1.0")
   // implementation 'androidx.localbroadcastmanager:localbroadcastmanager:1.0.0'
    implementation("androidx.core:core:1.1.0")
    implementation("androidx.compose.runtime:runtime:1.5.0-alpha01")
    implementation("androidx.viewpager2:viewpager2:1.0.0")


//
//    implementation("androidx.legacy:legacy-support-v4:1.0.0")
//
    implementation("com.alibaba:fastjson:1.2.83")
//
    implementation("com.facebook.fresco:fresco:2.5.0")
    implementation("com.facebook.fresco:animated-gif:2.5.0")
//
//    implementation("com.github.bumptech.glide:glide:4.9.0")
//
    implementation("androidx.webkit:webkit:1.5.0")
  //  ksp ("com.github.bumptech.glide:compiler:4.9.0")
    implementation(libs.mmkv)
    implementation(libs.okhttp)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.rxjava3)

    implementation(libs.rxkotlin)

    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.annotations)

    implementation(libs.retrofit)
    implementation(libs.retrofit.adapter.rxjava3)

    implementation(libs.okhttp.logging.interceptor)

    implementation("org.whispersystems:signal-protocol-android:2.8.1")
    //implementation("org.whispersystems:signal-client-android:0.9.5")
    testFixturesImplementation(libs.libsignal.client)
    compileOnly("org.projectlombok:lombok:1.18.16")
    annotationProcessor("org.projectlombok:lombok:1.18.16")

    annotationProcessor("org.projectlombok:lombok:1.18.28")  // 确保使用最新版本
    implementation("org.projectlombok:lombok:1.18.28")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.+")
    implementation("com.belerweb:pinyin4j:2.5.0")
    implementation("com.github.Brioal:CircleHeadView:1.0")

    implementation("com.github.bigdongdong:ChatView:1.1")

}