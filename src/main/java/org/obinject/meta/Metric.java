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
 * Metrics satisfy the postulates of a metric space (symmetry, non-negativity,
 * identity and triangle inequality).<p/>
 *
 * Metric delegates to indexed keys the responsibilities of the 
 * metric distance calculation.<p/>
 * 
 * The most common metrics for points (Lp family) were implemented in
 * auxiliary classes {@link DistanceUtil}.<p/>
 *
 * @param <K> The CRTP parameter is used in method signature that computes the
 * distance.
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 * @author Caetano Traina Júnior <caetano@icmc.usp.br>
 */
public interface Metric<K extends Metric<K> & Entity<? super K>> extends Key<K> {

    /**
     *
     * @param obj
     * @return
     */
    public double distanceTo(K obj);

    /**
     *
     * @return
     */
    public double getPreservedDistance();

    /**
     *
     * @param distance
     */
    public void setPreservedDistance(double distance);
}
