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

import org.obinject.block.HeaderNode;
import org.obinject.block.Node;
import org.obinject.meta.Uuid;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public abstract class AbstractWorkspace implements Workspace {

    /**
     * The name of the {@code Workspace}.
     */
    private String name;
    /**
     * The page ID of the descripor.
     */
    private final static long pageIdDescriptor = 0;
    /**
     * The size of blocks.
     */
    protected int sizeArray;

    /**
     * Constructs a new {@code Workspace}. This constructor is used to
     * reinstantiate an existent {@code Workspace}.
     *
     * @param name A name to the {@code Workspace}.
     */
    protected AbstractWorkspace(String name) {
        this(name, 4096);
    }

    /**
     * Constructs a new {@code Workspace}. This constructor is used to create a
     * new {@code Workspace}.
     *
     * @param name A name to the {@code Workspace}.
     * @param sizeArray The size of blocks.
     */
    public AbstractWorkspace(String name, int sizeArray) {
        this.sizeArray = sizeArray;
        this.name = name;

    }
    
    /**
     *
     */
    protected void initialize() {
        if (existWorkspace()) {
            loadWorkspace();
        } else {
            createWorkspace();
            createHeaderNode();
        }
    }

    /**
     *
     * @return
     */
    protected abstract boolean existWorkspace();

    /**
     *
     */
    protected abstract void loadWorkspace();

    /**
     *
     */
    protected abstract void createWorkspace();

    /**
     *
     */
    protected void createHeaderNode() {
        // create the descriptor
        byte array[] = new byte[sizeArray];
        Node node = new Node(0, array);
        HeaderNode header = new HeaderNode(node);
        header.writeSizeOfArray(sizeArray);
        header.writeLastPageId(0);
        header.writeLastSessionId(0);
        header.writePreviousPageId(0);
        header.writeNextPageId(0);
        this.flushPage(header);
    }

    /**
     * Deletes a page from the {@code Workspace}.
     *
     * @param id The ID of the page to be deleted.
     * @return {@code true} if page was deleted; {@code false} otherwise.
     */
    public abstract boolean deletePage(long id);
    
    public abstract boolean discardPage(Node node);

    /**
     *
     * @param classID
     * @return
     */
    public long findUniqueDescriptor(Uuid classID) {
        HeaderNode header = new HeaderNode(this.loadPage(pageIdDescriptor));
        int idx = header.indexOfDescriptorClassID(classID);
        if (idx == -1) {
            long pageId = newPageId(header);
            header.addEntry(classID, pageId);
            this.flushPage(header);
            return pageId;
        } else {
            return header.readDescriptorPageId(idx);
        }
    }

    /**
     *
     * @param node
     * @return
     */
    public boolean flushPage(Node node){
        if(node.readModified() == true)
        {
            node.resetModified();
            return this.writePage(node);
        }
        else
        {
            return this.discardPage(node);
        }
    }// flushPage

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public long incrementPageId() {
        //ajusting LastPageId
        HeaderNode header = new HeaderNode(this.loadPage(pageIdDescriptor));
        long pageId = newPageId(header);
        this.flushPage(header);
        return pageId;
    }

    /**
     *
     * @return
     */
    public long incrementSessionId() {
        HeaderNode header = new HeaderNode(this.loadPage(pageIdDescriptor));
        long inc = header.readLastSessionId() + 1;
        header.writeLastSessionId(inc);
        this.flushPage(header);
        return inc;
    }

    /**
     *
     * @param id
     * @return
     */
    public abstract Node loadPage(long id);

    private long newPageId(HeaderNode header) {
        long pageId = header.readLastPageId() + 1;
        header.writeLastPageId(pageId);
        //creating node
        byte array[] = new byte[this.sizeOfArray()];
        Node node = new Node(pageId, array);
        this.flushPage(node);
        return pageId;
    }

    /**
     *
     * @return
     */
    @Override
    public Session openSession() {
        return new Session(this);
    }

    /**
     *
     * @return
     */
    public int sizeOfArray() {
        return sizeArray;
    }
    
    public abstract boolean writePage(Node node);
    
    
}
