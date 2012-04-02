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
import java.util.Set;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.gosen.tokenAttributes.PartOfSpeechAttribute;

/**
 * Removes tokens that do NOT match a set of POS tags.
 * <p>
 * This is the inverse of {@link GosenPartOfSpeechStopFilter}.
 */
public final class GosenPartOfSpeechKeepFilter extends FilteringTokenFilter {
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((keepTags == null) ? 0 : keepTags.hashCode());
    result = prime * result + ((posAtt == null) ? 0 : posAtt.hashCode());
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
    GosenPartOfSpeechKeepFilter other = (GosenPartOfSpeechKeepFilter) obj;
    if (keepTags == null) {
      if (other.keepTags != null)
        return false;
    } else if (!keepTags.equals(other.keepTags))
      return false;
    if (posAtt == null) {
      if (other.posAtt != null)
        return false;
    } else if (!posAtt.equals(other.posAtt))
      return false;
    return true;
  }

  private final Set<String> keepTags;
  private final PartOfSpeechAttribute posAtt = addAttribute(PartOfSpeechAttribute.class);

  public GosenPartOfSpeechKeepFilter(boolean enablePositionIncrements, TokenStream input, Set<String> keepTags) {
    super(enablePositionIncrements, input);
    this.keepTags = keepTags;
  }

  @Override
  protected boolean accept() throws IOException {
    final String pos = posAtt.getPartOfSpeech();
    return pos != null && keepTags.contains(pos);
  }
}
