try {
    podTemplate(yaml: readTrusted('jenkins/build.yaml')) {
        node(POD_LABEL) {
            checkout scm

            milestone()
            lock(resource: 'testing', inversePrecedence: true) {
                stage('Compile & Test') {
                    try {
                        container('gradle') {
                            sh script: 'gradle test \
                             -Dspring.profiles.active=test,jenkins_test \
                             --info --stacktrace',
                             label: 'Compile & Test'
                        }
                    }
                    catch(exception) {
                        println "Failed to test - ${currentBuild.fullDisplayName}"
                        throw(exception)
                    }
                    finally {
                        junit '**/build/test-results/test/*.xml'
                    }
                }
                milestone()
            }

            if (env.BRANCH_NAME ==~ /(master|release\/\d+\.\d+.\d+)/) {
                stage('Build JAR') {
                    container('gradle') {
                        sh script: 'gradle build -x test --info --stacktrace', label: 'Build JAR archives'
                    }
                }

                stage('Build docker image') {
                    container('gcloud') {
                        if (hasChangeIn('docker/koderia-flows/')) {
                            dir('docker/koderia-flows') {
                                sh script: "gcloud builds submit .", label: 'Build Koderia-flows docker image'
                            }
                        }
                    }
                }
            }
        }
    }
} catch (org.jenkinsci.plugins.workflow.steps.FlowInterruptedException cause) {
  throw cause
} catch (cause) {
  env.STACK_TRACE = hudson.Functions.printThrowable(cause)
  emailext(body: '${SCRIPT, template="build-failed.template"}',
          subject: '[Jenkins] Notification center - build failed',
          to: 'bazik@objectify.sk',
          mimeType: 'text/html',
          recipientProviders: [
            [$class: 'DevelopersRecipientProvider'],
            [$class: 'RequesterRecipientProvider']
          ])
  throw cause
}
