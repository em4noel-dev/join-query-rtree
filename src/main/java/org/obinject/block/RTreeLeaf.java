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
import org.obinject.meta.Uuid;

/**
 * <p>This class implements an R-Tree Leaf.</p> 
 *
 * @param <K>
 * 
 * <blockquote><pre>
 * {@code
 * Design:
 *
 * +-------------------+--------+--------------------------+------------------+--------------+
 * |node|par-|prev|next| number |ent uuid|ent uuid|ent uuid|                  |key2|key1|key0|
 * |type|-ent|page|page|   of   |--------+--------+--------|                  |    |    |    |
 * |    |node| Id | Id |  keys  |entry[0]|entry[1]|entry[2]|<-- free space -->|    |    |    |
 * |-------------------+--------|--------+--------+--------+                  |--------------|
 * |      header       |features|         entries          |                  |     keys     |
 * +-------------------+--------+--------------------------+------------------+--------------+
 * }
 * </pre></blockquote>
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public class RTreeLeaf<K extends Rectangle<K> & Entity<? super K>> extends RTreeNode<K>
{

    public static final int nodeType = 8;

    /**
     *
     * @param node
     * @param keyClass
     */
    public RTreeLeaf(Node node, Class<K> keyClass)
    {
	super(node, keyClass);
        initialize(node);
    }

    /**
     *
     * @param key
     * @param uuid
     * @return
     */
    public final boolean addKey(K key, Uuid uuid)
    {
	int total = this.readNumberOfKeys();
	int size = key.sizeOfKey();

	if (size + this.sizeOfEntry() > this.freeSpace(size)) // It there is not space.
	{
	    return false;
	}
	else
	{
	    int off = this.getOffset(total, size);
	    key.pushKey(this.getArray(), off); // Adding
	    this.writeEntityUuid(total, uuid);
	    this.incrementNumberOfKeys();

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
     * @param node
     * @return
     */
    public static boolean matchNodeType(Node node)
    {
	return node.readNodeType() == RTreeLeaf.nodeType;
    }

    /**
     *
     * @param idx
     * @return
     */
    public final Uuid readEntityUuid(int idx)
    {
	int pos = RTreeNode.sizeOfHeader() + this.sizeOfFeatures() + (idx * this.sizeOfEntry());
	return this.readUuid(pos);
    }

    /**
     *
     * @return
     */
    @Override
    protected int sizeOfEntry()
    {
	return RTreeLeaf.sizeOfUuid; // entity uuid
    }

    /**
     *
     * @param idx
     * @param uuid
     */
    protected final void writeEntityUuid(int idx, Uuid uuid)
    {
	int pos = RTreeNode.sizeOfHeader() + this.sizeOfFeatures() + (idx * this.sizeOfEntry());
	this.writeUuid(pos, uuid);
    }
}
