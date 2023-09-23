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

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obinject.meta.Entity;
import org.obinject.meta.Key;
import org.obinject.meta.Uuid;
import org.obinject.storage.AbstractStructure;

/**
 *
 * @param <K>
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public abstract class KeyNode<K extends Key<K> & Entity<? super K>> extends AbstractNode {

    private final Class<K> keyClass;

    /**
     *
     * @param node
     * @param keyClass
     */
    public KeyNode(Node node, Class<K> keyClass) {
        super(node);

        this.keyClass = keyClass;
    }

    protected K newGenericType() {
        try {
            return keyClass.newInstance();
        } catch (IllegalArgumentException | SecurityException ex) {
            Logger.getLogger(KeyNode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(AbstractStructure.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    protected K newGenericType(Uuid uuid) {
        try {
            return keyClass.getConstructor(Uuid.class).newInstance(uuid);
        } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(KeyNode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(AbstractStructure.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     *
     * @return
     */
    protected Class<K> getKeyClass() {
        return keyClass;
    }

    /**
     *
     * @return
     */
    public abstract int readNumberOfKeys();
}
