package net.xmlizer.wordhierarchy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Word {
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