package org.elasticsearch.index.analysis.autophrasing;

import java.util.List;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.analysis.Analysis;

import com.lucidworks.analysis.autophrasing.AutoPhrasingTokenFilter;

public class AutoPhrasingTokenFilterFactory extends AbstractTokenFilterFactory {

	private final CharArraySet phraseSets;
	private final boolean emitSingleTokens;
	private final String replaceWhitespaceWith;

	public AutoPhrasingTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
		super(name);

		boolean ignoreCase = settings.getAsBoolean("ignoreCase", false);
		this.emitSingleTokens = settings.getAsBoolean("includeTokens", false);
		this.replaceWhitespaceWith = settings.get("replaceWhitespaceWith");

		// phrases_path: file relative to the config directory, or phrases: inline list
		List<String> phrases = Analysis.getWordList(env, settings, "phrases");
		if (phrases == null) {
			throw new IllegalArgumentException("autophrasing requires [phrases] or [phrases_path] to be configured");
		}
		this.phraseSets = new CharArraySet(phrases, ignoreCase);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		AutoPhrasingTokenFilter autoPhraseFilter = new AutoPhrasingTokenFilter(tokenStream, phraseSets, emitSingleTokens);
		if (replaceWhitespaceWith != null) {
			autoPhraseFilter.setReplaceWhitespaceWith(replaceWhitespaceWith.charAt(0));
		}
		return autoPhraseFilter;
	}
}
