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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
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
public abstract class KNearestNeighborSequential<M extends Metric<M> & Entity<? super M>> extends AbstractStrategy<M> {

    private final M object;
    private final int k;

    public KNearestNeighborSequential(Sequential sequential, M object, int k) {
        super(sequential);
        this.object = object;
        this.k = k;
    }

    private class InvertedResult implements Comparator<Metric> {

        @Override
        public int compare(Metric o1, Metric o2) {
            if (o1.getPreservedDistance() > o2.getPreservedDistance()) {
                return -1;
            } else if (o1.getPreservedDistance() == o2.getPreservedDistance()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    @Override
    public Collection<M> solve() {
        Session se = getStructure().getWorkspace().openSession();
        PriorityQueue<M> result = new PriorityQueue<>(this.k + 10,
                new KNearestNeighborSequential.InvertedResult());
        LinkedList<M> repetedList = new LinkedList<>();
        long firstNode = getStructure().getRootPageId();
        double range = Double.MAX_VALUE;
        if (firstNode != 0) {
            long actualPageId = firstNode;
            long firstPageId = actualPageId;
            int total;
            double dist;
            do {
                SequentialNode actualSeqNode
                        = new SequentialNode(se.load(actualPageId), getStructure().getObjectClass());
                total = actualSeqNode.readNumberOfEntitries();
                for (int i = 0; i < total; i++) {
                    M build = (M) actualSeqNode.buildEntity(i);
                    dist = object.distanceTo(build);
                    if (dist <= range) {
                        build.setPreservedDistance(dist);
                        result.offer(build);
                        if (result.size() >= k) {
                            if (result.size() > k) {
                                //remove of result list the upper bounds
                                while ((result.size() > 0)
                                        && (result.peek().getPreservedDistance() == range)) {
                                    repetedList.add(result.poll());
                                }
                                // if list has less than k
                                if (result.size() < k) {
                                    //add list of upper bound											
                                    result.addAll(repetedList);
                                }
                                repetedList.clear();
                            }
                            // update range
                            if (!result.isEmpty()) {
                                range = result.peek().getPreservedDistance();
                            }
                        }
                    }
                }
                actualPageId = actualSeqNode.readNextPageId();
            } while (actualPageId != firstPageId);

        }
        se.close();

        return result;
    }

}
