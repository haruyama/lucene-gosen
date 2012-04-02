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
import java.io.Reader;

import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.StreamFilter;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.gosen.tokenAttributes.BasicFormAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.ConjugationAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.CostAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.PartOfSpeechAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.PronunciationsAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.ReadingsAttribute;
import org.apache.lucene.analysis.gosen.tokenAttributes.SentenceStartAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * This is a Japanese tokenizer which uses "Sen" morphological
 * analyzer.
 * <p>
 * sets the surface form as the term text, but also sets these attributes:
 * <ul>
 *   <li>{@link BasicFormAttribute}
 *   <li>{@link ConjugationAttribute}
 *   <li>{@link PartOfSpeechAttribute}
 *   <li>{@link PronunciationsAttribute}
 *   <li>{@link ReadingsAttribute}
 *   <li>{@link CostAttribute}
 *   <li>{@link SentenceStartAttribute}
 * </ul>
 */
public final class GosenTokenizer extends Tokenizer {
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + accumulatedCost;
    result = prime * result
        + ((basicFormAtt == null) ? 0 : basicFormAtt.hashCode());
    result = prime * result
        + ((conjugationAtt == null) ? 0 : conjugationAtt.hashCode());
    result = prime * result + ((costAtt == null) ? 0 : costAtt.hashCode());
    result = prime * result + ((offsetAtt == null) ? 0 : offsetAtt.hashCode());
    result = prime * result
        + ((partOfSpeechAtt == null) ? 0 : partOfSpeechAtt.hashCode());
    result = prime * result
        + ((pronunciationsAtt == null) ? 0 : pronunciationsAtt.hashCode());
    result = prime * result
        + ((readingsAtt == null) ? 0 : readingsAtt.hashCode());
    result = prime * result
        + ((sentenceAtt == null) ? 0 : sentenceAtt.hashCode());
    result = prime * result + ((tagger == null) ? 0 : tagger.hashCode());
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
    GosenTokenizer other = (GosenTokenizer) obj;
    if (accumulatedCost != other.accumulatedCost)
      return false;
    if (basicFormAtt == null) {
      if (other.basicFormAtt != null)
        return false;
    } else if (!basicFormAtt.equals(other.basicFormAtt))
      return false;
    if (conjugationAtt == null) {
      if (other.conjugationAtt != null)
        return false;
    } else if (!conjugationAtt.equals(other.conjugationAtt))
      return false;
    if (costAtt == null) {
      if (other.costAtt != null)
        return false;
    } else if (!costAtt.equals(other.costAtt))
      return false;
    if (offsetAtt == null) {
      if (other.offsetAtt != null)
        return false;
    } else if (!offsetAtt.equals(other.offsetAtt))
      return false;
    if (partOfSpeechAtt == null) {
      if (other.partOfSpeechAtt != null)
        return false;
    } else if (!partOfSpeechAtt.equals(other.partOfSpeechAtt))
      return false;
    if (pronunciationsAtt == null) {
      if (other.pronunciationsAtt != null)
        return false;
    } else if (!pronunciationsAtt.equals(other.pronunciationsAtt))
      return false;
    if (readingsAtt == null) {
      if (other.readingsAtt != null)
        return false;
    } else if (!readingsAtt.equals(other.readingsAtt))
      return false;
    if (sentenceAtt == null) {
      if (other.sentenceAtt != null)
        return false;
    } else if (!sentenceAtt.equals(other.sentenceAtt))
      return false;
    if (tagger == null) {
      if (other.tagger != null)
        return false;
    } else if (!tagger.equals(other.tagger))
      return false;
    if (termAtt == null) {
      if (other.termAtt != null)
        return false;
    } else if (!termAtt.equals(other.termAtt))
      return false;
    return true;
  }

  private final StreamTagger2 tagger;
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  
  // morphological attributes
  private final BasicFormAttribute basicFormAtt = addAttribute(BasicFormAttribute.class);
  private final ConjugationAttribute conjugationAtt = addAttribute(ConjugationAttribute.class);
  private final PartOfSpeechAttribute partOfSpeechAtt = addAttribute(PartOfSpeechAttribute.class);
  private final PronunciationsAttribute pronunciationsAtt = addAttribute(PronunciationsAttribute.class);
  private final ReadingsAttribute readingsAtt = addAttribute(ReadingsAttribute.class);
  
  // sentence increment
  private final SentenceStartAttribute sentenceAtt = addAttribute(SentenceStartAttribute.class);

  // viterbi cost
  private final CostAttribute costAtt = addAttribute(CostAttribute.class);
  // viterbi costs from Token.getCost() are cumulative,
  // so we accumulate this so we can then subtract to present an absolute cost.
  private int accumulatedCost = 0;

  public GosenTokenizer(Reader in) {
    this(in, null, null);
  }

  public GosenTokenizer(Reader in, StreamFilter filter) {
    this(in, null, null);
  }
  
  public GosenTokenizer(Reader in, StreamFilter filter, String dictionaryDir) {
    super(in);
    StringTagger stringTagger = SenFactory.getStringTagger(dictionaryDir);
    if(filter != null)
      stringTagger.addFilter(filter);
    tagger = new StreamTagger2(stringTagger, in);
  }

  @Override
  public boolean incrementToken() throws IOException {
    Token token = tagger.next();
    if (token == null) {
      return false;
    } else {
      clearAttributes();
      final Morpheme m = token.getMorpheme();
    
      // note, unlike the previous implementation, we set the surface form
      termAtt.setEmpty().append(token.getSurface());
      final int cost = token.getCost();
      
      if (token.isSentenceStart()) {
        accumulatedCost = 0;
        sentenceAtt.setSentenceStart(true);
      }
      
      costAtt.setCost(cost - accumulatedCost);
      accumulatedCost = cost;
      basicFormAtt.setMorpheme(m);
      conjugationAtt.setMorpheme(m);
      partOfSpeechAtt.setMorpheme(m);
      pronunciationsAtt.setMorpheme(m);
      readingsAtt.setMorpheme(m);
      offsetAtt.setOffset(correctOffset(token.getStart()), correctOffset(token.end()));
      return true;
    }
  }

  @Override
  public void reset(Reader in) throws IOException {
    super.reset(in);
    tagger.reset(in);
    accumulatedCost = 0;
  }

  @Override
  public void end() throws IOException {
    // set final offset
    final int finalOffset = correctOffset(tagger.end());
    offsetAtt.setOffset(finalOffset, finalOffset);
  }
}
