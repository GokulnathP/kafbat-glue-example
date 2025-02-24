plugins {
    kotlin("jvm") version "2.0.21"
}

group = "com.gokulnathp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:3.7.1")
    implementation("org.apache.avro:avro:1.11.4")
    implementation("software.amazon.glue:schema-registry-serde:1.1.14")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
