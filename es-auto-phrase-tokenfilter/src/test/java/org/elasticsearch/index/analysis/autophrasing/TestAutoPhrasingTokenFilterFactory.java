package org.elasticsearch.index.analysis.autophrasing;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.common.settings.Settings;

import com.lucidworks.analysis.autophrasing.AutoPhrasingTokenFilter;

public class TestAutoPhrasingTokenFilterFactory extends TestCase {

	public void testInlinePhrases() throws Exception {
		Settings settings = Settings.builder()
				.putList("phrases", "new york", "new york city")
				.put("replaceWhitespaceWith", "_")
				.build();
		// the environment is only needed to read a phrases_path file
		AutoPhrasingTokenFilterFactory factory = new AutoPhrasingTokenFilterFactory(null, null, "autophrasing", settings);

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

	public void testMissingPhrases() {
		try {
			new AutoPhrasingTokenFilterFactory(null, null, "autophrasing", Settings.EMPTY);
			fail();
		} catch (IllegalArgumentException expected) {
			// OK
		}
	}

}
