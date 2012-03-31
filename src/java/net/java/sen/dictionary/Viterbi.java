/*
 * Copyright (C) 2002-2007
 * Taku Kudoh <taku-ku@is.aist-nara.ac.jp>
 * Takashi Okamoto <tora@debian.org>
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

package net.java.sen.dictionary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of the Viterbi algorithm used to find the most likely
 * sequence of morphemes comprising a sentence
 * 
 * <p><b>Thread Safety</b>: Objects of this class are <b>NOT</b> thread safe and
 * should not be accessed simultaneously by multiple threads. Note that creating
 * additional instances using {@link net.java.sen.SenFactory} is relatively
 * cheap in both memory and time
 */
public class Viterbi {
  
  /**
   * The Tokenizer used to decompose the sentence into prospective morphemes
   */
  private final Tokenizer tokenizer;
  
  /**
   * The beginning-of-string Node
   */
  private Node bosNode;
  
  /**
   * The end-of-string Node
   */
  private Node eosNode;
  
  /**
   * An array of linked lists of possible morphemes ending at a given position
   */
  private Node[] endNodeList;
  
  /**
   * Calculates the best connection for each of a linked list of Nodes
   *
   * @param position The shared starting position of the linked list of Nodes 
   * @param limit One greater than the last index of the sentence
   * @param rNode The head of the linked list of Nodes
   * @param sentence The original sentence
   */
  final private void calculateConnectionCosts(int position, int limit, Node rNode, Sentence sentence)throws IOException  {
    for (; rNode != null; rNode = rNode.rnext) {
      int bestCost = Integer.MAX_VALUE;
      Node bestNode = null;
      
      for (Node lNode = endNodeList[position]; lNode != null; lNode = lNode.lnext) {
        int cost = lNode.cost + tokenizer.getDictionary().getCost(lNode.prev, lNode, rNode);
        if (cost <= bestCost) {
          bestNode = lNode;
          bestCost = cost;
        }
      }
      
      rNode.prev = bestNode;
      rNode.cost = bestCost;
      int x = position + rNode.span;
      
      rNode.lnext = endNodeList[x];
      endNodeList[x] = rNode;
      
      if (rNode.rcAttr2 != 0) {
        SentenceIterator it = sentence.iterator();
        int pos2 = rNode.span + position;
        if(pos2 == limit){
          continue;
        }
        //winding to pos2
        int pos3 = 0;
        for(int i=0;i<=pos2;i++){
          if(it.hasNextOrigin()){
            pos3 = it.nextOrigin();
          }
        }
        // safety check
        if(pos2 != pos3){
          continue;
        }
        Node rNode2 = lookup(it, sentence.getCharacters(), sentence.getReadingConstraint(pos2));
        for (; rNode2 != null; rNode2 = rNode2.rnext) {
          rNode2 = rNode2.clone();
          rNode2.cost = rNode.cost + tokenizer.getDictionary().getCost(rNode.prev, rNode, rNode2);
          rNode2.prev = rNode;
          
          int y = pos2 + rNode2.span;
          
          rNode2.lnext = endNodeList[y];
          endNodeList[y] = rNode2;
        }
      }
      

    }
  }
  
  /**
   * Looks up potential Nodes from the current origin of the given
   * SentenceIterator
   *
   * @param iterator The iterator to search from
   * @param surface The underlying character array
   * @param constraint The reading constraint to apply
   * @return The head of a list of <code>Node</code>s linked through
   *         <code>Node.rnext</code> 
   * @throws IOException
   */
  private Node lookup(SentenceIterator iterator, char[] surface, Reading constraint) throws IOException {
    Node resultNode = tokenizer.lookup(iterator, surface);
    
    if (constraint == null) {
      return resultNode;
    }
    
    Node filteredResultNode = null;
    Node lastNode = null;
    for (Node node = resultNode; node != null; node = node.rnext) {
      if ((node.length == constraint.length) && (node.morpheme.getReadings().contains(constraint.text))) {
        if (filteredResultNode == null) {
          filteredResultNode = node;
        } else {
          lastNode.rnext = node;
        }
        lastNode = node;
      }
    }
    
    if (lastNode != null) {
      lastNode.rnext = null;
      return filteredResultNode;
    }
    
    // Synthesize Node
    Node unknownNode = tokenizer.getUnknownNode(surface, iterator.origin(), constraint.length, constraint.length + iterator.skippedCharCount());
    Morpheme unknownMorpheme = new Morpheme(unknownNode.morpheme.getPartOfSpeech(), null, null, "*", 
        new String[] { constraint.text }, new String[0], unknownNode.morpheme.getAdditionalInformation());
    unknownNode.morpheme = unknownMorpheme;
    return unknownNode;
  }
  
