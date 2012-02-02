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
	 * Creates a new child and adds it to this word and returns the new child.
	 * 
	 * @param theWord
	 *            the word to initialize the new child from
	 * @param theComplete
	 *            whether the new child is a complete word
	 * @return the new child
	 */
	Word addChild(final String theWord, final boolean theComplete) {
		final Word newChild = new Word(theWord, theComplete);
		addChild(newChild);
		return newChild;
	}

	/**
	 * Adds new child to this node and returns the new child.
	 * 
	 * @param theWord
	 *            the word to initialize the new child from
	 * @return the new child
	 */
	Word addChild(final String theWord) {
		final Word newChild = new Word(theWord);
		addChild(newChild);
		return newChild;
	}

	/**
	 * Adds child to this node and returns it.
	 * 
	 * @param theWord
	 *            the word to initialize the new child from
	 * @return the new child
	 */
	Word addChild(final Word theWord) {
		children.add(theWord);
		theWord.setParent(this);
		return theWord;
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
		return myToString();
	}

	/**
	 * Processes this word and its children.
	 * 
	 * Note: The order of processing may vary from call to call!
	 * If you need reproducible order, use
	 * {@link Word#processAllSorted(WordProcessor)}
	 * 
	 * @param wp
	 *            the WordProcessor to process this word and its children.
	 */
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

	/**
	 * Processes this word and its children.
	 * 
	 * Note: The order of processing is reproducible from call to call.
	 * If you don't need reproducible order, use
	 * {@link Word#processAll(WordProcessor)}
	 * 
	 * @param wp
	 *            the WordProcessor to process this word and its children.
	 */
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

	/**
	 * Returns collection as a sorted list.
	 * see {@linkplain http://stackoverflow.com/a/740351/642750}
	 * 
	 * @param <T>
	 *            parameterization of collection to sort
	 * @param c
	 *            collection to sort
	 * @return set as a sorted list
	 */
	public static <T extends Comparable<? super T>> List<T> asSortedList(
			Collection<T> c) {
		final List<T> list = new ArrayList<T>(c);
		Collections.sort(list);
		return list;
	}

	/**
	 * Stringifies this word. Optionally, each word gets its own id.
	 * 
	 * Note: The order of the children may vary from call to call!
	 * If you need reproducible order, use
	 * {@link Word#myToStringSorted(boolean)}
	 * 
	 * @param withId
	 *            Each word gets its own id displayed
	 * @return this word stringified.
	 */
	public String myToString(boolean withId) {
		final StringifyWordProcessor wp = new StringifyWordProcessor(withId);
		processAll(wp);
		return wp.getResult();
	}

	/**
	 * Stringifies this word. Optionally, each word gets its own id.
	 * 
	 * Note: The order of the children is reproducible from call to call.
	 * If you don't need reproducible order, use
	 * {@link Word#myToString(boolean)}
	 * 
	 * @param withId
	 *            Each word gets its own id displayed
	 * @return this word stringified.
	 */
	public String myToStringSorted(boolean withId) {
		final StringifyWordProcessor wp = new StringifyWordProcessor(withId);
		processAllSorted(wp);
		return wp.getResult();
	}

	/**
	 * Stringifies this word.
	 * 
	 * Note: The order of the children may vary from call to call!
	 * If you need reproducible order, use {@link Word#myToStringSorted()}
	 * 
	 * @return this word stringified.
	 */
	public String myToString() {
		return myToString(false);
	}

	/**
	 * Stringifies this word.
	 * 
	 * Note: The order of the children is reproducible from call to call.
	 * If you don't need reproducible order, use {@link Word#myToString()}
	 * 
	 * @return this word stringified.
	 */
	public String myToStringSorted() {
		return myToStringSorted(false);
	}

	/**
	 * Runs a TestWordProcessor on this word.
	 * 
	 * @param vocabulary
	 * @return
	 */
	public boolean testIt(final Collection<String> vocabulary) {
		final TestWordProcessor twp = new TestWordProcessor(vocabulary);
		processAll(twp);
		return twp.resultOk();
	}

	void addAll(Collection<Word> words) {
		for (final Word child : words) {
			addChild(child); // handles child.word == this.word
		}
	}

	/**
	 * Returns id unique to this word.
	 * 
	 * @return id unique to this word
	 */
	public int getId() {
		return id;
	}

	/**
	 * Generates a regex matching all words in this tree.
	 * Note: for the same input the ordering in the regex may change!
	 * If you need reproducible ordering (mainly for testing),
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
	 * Generates a reproducible regex matching all words in this tree.
	 * If you don't need reproducible ordering (mainly for testing),
	 * use see {@link Word#toRegex()} instead.
	 * 
	 * @return a reproducible regex matching all words in this tree
	 */
	public String toRegexSorted() {
		final RegexWordProcessor wp = new RegexWordProcessor();
		processAllSorted(wp);
		return wp.getResult();
	}

	public void setComplete(boolean theComplete) {
		complete = theComplete;
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