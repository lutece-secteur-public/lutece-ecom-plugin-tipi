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
        INPUTS = ''
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '2'))
        timeout(time: 10, unit: 'MINUTES')
        gitLabConnection('gitlab')
        gitlabCommitStatus(name: 'running')
    }

    parameters {
        choice(choices: 'Build\nRelease', description: 'Choose what you want to do', name: 'action')
    }

    stages {

        // CHECKOUT
        stage('Cloning gitlab repository') {
            when {
                expression { params.action == 'Build' }
            }
            steps {
                checkout([
                        $class                           : 'GitSCM',
                        branches                         : [[name: BRANCH_NAME]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [[$class: 'LocalBranch', localBranch: BRANCH_NAME]],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[credentialsId: 'gitlab-credentials', url: 'git@gestionversion.acn:applications_lutece/lutece-ecom-plugin-tipi.git']]
                ])

                script {
                    LAST_COMMIT_SHA = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                }
            }
        }

        // BUILD
        stage('Compile - Tests') {
            when {
                expression { params.action == 'Build' }
                not { branch 'master' }
            }
            steps {
                sh 'mvn clean compile'
            }
        }

        // SONAR
        stage('Analyse sonar of the last commit') {
            tools { jdk "jdk8" }
            when {
                expression { BRANCH_NAME ==~ /^feature.*/ }
            }
            steps {
                echo "Analyse du commit SHA: ${LAST_COMMIT_SHA}"
                configFileProvider(
                        [configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh "mvn -s $MAVEN_SETTINGS verify sonar:sonar -Dmaven.test.skip=true -Dsonar.analysis.mode=preview -Dsonar.issuesReport.console.enable=true -Dsonar.gitlab.commit_sha=${LAST_COMMIT_SHA}"
                }
            }
        }

        stage('Deploy Nexus + Full analyse Sonar') {
            tools { jdk "jdk8" }
            when {
                expression { BRANCH_NAME ==~ /^develop.*/ && params.action == 'Build' }
            }
            steps {
                configFileProvider(
                        [configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn -s $MAVEN_SETTINGS deploy'
                    sh 'mvn -s $MAVEN_SETTINGS sonar:sonar'
                }
            }
        }

        // RELEASE

        stage('Synchro gitHub') {
            when {
                expression { params.action == 'Release' }
            }
            steps {
                echo 'Mise à jour github'
                sshagent(['git-credentials']) {
                    sh "ssh -o StrictHostKeyChecking=no -l gitlab gestionversion.acn scripts/synchro-github.sh plugin-tipi $BRANCH_NAME"
                }
            }
        }

        stage('Cloning github repository') {
            when {
                expression { params.action == 'Release' }
            }
            steps {
                checkout([
                        $class                           : 'GitSCM',
                        branches                         : [[name: BRANCH_NAME]],
                        doGenerateSubmoduleConfigurations: false,
                        extensions                       : [[$class: 'LocalBranch', localBranch: BRANCH_NAME]],
                        submoduleCfg                     : [],
                        userRemoteConfigs                : [[credentialsId: 'github-credentials', url: 'https://github.com/lutece-secteur-public/lutece-ecom-plugin-tipi.git']]
                ])
            }
        }

        stage('Release Prepare') {
            when {
                expression { params.action == 'Release' }
            }
            agent none
            steps {
                script {
                    // Read pom.xml
                    def pom = readMavenPom file: 'pom.xml'
                    // Assign the default release version
                    def release_version = pom.version.replace("-SNAPSHOT", "")
                    // Increase the default next development version
                    def version = pom.version.toString().replace("-SNAPSHOT", "").split("\\.")
                    version[-1] = version[-1].toInteger() + 1
                    def next_version = version.join('.') + "-SNAPSHOT"

                    try {
                        timeout(time: 2, unit: 'MINUTES') {
                            INPUTS = input message: 'Perform realease ?', ok: 'Release!',
                                    parameters: [
                                            string(name: 'RELEASE_VERSION', defaultValue: "${release_version}", description: 'What is the release version'),
                                            string(name: 'NEXT_VERSION', defaultValue: "${next_version}", description: 'What is the development version')
                                    ]
                        }
                    } catch (err) {
                        currentBuild.result = "SUCCESS"
                        INPUTS = 'skip'
                    }
                }
            }
        }

        stage('Release Perform') {
            when {
                expression { params.action == 'Release' && INPUTS != 'skip' }
            }
            steps {
                configFileProvider(
                        [configFile(fileId: 'maven-settings', variable: 'MAVEN_SETTINGS')]) {
                    withCredentials([usernamePassword(credentialsId: 'github-credentials', passwordVariable: 'pwd', usernameVariable: 'user')]) {
                        sh "mvn -s $MAVEN_SETTINGS release:prepare release:perform -Dresume=false -DreleaseVersion=${INPUTS.RELEASE_VERSION} -DdevelopmentVersion=${INPUTS.NEXT_VERSION} " +
                                "-Darguments='-Dmaven.test.skip=true' -DignoreSnapshots=true -Dgoals=deploy -Dusername=${user} -Dpassword=${pwd}"
                    }
                }
                sshagent(['git-credentials']) {
                    sh "ssh -o StrictHostKeyChecking=no -l gitlab gestionversion.acn scripts/release-github.sh plugin-tipi"
                }
            }
            post {
                failure {
                    sh "mvn release:rollback"
                }
            }
        }

    }
}