  /**
   * Gets the possible tokens from a Sentence at a given position. Any reading
   * constraints on the Sentence are ignored
   *
   * @param sentence The Sentence to search within
   * @param position The position to search at
   * @return A list of possible Tokens
   * @throws IOException
   */
  public List<Token> getPossibleTokens(Sentence sentence, int position) throws IOException {
    Node resultNode = tokenizer.lookup(sentence.unconstrainedIterator(position), sentence.getCharacters());
    
    String sentenceString = new String(sentence.getCharacters());
    List<Token> tokenList = new ArrayList<Token>();
    while (resultNode != null) {
      Token token = new Token(sentenceString, resultNode);
      tokenList.add(token);
      resultNode = resultNode.rnext;
    }
    
    return tokenList;
  }
  
  /**
   * Analyses a sentence to find the most likely sequence of morphemes
   *
   * @param sentence The sentence to analyse
   * @return The most likely list of morphemes
   * @throws IOException
   */
  public List<Token> getBestTokens(Sentence sentence, List<Token> reuse) throws IOException {
    SentenceIterator iterator = sentence.iterator();
    int length = iterator.length();
    char[] surface = sentence.getCharacters();
    
    // Initialize the Viterbi lattice
    bosNode = tokenizer.getBOSNode();
    eosNode = tokenizer.getEOSNode();
    endNodeList = new Node[length + 1];
    endNodeList[0] = bosNode;
    endNodeList[length] = null;
    
    // Look up potential morphemes at each position in the sentence, and
    // join them to the lattice
    while (iterator.hasNextOrigin()) {
      int position = iterator.nextOrigin();
      int base = position - iterator.skippedCharCount();
      if (endNodeList[base] != null) {
        Node rNode = lookup(iterator, surface, sentence.getReadingConstraint(position));
        if (rNode != null) {
          calculateConnectionCosts(base, length, rNode, sentence);
        }
      }
    }
    
    // Find the most likely connection from the last position where a
    // morpheme ended to the end-of-string. If there were no morphemes, this
    // will connect to the beginning-of-string node.
    for (int position = length; position >= 0; position--) {
      if (endNodeList[position] != null) {
        calculateConnectionCosts(position, length, eosNode, sentence);
        // Once we have connected the end-of-string node, leave the loop. 
        break;
      }
    }
    
    // Working backwards from the end-of-string Node, make the forward
    // connections along the most likely path
    Node node = eosNode;
    for (Node prevNode; node.prev != null;) {
      prevNode = node.prev;
      prevNode.next = node;
      node = prevNode;
    }
    
    // Convert to Token list
    String sentenceString = new String(sentence.getCharacters());
    List<Token> tokenList = reuse;
    tokenList.clear();
    node = bosNode.next;
    while ((node != null) && (node.next != null)) {
      Token token = new Token(sentenceString, node);
      tokenList.add(token);
      node = node.next;
    }
    
    return tokenList;
  }
  
  /** @deprecated use {@link #getBestTokens(Sentence, List)} instead */
  @Deprecated
  public List<Token> getBestTokens(Sentence sentence) throws IOException {
    return getBestTokens(sentence, new ArrayList<Token>());
  }
  
  /**
   * Creates a Viterbi instance using the given Tokenizer
   * 
   * @param tokenizer The Tokenizer to use
   */
  public Viterbi(Tokenizer tokenizer) {
    this.tokenizer = tokenizer;
  }
}
