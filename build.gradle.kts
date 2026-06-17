plugins {
    id("org.springframework.boot") version "4.1.0"
    id("groovy")
    id("jacoco")
    id("com.github.spotbugs") version "6.5.6"
    id("com.gorylenko.gradle-git-properties") version "4.0.1"
    id("pmd")
    id("com.diffplug.spotless") version "8.7.0"
    id("com.google.cloud.tools.jib") version "3.5.3"
}

version = "0.70.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("tools.jackson.module:jackson-module-blackbird")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("org.freemarker:freemarker:2.3.34")
    implementation("com.vladsch.flexmark:flexmark:0.64.8")
    implementation("com.vladsch.flexmark:flexmark-ext-attributes:0.64.8")
    implementation("com.vladsch.flexmark:flexmark-ext-definition:0.64.8")
    implementation("com.vladsch.flexmark:flexmark-ext-tables:0.64.8")
    implementation("io.github.openhtmltopdf:openhtmltopdf-core:1.1.37")
    implementation("io.github.openhtmltopdf:openhtmltopdf-pdfbox:1.1.37")
    implementation("io.github.openhtmltopdf:openhtmltopdf-slf4j:1.1.37")
    implementation("io.github.openhtmltopdf:openhtmltopdf-objects:1.1.37")

    implementation("org.apache.xmlgraphics:batik-transcoder:1.19") {
        // work around https://issues.apache.org/jira/browse/BATIK-1289
        exclude(group = "xml-apis", module = "xml-apis")
    }
    implementation("org.apache.xmlgraphics:batik-codec:1.19") {
        // work around https://issues.apache.org/jira/browse/BATIK-1289
        exclude(group = "xml-apis", module = "xml-apis")
    }
    implementation("org.jfree:jfreechart:1.5.6")
    implementation("org.jsoup:jsoup:1.22.2")
    implementation("com.helger.font:ph-fonts-api:6.1.0")

    compileOnly("com.github.spotbugs:spotbugs-annotations:4.10.2")
    "spotbugsPlugins"("com.h3xstream.findsecbugs:findsecbugs-plugin:1.14.0")

    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("com.helger.font:ph-fonts-open-sans:6.1.0")
    runtimeOnly("ch.qos.logback.contrib:logback-json-classic:0.1.5")
    runtimeOnly("ch.qos.logback.contrib:logback-jackson:0.1.5")
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.apache.tomcat.embed" && requested.version == "11.0.21") {
            useVersion("11.0.22")
            because("Security fixes")
        }
    }
}

spotbugs {
    excludeFilter.set(rootProject.file("config/spotbugs-exclude.xml"))
}

tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsTest") {
    isEnabled = false
}

// write human readable report on normal builds
tasks.named<com.github.spotbugs.snom.SpotBugsTask>("spotbugsMain") {
    reports {
        create("xml") { required = false }
        create("html") { required = true }
    }
}

pmd {
    toolVersion = "7.25.0"
    ruleSetConfig = rootProject.resources.text.fromFile("config/pmd-ruleset.xml")
    ruleSets = emptyList()
    rulesMinimumPriority.set(3)
    isConsoleOutput = true
}

tasks.named<Pmd>("pmdTest") {
    isEnabled = false
}

springBoot {
    buildInfo {
        properties {
            if (rootProject.hasProperty("ciBuildNumber")) {
                additional.set(
                    mapOf(
                        "ci.buildnumber" to rootProject.property("ciBuildNumber").toString(),
                        "ci.jobname" to rootProject.property("ciJobName").toString(),
                    ),
                )
            }
        }
    }
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    environment("spring.output.ansi.console-available", true)
}

