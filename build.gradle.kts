plugins {
    kotlin("jvm") version "1.3.61"
}

repositories {
    jcenter()
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
    implementation ("mysql:mysql-connector-java:8.0.19")
    implementation ("com.zaxxer:HikariCP:3.4.2")

    //logging
    implementation ("io.github.microutils:kotlin-logging:1.7.9")
    implementation ("org.apache.logging.log4j:log4j-slf4j-impl:2.9.1")

    //tests
    testImplementation("junit:junit:4.12")
}

