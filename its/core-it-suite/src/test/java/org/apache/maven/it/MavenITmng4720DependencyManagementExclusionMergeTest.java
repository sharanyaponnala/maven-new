/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.it;

import java.io.File;
import java.util.List;

import org.apache.maven.shared.verifier.Verifier;
import org.apache.maven.shared.verifier.util.ResourceExtractor;
import org.junit.jupiter.api.Test;

/**
 * This is a test set for <a href="https://issues.apache.org/jira/browse/MNG-4720">MNG-4720</a>.
 *
 * @author Benjamin Bentmann
 * @author Ddiier Loiseau
 */
public class MavenITmng4720DependencyManagementExclusionMergeTest extends AbstractMavenIntegrationTestCase {

    public MavenITmng4720DependencyManagementExclusionMergeTest() {
        super("[2.0.6,)");
    }

    /**
     * Verify the effective exclusions applied during transitive dependency resolution when both the regular
     * dependency section and dependency management declare exclusions for a particular dependency.
     * <p>
     * By default, the dependency management of transitive dependencies is now taken into account.
     *
     * @throws Exception in case of failure
     */
    @Test
    public void testitWithTransitiveDependencyManager() throws Exception {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/mng-4720");

        Verifier verifier = newVerifier(testDir.getAbsolutePath());
        verifier.setAutoclean(false);
        verifier.deleteArtifacts("org.apache.maven.its.mng4720");
        verifier.addCliArgument("-s");
        verifier.addCliArgument("settings.xml");
        verifier.filterFile("settings-template.xml", "settings.xml", "UTF-8");
        verifier.addCliArgument("validate");
        verifier.execute();
        verifier.verifyErrorFreeLog();

        List<String> classpath = verifier.loadLines("target/classpath.txt", "UTF-8");

        assertTrue(classpath.toString(), classpath.contains("a-0.1.jar"));
        assertTrue(classpath.toString(), classpath.contains("c-0.1.jar"));

        assertFalse(classpath.toString(), classpath.contains("b-0.1.jar"));

        // dependency management in a excludes d
        if (matchesVersionRange("[4.0.0-beta-5,)")) {
            assertFalse(classpath.toString(), classpath.contains("d-0.1.jar"));
        } else {
            assertTrue(classpath.toString(), classpath.contains("d-0.1.jar"));
        }
    }

    /**
     * Verify the effective exclusions applied during transitive dependency resolution when both the regular
     * dependency section and dependency management declare exclusions for a particular dependency.
     * <p>
     * This tests the same behaviour with dependency manager transitivity disabled.
     *
     * @throws Exception in case of failure
     */
    @Test
    public void testitWithTransitiveDependencyManagerDisabled() throws Exception {
        File testDir = ResourceExtractor.simpleExtractResources(getClass(), "/mng-4720");

        Verifier verifier = newVerifier(testDir.getAbsolutePath());
        verifier.setAutoclean(false);
        verifier.deleteArtifacts("org.apache.maven.its.mng4720");
        verifier.addCliArgument("-s");
        verifier.addCliArgument("settings.xml");
        verifier.filterFile("settings-template.xml", "settings.xml", "UTF-8");
        verifier.addCliArgument("-Dmaven.resolver.dependencyManagerTransitivity=false");
        verifier.addCliArgument("validate");
        verifier.execute();
        verifier.verifyErrorFreeLog();

        List<String> classpath = verifier.loadLines("target/classpath.txt", "UTF-8");

        assertTrue(classpath.toString(), classpath.contains("a-0.1.jar"));
        assertTrue(classpath.toString(), classpath.contains("c-0.1.jar"));

        assertFalse(classpath.toString(), classpath.contains("b-0.1.jar"));

        // backward-compat: dependency management is ignored except in root pom
        assertTrue(classpath.toString(), classpath.contains("d-0.1.jar"));
    }
}
