/*
 * Copyright (C) 2006-2007
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

/**
 * A class representing a reading applied to a set of characters within a
 * sentence
 */
public class Reading {
  
  /** The starting point within the sentence */
  public final int start;
  
  /** The number of characters of the sentence covered by the reading */
  public final int length;
  
  /** The reading text applied to the covered span */
  public final String text;
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + length;
    result = prime * result + start;
    result = prime * result + ((text == null) ? 0 : text.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Reading other = (Reading) obj;
    if (length != other.length)
      return false;
    if (start != other.start)
      return false;
    if (text == null) {
      if (other.text != null)
        return false;
    } else if (!text.equals(other.text))
      return false;
    return true;
  }
  
  @Override
  public String toString() {
    return "Reading:{" + start + ":" + length + ":" + text + "}";
  }
  
  /**
   * @param start The starting point within the sentence
   * @param length The number of characters of the sentence covered by the
   *               reading
   * @param text The reading text applied to the covered span
   */
  public Reading(int start, int length, String text) {
    this.start = start;
    this.length = length;
    this.text = text;
  }
}
