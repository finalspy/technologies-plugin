/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019-2020 Pierre Leresteux.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.saagie.technologies

import com.saagie.technologies.model.Metadata
import com.saagie.technologies.model.MetadataDocker
import com.saagie.technologies.model.MetadataTechno
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class SaagieTechnologiesGradlePluginKtTest {

    @Nested
    inner class ProjectKotlinExtension {

        @TempDir
        lateinit var projectdir: File

        @Test
        fun `generateVersionForDocker with simple version`() {
            with(
                ProjectBuilder.builder()
                    .withProjectDir(projectdir)
                    .withName("myproject")
                    .build()
            ) {
                this.version = "1.2.3"
                assertEquals(this.version, this.getVersionForDocker())
            }
        }

        @Test
        fun `generateVersionForDocker with complex version`() {
            with(
                ProjectBuilder.builder()
                    .withProjectDir(projectdir)
                    .withName("myproject")
                    .build()
            ) {
                this.version = "1.2.3+TEST"
                assertEquals(this.version.toString().replace("+", "_"), this.getVersionForDocker())
            }
        }

        @Test
        fun `generateTag`() {
            with(
                ProjectBuilder.builder()
                    .withProjectDir(projectdir)
                    .withName("myproject")
                    .build()
            ) {
                this.version = "1.2.3"
                assertEquals("${this.name}-${this.getVersionForDocker()}", this.generateTag())
            }
        }
    }

    @Nested
    inner class StoreMetadata {

        @TempDir
        lateinit var projectdir: File

        @Test
        fun `generate and create metadata(dot)yml`() {
            with(
                ProjectBuilder.builder()
                    .withProjectDir(projectdir)
                    .withName("myproject")
                    .build()
            ) {
                this.version = "1.2.3"
                val plugin = SaagieTechnologiesGradlePlugin()

                File("$projectDir/version.yml").appendText(
                    """
                    version:
                      label: here_is_a_test
                """.trimIndent()
                )

                val techno = MetadataTechno(
                    "technoId", "technoLabel", true, null, "technoIcon", "technoRecommendedVersion",
                    MetadataDocker("image", "1.2.3")
                )
                val metadata = Metadata(techno = techno)
                storeMetadata(project, projectDir, metadata)
                val metadataFinalFile = File("${project.projectDir.absolutePath}/metadata.yml")
                assertTrue(metadataFinalFile.exists())
                assertEquals(
                    """version:
  label: here_is_a_test
techno:
  id: ${techno.id}
  label: ${techno.label}
  available: ${techno.available}
  icon: ${techno.icon}
  recommendedVersion: ${techno.recommendedVersion}
  docker:
    image: ${techno.docker.image}
    version: ${project.name}-${techno.docker.version}""".trimMargin(),
                    metadataFinalFile.readText().trimIndent()
                )
            }
        }
    }
}
