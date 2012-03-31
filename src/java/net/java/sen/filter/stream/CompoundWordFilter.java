/*
 * Copyright (C) 2004-2007
 * Tsuyoshi Fukui <fukui556@oki.com>
 * Matt Francis <asbel@neosheffield.co.uk>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package net.java.sen.filter.stream;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import net.java.sen.dictionary.Morpheme;
import net.java.sen.dictionary.Sentence;
import net.java.sen.dictionary.Token;
import net.java.sen.filter.StreamFilter;

/**
 * A Filter that replaces a single <code>Token</code> with one or more
 * alternative <code>Token</code>s. Can be used, for instance, to split
 * compound verbs or nouns into their constituent parts
 */
public class CompoundWordFilter implements StreamFilter {
  /**
   * Table of tokens to match and their replacements
   */
  private HashMap<String,String> compoundTable;
  
  /**
   * Finds the index of the next field starting at, or after, the given
   * position in a CSV string
   *
   * @param termInfo The CSV string
   * @param position The starting position
   * @return The position of the next field, or -1 if there are no more fields
   */
  private int getFieldStart(String termInfo, int position) {
    if (position == 0) {
      return 0;
    }
    int cnt = 0;
    int ptr = 0;
    
    while (cnt < position && ptr < termInfo.length()) {
      if (termInfo.charAt(ptr++) == ',') cnt++;
    }
    
    if (cnt != position) return -1;
    
    return ptr++;
  }
  
  /**
   * Gets a field from a CSV string starting at a given position
   *
   * @param termInfo The CSV string
   * @param position The starting position
   * @return The field
   */
  private String getField(String termInfo, int position) {
    int start = getFieldStart(termInfo, position);
    int end = getFieldStart(termInfo, position + 1);
    if (end == -1 || end == termInfo.length()) {
      end = termInfo.length();
    } else {
      end--;
    }
    return termInfo.substring(start, end);
  }
  
  public void preProcess(Sentence sentence) {
    // Do nothing
  }
  
  public List<Token> postProcess(List<Token> tokens) {
    if (tokens.size() == 0) {
      return tokens;
    }
    
    List<Token> newTokens = new ArrayList<Token>();
    for (int i = 0; i < tokens.size(); i++) {
      
      Token token = tokens.get(i);
      
      String compoundInfo = compoundTable.get(token.getMorpheme().toString());
      if (compoundInfo == null) {
        newTokens.add(token);
        continue;
      }
      
      StringTokenizer st = new StringTokenizer(compoundInfo);
      int start = token.getStart();
      while (st.hasMoreTokens()) {
        
        String termInfo = st.nextToken();
        Token newToken = new Token();
        String surface = getField(termInfo, 0);
        newToken.setSurface(surface);
        
        StringBuffer partOfSpeech = new StringBuffer(getField(termInfo, 2));
        
        String tmp = getField(termInfo, 3);
        if (!tmp.equals("*")) {
          partOfSpeech.append("-").append(tmp);
        }
        
        tmp = getField(termInfo, 4);
        if (!tmp.equals("*")) {
          partOfSpeech.append("-").append(tmp);
        }
        
        tmp = getField(termInfo, 5);
        if (!tmp.equals("*")) {
          partOfSpeech.append("-").append(tmp);
        }
        
        newToken.setCost(token.getCost());
        
        final String additionalInformation;
        if (getField(termInfo, 11).equals("-")) {
          additionalInformation = "p=" + token.getMorpheme().getPartOfSpeech();
        } else {
          additionalInformation = getField(termInfo, 11);
        }
        Morpheme newMorpheme = new Morpheme(partOfSpeech.toString(),
            getField(termInfo, 6),
            getField(termInfo, 7),
            getField(termInfo, 8),
            new String[] { getField(termInfo, 9) },
            new String[] { getField(termInfo, 10) },
            additionalInformation);
        newToken.setMorpheme(newMorpheme);        
        newToken.setLength(surface.length());
        newToken.setStart(start);
        start += surface.length();
        
        newTokens.add(newToken);
      }
    }
    
    return newTokens;
  }
  
  /**
   * Creates a CompoundWordFilter from the given file
   * 
   * @param compoundFile The compiled compound file
   */
  /* Unchecked warnings are suppressed because there is no type safe way to
   * read a parameterised type from an ObjectInputStream
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public CompoundWordFilter(String compoundFile) {
    try {
      ObjectInputStream is = new ObjectInputStream(new FileInputStream(compoundFile));
      compoundTable = (HashMap) is.readObject();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
