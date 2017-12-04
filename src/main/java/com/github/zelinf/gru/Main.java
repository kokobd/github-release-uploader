package com.github.zelinf.gru;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.nio.file.Path;

public class Main {

    public static void main(String... args) {
        OptionParser parser = new OptionParser();
        OptionSpec<String> tokenOpt = parser.accepts("token").withRequiredArg().ofType(String.class);
        OptionSpec<File> repoDirOpt = parser.accepts("repo-dir").withOptionalArg().defaultsTo(".").ofType(File.class);
        OptionSpec<File> fileOpt = parser.accepts("file").withRequiredArg().ofType(File.class);
        OptionSpec<String> fileMimeTypeOpt = parser.accepts("mime-type").withRequiredArg().ofType(String.class);
        OptionSpec<String> tagOpt = parser.accepts("tag").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);
        String token = options.valueOf(tokenOpt);
        Path file = options.valueOf(fileOpt).toPath();
        String fileMimeType = options.valueOf(fileMimeTypeOpt);
        String tag = options.valueOf(tagOpt);

        Uploader uploader = new Uploader(token, file, fileMimeType, tag);
        uploader.setRepoDir(options.valueOf(repoDirOpt).toPath());
        boolean succeeded = uploader.upload();
        if (!succeeded) {
            System.err.println("Failed to upload your file.");
        }
    }
}
