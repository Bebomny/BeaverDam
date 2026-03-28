import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import java.nio.file.Files

plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.hidetake.ssh") version "2.12.0"
}

group = "dev.bebomny"
version = "0.0.1"
description = "BeaverDam"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://m2.chew.pro/releases") }
}

extra["springModulithVersion"] = "2.0.3"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-starter-jpa")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-websocket-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    //Docker java
    implementation("com.github.docker-java:docker-java:3.7.0")
    implementation("com.github.docker-java:docker-java-transport-httpclient5:3.7.0")

    //RSS reader
    implementation("com.apptasticsoftware:rssreader:3.12.0")

    //Discord
    implementation("net.dv8tion:JDA:6.3.1") {
        exclude(module = "opus-java")
        exclude(module = "tink")
    }
    implementation("pw.chew:jda-chewtils:2.2.1")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val watermelonVmServer = Remote(
    mutableMapOf<String, Any>(
        "host" to "watermelon-vm",
        "user" to "luka",
        "identity" to File("${System.getProperties()["user.home"]}/.ssh/id_rsa")
    )
)

tasks.register("deployToServer") {
    group = "deployment"
    description = "Builds the app, transfers files to the server, and runs docker compose"

    dependsOn("bootJar")

    doLast {
        ssh.run(delegateClosureOf<RunHandler> {
            session(
                watermelonVmServer,
                delegateClosureOf<SessionHandler> {
                    val targetDir = "./beaverdam/"
                    println("Connected to server. Creating directories...")
                    execute("mkdir -p $targetDir")

                    println("Uploading Spring Boot jar...")
                    val jarFile = tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar").get().archiveFile.get().asFile

                    put(hashMapOf(
                        "from" to jarFile,
                        "into" to "$targetDir/app.jar"
                    ))

                    println("Uploading Production Docker/Compose files...")
                    put(hashMapOf("from" to file("docker-compose.prod.yml"), "into" to "$targetDir/docker-compose.yml"))
                    put(hashMapOf("from" to file(".env.prod"), "into" to "$targetDir/.env"))
                    put(hashMapOf("from" to file("Dockerfile"), "into" to "$targetDir/Dockerfile"))

                    println("Restarting Docker containers...")
                    execute("cd $targetDir && docker compose down")
                    execute("cd $targetDir && docker compose up -d --build")

                    println("Deployment successful")
                }
            )
        })
    }
}
