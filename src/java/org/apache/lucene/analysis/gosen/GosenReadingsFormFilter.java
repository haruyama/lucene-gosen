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
import java.util.List;

import org.apache.lucene.analysis.KeywordMarkerFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.gosen.tokenAttributes.ReadingsAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

/**
 * Replaces term text with the {@link ReadingsAttribute}.
 * <p>
 * To prevent terms from being replaced use an instance of
 * {@link KeywordMarkerFilter} or a custom {@link TokenFilter} that sets
 * the {@link KeywordAttribute} before this {@link TokenStream}.
 * </p>
 */
public final class GosenReadingsFormFilter extends TokenFilter {
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result
        + ((keywordAtt == null) ? 0 : keywordAtt.hashCode());
    result = prime * result
        + ((readingsAtt == null) ? 0 : readingsAtt.hashCode());
    result = prime * result + (romanized ? 1231 : 1237);
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
    GosenReadingsFormFilter other = (GosenReadingsFormFilter) obj;
    if (keywordAtt == null) {
      if (other.keywordAtt != null)
        return false;
    } else if (!keywordAtt.equals(other.keywordAtt))
      return false;
    if (readingsAtt == null) {
      if (other.readingsAtt != null)
        return false;
    } else if (!readingsAtt.equals(other.readingsAtt))
      return false;
    if (romanized != other.romanized)
      return false;
    if (termAtt == null) {
      if (other.termAtt != null)
        return false;
    } else if (!termAtt.equals(other.termAtt))
      return false;
    return true;
  }

  private boolean romanized;
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final ReadingsAttribute readingsAtt = addAttribute(ReadingsAttribute.class);
  private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

  public GosenReadingsFormFilter(TokenStream input) {
    this(input, false);
  }

  public GosenReadingsFormFilter(TokenStream input, boolean romanized) {
    super(input);
    this.romanized = romanized;
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (!keywordAtt.isKeyword()) {
        List<String> readings = readingsAtt.getReadings();
        if (readings != null){ 
          StringBuilder sb = new StringBuilder();
          for(String reading : readings){
            sb.append(romanized ? ToStringUtil.getRomanization(reading) : reading);
          }
          termAtt.setEmpty().append(sb.toString());
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
