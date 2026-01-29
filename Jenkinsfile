pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }
    stage('Test') {
      steps {
        sh 'mvn -U -q test'
      }
    }
    stage('Package Allure report') {
      steps {
        sh 'mvn -q allure:report'
        sh 'tar -czf allure-report.tgz -C target allure-report'
      }
    }
  }
  post {
    always {
      mail to: 'tofatty33@gmail.com',
           subject: "Jenkins Build Notification: ${currentBuild.fullDisplayName}",
           body: """\
              Build Status: ${currentBuild.currentResult}
              Project: ${env.JOB_NAME}
              Build Number: ${env.BUILD_NUMBER}
              Build URL: ${env.BUILD_URL}
              """
      
      junit 'target/surefire-reports/*.xml'
      
      script {
        archiveArtifacts artifacts: 'target/allure-results/**, allure-report.tgz', allowEmptyArchive: true
        
        allure([
          includeProperties: false,
          jdk: '',
          results: [[path: 'target/allure-results']]
        ])
      }
    }
  }
}
