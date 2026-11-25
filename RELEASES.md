Releases Notes
==============

The git history was restarted in November 2026 (branch main) : the previous versions below are no longer available as tags.

### solr-4.10.3 - 18 May 2015
 - Fork from https://github.com/lucidworks/auto-phrase-tokenfilter

### solr-6.2.1 - 9 January 2017
- IMP - Lucene / Solr 6.2.1 compliant
- IMP - Maven compliant project
- FIX - Potential memory leaks

### solr-6.2.1_es-5.0.1 - 9 January 2017
- IMP - elasticsearch 5.0.1 compliant

### solr-6.2.1_es-5.0.2 - 9 January 2017
- IMP - elasticsearch 5.0.2 compliant

### solr-6.3.0_es-5.0.2 - 9 January 2017
- IMP - lucene/solr 6.3.0 compliant
- IMP - elasticsearch 5.1.1 compliant

### solr-8.11.1 - 6 February 2025
- IMP - lucene/solr 8.11.1 compliant
- FIX - Memory leak (phraseMap is immutable after initialization)
- elasticsearch module removed

### solr-10.0.0_es-9.3.1 - November 2026
- IMP - lucene 10.3.2 / solr 10.0.0 compliant (Java 21)
- IMP - elasticsearch 9.3.1 compliant (classic plugin), elasticsearch module restored
- IMP - elasticsearch : inline phrases list (phrases) in addition to phrases_path
- FIX - debug output (System.out) removed from the token filter

### solr-10.0.0_es-9.3.1_stable - November 2026
- IMP - elasticsearch stable plugin (stable plugin API 9.3.1), module es-stable-auto-phrase-tokenfilter
- IMP - Gradle build of the stable plugin (Elastic plugin elasticsearch.stable-esplugin, Gradle wrapper 9.3.1), in addition to the Maven build
