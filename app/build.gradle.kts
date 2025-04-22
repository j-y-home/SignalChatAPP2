import org.gradle.kotlin.dsl.annotationProcessor
import org.gradle.kotlin.dsl.compileOnly
import kotlin.math.sign

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)

}
//必须配置
//def mfph = [
//    //宿主包名
//    "apk.applicationId" : "xxx.xxx.xxxxx"
//]
android {
    namespace = "com.example.endtoendencryptionsystem"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.endtoendencryptionsystem"
        minSdk = 24
        targetSdk = 30
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
    // 以下为uniMP-Android必须导入的SDK相关依赖arr
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar")))) //导入libs目录下的所有arr
    implementation(fileTree(mapOf("include" to arrayOf("*.jar"), "dir" to "libs")))
    implementation ("androidx.appcompat:appcompat:1.1.0")
   // implementation 'androidx.localbroadcastmanager:localbroadcastmanager:1.0.0'
    implementation("androidx.core:core:1.1.0")
    implementation("androidx.fragment:fragment:1.1.0")
    implementation("androidx.compose.runtime:runtime:1.5.0-alpha01")
    implementation("androidx.viewpager2:viewpager2:1.0.0")


    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    implementation("com.alibaba:fastjson:1.2.83")

    implementation("com.facebook.fresco:fresco:2.5.0")
    implementation("com.facebook.fresco:animated-gif:2.5.0")

    implementation("com.github.bumptech.glide:glide:4.9.0")

    implementation("androidx.webkit:webkit:1.5.0")
  //  ksp ("com.github.bumptech.glide:compiler:4.9.0")
    implementation(libs.mmkv)
    implementation(libs.okhttp)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    //ksp(libs.room.compiler)


    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.annotations)

    implementation("org.whispersystems:signal-protocol-android:2.8.1")
    //implementation("org.whispersystems:signal-client-android:0.9.5")

    compileOnly("org.projectlombok:lombok:1.18.16")
    annotationProcessor("org.projectlombok:lombok:1.18.16")

    annotationProcessor("org.projectlombok:lombok:1.18.28")  // 确保使用最新版本
    implementation("org.projectlombok:lombok:1.18.28")

//    implementation(libs.libsignal.client)
//    implementation(libs.libsignal.android)
//    implementation(libs.signal.ringrtc)
//    implementation(libs.signal.aesgcmprovider)
//    implementation(libs.signal.android.database.sqlcipher)
   // implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    implementation(libs.androidx.activity.compose)
//    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.ui)
//    implementation(libs.androidx.ui.graphics)
//    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.material3)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.ui.test.junit4)
//    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)
//
//    implementation(libs.activity.ktx)
//    implementation(libs.fragment.ktx)
//    implementation(libs.lifecycle.viewmodel.ktx)
//    implementation(libs.lifecycle.livedata.ktx)
//    implementation(libs.refresh.layout.kernel)
//    implementation(libs.refresh.header.classics)
//    implementation(libs.circleimageview)
//    implementation(libs.adapter)
//    implementation(libs.the.router)
////    implementation ("cn.therouter:router:1.2.1")
////    ksp ("cn.therouter:apt:1.2.1")
////    implementation(files("libs\\jxl.jar"))
//    ksp(libs.the.router.compile)
//    implementation(libs.sweet.dialog)
//    implementation(libs.autodispose)
//    implementation(libs.autodispose.lifecycle)
//    implementation(libs.autodispose.android)
//    implementation(libs.autodispose.androidx.lifecycle)
//    implementation(libs.glide)
//    implementation(libs.glide.compiler)
//    implementation(libs.pictureselector)
//    //implementation(libs.live.event.bus)
//    //Material_Spinner
//    implementation(libs.smartmaterialspinner)
//    // implementation(libs.powerspinner)
//    implementation(libs.material.spinner)
//    implementation(libs.rxbinding)
//    implementation(libs.rxbinding.core)
//    implementation(libs.rxbinding.appcompat)
//    implementation(libs.rxbinding.material)
//    implementation(libs.status.bar)
//    implementation(libs.x.popup)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.test.ext.junit)
////    androidTestImplementation(libs.espresso.core)
//    ksp(libs.room.compiler)
//    implementation(libs.jackson.core.compat)
//    implementation(libs.jackson.module.kotlin.compat)
//    implementation(libs.jackson.databind.compat)
//    implementation(libs.brv)
//    implementation(libs.pdf.viewer)
//    implementation("com.github.getActivity:XXPermissions:18.5")
//    implementation("com.github.lzyzsd:jsbridge:1.0.4")
//    implementation ("com.github.PhilJay:MPAndroidChart:v2.1.6")
    //implementation ("com.github.open-android:WheelPicker:v1.0.0")
//    implementation ("com.bm.photoview:library:1.4.1")
//    implementation ("net.lingala.zip4j:zip4j:2.8.0")
////    implementation ("com.github.lzyzsd:jsbridge:1.0.4")
   // implementation("com.facebook.fresco:fresco:1.13.0")
//    implementation ("me.relex:photodraweeview:1.1.2")
//    implementation ("com.github.satyan:sugar:1.5")
//    implementation ("com.mcxiaoke.volley:library:1.0.19")
//    implementation ("com.alibaba:fastjson:1.2.55")
//    implementation ("com.github.open-android:pinyin4j:2.5.0")
//    implementation ("com.github.bigdongdong:ChatView:1.1")//添加依赖

//    implementation ("cn.jiguang.sdk:jmessage:2.9.0")
//    implementation ("cn.jiguang.sdk:jpush:3.3.4")
//    implementation ("cn.jiguang.sdk:jcore:2.1.2")

  //  implementation ("me.leolin:ShortcutBadger:1.1.22@aar")

//    // 注意版本与项目一致
//    implementation ("androidx.recyclerview:recyclerview:1.1.0")

  //  implementation ("com.github.iielse:ImageWatcher:1.1.0")

//    implementation project(path: ':LibZxing')
  //  implementation("com.readystatesoftware.systembartint:systembartint:1.0.3")
//
    // 高仿微信图片选择
  //  implementation("com.github.lovetuzitong:MultiImageSelector:1.2")

   // implementation("com.lwkandroid.widget:NineGridView:1.4.4")
//    implementation 'com.github.bumptech.glide:glide:4.11.0'
//    annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'
//    implementation "com.lwkandroid:ImagePicker:1.3.0"
//
//    implementation 'com.qmuiteam:qmui:2.0.0-alpha10'
//    // 工具类
//    implementation 'com.blankj:utilcodex:1.28.0'

//
//    // 核心必须依赖
//    implementation 'com.scwang.smart:refresh-layout-kernel:2.0.1'
//    // 经典刷新头
//    implementation 'com.scwang.smart:refresh-header-classics:2.0.1'
//    implementation 'com.zhy:flowlayout-lib:1.0.3'
//
//    implementation 'com.github.HuanTanSheng:EasyPhotos:3.1.3'
//    implementation 'com.github.smarxpan:NotchScreenTool:0.0.1'
//
//    implementation 'com.google.zxing:core:3.5.3'

    testFixturesImplementation(libs.libsignal.client)
}