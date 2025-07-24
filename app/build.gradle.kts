plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.project.attendease"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.project.attendease"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures{
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.material:material:1.12.0")

    

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.16.0")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-collections4
    implementation ("org.apache.commons:commons-collections4:4.5.0-M2")

    implementation("com.github.bumptech.glide:glide:4.13.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("io.socket:socket.io-client:2.0.1")
    implementation ("org.json:json:20210307")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.cortinico:slidetoact:v0.3.0")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("com.jaredrummler:material-spinner:1.3.1")

    implementation ("androidx.recyclerview:recyclerview:1.3.0")
}