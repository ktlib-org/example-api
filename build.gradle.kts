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
    val javalinVersion: String by project

    implementation("com.github.ktapi:ktapi:$ktapiVersion")
    implementation("org.ktorm:ktorm-core:$ktormVersion")
    implementation("org.ktorm:ktorm-jackson:$ktormVersion")
    implementation("io.javalin:javalin:$javalinVersion")
    implementation("io.javalin.community.openapi:javalin-openapi-plugin:$javalinVersion")
    implementation("io.github.microutils:kotlin-logging:2.1.23")
    implementation("com.cronutils:cron-utils:9.2.0")
    implementation("org.flywaydb:flyway-core:9.3.0")
    implementation("org.postgresql:postgresql:42.2.27")
    testImplementation("io.javalin:javalin-testtools:$javalinVersion")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("io.mockk:mockk:1.12.7")
}

sourceSets {
    val javalinVersion: String by project

    dependencies {
        configurations["kapt"].dependencies.add(project.dependencies.create("io.javalin.community.openapi:openapi-annotation-processor:$javalinVersion"))
    }
}

val hash = System.getenv()["GIT_HASH"]?.take(10) ?: "${System.currentTimeMillis()}".take(10)
project.version = hash
project.file("src/main/resources/version").writeText(hash)

apply(plugin = "kotlin")
apply<org.ktapi.gradle.MigrationPlugin>()

plugins {
    application
    id("com.bmuschko.docker-java-application") version ("6.7.0")
    kotlin("jvm") version "1.8.10"
    kotlin("kapt") version "1.8.10"
}

application {
    mainClass.set("web.AppKt")
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