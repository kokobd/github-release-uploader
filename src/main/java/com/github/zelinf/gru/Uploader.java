package com.github.zelinf.gru;

import com.jcabi.github.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Uploader {

    public Uploader(String authToken, Path file, String fileMimeType, String tag) {
        this.tag = tag;
        this.file = file;
        this.fileMimeType = fileMimeType;

        github = new RtGithub(authToken);
    }

    private Path file;
    private String fileMimeType;
    private String tag;

    private Github github;

    private Path repoDir;

    public void setRepoDir(Path repoDir) {
        this.repoDir = repoDir;
    }

    /**
     * @return Whether the upload has succeeded.
     */
    public boolean upload() {
        try {
            RepoInfo config = new RepoInfo(repoDir);
            Repo repo = github.repos().get(config.getRepoCoordinates());
            Release release = getRelease(repo.releases(), tag);
            uploadFile(release);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Get the release with specific tag. Create one if there is not.
     * The created release's name is its tag.
     *
     * @param releases where to create the release
     * @param tag      release tag
     * @return the release
     * @throws IOException If there is any I/O problem.
     */
    private Release getRelease(Releases releases, String tag) throws IOException {
        for (Release release : releases.iterate()) {
            Release.Smart smartRelease = new Release.Smart(release);
            if (smartRelease.tag().equals(tag)) {
                return smartRelease;
            }
        }

        // release with specific tag can not be found.
        return createRelease(releases, tag);
    }

    private Release createRelease(Releases releases, String tag) throws IOException {
        Release.Smart release = new Release.Smart(releases.create(tag));
        release.name(tag);
        return release;
    }

    private void uploadFile(Release release) throws IOException {
        byte[] content = Files.readAllBytes(file);
        release.assets().upload(content, fileMimeType, file.getFileName().toString());
    }
}
