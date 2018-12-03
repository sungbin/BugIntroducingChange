package edu.handong.csee._2018_2.actual_project2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathFilter;

public class DiffProcessor {

	public static String[] diff(Repository repository, String targetFile, String oldCommit, String newCommit)
			throws IOException, GitAPIException {
		String[] diffs = null;
		// the diff works on TreeIterators, we prepare two for the two branches
		AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldCommit);
		AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newCommit);
		// then the porcelain diff-command returns a list of diff entries
		try (Git git = new Git(repository)) {
			List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser)
					.setPathFilter(PathFilter.create(targetFile)).call();
			diffs = new String[diff.size()];
			int i = 0;
			for (DiffEntry entry : diff) {
				StringBuffer sb = new StringBuffer();
//                    sb.append("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
//                    try (DiffFormatter formatter = new DiffFormatter(System.out)) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				try (DiffFormatter formatter = new DiffFormatter(stream)) {
					formatter.setRepository(repository);
					formatter.format(entry);
					String finalString = new String(stream.toByteArray());
					sb.append(finalString + "\n");
					diffs[i] = sb.toString();
				}
				i++;
			}
		}
		return diffs;
	}

	private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
		// from the commit we can build the tree which allows us to construct the
		// TreeParser
		// noinspection Duplicates
		try (RevWalk walk = new RevWalk(repository)) {
			ObjectId commitID = repository.resolve(objectId);
			RevCommit commit = walk.parseCommit(commitID);
			RevTree tree = walk.parseTree(commit.getTree().getId());

			CanonicalTreeParser treeParser = new CanonicalTreeParser();
			try (ObjectReader reader = repository.newObjectReader()) {
				treeParser.reset(reader, tree.getId());
			}

			walk.dispose();

			return treeParser;
		}
	}
}
