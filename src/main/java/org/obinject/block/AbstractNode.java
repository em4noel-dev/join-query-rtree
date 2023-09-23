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
 * <p>A {@code Node} is a block to store an object in bytes.</p> 
 * 
 * <blockquote><pre>
 * {@code
 * Design:
 *
 * +-------------------+----------------------------------------------------+
 * |modi|node|prev|next|                                                    |
 * |fied|type|page|page|                                                    |
 * |bool|    | Id | Id |<------------------- free space ------------------->|
 * |-------------------|                                                    |
 * |      header       |                                                    |
 * +-------------------+----------------------------------------------------+
 * }
 * </pre></blockquote>
 * 
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public abstract class AbstractNode extends Node
{
    
    /**
     *
     * @param node
     */
    public AbstractNode(Node node)
    {
	super(node.getPageId(), node.getArray());
    }

    /**
     *
     * @param node
     */
    protected void initialize(Node node){
	int type = node.readNodeType();

        //zero indica que nao foi definido o tipo do noh
	if (type == 0)
	{
	    this.writeNodeType(this.getNodeType());
	}
	else if (type != this.getNodeType())
	{
	    throw new ClassCastException("error match type node");
	}
    }
    
    /**
     *
     */
    public abstract void clear();

    /**
     *
     * @return
     */
    protected abstract int getNodeType();
}
