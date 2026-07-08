pipeline {
    agent {
        kubernetes {
            label 'maven-agent'
            defaultContainer 'maven'
        }
    }
    stages {
        stage('Build Maven') {
            steps {
                container('maven') {
                    sh 'mvn clean install'
                }
            }
        }
    }
}