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

#Overview

Search engines work by 'inverse' mapping terms or 'tokens' to the documents that contain
them. Sometimes a single token uniquely describes a real-world entity or thing but in many
other cases multiple tokens are required.  The problem that this presents is that the same
tokens may be used in multiple entity descriptions - a many-to-many problem. When users
search for a specific concept or 'thing' they are often confused by the results because of
this type of ambiguity - search engines return documents that contain the words but not
necessarily the 'things' they are looking for. Doing a better job of mapping tokens (the 'units'
of a search index) to specific things or concepts will help to address this problem.

#Version
<table>
 <tr><th>Version (tag)</th><th>Lucene / Solr version</th><th>elasticsearch version</th><tr>
 <tr><td>solr-4.10.3</td><td>solr-4.10.3</td><td>not supported</td></tr>
 <tr><td>solr-6.2.1</td><td>solr-6.2.1</td><td>not supported</td></tr>
 <tr><td>solr-6.2.1_es-5.0.1</td><td>solr-6.2.1</td><td>5.0.1</td></tr>
 <tr><td>solr-6.2.1_es-5.0.2</td><td>solr-6.2.1</td><td>5.0.2</td></tr>
 <tr><td>solr-6.3.0_es-5.1.1</td><td>solr-6.2.1</td><td>5.1.1</td></tr>
</table>

#Algorithm
The auto phrase token filter uses a list of phrases that should be kept together as single 
tokens. As tokens are received by the filter, it keeps a partial phrase that matches 
the beginning of one or more phrases in this list.  It will keep appending tokens to this 
‘match’ phrase as long as at least one phrase in the list continues to match the newly 
appended tokens. If the match breaks before any phrase completes, the filter will replay 
the now unmatched tokens that it has collected. If a phrase match completes, that phrase 
will be emitted to the next filter in the chain.  If a token does not match any of the 
leading terms in its phrase list, it will be passed on to the next filter unmolested.

#Solr
##Input Parameters:
<table>
 <tr><td>phrases</td><td>file containing auto phrases (one per line)</td><tr>
 <tr><td>includeTokens</td><td>true|false(default) - if true adds single tokens to output</td></tr>
 <tr><td>replaceWhitespaceWith</td><td>single character to use to replace whitespace in phrase</td></tr>
</table>

##Example schema.xml Configuration
<pre>
&lt;fieldType name="text_autophrase" class="solr.TextField" positionIncrementGap="100">
  &lt;analyzer type="index">
    &lt;tokenizer class="solr.StandardTokenizerFactory"/>
    &lt;filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" enablePositionIncrements="true" />
    &lt;filter class="solr.LowerCaseFilterFactory"/>
    &lt;filter class="com.lucidworks.analysis.AutoPhrasingTokenFilterFactory" phrases="autophrases.txt" includeTokens="true" />
    &lt;filter class="solr.PorterStemFilterFactory"/>
  &lt;/analyzer>
  &lt;analyzer type="query">
    &lt;tokenizer class="solr.StandardTokenizerFactory"/>
    &lt;filter class="solr.StopFilterFactory" ignoreCase="true" words="stopwords.txt" enablePositionIncrements="true" />
    &lt;filter class="solr.LowerCaseFilterFactory"/>
    &lt;filter class="solr.PorterStemFilterFactory"/>
  &lt;/analyzer>
&lt;/fieldType>
</pre>

##Query Parser Plugin
Due to an issue with Lucene/Solr query parsing, the AutoPhrasingTokenFilter is not effective at query time as
part of a standard analyzer chain. This is due to the LUCENE-2605 issue in which the query parser sends each token
to the Analyzer individually and it thus cannot "see" across whitespace boundries. To redress this problem, a wrapper
QParserPlugin is incuded (AutoPhrasingQParserPlugin) that first isolates query syntax (in place), auto phrases and then 
restores the query syntax (+/- operators) so that it functions as originally intended. The auto-phrased portions are
protected from the query parser by replacing whitespace within them with another character ('_'). 

To use it in a SearchHandler, add a queryParser section to solrconfig.xml:

<pre>
&lt;queryParser name="autophrasingParser" class="com.lucidworks.analysis.AutoPhrasingQParserPlugin" >
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

##Example Test Code:
The following Java code can be used to show what the AutoPhrasingTokenFilter does:

<pre>
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.util.CharArraySet;

...

public void testAutoPhrase( ) throws Exception {
    // sets up a list of phrases - Normally this would be supplied by AutoPhrasingTokenFilterFactory
    final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
        "income tax", "tax refund", "property tax"
        ), false);
    	 
    final String input = "what is my income tax refund this year now that my property tax is so high";
    WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
    AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter( TEST_VERSION_CURRENT, wt, phraseSets, false );
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

#elasticsearch
##Input Parameters:
<table>
 <tr><td>phrases_path</td><td>file containing auto phrases (one per line)</td><tr>
 <tr><td>includeTokens</td><td>true|false(default) - if true adds single tokens to output</td></tr>
 <tr><td>replaceWhitespaceWith</td><td>single character to use to replace whitespace in phrase</td></tr>
</table>

##Example index setting
<pre>
curl -X PUT 'localhost:9200/test_autophrasing' -d '
{
    "settings" : {
    "number_of_shards" : 1,
    "number_of_replicas" : 1,
    "analysis": {
      "analyzer": {
        "my_autophrasing_analyzer": {
          "type": "custom",
          "tokenizer": "my_standard",
          "filter": ["autophrasing", "my_stop"]
        }
      },
      "tokenizer": {
        "my_standard": { 
          "type": "standard",
          "max_token_length": 5
        }
      },
      "filter": {
        "autophrasing" : {
          "type": "autophrasing",
          "phrases_path":"dictionary.txt"
        },
        "my_stop": {
          "type":       "stop",
          "stopwords": ["and", "is", "the"]
        }
      }
    }
  }
}'
</pre>

##Analyzer test
With config/dictionary.txt file containing :

<pre>
new york
new-york city
new york city
</pre>

<pre>
curl -X GET 'localhost:9200/test_autophrasing/_analyze' -d '
{
    "analyzer": "my_autophrasing_analyzer",
    "text": "san paulo and new york"
}'
</pre>

Will produce:

<pre>
{
  "tokens": [
    {
      "token": "san",
      "start_offset": 0,
      "end_offset": 3,
      "type": "<ALPHANUM>",
      "position": 0
    },
    {
      "token": "paulo",
      "start_offset": 4,
      "end_offset": 9,
      "type": "<ALPHANUM>",
      "position": 2
    },
    {
      "token": "new york",
      "start_offset": 0,
      "end_offset": 0,
      "type": "word",
      "position": 9
    }
  ]
}
</pre>



