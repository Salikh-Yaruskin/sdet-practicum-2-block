pipeline {
  agent any
  stages {
    stage('Checkout') { steps { checkout scm } }
    stage('Test') { steps { sh 'mvn -U -q test' } }
  }
  post {
      always {
        junit 'target/surefire-reports/*.xml'
        archiveArtifacts artifacts: 'target/allure-results/**, allure-report.tgz', 
                       allowEmptyArchive: true
        allure([
          includeProperties: false,
          jdk: '',
          results: [[path: 'target/allure-results']]
        ])
        script {
          def testResults = junit(testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true)
          def total = testResults.totalCount
          def passed = total - testResults.failCount - testResults.skipCount
          def failed = testResults.failCount
          def skipped = testResults.skipCount
          
            emailext(
              to: 'tofatty33@gmail.com',
              subject: "Jenkins Build ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
              body: """\
                Build Status: ${currentBuild.currentResult}
                Project: ${env.JOB_NAME}
                Build Number: ${env.BUILD_NUMBER}
                Build URL: ${env.BUILD_URL}
                Total Tests: ${total}
                Passed: ${passed}
                Failed: ${failed}
                Skipped: ${skipped}
                Allure Report:
                  ${env.BUILD_URL}allure/
              """,
              attachmentsPattern: 'allure-report.tgz',
              mimeType: 'text/plain'
            )
        }
      }
    }
}
