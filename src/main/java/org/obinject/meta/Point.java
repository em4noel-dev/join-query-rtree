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
 * The punctual domain associates a set of coordinates to a key, 
 * using the Point interface. <p/>
 * 
 * Point delegates to indexed keys the responsibilities of the serialization,
 * the metric distance calculation and the spatial position definition.<p/>
 * 
 * Serialization can been seen in {@link Key} interface.<p/>
 * 
 * Metric distance calculation can been seen in {@link Metric}
 * interface.<p/>
 * 
 * Spatial position defines the number of dimensions and the coordinates 
 * on each dimension.<p/>
 * 
 * @param <K> The CRTP parameter is used in method signature
 * that computes the distance.
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public interface Point<K extends Point<K> & Entity<? super K>> extends Metric<K>
{

    /**
     * Returns the origin value of a axis.
     *
     * @param axis is the axis of interest.
     * @return the origin value
     */
    public double getOrigin(int axis);

    /**
     * Returns the number of dimensions.
     *
     * @return the amount of dimensions.
     */
    public int numberOfDimensions();

    /**
     * Sets the origin value of a axis.
     *
     * @param axis is the axis of interest.
     * @param value is the origin value.
     */
    public void setOrigin(int axis, double value);
}
