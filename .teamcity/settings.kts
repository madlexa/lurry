/*
 * Copyright 2020 Aleksey Dobrynin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2020.1"

project {

    vcsRoot(Git)

    buildType(Distributive)
    buildType(Test)
}

object Distributive : BuildType({
    name = "Distributive"

    artifactRules = """
        target/lurry-*.jar => .
    """.trimIndent()
    buildNumberPattern = "%maven.project.version%"

    vcs {
        root(Git)
    }

    steps {
        maven {
            name = "Packaging"
            goals = "clean package"
            jdkHome = "%env.JDK_1_8_x64%"
            runnerArgs = "-DskipTests=false"
        }
    }

    dependencies {
        snapshot(Test) {
            onDependencyFailure = FailureAction.CANCEL
        }
    }
})

object Test : BuildType({
    name = "Test"

    allowExternalStatus = true
    buildNumberPattern = "%maven.project.version%.%build.counter%"
    maxRunningBuilds = 1

    vcs {
        root(Git)
    }

    steps {
        maven {
            name = "Test + Coverage"
            goals = "clean test"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            jdkHome = "%env.JDK_1_8_x64%"
            coverageEngine = jacoco {
                classLocations = "+:target/classes/one/trifle/lurry/**"
                excludeClasses = "+:one.trifle.lurry.*"
                jacocoVersion = "%teamcity.tool.jacoco.0.8.4%"
            }
        }
    }

    triggers {
        vcs {
        }
    }
})

object Git : GitVcsRoot({
    name = "lurry git"
    url = "https://github.com/madlexa/lurry"
})
