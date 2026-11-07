plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

fun getEnvOrProperty(envKey: String, propKey: String, defaultValue: String): String {
    return System.getenv(envKey)
        ?: (project.findProperty(propKey) as String?)
        ?: defaultValue
}

android {
    namespace = "com.mindcluster.safediary"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.mindcluster.safediary"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Configurable endpoints from environment variables or gradle properties
        val endpointAuthSignIn = getEnvOrProperty("SAFEDY_ENDPOINT_AUTH_SIGN_IN", "safediary.endpointAuthSignIn", "api/v1/authentication/sign-in")
        val endpointAuthSignUp = getEnvOrProperty("SAFEDY_ENDPOINT_AUTH_SIGN_UP", "safediary.endpointAuthSignUp", "api/v1/authentication/sign-up")
        val endpointPatients = getEnvOrProperty("SAFEDY_ENDPOINT_PATIENTS", "safediary.endpointPatients", "api/v1/patients")
        val endpointPaymentsCheckout = getEnvOrProperty("SAFEDY_ENDPOINT_PAYMENTS_CHECKOUT", "safediary.endpointPaymentsCheckout", "api/v1/payments/subscriptions/checkout")
        val endpointPaymentsCurrent = getEnvOrProperty("SAFEDY_ENDPOINT_PAYMENTS_CURRENT", "safediary.endpointPaymentsCurrent", "api/v1/payments/subscriptions/current")
        val endpointPaymentsCancel = getEnvOrProperty("SAFEDY_ENDPOINT_PAYMENTS_CANCEL", "safediary.endpointPaymentsCancel", "api/v1/payments/subscriptions/cancel")
        val endpointPaymentsHistory = getEnvOrProperty("SAFEDY_ENDPOINT_PAYMENTS_HISTORY", "safediary.endpointPaymentsHistory", "api/v1/payments/history")
        val endpointPaymentsMethods = getEnvOrProperty("SAFEDY_ENDPOINT_PAYMENTS_METHODS", "safediary.endpointPaymentsMethods", "api/v1/payments/methods")
        val stripeDemoUrl = getEnvOrProperty("SAFEDY_STRIPE_DEMO_URL", "safediary.stripeDemoUrl", "https://checkout.stripe.com/pay/demo_session_safediary")

        buildConfigField("String", "ENDPOINT_AUTH_SIGN_IN", "\"$endpointAuthSignIn\"")
        buildConfigField("String", "ENDPOINT_AUTH_SIGN_UP", "\"$endpointAuthSignUp\"")
        buildConfigField("String", "ENDPOINT_PATIENTS", "\"$endpointPatients\"")
        buildConfigField("String", "ENDPOINT_PAYMENTS_CHECKOUT", "\"$endpointPaymentsCheckout\"")
        buildConfigField("String", "ENDPOINT_PAYMENTS_CURRENT", "\"$endpointPaymentsCurrent\"")
        buildConfigField("String", "ENDPOINT_PAYMENTS_CANCEL", "\"$endpointPaymentsCancel\"")
        buildConfigField("String", "ENDPOINT_PAYMENTS_HISTORY", "\"$endpointPaymentsHistory\"")
        buildConfigField("String", "ENDPOINT_PAYMENTS_METHODS", "\"$endpointPaymentsMethods\"")
        buildConfigField("String", "STRIPE_DEMO_URL", "\"$stripeDemoUrl\"")
    }

    buildTypes {
        debug {
            // Emulator loopback to the local safediary-platform backend.
            // On a physical device run: ./gradlew installDebug -Psafediary.apiBaseUrl=http://<pc-ip>:8080/
            val debugApiBaseUrl = getEnvOrProperty("SAFEDY_API_BASE_URL", "safediary.apiBaseUrl", "http://10.0.2.2:8080/")
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
        }
        release {
            // Backend deployed on Render (safediary-platform/render.yaml). Override with -Psafediary.releaseApiBaseUrl=...
            val releaseApiBaseUrl = getEnvOrProperty("SAFEDY_RELEASE_API_BASE_URL", "safediary.releaseApiBaseUrl", "https://safediary-platform.onrender.com/")
            buildConfigField("String", "API_BASE_URL", "\"$releaseApiBaseUrl\"")
            // Course project: sign with the debug key so the release APK can be installed directly.
            signingConfig = signingConfigs.getByName("debug")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Retrofit & OkHttp Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
