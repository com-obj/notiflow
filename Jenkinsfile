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
                             -Dspring.profiles.active=jenkins_test \
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

            if (isReleaseBranch() && !isPullRequest()) {
                stage('Build artifacts') {
                    container('gradle') {
                        sh script: 'gradle build -x test --info --stacktrace',
                            label: 'Build JAR archives'
                    }

                    container('gcloud') {
                        if (hasChangeIn('docker/koderia-flows/')) {
                            buildImages("koderia-flows")
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

/* Definitions */

def gradleResolveDependencies() {
  retry(5) {
    sh script: "gradle --max-workers=4 resolveDependencies --info --stacktrace",
      label: 'Resolve dependencies'
  }
}

def buildImages(String flows) {
  dir("build/${flows}-${version}") {
    sh script: """
        cp -a ../../docker/${flows}/. .
        chown -R 1000:1000 .
        gcloud builds submit .
    """, label: 'Build ${flows} docker image'

    deleteDir()
  }
}

def loadVersion(String flows) {
  def props = readProperties(file: '${flows}/build/manifest/build-info.properties')
  return props['build-version']
}

def tag(String version) {
  return version.replace('+', '_')
}

def isReleaseBranch() {
  return env.BRANCH_NAME ==~ /(master|(release(-|\/))?((\d+(\.\d+)?\.x)|(v?\d+\.\d+\.\d+))).*/
  //master
  //release-3.x //release-3.2.1 //release/3.2.1 //release/v4.0.0 //release/4.0.0
  //v3.2.1 //3.0.x //3.x
}

def isPullRequest() {
  return env.CHANGE_ID != null
}

@NonCPS
def hasChangeIn(String prefix) {
  def changeLogSets = currentBuild.changeSets
  for (int i = 0; i < changeLogSets.size(); i++) {
    def entries = changeLogSets[i].items
    for (int j = 0; j < entries.length; j++) {
      def files = new ArrayList(entries[j].affectedFiles)
      for (int k = 0; k < files.size(); k++) {
        def file = files[k]
        if (file.path.startsWith(prefix)) {
          return true
        }
      }
    }
  }
  return false
}
