package in.edu.ashoka.surf.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.attributes.AttributesNodeProvider;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Created by hangal on 2/15/18.
 */
public class GitLayer {

    public static Repository createNewRepository(String path) throws IOException {
        // prepare a new folder
        File localPath = File.createTempFile(path, "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        // create the directory
        Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"));
        repository.create();

        return repository;
    }

    public static void main (String args[]) throws GitAPIException, IOException {
        Repository repo = createNewRepository("/tmp/abc");

        String tmpfile = "/tmp/abc/123";

        Git git = new Git(repo);

        git.add().addFilepattern(tmpfile).call();
        git.commit().setMessage(tmpfile).call();
    }
}

