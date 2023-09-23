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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.obinject.meta.Entity;
import org.obinject.meta.Rectangle;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 *
 * @param <K>
 */
public class EuclideanGeometry<K extends Rectangle<K> & Entity<? super K>> {

    private Class<K> keyClass = null;
    private long calculatedOverlap = 0;
    private final double precisionError = 0.00000001; //incrementa nona casa

    /**
     *
     * @param keyClass
     */
    public EuclideanGeometry(Class<K> keyClass) {
        this.keyClass = keyClass;
    }

    private K newGenericType() {
        try {
            return keyClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(AbstractStructure.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     *
     * @param rect1
     * @param rect2
     * @return
     */
    public K union(K rect1, K rect2) {
        int dims = rect1.numberOfDimensions();
        double[] minPoint = new double[dims];
        double[] maxPoint = new double[dims];
        double coord1;
        double coord2;
        K mbrUnion = this.newGenericType();
        calculatedOverlap++;

        for (int i = 0; i < dims; i++) {
            coord1 = rect1.getOrigin(i);
            coord2 = rect2.getOrigin(i);
            minPoint[i] = Math.min(coord1, coord2);
            maxPoint[i] = Math.max(coord1 + rect1.getExtension(i), coord2 + rect2.getExtension(i));
            mbrUnion.setOrigin(i, minPoint[i]);
            mbrUnion.setExtension(i, maxPoint[i] - minPoint[i] + this.precisionError);
        }
        return mbrUnion;
    }

    /**
     *
     * @param rect
     * @return
     */
    public double occupancy(K rect) {
        double ocup = 1;
        int dims = rect.numberOfDimensions();

        for (int i = 0; i < dims; i++) {
            ocup *= rect.getExtension(i);
        }

        return ocup;
    }

    /**
     *
     * @param rectOverlap
     * @param rectOverlaped
     * @return
     */
    public boolean isOverlap(K rectOverlap, K rectOverlaped) {
        int dims = rectOverlap.numberOfDimensions();
        calculatedOverlap++;
//    return this.x < r.x + r.width && 
//           this.x + width > r.x && 
//           this.y < r.y + r.height && 
//           this.y + height > r.y;
        for (int i = 0; i < dims; i++) {
            if (rectOverlap.getOrigin(i) > 
                    rectOverlaped.getOrigin(i) + rectOverlaped.getExtension(i) ) {
                return false;
            }
            if (rectOverlap.getOrigin(i) + rectOverlap.getExtension(i) <
                    rectOverlaped.getOrigin(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     *
     * @return
     */
    public long getCalculatedOverlap() {
        return calculatedOverlap;
    }

    /**
     *
     */
    public void resetCalculatedOverlap() {
        calculatedOverlap = 0;
    }
}
