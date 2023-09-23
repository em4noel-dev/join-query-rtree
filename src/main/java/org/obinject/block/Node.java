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
 * <p>
 * A {@code Node} is a block to store an object in bytes.</p>
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
public class Node extends Page {

    private long pageId = 0;

    /**
     *
     * @return
     */
    public byte[] getArray() {
        return array;
    }

    /**
     *
     * @param pageId
     * @param array
     */
    public Node(long pageId, byte[] array) {
        super(array);
        this.pageId = pageId;
    }

    /**
     *
     * @return
     */
    public long getPageId() {
        return pageId;
    }

    /**
     *
     * @param dest
     * @param source
     * @param length
     */
    public void move(int dest, int source, int length) {
        if (dest > source) {
            this.moveRight(dest, source, length);
        } else {
            this.moveLeft(dest, source, length);
        }
    }

    /**
     *
     * @return
     */
    public long readNextPageId() {
        int pos = Page.sizeOfHeader() + sizeOfInteger + sizeOfLong;
        return this.readLong(pos);
    }

    /**
     *
     * @return
     */
    public int readNodeType() {
        int pos = Page.sizeOfHeader();
        return this.readInteger(pos);
    }

    /**
     *
     * @return
     */
    public long readPreviousPageId() {
        int pos = Page.sizeOfHeader() + sizeOfInteger;
        return this.readLong(pos);
    }

    /**
     *
     * @return
     */
    public static int sizeOfHeader() {
        return Page.sizeOfHeader() + sizeOfInteger + sizeOfLong + sizeOfLong;
    }

    /**
     *
     * @param pageId
     */
    public void writeNextPageId(long pageId) {
        int pos = Page.sizeOfHeader() + sizeOfInteger + sizeOfLong;
        this.writeLong(pos, pageId);
    }

    /**
     *
     * @param nodeType
     */
    protected void writeNodeType(int nodeType) {
        int pos = Page.sizeOfHeader();
        this.writeInteger(pos, nodeType);
    }

    /**
     *
     * @param pageId
     */
    public void writePreviousPageId(long pageId) {
        int pos = Page.sizeOfHeader() + sizeOfInteger;
        this.writeLong(pos, pageId);
    }
    
     /**
     * Reset boolean modified info.
     * WARNING: the array write must be done hardcode (array[0]) so that the
     * reset works.
     * 
     * If resetModified calls writeBoolean, then:
     *  writeBoolean -> writes 0 in array[0]
     *  writeModified -> writes 1 in array[0]
     * 
     */
    public void resetModified(){
        array[0] = 0;
    }

}
