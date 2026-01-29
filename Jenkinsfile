pipeline {
  agent any
  tools {
    maven 'maven'
  }

  triggers {
      cron('0 9 * * *')
    }

  stages {
    stage('Test') { 
      steps { 
        sh 'mvn -U -q test' 
      } 
    }
  }

  post {
    always {
      script {
        sh 'mvn -q allure:report || true'
        sh 'tar -czf allure-report.tgz -C target/site allure-maven-plugin || tar -czf allure-report.tgz -C target allure-report || true'

        def testResults = junit(testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true)
        def total = testResults.totalCount
        def passed = total - testResults.failCount - testResults.skipCount
        def failed = testResults.failCount
        def skipped = testResults.skipCount

        archiveArtifacts artifacts: 'target/allure-results/**, allure-report.tgz',
                         allowEmptyArchive: true

        allure([
          includeProperties: false,
          jdk: '',
          results: [[path: 'target/allure-results']]
        ])

        mail to: 'tofatty33@gmail.com',
             subject: "Jenkins Build ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
             body: """\
                Build Status: ${currentBuild.currentResult}
                Project: ${env.JOB_NAME}
                Build Number: ${env.BUILD_NUMBER}
                Build URL: ${env.BUILD_URL}

                Test Results:
                =============
                Total Tests: ${total}
                Passed: ${passed}
                Failed: ${failed}
                Skipped: ${skipped}

                Reports:
                ========
                Allure Report: ${env.BUILD_URL}allure/
                Download Archive: ${env.BUILD_URL}artifact/allure-report.tgz
                """
      }
    }
  }
}