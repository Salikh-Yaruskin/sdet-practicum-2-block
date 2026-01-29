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
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'

      archiveArtifacts artifacts: 'target/allure-results/**', allowEmptyArchive: true
      archiveArtifacts artifacts: 'target/allure-report/**',  allowEmptyArchive: true
      archiveArtifacts artifacts: 'allure-report.tgz',        allowEmptyArchive: true
      allure([
        includeProperties: false,
        jdk: '',
        results: [[path: 'target/allure-results']]
      ])

      emailext(
        to: 'tofatty@gmail.com',
        subject: "Autotests: ${JOB_NAME} #${BUILD_NUMBER} — ${BUILD_STATUS}",
        mimeType: 'text/html',
        body: """
          <h3>Autotest results</h3>
          <p><b>Job:</b> ${JOB_NAME}</p>
          <p><b>Build:</b> #${BUILD_NUMBER}</p>
          <p><b>Status:</b> ${BUILD_STATUS}</p>

          <p><b>Tests:</b><br>
            Total: ${TEST_COUNTS,var="total"}<br>
            Passed: ${TEST_COUNTS,var="pass"}<br>
            Failed: ${TEST_COUNTS,var="fail"}<br>
            Skipped: ${TEST_COUNTS,var="skip"}
          </p>
        """,
        attachmentsPattern: 'allure-report.tgz'
      )
    }
  }
}
