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

package org.obinject.meta;

import org.obinject.storage.KeyStructure;

/**
 * The indexed keys, allows persistent entity attributes to be associated to a
 * key domain.<p/>
 *
 * The Keys delegates to indexed keys the responsibilities of the
 * serialization.<p/>
 *
 * The serialization converts the attributes of the key to their serialized
 * format.<p/>
 *
 * Key domains are classified as ordered ({@link Order}), punctual
 * ({@link Point}), rectangular ({@link Rectangle}) and edition 
 * ({@link Edition})
 * .<p/>
 *
 * @param <K> The CRTP parameter is used to ensure that the keys are inherited
 * of a persistent entity.
 * @param <E>
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 * @author Caetano Traina Júnior <caetano@icmc.usp.br>
 */
public interface Key<K extends Key<K> & Entity<? super K>>{

    /**
     * Checks if another key have the same attribute 
     * values. To ensure that only objects from the same class 
     * are compared, Key is a parameterized class and their 
     * specializations follow the CRTP. 
     * 
     * @param key Another key
     * @return true if both keys has the same attribute values.
     */
    public boolean hasSameKey(K key);
    
    /**
     * Retrieve the key attribute values of a bytes vector. To assist this
     * recovery, see the PullStream class.
     *
     * @see PullStream
     * @param array is a bytes vector.
     * @param position is the position vector which starts the recovery.
     * @return true if the values​were recovered.
     */
    public boolean pullKey(byte[] array, int position);

    /**
     * Stores the key attribute values in a bytes vector. To assist this
     * storing, see the PushStream class.
     *
     * @see PushStream
     * @param array is a bytes vector.
     * @param position is the position vector which starts the storing.
     */
    public void pushKey(byte[] array, int position);

    /**
     * Returns the amount bytes required to convert this key to their serialized
     * format.
     *
     * @return the amount bytes.
     */
    public int sizeOfKey();

}
