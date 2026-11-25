package org.elasticsearch.plugin.analysis.autophrasing.stable;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.plugin.Inject;
import org.elasticsearch.plugin.NamedComponent;
import org.elasticsearch.plugin.analysis.TokenFilterFactory;

import com.lucidworks.analysis.autophrasing.AutoPhrasingTokenFilter;

@NamedComponent("autophrasing_stable")
public class AutoPhrasingTokenFilterFactory implements TokenFilterFactory {

	private final CharArraySet phraseSets;
	private final boolean emitSingleTokens;
	private final String replaceWhitespaceWith;

	@Inject
	public AutoPhrasingTokenFilterFactory(AutoPhrasingSettings settings) {
		// elasticsearch also creates an instance without any settings: no phrases, the filter then leaves the tokens unchanged
		this.phraseSets = new CharArraySet(settings.phrases(), settings.ignoreCase());
		this.emitSingleTokens = settings.includeTokens();
		this.replaceWhitespaceWith = settings.replaceWhitespaceWith();
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		AutoPhrasingTokenFilter autoPhraseFilter = new AutoPhrasingTokenFilter(tokenStream, phraseSets, emitSingleTokens);
		if (!replaceWhitespaceWith.isEmpty()) {
			autoPhraseFilter.setReplaceWhitespaceWith(replaceWhitespaceWith.charAt(0));
		}
		return autoPhraseFilter;
	}
}
