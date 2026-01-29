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
      junit 'target/surefire-reports/*.xml'
  
      script {
        def testResult = currentBuild.rawBuild.getAction(hudson.tasks.junit.TestResultAction)
  
        def total   = testResult?.totalCount ?: 0
        def failed  = testResult?.failCount ?: 0
        def skipped = testResult?.skipCount ?: 0
        def passed  = total - failed - skipped
  
        def status = currentBuild.currentResult
  
        emailext(
          subject: "Jenkins: ${env.JOB_NAME} #${env.BUILD_NUMBER} — ${status}",
          body: """
          <h2>Результаты автотестов</h2>
          <p><b>Статус сборки:</b> ${status}</p>
          <ul>
            <li>Всего тестов: ${total}</li>
            <li>Пройдено: ${passed}</li>
            <li>Упало: ${failed}</li>
            <li>Пропущено: ${skipped}</li>
          </ul>
          <p>
            <a href="${env.BUILD_URL}allure/">Allure Report</a>
          </p>
          """,
          mimeType: 'text/html',
          to: 'ТВОЙ_EMAIL@example.com',
          attachmentsPattern: 'allure-report.tgz'
        )
      }
  
      archiveArtifacts artifacts: 'target/allure-results/**, allure-report.tgz', allowEmptyArchive: true
  
      allure([
        includeProperties: false,
        jdk: '',
        results: [[path: 'target/allure-results']]
      ])
    }
  }
}
