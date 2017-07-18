pipeline {

  tools {
    maven 'maven-3.3.9'
    jdk "jdk7"
  }

  agent any
  
  options { 
    buildDiscarder(logRotator(numToKeepStr: '2'))
    timeout(time: 10, unit: 'MINUTES')
    gitLabConnection('gitlab')
    gitlabCommitStatus(name: 'pending')
  }

  stages {

    stage('Clone repository') {
        steps {
            checkout([
              $class: 'GitSCM', 
              branches: [[name: BRANCH_NAME]],
              doGenerateSubmoduleConfigurations: false,
              extensions: [],
              submoduleCfg: [],
              userRemoteConfigs: [[credentialsId: 'gitlab-credentials',
              url: 'git@gestionversion.acn:applications_lutece/lutece-ecom-plugin-tipi.git']]
            ])
        }
    }
    
    // BUILD 
    stage('Compile - Tests'){
      steps {
          gitlabCommitStatus {
            sh 'mvn clean install'
          }
        }
    }
   
    // SONAR
    stage('Analyse Sonar'){ 
      tools {jdk "jdk8"}
      when { branch 'develop' }
      steps {
          sh 'mvn sonar:sonar'
      }
    } 

    stage('Analyse Sonar 2'){
      when { expression { BRANCH_NAME ==~ /^feature.*/ } }
      steps {
          echo 'Analyse sonar des commits'        
        }
    }

    // Github
    stage('Update gitHub'){
      when { branch 'master' }
      steps {
        echo 'Mise à jour github'
      }
    }

  }

}