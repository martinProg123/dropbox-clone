pipeline {
    agent any

    environment {
        APP_NAME = 'dropbox-clone'
        // DATETIME = "${JENKINS_TIMESTAMP,format="yyyyMMdd-HHmmss"}"
        DOCKER_HUB_CREDS = credentials('docker-hub-creds')
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code...'
                checkout scm
            }
        }

        stage('Backend - Install Dependencies') {
            steps {
                dir('backend') {
                    echo 'Installing backend dependencies...'
                    sh 'mvn dependency:go-offline -B'
                }
            }
        }

        stage('Backend - Build & Test') {
            steps {
                dir('backend') {
                    echo 'Running backend tests...'
                    sh 'mvn test -B'
                }
            }
        }

        stage('Backend - Package') {
            steps {
                dir('backend') {
                    echo 'Packaging backend application...'
                    sh 'mvn package -DskipTests -B'
                }
            }
        }

        stage('Frontend - Install Dependencies') {
            steps {
                dir('frontend') {
                    echo 'Installing frontend dependencies...'
                    sh 'npm install'
                }
            }
        }

        stage('Frontend - Build & Test') {
            steps {
                dir('frontend') {
                    echo 'Running frontend tests...'
                    sh 'npm test'
                }
            }
        }

        stage('Frontend - Build') {
            steps {
                dir('frontend') {
                    echo 'Building frontend application...'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker - Build Backend Image') {
            steps {
                echo 'Building backend Docker image...'
                sh 'docker build -t dropbox-backend:latest ./backend'
            }
        }

        stage('Docker - Build Frontend Image') {
            steps {
                echo 'Building frontend Docker image...'
                sh 'docker build -t dropbox-frontend:latest ./frontend'
            }
        }

        stage('Docker - Push Images') {
            steps {
                echo 'Pushing Docker images...'
                sh '''
                    docker tag dropbox-backend:latest $DOCKER_HUB_CREDS_USR/dropbox-backend:latest
                    docker tag dropbox-frontend:latest $DOCKER_HUB_CREDS_USR/dropbox-frontend:latest
                    echo $DOCKER_HUB_CREDS_PSW | docker login -u $DOCKER_HUB_CREDS_USR --password-stdin
                    docker push $DOCKER_HUB_CREDS_USR/dropbox-backend:latest
                    docker push $DOCKER_HUB_CREDS_USR/dropbox-frontend:latest
                    docker logout
                '''
            }
        }

        stage('Deploy to Local') {
            steps {
                echo 'Deploying to local environment...'
                withCredentials([file(credentialsId: 'env-prod-file', variable: 'PROD_ENV_FILE')]) {
                    sh 'cp $PROD_ENV_FILE .env.prod'
                    sh 'docker-compose -f docker-compose.yml pull'
                    sh 'docker-compose -f docker-compose.yml up -d'
                }
            }
        }
    }

    post {
        always {
            echo 'Cleaning up workspace...'
            cleanWs()
        }
        success {
            echo 'CI/CD pipeline completed successfully!'
        }
        failure {
            echo 'CI/CD pipeline failed. Check logs for details.'
        }
    }
}