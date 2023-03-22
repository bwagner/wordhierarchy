# wordhierarchy


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

    (?:Eu(?:er|ch|re(?:r)?))

The output of the command line program is the input partitioned into common parts of words.
If a part of a word does not complete a word, a ` - ` is appended (above: `Eu -`). If a part
of a word does indeed complete a word, no ` - ` is appended (above: `er`, `ch`, `re`, `r`).

The last line of the output is a Java regexp that matches the set of words. It is built by
the included `RegexWordProcessor`, which can easily be adapted to other regexp dialects
(e.g. [perl](http://perldoc.perl.org/perlfaq6.html), etc.).

This example shows the command line use which is merely intended for demonstration purposes.
It's mainly to be used is as a library.

# Todo

- works best for words with common substrings from the left.
  Could be improved to work with substrings *anywhere*.

- look at [Trie](http://en.wikipedia.org/wiki/Trie)

- simplify using ideas from this [post](http://stackoverflow.com/a/7433899/642750)

- improve when there's a row of single characters: Instead of e.g. `(?:3|8|1|6|4)` it could generate `[38164]`.

- improve command line: offer options to generate different regexp dialects.

# Build

```
mvn -B package
```

# Release

```
mvn release:prepare
```

Perform a release without deploying. The deploy step is done in a gh action.

```
mvn release:perform -Darguments="-Dmaven.deploy.skip=true"
```

# Authors

**Bernhard Wagner**

+ http://github.com/bwagner
+ http://xlmizer.net

# License

Copyright 2011 Bernhard Wagner.

Licensed under GNU Lesser General Public License as published by the Free Software Foundation,
either [version 3](http://www.gnu.org/licenses/gpl-3.0.html) of the License, or (at your option) any later version.
