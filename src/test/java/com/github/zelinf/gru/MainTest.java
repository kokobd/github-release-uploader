package com.github.zelinf.gru;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.*;

public class MainTest {

    public MainTest() throws IOException {
        config = new Properties();
        config.load(MainTest.class.getResourceAsStream("test-config.properties"));
    }

    @Before
    void setUp() throws Exception {
        Path tempDir = Files.createTempDirectory("gru-test");
        try (Git git = cloneRepo()) {
            // TODO
        }
    }

    @Test
    void main() {
        Main.main("--token", "abc",
                "--file", "path",
                "--mime-type", "application/zip",
                "--tag", "v0.1.0");
    }

    @After
    void tearDown() throws Exception {

    }

    private Properties config;

    private Git cloneRepo() throws GitAPIException {
        String uri = String.format("https://github.com/%s/%s",
                config.getProperty("username"),
                config.getProperty("repoName"));

        return Git.cloneRepository()
                .setURI(uri)
                .call();
    }
}