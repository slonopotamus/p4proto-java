import org.gradle.api.tasks.testing.logging.TestExceptionFormat

val ossrhUsername: String? = System.getenv("OSSRH_USERNAME")
val ossrhPassword: String? = System.getenv("OSSRH_PASSWORD")
val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
val gitCommit = System.getenv("TRAVIS_COMMIT") ?: ""

tasks.wrapper {
    gradleVersion = "5.4.1"
    distributionType = Wrapper.DistributionType.ALL
}

repositories {
    jcenter()
}

plugins {
    id("com.github.ben-manes.versions") version "0.21.0"
    id("de.marcphilipp.nexus-publish") version "0.2.0"
    id("io.codearte.nexus-staging") version "0.21.0"
    idea
    application
    signing
}

group = "ru.bozaro.p4"
version = "0.1.0-SNAPSHOT"

val javaVersion = JavaVersion.VERSION_1_8

idea {
    project.jdkName = javaVersion.name

    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    useTestNG {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
        }
    }
}

application {
    mainClassName = "ru.bozaro.p4.P4"
}

dependencies {
    compile("org.jetbrains:annotations:17.0.0")
    compile("com.beust:jcommander:1.48")

    testCompile("org.testng:testng:6.14.3")
}

tasks.javadoc {
    (options as? CoreJavadocOptions)?.addStringOption("Xdoclint:none", "-quiet")
}

val javadocJar by tasks.creating(Jar::class) {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

val sourcesJar by tasks.creating(Jar::class) {
    from(project.sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])

            artifact(sourcesJar)
            artifact(javadocJar)

            pom {
                name.set(project.name)

                val pomDescription = description
                afterEvaluate {
                    pomDescription.set(project.description)
                }

                url.set("https://github.com/bozaro/p4proto-java")

                scm {
                    connection.set("scm:git:git://github.com/bozaro/p4proto-java.git")
                    tag.set(gitCommit)
                    url.set("https://github.com/bozaro/p4proto-java")
                }

                licenses {
                    license {
                        name.set("Lesser General Public License, version 3 or greater")
                        url.set("http://www.gnu.org/licenses/lgpl.html")
                    }
                }

                developers {
                    developer {
                        id.set("bozaro")
                        name.set("Artem V. Navrotskiy")
                        email.set("bozaro@yandex.ru")
                    }

                    developer {
                        id.set("slonopotamus")
                        name.set("Marat Radchenko")
                        email.set("marat@slonopotamus.org")
                    }
                }
            }
        }
    }
}

val secretKeyRingFile = "${rootProject.projectDir}/secring.gpg"
extra["signing.secretKeyRingFile"] = secretKeyRingFile
extra["signing.keyId"] = "4B49488E"
extra["signing.password"] = signingPassword

signing {
    isRequired = signingPassword != null && file(secretKeyRingFile).exists()

    sign(publishing.publications)
}

tasks.closeRepository.configure {
    onlyIf { !project.version.toString().endsWith("-SNAPSHOT") }
}

tasks.releaseRepository.configure {
    onlyIf { !project.version.toString().endsWith("-SNAPSHOT") }
}

nexusStaging {
    packageGroup = "ru.bozaro"
    username = ossrhUsername
    password = ossrhPassword
}
