package com.github.zelinf.gru;

import com.jcabi.github.*;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class MainTest {

    private final String authToken;
    private final String username;
    private final String repoName;

    public MainTest() throws IOException {
        Properties config = new Properties();
        config.load(MainTest.class.getResourceAsStream("/resources/test-config.properties"));

        authToken = config.getProperty("authToken");
        username = config.getProperty("username");
        repoName = config.getProperty("repoName");

        Github github = new RtGithub(authToken);
        remoteRepository = github.repos().get(new Coordinates.Simple(username, repoName));
    }

    @Before
    public void setUp() throws Exception {
        cloneRepo();
        createUploadFile();

        deleteRelease();
    }

    private Path testDir; // repoDir is placed inside testDir
    private Path repoDir;

    private void createUploadFile() throws IOException {
        final String fileName = "test-archive.zip";
        uploadFile = repoDir.resolve(fileName);
        Files.copy(MainTest.class.getResourceAsStream("/resources/" + fileName),
                uploadFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private Path uploadFile;

    private final String tag = "v0.1.0";

    @Test
    public void main() throws IOException {
        Main.main("--token", authToken,
                "--repo-dir", repoDir.toString(),
                "--file", uploadFile.toString(),
                "--mime-type", "application/zip",
                "--tag", tag);

        expectsReleaseAsset(uploadFile.getFileName().toString(),
                uploadFile);
    }

    private Repo remoteRepository;

    private void expectsReleaseAsset(String assetName, Path assetFile) throws IOException {
        Iterable<Release> releases = remoteRepository.releases().iterate();

        boolean foundRelease = false;
        for (Release release : releases) {
            if (foundRelease)
                break;

            Release.Smart smartRelease = new Release.Smart(release);
            if (smartRelease.tag().equals(tag)) {
                foundRelease = true;
                boolean foundAsset = false;

                Iterable<ReleaseAsset> assets = smartRelease.assets().iterate();
                for (ReleaseAsset someAsset : assets) {
                    if (foundAsset)
                        break;

                    ReleaseAsset.Smart releaseAsset = new ReleaseAsset.Smart(someAsset);
                    if (releaseAsset.name().equals(assetName)) {
                        foundAsset = true;

                        byte[] fileContent = Files.readAllBytes(assetFile);
                        byte[] assetContent = new byte[fileContent.length];

                        URL downloadUrl = new URL(releaseAsset.json().getString("browser_download_url"));
                        try (InputStream in = downloadUrl.openStream()) {
                            //noinspection ResultOfMethodCallIgnored
                            in.read(assetContent);
                        }

                        Assert.assertArrayEquals(fileContent, assetContent);
                    }
                }

                Assert.assertTrue(foundAsset);
            }
        }

        Assert.assertTrue(foundRelease);
    }

    private void deleteRelease() {
        Releases.Smart releases = new Releases.Smart(remoteRepository.releases());
        try {
            Release release = releases.find(tag);
            release.delete();
        } catch (IOException | IllegalArgumentException ignored) {
        }
    }

    @After
    public void tearDown() throws Exception {
        deleteRelease();
        FileUtils.deleteDirectory(testDir.toFile());
    }

    private void cloneRepo() throws GitAPIException, IOException {
        testDir = Files.createTempDirectory("gru-test");
        repoDir = testDir.resolve(repoName);

        String uri = String.format("https://github.com/%s/%s",
                username,
                repoName);

        //noinspection EmptyTryBlock
        try (Git ignored = Git.cloneRepository()
                .setURI(uri)
                .setDirectory(repoDir.toFile())
                .call()) {
        }
    }
}