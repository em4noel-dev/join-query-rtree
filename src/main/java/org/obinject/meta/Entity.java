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


/**
 * The persistent entities, extends the user-defined classes 
 * and implements the Entity interface. The extension avoids 
 * modifications in the user-defined classes.<p/>
 * 
 * The Entity implementation provides the methods required for 
 * persistence.<p/>
 * 
 * The Entity delegates to persistent entities the responsibilities: 
 * object serialization, object equivalence, object uniqueness and class 
 * identity.<p/>
 * 
 * Object serialization is performed by using auxiliary classes
 * {@link PushStream} and {@link PullStream}. These classes are used in push and
 * pull methods in order to convert objects to their serialized format.<p/>
 * 
 * Object equivalence checks if two distinct entities have the same 
 * attribute values.<p/>
 * 
 * Object uniqueness is used by the data structure to retrieve 
 * instances of each stored object.<p/>
 * 
 * Class identity is used to ensure that a retrieved object is 
 * indeed an instance of the correct class.<p/>
 * 
 * @param <E> The CRTP parameter is used in method signature that 
 * verifies the object equivalence.
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 * @author Caetano Traina Júnior <caetano@icmc.usp.br>
 */
public interface Entity<E extends Entity<E>>{        
    
    /**
     * Checks if another entity have the same attribute 
     * values. To ensure that only objects from the same class 
     * are compared, Entity is a parameterized class and their 
     * specializations follow the CRTP. 
     * 
     * @param entity Another entity
     * @return true if both entities has the same attribute values.
     */
    public boolean isEqual(E entity);

    /**
     * Returns a 128-bits value that always is unique, ensuring
     * the uniqueness of each object instance. This value is used by 
     * the data structure to retrieve instances of each stored object. 
     *
     * @return The value unique
     */
    public Uuid getUuid();

    /**
     * Retrieve the class attribute values of a bytes vector.
     * To assist this recovery, see the PullStream class.
     * 
     * @see PullStream
     * @param array is a bytes vector.
     * @param position is the position vector which starts the recovery.
     * @return true if values are really of this class.
     */
    public boolean pullEntity(byte[] array, int position);

    /**
     * Stores the class attribute values in a bytes vector.
     * To assist this storing, see the PushStream class.
     *
     * @see PushStream
     * @param array is a bytes vector.
     * @param position is the position vector which starts the storing.
     */
    public void pushEntity(byte[] array, int position);

    /**
     * Returns the amount bytes required to convert this entity 
     * to their serialized format.
     *
     * @return the amount bytes.
     */
    public int sizeOfEntity();
    
}
