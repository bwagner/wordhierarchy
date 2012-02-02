package net.xmlizer.wordhierarchy;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Copyright (C) 2010 Bernhard Wagner
 * 
 * This file is part of wordhierarchy.
 * 
 * wordhierarchy is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

public class WordHierarchyBuilder {

	private static void addWordTree(final Word root, final String theNewWord,
			boolean theComplete) {
		boolean subtreeFound = false;
		final Iterator<Word> childrenIterator = root.getChildren().iterator();
		while (!subtreeFound && childrenIterator.hasNext()) {
			final Word oldChild = childrenIterator.next();
			int k = findCommonSubstring(oldChild, theNewWord);

			// k == child.word.length || k == theWord.length ||
			// child[k] != word[k]

			if (k == oldChild.getWord().length()) {
				subtreeFound = true;
				if (k == theNewWord.length()) {
					oldChild.setComplete(true);
				}
				else {
					addWordTree(oldChild, theNewWord.substring(k), theComplete);
				}
			}
			else if (k == theNewWord.length()) {
				subtreeFound = true;
				final Word parent = oldChild.getParent();
				parent.removeChild(oldChild);
				final Word newChild = parent.addChild(theNewWord, theComplete);
				final Word remainder = newChild.addChild(oldChild.getWord()
						.substring(k), oldChild.isComplete());
				remainder.addAll(oldChild.getChildren());
			}
			else if (k > 0) {
				subtreeFound = true;
				// split the current child c into two by splitting the word at k
				// obtaining new children child1 and child2
				final String commonHeadSubstring = oldChild.getWord()
						.substring(0, k);
				final String tailOfOldWord = oldChild.getWord().substring(k);
				final String tailOfNewWord = theNewWord.substring(k);
				final Word parent = oldChild.getParent();
				// remove oldChild from its parent.
				parent.removeChild(oldChild);
				// add commonHeadChild as a child of parent of oldChild
				final Word commonHeadChild = parent.addChild(
						commonHeadSubstring, false);
				// add tailOfOldChild as a child of commonHeadChild
				final Word tailOfOldChild = commonHeadChild.addChild(
						tailOfOldWord, oldChild.isComplete());
				// add all children of oldChild to tailOfOldChild.
				tailOfOldChild.addAll(oldChild.getChildren());
				// add new substring to commonHeadChild
				commonHeadChild.addChild(tailOfNewWord, theComplete);
			}
		}
		if (!subtreeFound) {
			root.addChild(new Word(theNewWord, true));
		}
	}

	/**
	 * Returns index up to which given theWord and theString have common
	 * substrings starting from the beginning (index 0).
	 * 
	 * @param theWord
	 * @param theString
	 * @return
	 */
	private static int findCommonSubstring(final Word theWord,
			final String theString) {
		int k = 0;
		while (k < theWord.getWord().length() && k < theString.length()
				&& theWord.getWord().charAt(k) == theString.charAt(k)) {
			++k;
		}
		return k;
	}

	public static Word createWordTree(final String[] vocabulary,
			final Writer out) {
		return createWordTree(Arrays.asList(vocabulary), out);
	}

	public static Word createWordTree(final String[] vocabulary) {
		return createWordTree(vocabulary, null);
	}

	/**
	 * @param vocabulary
	 * @param out
	 *            A writer to write diagnostic output to. If null, no output
	 *            will be generated.
	 * @return
	 */
	public static Word createWordTree(final Collection<String> vocabulary,
			Writer out) {
		final Word result = createWordTree(vocabulary.iterator());
		if (out != null) {
			try {
				out.write("result ok?:" + result.testIt(vocabulary));
				out.write("\n");
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static Word createWordTree(final Collection<String> vocabulary) {
		return createWordTree(vocabulary, null);
	}

	public static Word createWordTree(final Iterator<String> vocabularyIter) {
		final Word root = new Word();
		final String next = vocabularyIter.next();
		// System.out.println("adding " + next);
		root.addChild(next, true);
		while (vocabularyIter.hasNext()) {
			final String word = vocabularyIter.next();
			// System.out.println("adding " + word);
			addWordTree(root, word, true);
			// System.out.println(root.myToString());
		}
		return root;
	}

	public static void main(final String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: pass a list of strings as arguments");
			System.exit(1);
		}
		final Word word = createWordTree(args);
		System.out.println(word.myToString(false));
		System.out.println(word.toRegex());
		System.out.println();
	}
}