jib {
    from {
        image = "gcr.io/distroless/java25-debian13:nonroot"
    }
    container {
        labels.set(
            project.provider {
                mapOf(
                    "org.opencontainers.image.title" to "vernice.veo reporting",
                    "org.opencontainers.image.description" to "Backend of the verinice.veo-reporting web application.",
                    "org.opencontainers.image.ref.name" to "verinice.veo-reporting",
                    "org.opencontainers.image.vendor" to "SerNet GmbH",
                    "org.opencontainers.image.authors" to "verinice@sernet.de",
                    "org.opencontainers.image.licenses" to "AGPL-3.0",
                    "org.opencontainers.image.source" to "https://github.com/verinice/verinice-veo-reporting",
                    "org.opencontainers.image.version" to project.version.toString(),
                    "org.opencontainers.image.revision" to rootProject.property("ciCommitId").toString(),
                )
            },
        )
        environment = mapOf("JDK_JAVA_OPTIONS" to "-Djdk.serialFilter=maxbytes=0")
        user = "nonroot"
        ports = listOf("8080")
        mainClass = "org.veo.reporting.VeoReportingApplication"
    }
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useSpock("2.4-groovy-5.0")
            dependencies {
                implementation("org.spockframework:spock-spring:2.4-groovy-5.0")
                implementation("org.springframework.boot:spring-boot-starter-webmvc-test")
                runtimeOnly("org.springframework.boot:spring-boot-starter-security-test")
                runtimeOnly("org.springframework.boot:spring-boot-starter-validation-test")
                implementation("org.apache.groovy:groovy-json")
                implementation("org.apache.groovy:groovy-xml")
            }
        }
    }
}

spotless {
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/*.gradle.kts")
        ktlint()
    }
    java {
        target("src/**/*.java", "buildSrc/**/*.java")
        googleJavaFormat()
        importOrder("java", "jakarta", "javax", "org", "com", "org.veo", "")
        addStep(org.veo.LicenseHeaderStep.create(project.rootDir))
        addStep(org.veo.NoWildcardImportsStep.create())
        removeUnusedImports()
        trimTrailingWhitespace()
        replaceRegex("Consecutive empty block comment lines", "( *\\*\\n){2,}", "*\n")
        replaceRegex("Empty line at block comment end", " \\*\\n *\\*/", " */")
        replaceRegex("Empty comment block", "\\/\\*+\\s+\\*\\/", "")
        replaceRegex("Empty line after annotation", "(^ +@[a-zA-Z0-9]+\\([^)]+\\)\\n)\\n+", "$1")
        replaceRegex("Empty line inside annotation", "(^ +@[a-zA-Z0-9]+\\([^)]+\\n)\\n+", "$1")
    }
    groovy {
        target("src/**/*.groovy", "buildSrc/**/*.groovy")
        addStep(org.veo.LicenseHeaderStep.create(project.rootDir, "package |runner \\{"))
        addStep(org.veo.NoWildcardImportsStep.create())
        greclipse().configProperties(
            """
            org.eclipse.jdt.core.formatter.tabulation.char=space
            groovy.formatter.remove.unnecessary.semicolons=true
            """.trimIndent(),
        )
        importOrder("java", "javax", "org", "com", "org.veo", "")
        replaceRegex("Excessive line breaks", "\n{3,}", "\n\n")
        replaceRegex("Extra space around equals sign", "(  += )|( =  +)", " = ")
        replaceRegex("Not one space between right round and left curly bracket", "\\) *\\{", ") {")
        toggleOffOn()
    }
    format("git") {
        target("**/.gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
    json {
        target("**/*.json")
        targetExclude("**/bin/**", "**/target/**")
        gson().indentWithSpaces(2)
        endWithNewline()
    }
    format("xml") {
        target("config/**/*.xml")
        eclipseWtp(com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.XML)
    }
    yaml {
        target(".gitlab-ci.yml")
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("templates") {
        target("src/main/resources/**/*.md")
        replaceRegex("self-closing divs", "<div([^>]*?)\\s*/>", "<div$1></div>")
        forbidRegex("no self-closing divs", "<div[^>]*/>", "div tags must not be self-closing")
    }
}
