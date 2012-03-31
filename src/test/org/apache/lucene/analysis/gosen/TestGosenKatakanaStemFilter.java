package org.apache.lucene.analysis.gosen;

import java.io.IOException;
import java.io.Reader;

import net.java.sen.SenTestUtil;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.gosen.GosenKatakanaStemFilter;
import org.apache.lucene.analysis.gosen.GosenTokenizer;

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

public class TestGosenKatakanaStemFilter extends BaseTokenStreamTestCase {
  private Analyzer analyzer = new ReusableAnalyzerBase() {
    @Override
    protected TokenStreamComponents createComponents(String field, Reader reader) {
      Tokenizer tokenizer = new GosenTokenizer(reader, null, SenTestUtil.IPADIC_DIR);
      TokenStream stream = new GosenKatakanaStemFilter(tokenizer);
      return new TokenStreamComponents(tokenizer, stream);
    }
  };
  
  public void testBasics() throws IOException {
    assertAnalyzesTo(analyzer, "スパゲッティー",
        new String[] { "スパゲッティ" }
    );
  }
  
  public void testRandomData() throws IOException {
    checkRandomData(random, analyzer, 10000);
  }
}
