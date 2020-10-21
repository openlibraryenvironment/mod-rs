#!groovy

podTemplate(
  containers:[
    containerTemplate(name: 'jdk11',                image:'adoptopenjdk:11-jdk-openj9',   ttyEnabled:true, command:'cat'),
    containerTemplate(name: 'docker',               image:'docker:18',                    ttyEnabled:true, command:'cat')
  ],
  volumes: [
    hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock')
  ])
{
  node(POD_LABEL) {

    // See https://www.jenkins.io/doc/pipeline/steps/pipeline-utility-steps/
    // https://www.jenkins.io/doc/pipeline/steps/
    // https://github.com/jenkinsci/nexus-artifact-uploader-plugin

    stage ('checkout') {
      checkout_details = checkout scm
      props = readProperties file: './service/gradle.properties'
      app_version = props.appVersion
      deploy_cfg = null;
      semantic_version_components = app_version.toString().split('\\.')
      is_snapshot = app_version.contains('SNAPSHOT')
      constructed_tag = "build-${props?.appVersion}-${checkout_details?.GIT_COMMIT?.take(12)}"
      println("Got props: ${props} appVersion:${props.appVersion}/${props['appVersion']}/${semantic_version_components} is_snapshot=${is_snapshot}");
      sh 'echo branch:$BRANCH_NAME'
      sh 'echo commit:$checkout_details.GIT_COMMIT'
    }

    stage ('check') {
      container('jdk11') {
        echo 'Hello, JDK'
        sh 'java -version'
      }
    }

    stage ('build') {
      container('jdk11') {
        dir('service') {
          sh './gradlew --version'
          sh './gradlew --no-daemon -x integrationTest --console=plain clean build'
          sh 'ls ./build/libs'
        }
      }
    }

    // https://www.jenkins.io/doc/book/pipeline/docker/
    stage('Build Docker Image') {
      container('docker') {
        //sh 'ls service/build/libs'
        docker_image = docker.build("knowledgeintegration/mod-rs")
      }
    }

    stage('Publish Docker Image') {
      container('docker') {
        dir('docker') {
          if ( checkout_details?.GIT_BRANCH == 'origin/master' ) {

            println("Considering build tag : ${constructed_tag} version:${props.appVersion} is_snapshot:${is_snapshot}");

            // Some interesting stuff here https://github.com/jenkinsci/pipeline-examples/pull/83/files
            if ( !is_snapshot ) {
              // do_k8s_update=true
              docker.withRegistry('','nexus-kidevops') {
                println("Publishing released version with latest tag and semver ${semantic_version_components}");
                docker_image.push('latest')
                docker_image.push("v${app_version}".toString())
                docker_image.push("v${semantic_version_components[0]}.${semantic_version_components[1]}".toString())
                docker_image.push("v${semantic_version_components[0]}".toString())
                // deploy_cfg='deploy_latest.yaml'
              }
            }
            else {
              docker.withRegistry('','nexus-kidevops') {
                println("Publishing snapshot-latest");
                docker_image.push('snapshot-latest')
                // deploy_cfg='deploy_snapshot.yaml'
              }
            }
          }
          else {
            println("Not publishing docker image for branch ${checkout_details?.GIT_BRANCH}. Please merge to master for a docker image build");
          }
        }
      }
    }

  }

  stage ('Remove old builds') {
    //keep 3 builds per branch
    properties([[$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '3', numToKeepStr: '3']]]);
  }

}
