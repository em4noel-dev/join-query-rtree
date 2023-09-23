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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obinject.block.HeaderNode;
import org.obinject.block.Node;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public class File extends AbstractWorkspace {

    private java.io.File objectFile;
    private FileInputStream fileInput;
    private RandomAccessFile fileOutput;

    /**
     *
     * @param fileName
     */
    public File(String fileName) {
        super(fileName);
        this.initialize();
    }

    /**
     *
     * @param fileName
     * @param sizeArray
     */
    public File(String fileName, int sizeArray) {
        super(fileName, sizeArray);
        this.initialize();
    }
    
    @Override
    public boolean discardPage(Node node) {
        return true;
    }

    @Override
    public boolean deletePage(long id) {
        //remove in file
        //....
        return false;
    }

    /**
     *
     * @param node
     * @return
     */
    @Override
    public final boolean writePage(Node node) {
        long pos = node.getPageId() * this.sizeOfArray();
        try {
            //writing
            fileOutput.seek(pos);
            fileOutput.write(node.getArray());
            return true;
        } catch (IOException ex) {
            Logger.getLogger(File.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public Node loadPage(long id) {
        byte[] array = new byte[this.sizeOfArray()];
        long pos = id * this.sizeOfArray();
        //page is not memory
        try {
            fileInput.getChannel().position(pos);
            fileInput.read(array);
        } catch (IOException ex) {
            Logger.getLogger(File.class.getName()).log(Level.SEVERE, null, ex);
        }//endtry
        //create page
        return new Node(id, array);
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean existWorkspace() {
        objectFile = new java.io.File(this.getName());
        return objectFile.exists();
    }

    /**
     *
     */
    @Override
    protected void loadWorkspace() {
        objectFile = new java.io.File(this.getName());
        try {
            fileOutput = new RandomAccessFile(objectFile, "rw");
            fileInput = new FileInputStream(objectFile);
            HeaderNode header = new HeaderNode(this.loadPage(0));
            sizeArray = header.readSizeOfArray();
        } catch (IOException ioe) {
            Logger.getLogger(File.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }

    /**
     *
     */
    @Override
    protected void createWorkspace() {
        objectFile = new java.io.File(this.getName());
        try {
            fileOutput = new RandomAccessFile(objectFile, "rw");
            fileInput = new FileInputStream(objectFile);
        } catch (IOException ioe) {
            Logger.getLogger(File.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }
}
