/*
Copyright (C) 2013     Enzo Seraphim

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
or visit <http://www.gnu.org/licenses/>
*/

package org.obinject.block;

import org.obinject.meta.Entity;
import org.obinject.meta.Rectangle;
import org.obinject.storage.EuclideanGeometry;

/**
 * <p>This class implements an R-Tree Index.</p>
 *
 * @param <K>
 * 
 * <blockquote><pre>
 * {@code
 * Design:
 *
 *
 * +-------------------+--------+--------------------------+------------------+--------------+
 * |node|par-|prev|next| number |sub node|sub node|sub node|                  |key2|key1|key0|
 * |type|-ent|page|page|   of   |--------+--------+--------|                  |    |    |    |
 * |    |node| Id | Id |  keys  |entry[0]|entry[1]|entry[2]|<-- free space -->|    |    |    |
 * |-------------------+--------|--------+--------+--------+                  |--------------|
 * |      header       |features|          entries         |                  |     keys     |
 * +-------------------+--------+--------------------------+------------------+--------------+
 * }
 * </pre></blockquote>
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public class RTreeIndex<K extends Rectangle<K> & Entity<? super K>> extends RTreeNode<K>
{

    public static final int nodeType = 9;
    private static long numMbrs = 0;

    /**
     *
     * @param node
     * @param keyClass
     */
    public RTreeIndex(Node node, Class<K> keyClass)
    {
	super(node, keyClass);
        initialize(node);
    }

    /**
     *
     * @param key
     * @param subPageId
     * @return
     */
    public final boolean addKey(K key, long subPageId)
    {
	int total = this.readNumberOfKeys();
	int size = key.sizeOfKey();

	if (size + this.sizeOfEntry() > this.freeSpace(size)) // It there is not space.
	{
	    return false;
	}
	else
	{
	    numMbrs++;
	    int off = this.getOffset(total, size);
	    key.pushKey(this.getArray(), off); // Adding			
	    this.writeSubPageId(total, subPageId);
	    this.incrementNumberOfKeys();

	    return true;
	}
    }

    /**
     *
     * @param key1
     * @param subPageId1
     * @param key2
     * @param subPageId2
     * @return
     */
    public final boolean addKey(K key1, long subPageId1, K key2, long subPageId2)
    {
	int size = key1.sizeOfKey();

	if (size + size + 2 * this.sizeOfEntry() > this.freeSpace(size))
	{
	    return false;
	}
	else
	{
	    numMbrs++;
	    addKey(key1, subPageId1);
	    addKey(key2, subPageId2);
	    return true;
	}
    }

    /**
     *
     * @return
     */
    @Override
    protected int getNodeType()
    {
	return nodeType;
    }

    /**
     *
     * @param key
     * @param geometry
     * @return
     */
    public final int indexOfInsertion(K key, EuclideanGeometry<K> geometry)
    {
	int total = this.readNumberOfKeys(); // Number of keys in node
	double minOccupancy = Double.MAX_VALUE; // Minimum occupancy (area, volume...) of MBR.
	double minEnlargement = Double.MAX_VALUE; // Minimum enlargement of occupancy (area, volume...) of MBR.
	int idx = -1; // Qualified sub page
	K storedKey; // To build keys stored into this node.
	K newMbr = null;
	for (int i = 0; i < total; i++)
	{
	    storedKey = this.buildKey(i);

	    // Calc occupancy of stored key
	    double befOcup = geometry.occupancy(storedKey); // Occupancy (area, volume...) BEFORE enlargement.
	    K mbrUnion = geometry.union(key, storedKey); // Enlargment
	    double aftOcup = geometry.occupancy(mbrUnion); // Occupancy (area, volume...) AFTER enlargement.

	    double enlarg = aftOcup - befOcup; // Enlargement of occupancy

	    // Qualifies key with minimum enlargement. Resolve ties selecting minimum occupancy (area, volume...) 
	    if (enlarg < minEnlargement)
	    {
		minEnlargement = enlarg;
		minOccupancy = befOcup;
		idx = i; // Qualify
		newMbr = mbrUnion;
	    }
	    else if (enlarg == minEnlargement && befOcup < minOccupancy)
	    {
		minOccupancy = befOcup;
		idx = i; // Qualify
		newMbr = mbrUnion;
	    }
	}

	// If MBR was enlarged:
	if (minEnlargement != 0)
	{
	    // Writes enlargement
	    newMbr.pushKey(this.getArray(), this.getOffset(idx, newMbr.sizeOfKey()));
	}

	return idx;
    }

    /**
     *
     * @param pageId
     * @return
     */
    public final int indexOfSubPageId(long pageId)
    {
	int total = this.readNumberOfKeys();
	for (int i = 0; i < total; i++)
	{
	    if (this.readSubPageId(i) == pageId)
	    {
		return i;
	    }
	}

	return -1;
    }

    /**
     *
     * @param node
     * @return
     */
    public static boolean matchNodeType(Node node)
    {
	return node.readNodeType() == RTreeIndex.nodeType;
    }

    /**
     *
     * @param idx
     * @return
     */
    public final long readSubPageId(int idx)
    {
	int pos = RTreeNode.sizeOfHeader() + this.sizeOfFeatures() + (idx * this.sizeOfEntry());
	return this.readLong(pos);
    }

    /**
     *
     * @param idx
     * @param key
     */
    public void replace(int idx, K key)
    {
	int off = this.getOffset(idx, key.sizeOfKey());
	key.pushKey(this.getArray(), off);
    }

    /**
     *
     * @return
     */
    @Override
    protected int sizeOfEntry()
    {
	return RTreeIndex.sizeOfLong; // sub
    }

    /**
     *
     * @param idx
     * @param pageId
     */
    protected final void writeSubPageId(int idx, long pageId)
    {
	int pos = RTreeNode.sizeOfHeader() + this.sizeOfFeatures() + (idx * this.sizeOfEntry());
	this.writeLong(pos, pageId);
    }

    /**
     *
     * @return
     */
    public static long getNumMbrs()
    {
	return numMbrs;
    }

    /**
     *
     */
    public static void resetNumMbrs()
    {
	numMbrs = 0;
    }
}
