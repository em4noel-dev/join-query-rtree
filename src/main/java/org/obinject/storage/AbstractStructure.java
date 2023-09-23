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
package org.obinject.storage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.obinject.device.Workspace;
import org.obinject.meta.Uuid;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 *
 * @param <T>
 */
public abstract class AbstractStructure<T> implements Structure<T> {

    private final Workspace workspace;
    private final Class<T> objectClass;
    private Uuid classUuid;

    /**
     *
     * @param workspace
     */
    public AbstractStructure(Workspace workspace) {
        this.workspace = workspace;
        this.objectClass = ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        try {
            this.classUuid = (Uuid) objectClass.getMethod("getClassId").invoke(null);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            Logger.getLogger(AbstractStructure.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return
     */
    public Workspace getWorkspace() {
        return workspace;
    }

    public Uuid getClassUuid() {
        return classUuid;
    }

    @Override
    public Class<T> getObjectClass() {
        return objectClass;
    }

    @Override
    public abstract boolean add(T obj);

    @Override
    public abstract long getRootPageId();

    @Override
    public abstract boolean remove(T obj);

}
