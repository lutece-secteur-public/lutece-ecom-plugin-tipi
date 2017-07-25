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

    environment {
        LAST_COMMIT_SHA = ''
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

        stage('Analyse sonar of the last commit') {
            tools { jdk "jdk8" }
            when { expression { BRANCH_NAME ==~ /^feature.*/ } }
            steps {
                echo 'Analyse sonar des commits...'
                script {
                    def commitSha = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    LAST_COMMIT_SHA = commitSha
                    echo "Last commit SHA: ${LAST_COMMIT_SHA}"
                }
                configFileProvider(
                        [configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh "mvn -s $MAVEN_SETTINGS verify sonar:sonar -Dmaven.test.skip=true -Dsonar.analysis.mode=preview -Dsonar.issuesReport.console.enable=true -Dsonar.gitlab.commit_sha=${LAST_COMMIT_SHA}"
                }
            }
        }

        stage('Deploy Nexus + Full analyse Sonar') {
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

        // Github
        stage('Synchro gitHub') {
            when { branch 'develop' }
            steps {
                echo 'Mise à jour github'
                sshagent(['git-credentials']) {
                    sh "ssh -o StrictHostKeyChecking=no -l gitlab gestionversion.acn scripts/synchro-github.sh $WORKSPACE $BRANCH_NAME"
                }
            }
        }

        stage('Release') {
            when { branch 'develop' }
            steps {
                script {

                    // Read pom.xml
                    def pom = readMavenPom file: 'pom.xml'
                    // Assign the default release version
                    def release_version = pom.version.replace("-SNAPSHOT", "")
                    // Increase the default next development version
                    def next_version = pom.version.toString().replace("-SNAPSHOT", "").split("\\.")
                    version[-1] = version[-1].toInteger() + 1
                    DEFAULT_DEVELOPMENT_VERSION = version.join('.') + "-SNAPSHOT"

                    PARAMS = input message: 'Perform realease ?', ok: 'Release!',
                            parameters: [
                                    string(name: 'RELEASE_VERSION', defaultValue: "${release_version}", description: 'What is the release version'),
                                    string(name: 'NEXT_VERSION', defaultValue: "${next_version}", description: 'What is the development version')
                            ]

                    configFileProvider(
                            [configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                        sh "mvn -s $MAVEN_SETTINGS release:prepare release:perform -Dresume=false -DreleaseVersion=${PARAMS.RELEASE_VERSION} -DdevelopmentVersion=${PARAMS.NEXT_VERSION} -Darguments='-Dmaven.test.skip=true' -DignoreSnapshots=true -Dgoals=deploy"
                    }

                    echo "Mise à jour github"
                    sshagent(['git-credentials']) {
                        sh "ssh -o StrictHostKeyChecking=no -l gitlab gestionversion.acn scripts/realease-github.sh $WORKSPACE"
                    }
                }
            }
            post {
                failure {
                    sh "mvn release:rollback"
                }
            }
        }
    }

    post {
        always {
            sh "echo ${env.CHANGE_AUTHOR_EMAIL}"
            sh "echo ${env.CHANGE_AUTHOR}"
        }
    }

}