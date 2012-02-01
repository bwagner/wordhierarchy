package net.xmlizer.wordhierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class Word implements Comparable<Word> {
	private static int instanceCount;
	private final int id;
	private final String word;
	final Set<Word> children = new HashSet<Word>();
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

	public void processAllSorted(final WordProcessor wp) {
		wp.processWord(this);
		if (!getChildren().isEmpty()) {
			wp.preChildren(this);
			for (final Word child : asSortedList(getChildren())) {
				child.processAllSorted(wp);
			}
			wp.postChildren(this);
		}
	}

	public static <T extends Comparable<? super T>> List<T> asSortedList(
			Collection<T> c) {
		final List<T> list = new ArrayList<T>(c);
		Collections.sort(list);
		return list;
	}

	public String myToString(boolean withId) {
		final StringifyWordProcessor wp = new StringifyWordProcessor(withId);
		processAll(wp);
		return wp.getResult();
	}

	public boolean testIt(final Collection<String> vocabulary, final Word result) {
		TestWordProcessor twp = new TestWordProcessor(vocabulary);
		processAll(twp);
		return twp.resultOk();
	}

	void addAll(Collection<Word> words) {
		for (final Word child : words) {
			addChild(child); // handles child.word == this.word
		}
	}

	public int getId() {
		return id;
	}

	/**
	 * Generates a regex matching all words in this tree.
	 * Note: for the same input the ordering in the regex may change!
	 * If you need predictable ordering (mainly for testing),
	 * use see {@link Word#toRegexSorted()} instead.
	 * 
	 * @return a non-reproducible regex matching all words in this tree
	 */
	public String toRegex() {
		final RegexWordProcessor wp = new RegexWordProcessor();
		processAll(wp);
		return wp.getResult();
	}

	/**
	 * Generates a reproducable regex matching all words in this tree.
	 * If you don't need predictable ordering (mainly for testing),
	 * use see {@link Word#toRegex()} instead.
	 * 
	 * @return a reproducible regex matching all words in this tree
	 */
	public String toRegexSorted() {
		final RegexWordProcessor wp = new RegexWordProcessor();
		processAllSorted(wp);
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

	@Override
	public int compareTo(final Word o) {
		return word.compareTo(o.word);
	}
}