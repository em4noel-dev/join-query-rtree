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
import org.obinject.meta.Uuid;

/**
 *
 * <blockquote><pre>
 * {@code
 * Design:
 *
 * +-------------------+---------------------------+-------------------+----------------+
 * |modi|node|prev|next| size | last | last |number|desc|desc|desc|desc|                |
 * |fied|type|page|page|  of  | page | sess |  of  |node|page|node|page|                |
 * |    |    | Id | Id | array|  Id  |  Id  |enties|type| Id |type| Id |<--free space-->|
 * |    |    |    |    |      |      |      |      |---------+---------|                |
 * |-------------------+------+------+------+------|entry[0] |entry[1] |                |
 * |      header       |          features         |      enties       |                |
 * +-------------------+---------------------------+-------------------+----------------+
 * }
 * </pre></blockquote>
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public class HeaderNode extends AbstractNode
{

    /**
     *
     */
    public static final int nodeType = 1000;

    /**
     *
     * @param node
     */
    public HeaderNode(Node node)
    {
	super(node);
        initialize(node);
    }

    /**
     *
     * @param classID
     * @param pageId
     * @return
     */
    public boolean addEntry(Uuid classID, long pageId)
    {
	if (HeaderNode.sizeOfEntry() < this.freeSpace())
	{
	    int total = this.readNumberOfEntries();
	    this.writeDescriptorClassID(total, classID);
	    this.writeDescriptorPageId(total, pageId);
	    this.incrementNumberOfKeys();
	    return true;
	}
	else
	{
	    return false;
	}
    }

    /**
     * @see AbstractNode#clear()
     */
    @Override
    public final void clear()
    {
	this.writeSizeOfArray(0);
	this.writeLastPageId(0);
	this.writeLastSessionId(0);
    }

    /**
     *
     * @return
     */
    protected int freeSpace()
    {
	int num = this.readNumberOfEntries();
	if (num == 0)
	{
	    return this.sizeOfArray() - HeaderNode.sizeOfHeader() - HeaderNode.sizeOfFeatures();
	}
	else
	{
	    return this.sizeOfArray() - HeaderNode.sizeOfHeader()
		    - HeaderNode.sizeOfFeatures() - (num * HeaderNode.sizeOfEntry());
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
    protected void incrementNumberOfKeys()
    {
	int num = this.readNumberOfEntries() + 1;
	int pos = HeaderNode.sizeOfHeader() + AbstractNode.sizeOfInteger + AbstractNode.sizeOfLong + AbstractNode.sizeOfLong;
	this.writeInteger(pos, num);
    }

    /**
     *
     * @param classID
     * @return
     */
    public int indexOfDescriptorClassID(Uuid classID)
    {
	int total = this.readNumberOfEntries();

	for (int i = 0; i < total; i++)
	{
	    if (classID.compareTo(this.readDescriptorClassID(i)) == 0)
	    {
		return i;
	    }
	}
	return -1;
    }

    private Uuid readDescriptorClassID(int idx)
    {
	int pos = HeaderNode.sizeOfHeader() + HeaderNode.sizeOfFeatures() + (idx * HeaderNode.sizeOfEntry());
	return this.readUuid(pos);
    }

    /**
     *
     * @param idx
     * @return
     */
    public long readDescriptorPageId(int idx)
    {
	int pos = HeaderNode.sizeOfHeader() + HeaderNode.sizeOfFeatures() + (idx * HeaderNode.sizeOfEntry()) + HeaderNode.sizeOfUuid;
	return this.readLong(pos);
    }

    /**
     *
     * @return
     */
    public int readNumberOfEntries()
    {
	int pos = AbstractNode.sizeOfHeader() + AbstractNode.sizeOfInteger + AbstractNode.sizeOfLong + AbstractNode.sizeOfLong;
	return this.readInteger(pos);
    }

    /**
     *
     * @return
     */
    public long readLastPageId()
    {
	int pos = AbstractNode.sizeOfHeader() + AbstractNode.sizeOfInteger;
	return this.readLong(pos);
    }

    /**
     *
     * @return
     */
    public long readLastSessionId()
    {
	int pos = AbstractNode.sizeOfHeader() + AbstractNode.sizeOfInteger + AbstractNode.sizeOfLong;
	return this.readLong(pos);
    }

    /**
     *
     * @return
     */
    public int readSizeOfArray()
    {
	int pos = AbstractNode.sizeOfHeader();
	return this.readInteger(pos);
    }

    private static int sizeOfEntry()
    {
	return AbstractNode.sizeOfUuid + AbstractNode.sizeOfLong;
    }

    private static int sizeOfFeatures()
    {
	return AbstractNode.sizeOfInteger + AbstractNode.sizeOfLong + AbstractNode.sizeOfLong + AbstractNode.sizeOfInteger;
    }

    private void writeDescriptorClassID(int idx, Uuid classID)
    {
	int pos = HeaderNode.sizeOfHeader() + HeaderNode.sizeOfFeatures() + (idx * HeaderNode.sizeOfEntry());
	this.writeUuid(pos, classID);
    }

    private void writeDescriptorPageId(int idx, long pageId)
    {
	int pos = HeaderNode.sizeOfHeader() + HeaderNode.sizeOfFeatures() + (idx * HeaderNode.sizeOfEntry()) + HeaderNode.sizeOfUuid;
	this.writeLong(pos, pageId);
    }

    /**
     *
     * @param last
     */
    public void writeLastPageId(long last)
    {
	int pos = AbstractNode.sizeOfHeader() + AbstractNode.sizeOfInteger;
	this.writeLong(pos, last);
    }

    /**
     *
     * @param last
     */
    public void writeLastSessionId(long last)
    {
	int pos = AbstractNode.sizeOfHeader() + AbstractNode.sizeOfInteger + AbstractNode.sizeOfLong;
	this.writeLong(pos, last);
    }

    /**
     *
     * @param size
     */
    public void writeSizeOfArray(int size)
    {
	int pos = AbstractNode.sizeOfHeader();
	this.writeInteger(pos, size);
    }
}
