package com.lucidworks.analysis.autophrasing;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilterFactory;
import org.apache.lucene.util.ResourceLoader;
import org.apache.lucene.util.ResourceLoaderAware;

import com.lucidworks.analysis.autophrasing.AutoPhrasingTokenFilter;

public class AutoPhrasingTokenFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {

	/** SPI name */
	public static final String NAME = "autoPhrasing";

	private CharArraySet phraseSets;
	private final String phraseSetFiles;
	private final boolean ignoreCase;
	private final boolean emitSingleTokens;

	private String replaceWhitespaceWith = null;

	public AutoPhrasingTokenFilterFactory(Map<String, String> initArgs) {
		super( initArgs );
		phraseSetFiles = get(initArgs, "phrases");
		ignoreCase = getBoolean( initArgs, "ignoreCase", false);
		emitSingleTokens = getBoolean( initArgs, "includeTokens", false );

		String replaceWhitespaceArg = initArgs.get( "replaceWhitespaceWith" );
		if (replaceWhitespaceArg != null) {
			replaceWhitespaceWith = replaceWhitespaceArg;
		}
	}

	/** Default ctor for compatibility with SPI */
	public AutoPhrasingTokenFilterFactory() {
		throw defaultCtorException();
	}

	@Override
	public void inform(ResourceLoader loader) throws IOException {
		if (phraseSetFiles != null) {
			phraseSets = getWordSet(loader, phraseSetFiles, ignoreCase);
		}
	}


	@Override
	public TokenStream create( TokenStream input ) {
		AutoPhrasingTokenFilter autoPhraseFilter = new AutoPhrasingTokenFilter( input, phraseSets, emitSingleTokens );
		if (replaceWhitespaceWith != null) {
			autoPhraseFilter.setReplaceWhitespaceWith( replaceWhitespaceWith.charAt( 0 ) );
		}
		return autoPhraseFilter;
	}

}
