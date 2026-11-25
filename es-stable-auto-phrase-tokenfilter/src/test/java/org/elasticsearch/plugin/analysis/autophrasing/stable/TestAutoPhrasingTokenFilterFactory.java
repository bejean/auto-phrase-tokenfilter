package org.elasticsearch.plugin.analysis.autophrasing.stable;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.lucidworks.analysis.autophrasing.AutoPhrasingTokenFilter;

public class TestAutoPhrasingTokenFilterFactory extends TestCase {

	private static AutoPhrasingSettings settings(List<String> phrases) {
		return new AutoPhrasingSettings() {
			public List<String> phrases() { return phrases; }
			public boolean ignoreCase() { return false; }
			public boolean includeTokens() { return false; }
			public String replaceWhitespaceWith() { return "_"; }
		};
	}

	public void testPhrases() throws Exception {
		AutoPhrasingTokenFilterFactory factory = new AutoPhrasingTokenFilterFactory(settings(Arrays.asList("new york", "new york city")));

		WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();
		tokenizer.setReader(new StringReader("i love new york city"));
		TokenStream stream = factory.create(tokenizer);
		assertTrue(stream instanceof AutoPhrasingTokenFilter);

		CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
		List<String> tokens = new ArrayList<String>();
		stream.reset();
		while (stream.incrementToken()) {
			tokens.add(term.toString());
		}
		stream.end();
		stream.close();
		assertEquals(Arrays.asList("i", "love", "new_york_city"), tokens);
	}

	public void testNoPhrases() throws Exception {
		AutoPhrasingTokenFilterFactory factory = new AutoPhrasingTokenFilterFactory(settings(Collections.<String>emptyList()));

		WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();
		tokenizer.setReader(new StringReader("i love new york"));
		TokenStream stream = factory.create(tokenizer);
		CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
		List<String> tokens = new ArrayList<String>();
		stream.reset();
		while (stream.incrementToken()) {
			tokens.add(term.toString());
		}
		stream.end();
		stream.close();
		assertEquals(Arrays.asList("i", "love", "new", "york"), tokens);
	}

}
