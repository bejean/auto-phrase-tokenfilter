package org.elasticsearch.plugin.analysis.autophrasing;

import java.util.Map;
import static java.util.Collections.singletonMap;

import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.autophrasing.AutoPhrasingTokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

public class AnalysisAutoPhrasingPlugin extends Plugin implements AnalysisPlugin {
    @Override
    public Map<String, AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        // the filter needs its settings (phrases or phrases_path): no instance without index settings
        return singletonMap("autophrasing", AnalysisPlugin.requiresAnalysisSettings(AutoPhrasingTokenFilterFactory::new));
    }
}
