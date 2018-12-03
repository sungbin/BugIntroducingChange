package edu.handong.csee._2018_2.actual_project2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class Main {

	public static void main(String[] args) throws IOException, RevisionSyntaxException, GitAPIException {
		// 0. Directory path to access
		String directoryPath = "/Users/imseongbin/Documents/Java/okhttp";

		// 1. Initial setting
		Git git = Git.open(new File(directoryPath));
		Repository repository = git.getRepository();
		File temp = new File("/Users/imseongbin/Desktop/temp.txt");

//		 4. Parsing Reference
		String reference = "/Users/imseongbin/Desktop/okhttp.csv";
		File file = new File(reference);
		ArrayList<String> paths = new CSVgetter(reference).getColumn(1);
		ArrayList<String> commits = new CSVgetter(reference).getColumn(3);
		HashSet<String> fixCommits = new HashSet<String>();
		for (int i = 0; i < paths.size(); i++) {
			String path = paths.get(i);
			if (path.contains("DiskLruCache.java"))
				fixCommits.add(commits.get(i));
		}
		StringBuffer sb = new StringBuffer();
		for (String fixCommit : fixCommits) {
			System.out.println(fixCommit);

			// 2. The relative path of the file to be blamed (MovieLens에서 blame 하고 싶은 파일의
			// 상대주소)
			String targetFilePath = "okhttp/src/main/java/com/squareup/okhttp/internal/DiskLruCache.java";
			String ref = fixCommit; // Fix Commits //
			String targetRef = "52454c61993e4d4376d429652104ae404503c3ad"; // 2015-1

			// 3. Blame to get fix commits
			aLine[] lines = blame(git, repository, targetFilePath, ref);
			
			for (aLine line : lines)
				sb.append(line + "\n");

			// to make easily
//		FileUtils.writeStringToFile(temp, sb.toString());

			// 5. Diff
			String[] diffs = DiffProcessor.diff(repository, targetFilePath, ref + "~1", ref);
			String rex = "@@\\s\\-?\\+?(\\d+),(\\d+)\\s\\+\\d+,\\d+\\s@@";
			Pattern p = Pattern.compile(rex);
			class LN {
				LN(int start, int end) {
					this.start = start;
					this.end = end;
				}

				public int start;
				public int end;

				public String toString() {
					return start + ", " + end;
				}
			}
			;

			// 6. get targetLines
			lines = blame(git, repository, targetFilePath, ref + "~1");
			ArrayList<LN> lineNumbers = new ArrayList<>();
			for (String diff : diffs) {
//			System.out.println(diff);
				Matcher m = p.matcher(diff);
				while (m.find()) {
					int start = Integer.parseInt(m.group(1)) - 1;// for Line count start from Zero
					int end = start + Integer.parseInt(m.group(2));
//				System.out.println(start+", " +end);
					lineNumbers.add(new LN(start, end));
				}
			}

			HashSet<aLine> targetLines = new HashSet<aLine>();
			for (LN a : lineNumbers) {
//			System.out.println(a);
				for (int i = a.start; i < a.end; i++) {
//				System.out.println(lines[i]);
					targetLines.add(lines[i]);
				}
			}
			sb = new StringBuffer();
			aLine[] result = blame(git, repository, targetFilePath, targetRef);
			for (aLine a : result) {
				if (targetLines.contains(a)) {
					a.buggy();
//				System.out.println(a);
				}
			}
			for (aLine line : result) {
				sb.append(line + "\n");
			System.out.println(line);
			}
		}
		FileUtils.writeStringToFile(temp, sb.toString());

	}

	// 출처:
	// https://github.com/sungbin/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/porcelain/BlameFile.java
	public static aLine[] blame(Git git, Repository repository, String path, String rev) throws RevisionSyntaxException,
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
			linelist[i] = new aLine(i, commit.getAuthorIdent(), rawText.getString(i), commit.getName());
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
