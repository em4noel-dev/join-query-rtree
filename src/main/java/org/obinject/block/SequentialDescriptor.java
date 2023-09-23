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

/**
 *
 * <blockquote><pre>
 * {@code
 * Design:
 *
 * +-------------------+-------------+----------------+
 * |modi|node|prev|next| begin| end  |                |
 * |fied|type|page|page| page | page |                |
 * |    |    | Id | Id |  Id  |  Id  |<--free space-->|
 * |bool|    |    |    |      |      |                |
 * |-------------------+-------------|                |
 * |       header      |  features   |                |
 * +-------------------+-------------+----------------+
 * }
 * </pre></blockquote>
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public class SequentialDescriptor extends DescriptorNode
{

    /**
     *
     */
    public static final int nodeType = 1002;

    /**
     *
     * @param node
     */
    public SequentialDescriptor(Node node)
    {
	super(node);
        initialize(node);
    }

    /**
     * @see AbstractNode#clear()
     */
    @Override
    public final void clear()
    {
	writePreviousPageId(0);
	writeNextPageId(0);
	writeBeginPageId(0);
	writeEndPageId(0);
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
     * @return
     */
    public long readBeginPageId()
    {
	int pos = AbstractNode.sizeOfHeader();
	return this.readLong(pos);
    }

    /**
     *
     * @return
     */
    public long readEndPageId()
    {
	int pos = AbstractNode.sizeOfHeader() + AbstractNode.sizeOfLong;
	return this.readLong(pos);
    }

    /**
     *
     * @param pageId
     */
    public void writeBeginPageId(long pageId)
    {
	int pos = AbstractNode.sizeOfHeader();
	this.writeLong(pos, pageId);
    }

    /**
     *
     * @param pageId
     */
    public void writeEndPageId(long pageId)
    {
	int pos = AbstractNode.sizeOfHeader() + AbstractNode.sizeOfLong;
	this.writeLong(pos, pageId);
    }
}
