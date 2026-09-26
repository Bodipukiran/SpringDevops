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
        APP_PORT        = "8080"
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
                sh 'mvn -B clean compile'
            }
        }

        stage('Test') {
            steps {
                echo "Running unit and integration tests..."
                sh 'mvn -B test'
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
                sh 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image ${IMAGE_NAME}:${IMAGE_TAG}..."
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest ."
            }
        }

        stage('Run Docker Container') {
            steps {
                echo "Stopping any previous container and starting a fresh one..."
                sh """
                    docker rm -f ${CONTAINER_NAME} || true
                    docker run -d --name ${CONTAINER_NAME} \
                        -p ${APP_PORT}:8080 \
                        ${IMAGE_NAME}:latest
                """
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
