pipeline {

    tools {
        maven 'maven-3.3.9'
        jdk "jdk7"
    }

    agent {
        label {
            label ""
            customWorkspace "workspace/$JOB_NAME"
        }
    }

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
                        $class                           : 'GitSCM',
                        branches                         : [[name: BRANCH_NAME]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[credentialsId: 'gitlab-credentials',
                                                             url          : 'git@gestionversion.acn:applications_lutece/lutece-ecom-plugin-tipi.git']]
                ])
            }
        }

        // BUILD
        stage('Compile - Tests') {
            steps {
                gitlabCommitStatus {
                    sh 'mvn clean install'
                }
            }
        }

        stage('Deploy Nexus + Analyse Sonar') {
            tools { jdk "jdk8" }
            when { branch 'develop' }
            steps {
                configFileProvider(
                        [configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS deploy'
                    sh 'mvn -s $MAVEN_SETTINGS sonar:sonar'
                }
            }
        }

        stage('Plugin Sonar-Gitlab') {
            tools { jdk "jdk8" }
            when { expression { BRANCH_NAME ==~ /^feature.*/ } }
            steps {
                echo 'Analyse sonar des commits...'
                sh "/home/docker_app/scripts_indus/plugin_gitlab_sonar_evolution.sh plugin-tipi $WORKSPACE"
            }
        }

        // Github
        stage('Synchro gitHub') {
            when { branch 'master' }
            steps {
                echo 'Mise à jour github'
                sshagent(['gitlab-credentials']) {
                    sh "ssh -o StrictHostKeyChecking=no -l git gestionversion.acn cd /home/git/repositories/plugin-tipi && git fetch && git checkout master " +
                            "&& git pull origin master && git push github master"
                }

            }
        }

        // mvn release:prepare release:perform -Dresume=false -Dusername=XXX -Dpassword=XXX

    }

    post {
        always {
            echo "autheur is $CHANGE_AUTHOR_EMAIL"
        }
    }

}