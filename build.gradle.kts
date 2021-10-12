plugins {
    java
    idea
}

group = "com.github.grimsa.loggingpermits"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.slf4j:slf4j-api:1.7.32")
    runtimeOnly("org.slf4j:slf4j-simple:1.7.32")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-csv:1.9.0")

    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("file.encoding", "UTF-8")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

task("runScraper", JavaExec::class) {
    group = "Execution"
    description = "Run the scraper"
    main = "com.github.grimsa.loggingpermits.scraper.Scraper"
    classpath = sourceSets["main"].runtimeClasspath
}