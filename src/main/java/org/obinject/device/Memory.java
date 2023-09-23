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
import org.obinject.block.Node;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public class Memory extends AbstractWorkspace {

    private final HashMap<Long, byte[]> nodes = new HashMap<>();

    /**
     *
     * @param sizeArray
     */
    public Memory(int sizeArray) {
        super("Memory", sizeArray);
        this.initialize();
    }

    @Override
    public boolean deletePage(long id) {
        //remove in memory
        byte[] array = nodes.remove(id);
        if (array != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public boolean discardPage(Node node){
        return true;
    }
    
    /**
     *
     * @param node
     * @return
     */
    @Override
    public boolean writePage(Node node) {
        nodes.put(node.getPageId(), node.getArray());
        return true;
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public Node loadPage(long id) {
        byte array[] = nodes.get(id);
        return new Node(id, array);
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean existWorkspace() {
        return false;
    }

    /**
     *
     */
    @Override
    protected void loadWorkspace() {
        //ok
    }

    /**
     *
     */
    @Override
    protected void createWorkspace() {
        //ok
    }
}
