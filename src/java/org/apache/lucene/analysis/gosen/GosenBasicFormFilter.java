package org.apache.lucene.analysis.gosen;

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

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.gosen.tokenAttributes.BasicFormAttribute;
import org.apache.lucene.analysis.KeywordMarkerFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

/**
 * Replaces term text with the {@link BasicFormAttribute}.
 * <p>
 * This acts as a lemmatizer for verbs and adjectives.
 * </p>
 * <p>
 * To prevent terms from being stemmed use an instance of
 * {@link KeywordMarkerFilter} or a custom {@link TokenFilter} that sets
 * the {@link KeywordAttribute} before this {@link TokenStream}.
 * </p>
 */
public final class GosenBasicFormFilter extends TokenFilter {
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final BasicFormAttribute basicFormAtt = addAttribute(BasicFormAttribute.class);
  private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

  public GosenBasicFormFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (!keywordAtt.isKeyword()) {
        String basicForm = basicFormAtt.getBasicForm();
        if (basicForm != null && !basicForm.equals("*")) termAtt.setEmpty().append(basicFormAtt.getBasicForm());
      }
      return true;
    } else {
      return false;
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((basicFormAtt == null) ? 0 : basicFormAtt.hashCode());
    result = prime * result
        + ((keywordAtt == null) ? 0 : keywordAtt.hashCode());
    result = prime * result + ((termAtt == null) ? 0 : termAtt.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    GosenBasicFormFilter other = (GosenBasicFormFilter) obj;
    if (basicFormAtt == null) {
      if (other.basicFormAtt != null)
        return false;
    } else if (!basicFormAtt.equals(other.basicFormAtt))
      return false;
    if (keywordAtt == null) {
      if (other.keywordAtt != null)
        return false;
    } else if (!keywordAtt.equals(other.keywordAtt))
      return false;
    if (termAtt == null) {
      if (other.termAtt != null)
        return false;
    } else if (!termAtt.equals(other.termAtt))
      return false;
    return true;
  }
}
