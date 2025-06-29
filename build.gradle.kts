plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("io.javalin:javalin:6.7.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // env
    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    // MySQL
    implementation(dependencyNotation = "mysql:mysql-connector-java:8.0.33")

    // HikariCP para pool de conexiones
    implementation("com.zaxxer:HikariCP:5.0.1")

    // BCrypt para hashing de contraseñas
    implementation("at.favre.lib:bcrypt:0.10.2")

    // Jackson para JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("org.assertj:assertj-core:3.24.2")
}

tasks.test {
    useJUnitPlatform()
}