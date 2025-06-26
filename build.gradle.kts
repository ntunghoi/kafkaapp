plugins {
    id("java")
    alias(libs.plugins.springframework.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.ntunghoi"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    implementation(libs.commons.cli)
    implementation(libs.embedded.redis)
    implementation(libs.jackson.csv)
    implementation(libs.jjwt.api)
    implementation(libs.jjwt.impl)
    implementation(libs.jjwt.jackson)
    implementation(libs.ksql.api.client) {
        exclude(group = "ch.qos.reload4j", module = "reload4j")
        exclude(group = "org.slf4j", module = "slf4j-reload4j")
    }
    implementation(libs.springdoc.openapi.webmvc.ui)

    runtimeOnly(libs.h2)
    runtimeOnly(libs.kafka.streams)
    runtimeOnly(libs.logback.classic)

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "com.vaadin.external.google", module = "android-json")
    }
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

// Example: ./gradlew bootRunCli -Pargs="-i load-data.json"
tasks.register("bootRunCli") {
    group = "application"
    description = "Runs the Spring Boot with 'cli' profile"
    doFirst {
        tasks.bootRun.configure {
            if (project.hasProperty("args")) {
                args(project.property("args"))
            }
            //systemProperty("logging.level.root", "OFF")
            systemProperty("logging.level.com.ntunghoi", "DEBUG")
            systemProperty("spring.main.banner-mode", "off")
            systemProperty("spring.profiles.active", "cli")
        }
    }

    finalizedBy("bootRun")
}
