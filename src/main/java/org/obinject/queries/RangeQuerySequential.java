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
package org.obinject.queries;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.obinject.block.SequentialNode;
import org.obinject.device.Session;
import org.obinject.meta.Entity;
import org.obinject.meta.Metric;
import org.obinject.storage.Sequential;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Carlos Ferro <carlosferro@gmail.com>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 * @param <M>
 */
public abstract class RangeQuerySequential<M extends Metric<M> & Entity<? super M>> extends AbstractStrategy<M>{

    private final M object;
    private final double range;

    public RangeQuerySequential(Sequential sequential, M object, double range) {
        super(sequential);
        this.object = object;
        this.range = range;
    }


    @Override
    public Collection<M> solve() {
        Session se = this.getStructure().getWorkspace().openSession();
        List<M> result = new LinkedList<>();
        long firstNode = this.getStructure().getRootPageId();
        if (firstNode != 0) {
            long actualPageId = firstNode;
            long firstPageId = actualPageId;
            int total;
            double dist;
            do {
                SequentialNode actualSeqNode = 
                        new SequentialNode(se.load(actualPageId), this.getStructure().getObjectClass());
                total = actualSeqNode.readNumberOfEntitries();
                for (int i = 0; i < total; i++) {
                    M build = (M) actualSeqNode.buildEntity(i);
                    dist = object.distanceTo(build);
                    if (dist <= range) {
                        build.setPreservedDistance(dist);
                        result.add(build);
                    }
                }
                actualPageId = actualSeqNode.readNextPageId();
            } while (actualPageId != firstPageId);

        }
        se.close();

        return result;
    }

}
