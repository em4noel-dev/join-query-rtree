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
import org.obinject.meta.Uuid;

/**
 * 
 * @param <E>
 * 
 * <blockquote><pre>
 * {@code
 * Design:
 *
 *                  +-----------------------------------------+
 *                  |     +------------------------------+    |
 *                  |     |     +-------------------+    |    |
 *                  |     |     |                   |    |    |
 * +----------------|-----|-----|-------------------V----V----V----+
 * |      |number| off | off | off |                |obj2|obj1|obj0|
 * |      |  of  | set | set | set |                |              |
 * |      |entity| [0] | [1] | [2] |                |              |
 * |header| feat.|     entries     |<--free space-->|    objects   |
 * +---------------------------------------------------------------+
 * }
 * </pre></blockquote>
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public class SequentialNode<E extends Entity<E>> extends EntityNode<E>
{
    protected static int sizeOfFeatures = SequentialNode.sizeOfLong + SequentialNode.sizeOfInteger;
    protected static int sizeOfEntry = SequentialNode.sizeOfInteger;
    public static final int nodeType = 3;
    protected long verifications = 0;
    
    public SequentialNode(Node node, Class<E> clazz)
    {
	super(node, clazz);
        initialize(node);
    }

    public long getVerifications() {
        return verifications;
    }

    public boolean addEntity(E entity)
    {
	int off;
	int idx = this.readNumberOfEntitries();
	int size = entity.sizeOfEntity();
	if (size + sizeOfEntry < freeSpace())
	{
	    //if is first position
	    if (idx == 0)
	    {
		off = this.sizeOfArray() - size;
	    }
	    else
	    {
		off = readOffset(idx - 1) - size;
	    }//endif
	    //position
	    writeOffset(idx, off);
	    //push the entity in page
	    entity.pushEntity(this.getArray(), off);
	    //increment
	    incrementNumberEntities();
	    return true;
	}
	else
	{
	    return false;
	}//endif
    }

    /**
     *
     * @param idx
     * @return
     */
    public E buildEntity(int idx)
    {
	//instantiation by reflection
	if ((idx >= 0) && (idx < this.readNumberOfEntitries()))
	{
	    E entity = this.newGenericType();
	    entity.pullEntity(this.getArray(), this.readOffset(idx));
	    return entity;
	}//endif

	return null;
    }

    /**
     *
     * @param idx
     * @param entity
     */
    public void rebuildEntity(int idx, E entity)
    {
	//instantiation by reflection
	if ((idx >= 0) && (idx < this.readNumberOfEntitries()))
	{
	    entity.pullEntity(this.getArray(), this.readOffset(idx));
	}//endif
    }

    /**
     *
     */
    @Override
    public void clear()
    {
	//zering next page
	int pos = SequentialNode.sizeOfHeader();
	this.writeInteger(pos, 0);
	//zering numberOfKey
	pos = SequentialNode.sizeOfHeader() + SequentialNode.sizeOfLong;
	this.writeInteger(pos, 0);
    }

    /**
     *
     */
    protected void decrementNumberOfEntities()
    {
	int num = this.readNumberOfEntitries() + 1;
	int pos = SequentialNode.sizeOfHeader() + SequentialNode.sizeOfLong;
	this.writeInteger(pos, num);
    }

    /**
     *
     * @param entity
     * @return
     */
    public Uuid findEntity(E entity)
    {
	int i;
	int total = this.readNumberOfEntitries();
	E objSer;
	for (i = 0; i < total; i++)
	{
	    objSer = buildEntity(i);
            verifications++;
	    if (objSer.isEqual(entity) == true)
	    {
		return objSer.getUuid();
	    }//endif
	}//endfor
	return null;
    }

    /**
     *
     * @param uuid
     * @return
     */
    public E findUuid(Uuid uuid)
    {
	int i;
	int total = this.readNumberOfEntitries();
	E objSer;
	for (i = 0; i < total; i++)
	{
	    objSer = buildEntity(i);
            verifications++;
	    if (objSer.getUuid().equals(uuid))
	    {
		return objSer;
	    }//endif
	}//endfor
	return null;
    }

    /**
     *
     * @return
     */
    protected int freeSpace()
    {
	int size = this.readNumberOfEntitries();
	if (size == 0)
	{
	    return this.sizeOfArray() - SequentialNode.sizeOfHeader();
	}
	else
	{
	    return this.sizeOfArray() - SequentialNode.sizeOfHeader()
		    - SequentialNode.sizeOfFeatures - (size * SequentialNode.sizeOfEntry)
		    - (this.sizeOfArray() - this.readOffset(size - 1));
	}//endif
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
     */
    protected void incrementNumberEntities()
    {
	int num = this.readNumberOfEntitries() + 1;
	int pos = SequentialNode.sizeOfHeader() + SequentialNode.sizeOfLong;
	this.writeInteger(pos, num);
    }

    /**
     *
     * @param node
     * @return
     */
    public static boolean matchNodeType(Node node)
    {
	return node.readNodeType() == SequentialNode.nodeType;
    }

    /**
     *
     * @return
     */
    public int readNumberOfEntitries()
    {
	int pos = SequentialNode.sizeOfHeader() + SequentialNode.sizeOfLong;
	return this.readInteger(pos);
    }

    /**
     *
     * @param idx
     * @return
     */
    protected int readOffset(int idx)
    {
	int pos = SequentialNode.sizeOfHeader() + SequentialNode.sizeOfFeatures + (idx * SequentialNode.sizeOfEntry);
	return this.readInteger(pos);
    }

    /**
     *
     * @param idx
     * @param off
     */
    protected void writeOffset(int idx, int off)
    {
	int pos = SequentialNode.sizeOfHeader() + SequentialNode.sizeOfFeatures + (idx * SequentialNode.sizeOfEntry);
	this.writeInteger(pos, off);
    }
}
