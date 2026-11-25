package org.elasticsearch.plugin.analysis.autophrasing.stable;

import java.util.List;

import org.elasticsearch.plugin.settings.AnalysisSettings;
import org.elasticsearch.plugin.settings.BooleanSetting;
import org.elasticsearch.plugin.settings.ListSetting;
import org.elasticsearch.plugin.settings.StringSetting;

/**
 * Settings of the token filter, read by elasticsearch from the index settings.
 */
@AnalysisSettings
public interface AutoPhrasingSettings {

	@ListSetting(path = "phrases")
	List<String> phrases();

	@BooleanSetting(path = "ignoreCase", defaultValue = false)
	boolean ignoreCase();

	@BooleanSetting(path = "includeTokens", defaultValue = false)
	boolean includeTokens();

	@StringSetting(path = "replaceWhitespaceWith", defaultValue = "")
	String replaceWhitespaceWith();
}
