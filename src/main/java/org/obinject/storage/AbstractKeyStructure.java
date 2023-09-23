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

import java.lang.reflect.ParameterizedType;
import org.obinject.device.Workspace;
import org.obinject.meta.Entity;
import org.obinject.meta.Key;
import org.obinject.meta.Uuid;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 * 
 * @param <K>
 */
public abstract class AbstractKeyStructure<K extends Key<K> & Entity<? super K>> extends AbstractStructure<K> implements KeyStructure<K>
{

    /**
     *
     * @param workspace
     */
    public AbstractKeyStructure(Workspace workspace)
    {
	super(workspace);
    }

    @Override
    public abstract Uuid find(K key);
}
