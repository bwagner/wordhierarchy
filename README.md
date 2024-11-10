wordhierarchy
=============

This project provides a word hierarchy builder.
It builds a tree out of a set of words which can then be navigated by a `WordProcessor` to generate e.g.
Regexps that match any of the words in the given set.

Example:

    java -jar dist/wordhierarchy.jar Euch Euer Eure Eurer
     Eu -
      er
      ch
      re
       r

    Eu(?:er|ch|rer?)

The output of the command line program is the input partitioned into common parts of words.
If a part of a word does not complete a word, a ` - ` is appended (above: `Eu -`). If a part
of a word does indeed complete a word, no ` - ` is appended (above: `er`, `ch`, `re`, `r`).

The last line of the output is a Java regexp that matches the set of words. It is built by
the included `RegexWordProcessor`, which can easily be adapted to other regexp dialects
(e.g. [Python](https://docs.python.org/3/library/re.html),
[JavaScript](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_expressions),
[Perl](http://perldoc.perl.org/perlfaq6.html), etc.).

This example shows the command line interface which is merely intended for demonstration purposes,
as its mainly to be used is as a library.

Build
-------
ant jar

Todo
-------

- works best for words with common substrings from the left.
  Could be improved to work with substrings *anywhere*.

- look at [Trie](http://en.wikipedia.org/wiki/Trie)

- simplify using ideas from this [post](http://stackoverflow.com/a/7433899/642750)

- improve command line: offer options to generate different regexp dialects.

External Dependendencies
-------

- https://github.com/bwagner/interval-tree as forked from https://github.com/dyoo/interval-tree
- https://github.com/bwagner/permutation (for tests only)

Authors
-------

**Bernhard Wagner**

+ http://github.com/bwagner
+ http://xlmizer.net

License
-------

Copyright 2011 Bernhard Wagner.

Licensed under GNU Lesser General Public License as published by the Free Software Foundation,
either [version 3](http://www.gnu.org/licenses/gpl-3.0.html) of the License, or (at your option) any later version.
