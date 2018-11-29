package edu.handong.csee._2018_2.actual_project2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class Main {

	public static void main(String[] args) throws IOException, RevisionSyntaxException, GitAPIException {
		// 0. Directory path to access
		// TODO: Change directoryPath to path of your MovieLens
		String directoryPath = "/Users/imseongbin/Documents/Java/MovieLens";

		// 1. Initial setting
		Git git = Git.open(new File(directoryPath));
		Repository repository = git.getRepository();

		// 2. The relative path of the file to be blamed (MovieLens에서 blame 하고 싶은 파일의
		// 상대주소)
		String path = "src/main/java/edu/handong/csee/pp1/bigdata/movielens/Recommender.java";
		String ref = "7d97c959d36745b931220e29dcec27b16095554b~~";
		// String path =
		// "src\\main\\java\\edu\\handong\\csee\\pp1\\bigdata\\movielens\\Recommender.java";

		// 3. Blame
		aLine[] lines = blame(git, repository, path,ref);
		for(aLine line : lines)
			System.out.println(line);
		
		String reference = "/Users/imseongbin/Desktop/okhttp.csv";
		File file = new File(reference);

//		ArrayList<String> strs = new CSVgetter(reference).getColumn(2);
//		for(String str : strs) {
//			if(str.contains("DiskLruCache.java"))
//				System.out.println(str);
//		}

	}

	// 출처:
	// https://github.com/sungbin/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/porcelain/BlameFile.java
	public static aLine[] blame(Git git, Repository repository, String path,String rev) throws RevisionSyntaxException,
			AmbiguousObjectException, IncorrectObjectTypeException, IOException, GitAPIException {

		BlameCommand blamer = new BlameCommand(repository);
		ObjectId commitID = repository.resolve(rev);
		blamer.setStartCommit(commitID);
		blamer.setFilePath(path);
		BlameResult blame = blamer.call();
		final RawText rawText = blame.getResultContents();
		
		// read the number of lines from the given revision, this excludes changes from
		// the last two commits due to the "~~" above
		int lines = countLinesOfFileInCommit(repository, commitID, path);
		aLine[] linelist = new aLine[lines];
		for (int i = 0; i < lines; i++) {
			RevCommit commit = blame.getSourceCommit(i);
//			System.out.println("Line: " + i + ": " + commit.getAuthorIdent()+ rawText.getString(i));
			linelist[i] = new aLine(i,commit.getAuthorIdent(),rawText.getString(i));
		}
		return linelist;
	}

	private static int countLinesOfFileInCommit(Repository repository, ObjectId commitID, String name)
			throws IOException {
		try (RevWalk revWalk = new RevWalk(repository)) {
			RevCommit commit = revWalk.parseCommit(commitID);
			RevTree tree = commit.getTree();
//			System.out.println("Having tree: " + tree);

			// now try to find a specific file
			try (TreeWalk treeWalk = new TreeWalk(repository)) {
				treeWalk.addTree(tree);
				treeWalk.setRecursive(true);
				treeWalk.setFilter(PathFilter.create(name));
				if (!treeWalk.next()) {
					throw new IllegalStateException("Did not find expected file 'README.md'");
				}

				ObjectId objectId = treeWalk.getObjectId(0);
				ObjectLoader loader = repository.open(objectId);

				// load the content of the file into a stream
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				loader.copyTo(stream);

				revWalk.dispose();

				return IOUtils.readLines(new ByteArrayInputStream(stream.toByteArray()), "UTF-8").size();
			}
		}
	}
}
