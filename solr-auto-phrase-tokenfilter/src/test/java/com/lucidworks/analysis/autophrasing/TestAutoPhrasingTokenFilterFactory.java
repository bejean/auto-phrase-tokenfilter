package com.lucidworks.analysis.autophrasing;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.ClasspathResourceLoader;

public class TestAutoPhrasingTokenFilterFactory extends TestCase {

	public void testAutoPhrasingTokenFilterFactory() throws Exception {
		Map<String, String> args = new HashMap<String, String>();
		args.put("phrases", "dictionary.txt");
		args.put("ignoreCase", "true");
		args.put("replaceWhitespaceWith", "_");
		AutoPhrasingTokenFilterFactory f = new AutoPhrasingTokenFilterFactory(args);
		f.inform(new ClasspathResourceLoader(getClass().getClassLoader()));

		WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();
		tokenizer.setReader(new StringReader("i love new york city"));
		TokenStream stream = f.create(tokenizer);
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

}
