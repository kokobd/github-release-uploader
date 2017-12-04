package com.github.zelinf.gru;

import com.jcabi.github.Coordinates;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RemoteConfig;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class RepoInfo {

    public static class Exception extends IOException {
        public Exception(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public RepoInfo() {
        this(FileSystems.getDefault().getPath("."));
    }

    public RepoInfo(Path repoDir) {
        this.repoDir = repoDir;
    }

    private Path repoDir;

    public Coordinates getRepoCoordinates() throws Exception {
        try (Git git = Git.open(repoDir.toFile())) {

            Optional<String> fullRepoName = Optional.empty();
            Throwable cause = null;
            try {
                List<RemoteConfig> remotes = git.remoteList().call();
                Optional<RemoteConfig> origin = remotes.stream()
                        .filter(remote -> remote.getName().equals("origin"))
                        .findAny();
                fullRepoName = origin.map(config -> config.getURIs().get(0).getPath());
                // TODO get 'user/repo'
            } catch (GitAPIException e) {
                cause = e;
            }
            if (!fullRepoName.isPresent()) {
                throw new Exception("Failed to get full repository name.", cause);
            }

            return new Coordinates.Simple(fullRepoName.get());
        } catch (IOException e) {
            throw new Exception("Unknown I/O error occurred.", e);
        }
    }

}
