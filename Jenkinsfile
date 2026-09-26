// =====================================================================
// Jenkinsfile - Declarative CI/CD pipeline for Student Management System
//
// Pipeline stages:
//   1. Checkout        - pull the source code from Git
//   2. Build           - compile the project with Maven
//   3. Test            - run the JUnit/Mockito test suite
//   4. Package         - build the executable jar
//   5. Build Docker Image - build the app's Docker image
//   6. Run Docker Container - (re)deploy the container locally
//
// Requires a Jenkins agent with the "Maven" and "JDK 25" tools configured
// under Manage Jenkins > Tools (names must match MAVEN_HOME / JDK below),
// and Docker installed & available to the Jenkins user.
//
// NOTE: bumped from JDK 21 -> JDK 25 to match pom.xml's java.version. In
// Jenkins, go to Manage Jenkins > Tools > JDK installations and add a JDK
// named exactly "JDK25" pointing at a JDK 25 install (or an auto-installer),
// otherwise this pipeline will fail to resolve the "jdk 'JDK25'" tool below.
//
// FIX (this version): every `sh` step was replaced with a cross-platform
// runCmd() helper. `sh` only exists on Linux/macOS agents - on a Windows
// agent it fails immediately with:
//   java.io.IOException: Cannot run program "sh": CreateProcess error=2,
//   The system cannot find the file specified
// runCmd() uses Jenkins' built-in isUnix() check to run `sh` on Unix-like
// agents and `bat` (cmd.exe) on Windows agents, so this same Jenkinsfile
// now works on either without any other changes.
// =====================================================================

pipeline {
    agent any

    tools {
        maven 'Maven3'     // Name must match a Maven installation configured in Jenkins Global Tool Configuration
        jdk 'JDK25'        // Name must match a JDK 25 installation configured in Jenkins Global Tool Configuration
    }

    environment {
        IMAGE_NAME      = "student-management-system"
        IMAGE_TAG       = "${env.BUILD_NUMBER}"
        CONTAINER_NAME  = "sms-app"
        APP_PORT        = "8081"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Cloning repository..."
                checkout scm
                // If triggering this job manually against a Git URL instead of
                // using Jenkins' built-in SCM checkout, use instead:
                // git branch: 'main', url: 'https://github.com/<your-org>/student-management-system.git'
            }
        }

        stage('Build') {
            steps {
                echo "Building the project with Maven..."
                runCmd('mvn -B clean compile')
            }
        }

        stage('Test') {
            steps {
                echo "Running unit and integration tests..."
                runCmd('mvn -B test')
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            steps {
                echo "Packaging the application as a jar..."
                runCmd('mvn -B package -DskipTests')
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image ${IMAGE_NAME}:${IMAGE_TAG}..."
                runCmd("docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest .")
            }
        }

        stage('Run Docker Container') {
            steps {
                echo "Stopping any previous container and starting a fresh one..."
                script {
                    // Equivalent of the old "docker rm -f ... || true": on a fresh
                    // agent there is no previous container yet, so this command is
                    // expected to fail the first time. Catching it here (rather than
                    // relying on shell-specific "|| true"/"|| exit 0" syntax) works
                    // identically whether runCmd used sh or bat underneath.
                    try {
                        runCmd("docker rm -f ${CONTAINER_NAME}")
                    } catch (err) {
                        echo "No previous container named ${CONTAINER_NAME} to remove - continuing."
                    }
                }
                runCmd("docker run -d --name ${CONTAINER_NAME} -p ${APP_PORT}:8080 ${IMAGE_NAME}:latest")
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully. App is running on port ${APP_PORT}."
        }
        failure {
            echo "Pipeline failed. Check the stage logs above for details."
        }
        always {
            echo "Pipeline finished with status: ${currentBuild.currentResult}"
        }
    }
}

// Runs `command` with the right shell for whatever OS the current agent is
// on: `sh` (Bourne shell) on Unix-like agents, `bat` (cmd.exe) on Windows
// agents. Declarative pipelines can call ordinary functions defined at the
// bottom of the file like this one.
def runCmd(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}
