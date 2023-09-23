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

/**
 * <p>This class implements an R-Tree Node.</p> 
 *
 * @param <K>
 * 
 * <blockquote><pre>
 * {@code
 * Design:
 *
 * +-------------------+--------+----------------------------------------+
 * |node|par-|prev|next| number |                                        |
 * |type|-ent|page|page|   of   |                                        |
 * |    |node| Id | Id |  keys  |<------------- free space ------------->|
 * |-------------------+--------|                                        |
 * |      header       |features|                                        |
 * +-------------------+--------+----------------------------------------+
 * }
 * </pre></blockquote>
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public abstract class RTreeNode<K extends Rectangle<K> & Entity<? super K>> extends KeyNode<K>
{

    /**
     *
     * @param node
     * @param keyClass
     */
    public RTreeNode(Node node, Class<K> keyClass)
    {
	super(node, keyClass);
    }

    /**
     *
     * @param idx
     * @return
     */
    public K buildKey(int idx)
    {
	//instantiation by reflection
	if ((idx >= 0) && (idx < this.readNumberOfKeys()))
	{
            K key;
            if(this instanceof RTreeLeaf){
                RTreeLeaf leaf = (RTreeLeaf)this;
                key = this.newGenericType(leaf.readEntityUuid(idx));
            }else{
                key = this.newGenericType();
            }
	    //pull the key of the page
	    key.pullKey(this.getArray(), this.getOffset(idx, key.sizeOfKey()));
	    return key;
	}//endif

	return null;
    }

    /**
     *
     */
    @Override
    public final void clear()
    {
	// Zering number of keys.
	this.writeInteger(RTreeNode.sizeOfHeader(), 0);
    }

    /**
     *
     */
    protected final void decrementNumberOfKeys()
    {
	this.writeInteger(RTreeNode.sizeOfHeader(), this.readNumberOfKeys() - 1);
    }

    /**
     *
     * @param sizeOfKey
     * @return
     */
    protected final int freeSpace(int sizeOfKey)
    {
	int total = this.readNumberOfKeys();

	return total == 0
		? this.sizeOfArray() // Total
		- RTreeNode.sizeOfHeader() // Header
		- this.sizeOfFeatures() // Features

		: this.sizeOfArray() // Total
		- RTreeNode.sizeOfHeader() // Header
		- this.sizeOfFeatures() // Features
		- (total * this.sizeOfEntry()) // Entries
		- (this.sizeOfArray() - this.getOffset(total - 1, sizeOfKey)); // Keys
    }

    /**
     *
     * @param idx
     * @param sizeOfKey
     * @return
     */
    protected final int getOffset(int idx, int sizeOfKey)
    {
	return this.sizeOfArray() - ((idx + 1) * sizeOfKey);
    }

    /**
     *
     */
    protected final void incrementNumberOfKeys()
    {
	this.writeInteger(RTreeNode.sizeOfHeader(), this.readNumberOfKeys() + 1);
    }

    /**
     *
     * @param key
     * @return
     */
    public final int indexOfKey(K key)
    {
	K objKey;
	int total = this.readNumberOfKeys();

	for (int i = 0; i < total; i++)
	{
	    objKey = this.buildKey(i);
	    if (objKey.distanceTo(key) == 0)
	    {
		return i;
	    }
	}

	return -1;
    }

    /**
     *
     * @return
     */
    @Override
    public final int readNumberOfKeys()
    {
	return this.readInteger(RTreeNode.sizeOfHeader());
    }

    /**
     *
     * @return
     */
    protected abstract int sizeOfEntry();

    /**
     *
     * @return
     */
    protected final int sizeOfFeatures()
    {
	return RTreeNode.sizeOfInteger; // number of keys
    }
}
