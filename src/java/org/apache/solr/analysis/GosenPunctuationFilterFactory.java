package org.apache.solr.analysis;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.gosen.GosenPunctuationFilter;

/**
 * Factory for {@link GosenPunctuationFilter}.
 * <pre class="prettyprint" >
 * &lt;fieldType name="text_ja" class="solr.TextField"&gt;
 *   &lt;analyzer&gt;
 *     &lt;tokenizer class="solr.GosenJapaneseTokenizerFactory"/&gt;
 *     &lt;filter class="solr.GosenPunctuationFilterFactory" 
 *             enablePositionIncrements="true"/&gt;
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 */
public class GosenPunctuationFilterFactory extends BaseTokenFilterFactory {
  private boolean enablePositionIncrements;

  public void init(Map<String,String> args) {
    super.init(args);
    enablePositionIncrements = getBoolean("enablePositionIncrements", false);
  }

  public TokenStream create(TokenStream stream) {
    return new GosenPunctuationFilter(enablePositionIncrements, stream);
  }
}
