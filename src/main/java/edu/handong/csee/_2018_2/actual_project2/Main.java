package edu.handong.csee._2018_2.actual_project2;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

public class Main {

	public static void main(String[] args) throws IOException, RevisionSyntaxException, GitAPIException {
		// 0. Directory path to access
		// TODO: Change directoryPath to path of your MovieLens
		String directoryPath = "/Users/imseongbin/Documents/Java/MovieLens";

		// 1. Initial setting
		Git git = Git.open(new File(directoryPath));
		Repository repository = git.getRepository();

		// 2. The relative path of the file to be blamed (MovieLens에서 blame 하고 싶은 파일의 상대주소)
		String path = "src/main/java/edu/handong/csee/pp1/bigdata/movielens/Recommender.java";

		// 3. Blame
		blame(git, repository, path);

	}
	
	//출처: https://github.com/sungbin/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/porcelain/BlameFile.java
	public static void blame(Git git, Repository repository, String file) throws RevisionSyntaxException,
			AmbiguousObjectException, IncorrectObjectTypeException, IOException, GitAPIException {

		System.out.println("Blaming " + file);
		final BlameResult result = new Git(repository).blame().setFilePath(file)
				.setTextComparator(RawTextComparator.WS_IGNORE_ALL).call();
		final RawText rawText = result.getResultContents();
		for (int i = 0; i < rawText.size(); i++) {
			final PersonIdent sourceAuthor = result.getSourceAuthor(i);
			final RevCommit sourceCommit = result.getSourceCommit(i);
			System.out.println(sourceAuthor.getName()
					+ (sourceCommit != null ? "/" + sourceCommit.getCommitTime() + "/" + sourceCommit.getName() : "")
					+ ": " + rawText.getString(i));
		}
	}
}
