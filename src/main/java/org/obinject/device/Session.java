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

package org.obinject.device;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.obinject.block.Node;
import org.obinject.meta.Uuid;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public class Session
{

    private long sessionId;
    private AbstractWorkspace workspace;
    private HashMap<Long, Node> nodesCache = new HashMap<>();
    private long blockAccess = 0;
    
    /**
     *
     * @param workspace
     */
    protected Session(AbstractWorkspace workspace)
    {
	this.workspace = workspace;
	sessionId = workspace.incrementSessionId();
    }

    /**
     *
     */
    public void close()
    {        
        Iterator<Map.Entry<Long, Node>> it = nodesCache.entrySet().iterator();
	while (it.hasNext())
	{
	    workspace.flushPage(it.next().getValue());
	}//endwhile
	nodesCache.clear();
//        workspace = null;
    }

    /**
     *
     * @return
     */
    public Node create()
    {       
	long pageId = workspace.incrementPageId();
	return this.load(pageId);
    }

    /**
     * Returns the pageId. If not exists nodeType, Node is created.
     *
     * @param classID
     * @return
     */
    public long findPageIdDescriptor(Uuid classID)
    {
	return workspace.findUniqueDescriptor(classID);
    }

    /**
     *
     * @return
     */
    public long getBlockAccess() {
        return blockAccess;
    }

    /**
     *
     * @return
     */
    public long getSessionId()
    {
	return sessionId;
    }
    
    /**
     *
     * @param id
     * @return
     */
    public Node load(long id)
    {
        //increment BlockAccess
	this.blockAccess++;
	Node nodeFind = nodesCache.get(id);
	if (nodeFind == null)
	{
	    nodeFind = workspace.loadPage(id);
	    nodesCache.put(id, nodeFind);
	}
	return nodeFind;
    }

}
