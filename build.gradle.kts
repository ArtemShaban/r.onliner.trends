import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "r.onliner.trends"
version = "0.1"

val ktor_version = "1.3.2"

plugins {
    application
    kotlin("jvm") version "1.3.61"

    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    jcenter()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks.withType<AbstractArchiveTask> {
    setProperty("archiveBaseName", rootProject.group)
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
    manifest {
        attributes(
                mapOf(
                        "Main-Class" to application.mainClassName
                )
        )
    }
}

dependencies {
    implementation(kotlin("stdlib", "1.2.31"))

    //Rx
    implementation("io.reactivex.rxjava2:rxjava:2.2.19")

    //Http + serialization
    implementation("com.github.kittinunf.fuel:fuel:2.2.2")
    implementation("com.github.kittinunf.fuel:fuel-rxjava:2.2.2")
    implementation("com.github.kittinunf.fuel:fuel-gson:2.2.2")
    implementation("com.google.code.gson:gson:2.8.6")

    //database
    implementation("org.jetbrains.exposed:exposed-core:0.24.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.24.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.24.1")
    implementation("mysql:mysql-connector-java:8.0.19")
    implementation("com.zaxxer:HikariCP:3.4.2")

    //logging
    implementation("io.github.microutils:kotlin-logging:1.7.9")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.9.1")

    //ktor server
    implementation("io.ktor:ktor-server-netty:$ktor_version")

    //coroutines to rx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:1.3.7")

    //tests
    testImplementation("junit:junit:4.12")
}

