package in.edu.ashoka.surf.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by hangal on 2/15/18.
 */
class GitLayer {

    public static Repository createNewRepository(String path) throws IOException {
        // prepare a new folder
        String gitRepoPath = "/Users/hangal/tmp/abc";

        if (new File(gitRepoPath).exists())
            return new FileRepository(new File(gitRepoPath, ".git"));

        // create the directory
        Repository repository = FileRepositoryBuilder.create(new File(gitRepoPath, ".git"));
        repository.create();

        return repository;
    }

    public static void commit(Repository repo, String filename, String path) throws GitAPIException {
        Git git = new Git(repo);
        String tmpfile = "123";
        git.add().addFilepattern(tmpfile).call();
        git.commit().setMessage("some message").call();
    }


    public static void checkout(Repository repo, String path, String version) throws GitAPIException {
        Git git = new Git(repo);
        String tmpfile = "123";
        git.add().addFilepattern(tmpfile).call();
        git.commit().setMessage("some message").call();
    }

    public static List<String> versions(Repository repo, String path, String version) throws GitAPIException {
        Git git = new Git(repo);
        String tmpfile = "123";
        git.add().addFilepattern(tmpfile).call();
        git.commit().setMessage("some message").call();
        return null;
    }


    public static void main(String args[]) {

    }
}

