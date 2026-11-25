auto-phrase-tokenfilter
=======================
**Lucene Auto Phrase TokenFilter implementation**

Performs "auto phrasing" on a token stream. Auto phrases refer to sequences of tokens that
are meant to describe a single thing and should be searched for as such. When these phrases
are detected in the token stream, a single token representing the phrase is emitted rather than
the individual tokens that make up the phrase. The filter supports overlapping phrases.

The Autophrasing filter can be combined with a synonym filter to handle cases in which prefix or
suffix terms in a phrase are synonymous with the phrase, but where other parts of the phrase are
not. This enables searching within the phrase to occur selectively, rather than randomly.

This project is a fork of [lucidworks/auto-phrase-tokenfilter](https://github.com/lucidworks/auto-phrase-tokenfilter)
(Apache License 2.0), with a Solr factory, a Solr query parser and an elasticsearch plugin.

# Overview

Search engines work by 'inverse' mapping terms or 'tokens' to the documents that contain
them. Sometimes a single token uniquely describes a real-world entity or thing but in many
other cases multiple tokens are required.  The problem that this presents is that the same
tokens may be used in multiple entity descriptions - a many-to-many problem. When users
search for a specific concept or 'thing' they are often confused by the results because of
this type of ambiguity - search engines return documents that contain the words but not
necessarily the 'things' they are looking for. Doing a better job of mapping tokens (the 'units'
of a search index) to specific things or concepts will help to address this problem.

# Version
<table>
 <tr><th>Version (tag)</th><th>Lucene version</th><th>Solr version</th><th>elasticsearch version</th><tr>
 <tr><td>solr-10.0.0_es-9.3.1</td><td>10.3.2</td><td>10.0.0</td><td>9.3.1</td></tr>
</table>

The token filter is compiled once for Solr and elasticsearch : their Lucene versions must be compatible.
Previous versions are listed in RELEASES.md.

# Build
Java 21 and Maven 3.9 :

<pre>
mvn package
</pre>

The build produces :
 - lucene-auto-phrase-tokenfilter/target/lucene-auto-phrase-tokenfilter-0.0.1-SNAPSHOT.jar : the Lucene token filter
 - solr-auto-phrase-tokenfilter/target/solr-auto-phrase-tokenfilter-0.0.1-SNAPSHOT.jar : the Solr factory and query parser
 - es-auto-phrase-tokenfilter/target/releases/es-auto-phrase-tokenfilter-0.0.1-SNAPSHOT.zip : the elasticsearch plugin

The elasticsearch plugin is a "classic" plugin : it must be rebuilt for each elasticsearch version
(property elasticsearch.version in pom.xml).

# Algorithm
The auto phrase token filter uses a list of phrases that should be kept together as single 
tokens. As tokens are received by the filter, it keeps a partial phrase that matches 
the beginning of one or more phrases in this list.  It will keep appending tokens to this 
‘match’ phrase as long as at least one phrase in the list continues to match the newly 
appended tokens. If the match breaks before any phrase completes, the filter will replay 
the now unmatched tokens that it has collected. If a phrase match completes, that phrase 
will be emitted to the next filter in the chain.  If a token does not match any of the 
leading terms in its phrase list, it will be passed on to the next filter unmolested.

# Solr
## Input Parameters:
<table>
 <tr><td>phrases</td><td>file containing auto phrases (one per line)</td><tr>
 <tr><td>includeTokens</td><td>true|false(default) - if true adds single tokens to output</td></tr>
 <tr><td>replaceWhitespaceWith</td><td>single character to use to replace whitespace in phrase</td></tr>
</table>

## Installation
Copy lucene-auto-phrase-tokenfilter-0.0.1-SNAPSHOT.jar and solr-auto-phrase-tokenfilter-0.0.1-SNAPSHOT.jar
in the lib directory of the Solr installation (or of the Solr home) on every node, then restart Solr.
The phrases file (autophrases.txt) is part of the configset.

## Example schema Configuration
<pre>
&lt;fieldType name="text_autophrase" class="solr.TextField" positionIncrementGap="100">
  &lt;analyzer type="index">
    &lt;tokenizer name="standard"/>
    &lt;filter name="stop" ignoreCase="true" words="stopwords.txt" />
    &lt;filter name="lowercase"/>
    &lt;filter class="com.lucidworks.analysis.autophrasing.AutoPhrasingTokenFilterFactory" phrases="autophrases.txt" includeTokens="true" />
    &lt;filter name="porterStem"/>
  &lt;/analyzer>
  &lt;analyzer type="query">
    &lt;tokenizer name="standard"/>
    &lt;filter name="stop" ignoreCase="true" words="stopwords.txt" />
    &lt;filter name="lowercase"/>
    &lt;filter name="porterStem"/>
  &lt;/analyzer>
&lt;/fieldType>
</pre>

## Query Parser Plugin
Note : the standard and edismax query parsers no longer split the query on whitespace before analysis by default (sow=false, checked with Solr 10.0).
With Solr 10, the AutoPhrasingTokenFilterFactory can be used in the query analyzer, and the query parser plugin below is no longer needed.

Due to an issue with Lucene/Solr query parsing, the AutoPhrasingTokenFilter is not effective at query time as
part of a standard analyzer chain. This is due to the LUCENE-2605 issue in which the query parser sends each token
to the Analyzer individually and it thus cannot "see" across whitespace boundries. To redress this problem, a wrapper
QParserPlugin is incuded (AutoPhrasingQParserPlugin) that first isolates query syntax (in place), auto phrases and then 
restores the query syntax (+/- operators) so that it functions as originally intended. The auto-phrased portions are
protected from the query parser by replacing whitespace within them with another character ('_'). 

To use it in a SearchHandler, add a queryParser section to solrconfig.xml:

<pre>
&lt;queryParser name="autophrasingParser" class="com.lucidworks.analysis.autophrasing.AutoPhrasingQParserPlugin" >
      &lt;str name="phrases">autophrases.txt&lt/str>
&lt;/queryParser> 
</pre>

And a new search handler that uses the query parser:

<pre>
&lt;requestHandler name="/autophrase" class="solr.SearchHandler">
 &lt;lst name="defaults">
   &lt;str name="echoParams">explicit&lt;/str>
   &lt;int name="rows">10&lt;/int>
   &lt;str name="df">text&lt;/str>
   &lt;str name="defType">autophrasingParser&lt;/str>
 &lt;/lst>
&lt;/requestHandler>
</pre>

## Example Test Code:
The following Java code can be used to show what the AutoPhrasingTokenFilter does:

<pre>
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

...

public void testAutoPhrase( ) throws Exception {
    // sets up a list of phrases - Normally this would be supplied by AutoPhrasingTokenFilterFactory
    final CharArraySet phraseSets = new CharArraySet(Arrays.asList(
        "income tax", "tax refund", "property tax"
        ), false);
    	 
    final String input = "what is my income tax refund this year now that my property tax is so high";
    WhitespaceTokenizer wt = new WhitespaceTokenizer();
    wt.setReader(new StringReader(input));
    AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter( wt, phraseSets, false );
    CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
    aptf.reset();

    boolean hasToken = false;
    do {
      hasToken = aptf.incrementToken( );
      if (hasToken) System.out.println( "token:'" + term.toString( ) + "'" );
    } while (hasToken);
}
</pre>

This produces the following output:

<pre>
token:'what'
token:'is'
token:'my'
token:'income tax'
token:'tax refund'
token:'this'
token:'year'
token:'now'
token:'that'
token:'my'
token:'property tax'
token:'is'
token:'so'
token:'high'
</pre>

# elasticsearch
## Installation
On every node, then restart elasticsearch :

<pre>
bin/elasticsearch-plugin install file:///path/to/es-auto-phrase-tokenfilter-0.0.1-SNAPSHOT.zip
</pre>

## Input Parameters:
<table>
 <tr><td>phrases_path</td><td>file containing auto phrases (one per line), relative to the elasticsearch config directory</td><tr>
 <tr><td>phrases</td><td>inline list of auto phrases (instead of phrases_path)</td><tr>
 <tr><td>ignoreCase</td><td>true|false(default)</td></tr>
 <tr><td>includeTokens</td><td>true|false(default) - if true adds single tokens to output</td></tr>
 <tr><td>replaceWhitespaceWith</td><td>single character to use to replace whitespace in phrase</td></tr>
</table>

## Example index setting
Security (HTTPS and authentication) is enabled by default since elasticsearch 8.

<pre>
curl -k -u elastic -X PUT 'https://localhost:9200/test_autophrasing' -H 'Content-Type: application/json' -d '
{
  "settings": {
    "analysis": {
      "analyzer": {
        "autophrasing_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "autophrasing"]
        }
      },
      "filter": {
        "autophrasing": {
          "type": "autophrasing",
          "phrases_path": "autophrases.txt",
          "replaceWhitespaceWith": "_"
        }
      }
    }
  }
}'
</pre>

## Analyzer test
With config/autophrases.txt file containing :

<pre>
new york
new york city
san francisco
</pre>

<pre>
curl -k -u elastic 'https://localhost:9200/test_autophrasing/_analyze' -H 'Content-Type: application/json' -d '
{
  "analyzer": "autophrasing_analyzer",
  "text": "Flights from San Francisco to New York City"
}'
</pre>

Will produce the tokens : flights, from, san_francisco, to, new_york_city.
