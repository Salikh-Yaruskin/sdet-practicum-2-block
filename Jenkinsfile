pipeline {
  agent any
  stages {
    stage('Checkout') { steps { checkout scm } }
    stage('Test') { steps { sh 'mvn -U -q test' } }
  }
  post {
      always {
        archiveArtifacts artifacts: 'target/allure-results/**', allowEmptyArchive: true
          allure([
          includeProperties: false,
          jdk: '',
          results: [[path: 'target/allure-results']]
        ])
      }
    }
}
