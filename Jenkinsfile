pipeline {
  agent any
  stages {
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
        mail to: 'tofatty33@gmail.com',
                 subject: "Jenkins Build Notification: ${currentBuild.fullDisplayName}",
                 body: """\
                 Build Status: ${currentBuild.currentResult}
                 Project: ${env.JOB_NAME}
                 Build Number: ${env.BUILD_NUMBER}
                 Build URL: ${env.BUILD_URL}
                 """
      }
    }
}
