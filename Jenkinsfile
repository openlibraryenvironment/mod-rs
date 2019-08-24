#!groovy

node {
  stage ('checkout') {
    checkout scm
  }

  stage ('test') {
    dir ( 'service' ) {
      sh './gradlew --no-daemon --console=plain clean test'
    }
  }

  stage ('build') {
    dir ( 'service' ) {
      sh './gradlew --no-daemon --console=plain build'
    }
  }

  stage ('archive') {
    archiveArtifacts artifacts: 'service/build/libs/mod-rs-*'
  }

  stage ('Package') {
    if ( env.BRANCH_NAME == null ) {
      // Assume single branch should be packaged.
      echo "Single branch pipeline is assumed to require packaging."
    }
    if ( env.BRANCH_NAME == null || ['master', 'test'].contains(env.BRANCH_NAME) ) {
      def war_files = findFiles glob: 'service/build/libs/*.war'
      echo "${war_files}"
      if ( war_files.length == 1 ) {
        echo "Single file found. ${war_files[0].name} at ${war_files[0].path}"
        openshift.withCluster {
          openshift.withProject {
            echo "call packager.startBuild"
          }
        }
      }
    }
  }


  // step([$class: 'ArtifactArchiver', artifacts: 'build/libs/*.jar', fingerprint: true])
  // 
  // stage 'reports'
  // step([$class: 'JUnitResultArchiver', testResults: 'build/test-results/*.xml'])
}
