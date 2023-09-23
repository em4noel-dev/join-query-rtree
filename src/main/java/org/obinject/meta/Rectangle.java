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
 * The rectangular domain associates an area, volume or hyper-volume to a key by
 * using Rectangle interface.
 *
 * Point delegates to indexed keys the responsibilities of the serialization,
 * the metric distance calculation, the spatial position definition and the
 * spatial extension.<p/>
 *
 * Serialization can been seen in {@link Key} interface.<p/>
 *
 * Metric distance calculation can been seen in {@link Metric} interface.<p/>
 *
 * Spatial position defines the number of dimensions and the coordinates on each
 * dimension.<p/>
 *
 * Spatial extension defines the size in each dimension (width, height, depth...)
 * .<p/>
 *
 * Rectangles are used as a simplified geometry for line and polygon objects in
 * spatial databases and such representation is referred as Minimum Bounding
 * Rectangle (MBR)
 * .<p/>
 *
 * @param <K> The CRTP parameter is used in method signature that computes the
 * distance.
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 * @author Caetano Traina Júnior <caetano@icmc.usp.br>
 */
public interface Rectangle<K extends Rectangle<K> & Entity<? super K>> extends Metric<K> {

    /**
     * Returns the extension value of a axis.
     *
     * @param axis is the axis of interest.
     * @return the origin value
     */
    public double getExtension(int axis);

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
     * Sets the extension value of a axis.
     *
     * @param axis is the axis of interest.
     * @param value is the origin value.
     */
    public void setExtension(int axis, double value);

    /**
     * Sets the origin value of a axis.
     *
     * @param axis is the axis of interest.
     * @param value is the origin value.
     */
    public void setOrigin(int axis, double value);
}
