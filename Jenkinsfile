// required plugins:
// - OAuth Credentials plugin, org.jenkins-ci.plugins:oauth-credentials:0.4
// - Google Container Registry Auth0, google-container-registry-auth:0.3

def projectVersion
def imageForGradleStages = 'eclipse-temurin:17-jdk'
def dockerArgsForGradleStages = '-v /data/gradle-homes/executor-$EXECUTOR_NUMBER:/gradle-home -e GRADLE_USER_HOME=/gradle-home'

pipeline {
    agent none

    options {
        buildDiscarder(logRotator(numToKeepStr: '50', artifactNumToKeepStr: '5'))
    }

    environment {
        // In case the build server exports a custom JAVA_HOME, we fix the JAVA_HOME
        // to the one used by the docker image.
        JAVA_HOME='/opt/java/openjdk'
        GRADLE_OPTS='-Dhttp.proxyHost=cache.int.sernet.de -Dhttp.proxyPort=3128 -Dhttps.proxyHost=cache.int.sernet.de -Dhttps.proxyPort=3128'
        // pass -Pci=true to gradle, https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties
        ORG_GRADLE_PROJECT_ci=true
    }

    stages {
        stage('Setup') {
            agent {
                docker {
                    image imageForGradleStages
                    args dockerArgsForGradleStages
                }
            }
            steps {
                sh 'env'
                buildDescription "${env.GIT_BRANCH} ${env.GIT_COMMIT[0..8]}"
                script {
                    projectVersion = sh(returnStdout: true, script: '''./gradlew properties -q | awk '/^version:/ {print $2}' ''').trim()
                }
            }
        }
        stage('Build') {
            agent {
                docker {
                    image imageForGradleStages
                    args dockerArgsForGradleStages
                }
            }
            steps {
                sh './gradlew --no-daemon classes'
            }
        }
        stage('Test') {
            agent {
                docker {
                    image imageForGradleStages
                    args dockerArgsForGradleStages
                }
            }
            steps {
                // Don't fail the build here, let the junit step decide what to do if there are test failures.
                sh script: './gradlew --no-daemon test', returnStatus: true
                // Touch all test results (to keep junit step from complaining about old results).
                sh script: 'find build/test-results | xargs touch'
                junit testResults: 'build/test-results/test/**/*.xml'
                jacoco classPattern: 'build/classes/*/main', sourcePattern: 'src/main'
            }
        }
        stage('Artifacts') {
            agent {
                docker {
                    image imageForGradleStages
                    args dockerArgsForGradleStages
                }
            }
            steps {
                sh './gradlew -PciBuildNumer=$BUILD_NUMBER -PciJobName=$JOB_NAME --no-daemon build -x check'
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            }
        }
        stage('Analyze') {
            agent {
                docker {
                    image imageForGradleStages
                    args dockerArgsForGradleStages
                }
            }
            steps {
                sh './gradlew -PciBuildNumer=$BUILD_NUMBER -PciJobName=$JOB_NAME --no-daemon check -x test'
            }
            post {
                failure {
                    recordIssues(enabledForFailure: true, tools: [
                        spotBugs(pattern: 'build/reports/spotbugs/main.xml', useRankAsPriority: true, trendChartType: 'NONE')
                    ])
                    recordIssues(enabledForFailure: true, tools: [
                        pmdParser(pattern: 'build/reports/pmd/main.xml', trendChartType: 'NONE')
                    ])
                }
            }
        }
        stage('Dockerimage') {
            agent {
                label 'docker-image-builder'
            }
            steps {
                script {
                    def dockerImage = docker.build("eu.gcr.io/veo-projekt/veo-reporting:git-${env.GIT_COMMIT}", "--build-arg VEO_REPORTING_VERSION='$projectVersion' --label org.opencontainers.image.version='$projectVersion' --label org.opencontainers.image.revision='$env.GIT_COMMIT' .")
                    // Finally, we'll push the image with several tags:
                    // Pushing multiple tags is cheap, as all the layers are reused.
                    withDockerRegistry(credentialsId: 'gcr:verinice-projekt@gcr', url: 'https://eu.gcr.io') {
                        dockerImage.push("git-${env.GIT_COMMIT}")
                        if (env.GIT_BRANCH == 'main') {
                            dockerImage.push(projectVersion)
                            dockerImage.push("latest")
                            dockerImage.push(env.BUILD_NUMBER)
                        }  else if (env.GIT_BRANCH == 'develop') {
                            dockerImage.push("develop")
                            dockerImage.push("develop-build-${env.BUILD_NUMBER}")
                        }
                    }
                }
            }
        }
        stage('Trigger Deployment') {
            agent none
            when {
                anyOf { branch 'main'; branch 'develop' }
            }
            steps {
                build job: 'verinice-veo-deployment/master'
            }
        }
    }
    post {
        always {
            node('') {
                recordIssues(enabledForFailure: true, tools: [java()])
                recordIssues(
                        enabledForFailure: true,
                        tools: [
                            taskScanner(
                            highTags: 'FIXME',
                            ignoreCase: true,
                            normalTags: 'TODO',
                            excludePattern: 'Jenkinsfile, gradle-home/**, .gradle/**, build/**'
                            )
                        ]
                        )
            }
        }
    }
}
