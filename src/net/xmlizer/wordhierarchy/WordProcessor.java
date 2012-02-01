package net.xmlizer.wordhierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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

interface WordProcessor {
	boolean processWord(final Word word);

	void preChildren(final Word word);

	void postChildren(final Word word);
}

class TestWordProcessor implements WordProcessor {
	private final Collection<String> bkup;
	private final Collection<String> ref = new HashSet<String>();

	public TestWordProcessor(final Collection<String> vocabulary) {
		bkup = new HashSet<String>();
		bkup.addAll(vocabulary);
	}

	@Override
	public boolean processWord(final Word word) {
		String realword = word.getWord();
		if (word.getWord() != null && word.isComplete()) {
			Word parent = word;
			while ((parent = parent.getParent()) != null) {
				if (parent.getWord() != null) {
					realword = parent.getWord() + realword;
				}
			}
			if (!bkup.contains(realword)) {
				System.err.println(realword + " not in vocabulary");
			}
			else {
				// System.out.println(realword + " in vocabulary");
				ref.add(realword);
			}
		}
		return true;
	}

	@Override
	public void preChildren(final Word word) {
		// nothing to do
	}

	@Override
	public void postChildren(final Word word) {
		// nothing to do
	}

	public boolean resultOk() {
		bkup.removeAll(ref);
		if (!bkup.isEmpty()) {
			System.err.println("bkup not empty! We still have:");
			for (final String str : bkup) {
				System.err.println(str);
			}

		}
		return bkup.isEmpty();
	}
}

class StringifyWordProcessor implements WordProcessor {

	public StringifyWordProcessor() {

	}

	public StringifyWordProcessor(boolean theWithId) {
		withId = theWithId;
	}

	private boolean withId;

	final StringBuilder sb = new StringBuilder();

	private static int indent;

	private static String makeIndent() {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; ++i) {
			sb.append(" ");
		}
		return sb.toString();
	}

	@Override
	public boolean processWord(final Word word) {
		final boolean DEBUG = false;
		if (word.getWord() == null)
			return false;
		sb.append(makeIndent());
		if (DEBUG) {
			sb.append(indent);
			sb.append(": '");
		}
		sb.append(word.getWord());
		sb.append(" ");
		if (withId) {
			sb.append(word.getId());
			sb.append(" ");
		}
		sb.append(word.isComplete() ? "" : "-");
		if (DEBUG) {
			sb.append("'");
		}
		sb.append("\n");
		return false;
	}

	@Override
	public void preChildren(final Word word) {
		indent++;
	}

	@Override
	public void postChildren(final Word word) {
		indent--;
	}

	public String getResult() {
		return sb.toString();
	}
}

class RegexWordProcessor implements WordProcessor {

	private final StringBuilder sb = new StringBuilder();

	@Override
	public boolean processWord(final Word word) {
		if (word.getWord() == null)
			return false;
		sb.append(word.getWord());
		sb.append(word.getChildren().isEmpty() ? "|" : "");
		return false;
	}

	@Override
	public void preChildren(final Word word) {
		// sb.setLength(sb.length() - 1); // chop off "|"

		sb.append(addParenthesis(word) ? "(?:" : "");
	}

	@Override
	public void postChildren(final Word word) {
		sb.setLength(sb.length() - 1); // chop off "|"
		sb.append(addParenthesis(word) ? ")" : "");
		sb.append(word.isComplete() ? "?" : "");
		sb.append("|");
	}

	/**
	 * Checks whether a parenthesis is required around the children of this
	 * word.
	 * Append parenthesis only, if:
	 * - word has more than one child or
	 * - word has one child c and ( c has children or c.word.length > 1 )
	 * 
	 * But: A precondition for preChildren to be called is that there is at
	 * least 1 child, so:
	 * Append parenthesis only, if:
	 * - word has more than one child or
	 * - the one child c has children or c.word.length > 1
	 * 
	 * @param word
	 * @return true of children of this word should be put in parenthesis.
	 */
	private static boolean addParenthesis(final Word word) {
		if (word.getWord() == null)
			return false;
		final List<Word> children = new ArrayList<Word>(word.getChildren());
		return (children.size() > 1 || !children.get(0).getChildren().isEmpty() || children
				.get(0).getWord().length() > 1);
	}

	public String getResult() {
		sb.setLength(sb.length() - 1); // chop off "|"
		return sb.toString();
	}
}
