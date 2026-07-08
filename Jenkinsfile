pipeline {
    agent {
        kubernetes {
            label 'maven-agent'
            defaultContainer 'maven'
        }
    }

    environment {
        SPRING_DATASOURCE_URL      = 'jdbc:postgresql://localhost:5432/robofleet_db'
        SPRING_DATASOURCE_USERNAME = 'admin'
        SPRING_DATASOURCE_PASSWORD = 'admin'
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
