buildscript {
    val kotlinVersion: String by project
    val ktapiVersion: String by project

    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
        classpath("com.github.ktapi:ktapi:$ktapiVersion")
    }
}

version = "0.0.1"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    val ktormVersion: String by project
    val ktapiVersion: String by project

    implementation("com.github.ktapi:ktapi:$ktapiVersion")
    implementation("org.ktorm:ktorm-core:$ktormVersion")
    implementation("org.ktorm:ktorm-jackson:$ktormVersion")
    implementation("io.javalin:javalin:4.6.4")
    implementation("io.javalin:javalin-openapi:4.6.4")
    implementation("io.github.microutils:kotlin-logging:2.1.23")
    implementation("com.cronutils:cron-utils:9.2.0")
    implementation("org.flywaydb:flyway-core:9.3.0")
    testImplementation("io.javalin:javalin-testtools:4.6.4")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("io.mockk:mockk:1.12.7")
}

val hash = System.getenv()["GIT_HASH"]?.take(10) ?: "${System.currentTimeMillis()}".take(10)
project.version = hash
project.file("src/main/resources/version").writeText(hash)

apply(plugin = "kotlin")
apply<org.ktapi.gradle.MigrationPlugin>()

plugins {
    application
    id("com.bmuschko.docker-java-application") version ("6.7.0")
}

application {
    mainClass.set("api.AppKt")
}

docker {
    javaApplication {
        baseImage.set("adoptopenjdk/openjdk17")
        jvmArgs.set(listOf("-Xms512m", "-Xmx2048m"))
        ports.set(listOf(8080))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}