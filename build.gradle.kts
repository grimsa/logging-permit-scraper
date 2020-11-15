plugins {
    java
    idea
}

group = "com.github.grimsa.loggingpermits"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.slf4j:slf4j-api:1.7.30")
    runtimeOnly("org.slf4j:slf4j-simple:1.7.30")
    implementation("org.apache.commons:commons-lang3:3.11")
    implementation("org.apache.commons:commons-csv:1.8")

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("file.encoding", "utf-8")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}