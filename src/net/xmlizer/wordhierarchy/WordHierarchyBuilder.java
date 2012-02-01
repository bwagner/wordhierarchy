package net.xmlizer.wordhierarchy;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
	* Copyright (C) 2010 Swiss Library for the Blind, Visually Impaired and Print Disabled
	*
	* This file is part of dtbook-preptools.
	* 	
	* dtbook-preptools is free software: you can redistribute it
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

	public static class Word {
		private static int instanceCount;
		private final int id;
		private final String word;
		private final Set<Word> children = new HashSet<Word>();
		private boolean complete;
		private Word parent;

		public Word getParent() {
			return parent;
		}

		public void setParent(Word theParent) {
			parent = theParent;
		}

		Word(final String theWord) {
			word = theWord;
			id = instanceCount++;
		}

		Word(final String theWord, final boolean theComplete) {
			this(theWord);
			// since there seems to be a word matching this stem
			// this stem is a complete word! (if it wasn't already)
			setComplete(theComplete);
		}

		public Word() {
			this(null);
		}

		/**
		 * Adds new child to this node and returns the new child.
		 * If theWord equals this.word ignores theWord and returns this.
		 * 
		 * @param theWord
		 * @return
		 */
		Word addChild(final String theWord, final boolean theComplete) {
			if (theWord.equals(getWord())) {
				// since there seems to be a word matching this stem
				// this stem is a complete word!
				setComplete(true);
				return this;
			}
			final Word newChild = new Word(theWord, theComplete);
			addChild(newChild);
			return newChild;
		}

		/**
		 * Adds new child to this node and returns the new child.
		 * If theWord equals this.word ignores theWord and returns this.
		 * 
		 * @param theWord
		 * @return
		 */
		Word addChild(final String theWord) {
			if (theWord.equals(getWord())) {
				// since there seems to be a word matching this stem
				// this stem is a complete word! (if it wasn't already)
				setComplete(true);
				return this;
			}
			final Word newChild = new Word(theWord);
			addChild(newChild);
			return newChild;
		}

		/**
		 * Adds child to this node and returns it.
		 * If theWord.word equals this.word drops theWord, adds its
		 * children to this and returns this.
		 * 
		 * @param theWord
		 * @return
		 */
		Word addChild(final Word theWord) {
			if (theWord.getWord().equals(getWord())) {
				// since there seems to be a word matching this stem
				// this stem is a complete word! (if it wasn't already)
				setComplete(true);
				addAll(theWord.getChildren());
				return this;
			}
			else {
				children.add(theWord);
				theWord.setParent(this);
				return theWord;
			}
		}

		void removeChild(final Word theWord) {
			if (!children.contains(theWord)) {
				throw new RuntimeException("removeChild:" + getWord()
						+ " does not contain " + theWord);
			}
			children.remove(theWord);
			theWord.setParent(null);
		}

		public Set<Word> getChildren() {
			return children;
		}

		@Override
		public String toString() {
			return getWord();
		}

		public void processAll(final WordProcessor wp) {
			wp.processWord(this);
			if (!getChildren().isEmpty()) {
				wp.preChildren(this);
				for (final Word child : getChildren()) {
					child.processAll(wp);
				}
				wp.postChildren(this);
			}
		}

		public String myToString(boolean withId) {
			final StringifyWordProcessor wp = new StringifyWordProcessor(withId);
			processAll(wp);
			return wp.getResult();
		}

		public boolean testIt(final Collection<String> vocabulary,
				final Word result) {
			TestWordProcessor twp = new TestWordProcessor(vocabulary);
			processAll(twp);
			return twp.resultOk();
		}

		private void addAll(Collection<Word> words) {
			for (final Word child : words) {
				addChild(child); // handles child.word == this.word
			}
		}

		public int getId() {
			return id;
		}

		public String toRegex() {
			final RegexWordProcessor wp = new RegexWordProcessor();
			processAll(wp);
			return wp.getResult();
		}

		public void setComplete(boolean complete) {
			this.complete = complete;
		}

		public boolean isComplete() {
			return complete;
		}

		public String getWord() {
			return word;
		}
	}

	private static void addWordTree(final Word root, final String theWord,
			boolean theComplete) {
		boolean subtreeFound = false;
		final Iterator<Word> childrenIterator = root.children.iterator();
		while (!subtreeFound && childrenIterator.hasNext()) {
			int k = 0;
			final Word child = childrenIterator.next();
			while (k < child.getWord().length() && k < theWord.length()
					&& child.getWord().charAt(k) == theWord.charAt(k)) {
				++k;
			}

			// k == child.word.length || k == theWord.length ||
			// child[k] != word[k]

			if (k == child.getWord().length()) {
				subtreeFound = true;
				if (k == theWord.length()) {
					child.setComplete(true);
				}
				else {
					addWordTree(child, theWord.substring(k), theComplete);
				}
			}
			else if (k == theWord.length()) {
				subtreeFound = true;
				final Word parent = child.getParent();
				parent.removeChild(child);
				final Word newChild = parent.addChild(theWord, theComplete);
				final Word remainder = newChild.addChild(child.getWord()
						.substring(k), child.isComplete());
				remainder.addAll(child.getChildren());
			}
			else if (k > 0) {
				subtreeFound = true;
				// split the current child c into two by splitting the word at k
				// obtaining new children child1 and child2
				final String beginningOfOldWord = child.getWord().substring(0,
						k);
				final String endingOfOldWord = child.getWord().substring(k);
				final String endingOfNewWord = theWord.substring(k);
				final Word parent = child.getParent();
				// remove child from its parent.
				parent.removeChild(child);
				// add child1 as a child of parent of child
				final Word child1 = parent.addChild(beginningOfOldWord, false);
				// add child2 as a child of child1
				final Word child2 = child1.addChild(endingOfOldWord,
						child.isComplete());
				// add all children of child to child2.
				child2.addAll(child.children);
				// add new substring to child1
				child1.addChild(endingOfNewWord, theComplete);
			}
		}
		if (!subtreeFound) {
			root.addChild(new Word(theWord, true));
		}
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
				out.write("result ok?:" + result.testIt(vocabulary, result));
				out.write("\n");
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
		String next = vocabularyIter.next();
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
