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

import java.util.LinkedList;
import java.util.Stack;
import org.obinject.block.Node;
import org.obinject.block.RTreeDescriptor;
import org.obinject.block.RTreeIndex;
import org.obinject.block.RTreeLeaf;
import org.obinject.block.RTreeNode;
import org.obinject.device.Session;
import org.obinject.device.Workspace;
import org.obinject.meta.Entity;
import org.obinject.meta.Rectangle;
import org.obinject.meta.Uuid;
import org.obinject.queries.AveragePerformance;
import org.obinject.queries.PerformanceMeasurement;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 *
 * @param <R>
 */
public abstract class RTree<R extends Rectangle<R> & Entity<? super R>> extends AbstractKeyStructure<R> {

    public EuclideanGeometry<R> geometry = new EuclideanGeometry<>(this.getObjectClass());
    PerformanceMeasurement averageForAdd = new AveragePerformance();
    PerformanceMeasurement averageForFind = new AveragePerformance();

    /**
     *
     * @param workspace
     */
    public RTree(Workspace workspace) {
        super(workspace);
        Session se = this.getWorkspace().openSession();
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        RTreeDescriptor descriptor = new RTreeDescriptor(se.load(pageIdDescriptor));

        se.close();
    }

//	public void height()
//	{
//		Session se = this.getWorkspace().openSession();
//		long pageIdDescriptor = se.findPageIdDescriptor(key.getClassID());
//		RTreeDescriptor descriptor = new RTreeDescriptor(se.load(pageIdDescriptor));
//		System.out.println("altura: " + descriptor.readTreeHeight());
//	}
    @Override
    public boolean add(R key) {
        long time = System.nanoTime();
        Session se = this.getWorkspace().openSession();
        long diskAccess = se.getBlockAccess();
        long verifications = this.geometry.getCalculatedOverlap();
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        RTreeDescriptor descriptor = new RTreeDescriptor(se.load(pageIdDescriptor));
        long rootPageId = descriptor.readRootPageId();

        if (rootPageId == 0) // Tree is empty
        {
            // is not necessary to increments disk access
            RTreeLeaf<R> leaf = new RTreeLeaf<>(se.create(), this.getObjectClass());
            leaf.addKey(key, key.getUuid());
            // Circularly link
            leaf.writePreviousPageId(leaf.getPageId());
            leaf.writeNextPageId(leaf.getPageId());
            descriptor.writeRootPageId(leaf.getPageId());
            descriptor.incTreeHeight();
        } else {
            long[] path = new long[descriptor.readTreeHeight()];
            Node node = se.load(rootPageId);

            int level = 1;
            // Descend to leaf
            while (RTreeIndex.matchNodeType(node)) {
                RTreeIndex<R> index = new RTreeIndex<>(node, this.getObjectClass());
                path[level] = index.getPageId();
                level++;
                int qualify = index.indexOfInsertion(key, this.geometry); // Entry that qualifies
                long subPageId = index.readSubPageId(qualify);
                node = se.load(subPageId);
            }

            RTreeLeaf<R> leaf = new RTreeLeaf<>(node, this.getObjectClass());

            if (leaf.addKey(key, key.getUuid()) == false) // If cannot add, split leaf
            {
                //////////////////////////////////////
                boolean promote = true;
                RTreeLeaf<R> newLeaf = new RTreeLeaf<>(se.create(), this.getObjectClass());
                RTreeIndex<R> index, newIndex;
                long pageIdRem = leaf.getPageId();

                RTreePromotion objPromote = this.splitLeaf(se, leaf, newLeaf, key, key.getUuid());

                level = path.length - 1;

                while (level > 0) {
                    node = se.load(path[level]);

                    if (promote == true) {
                        index = new RTreeIndex<>(node, this.getObjectClass());
                        int idxReplc = index.indexOfSubPageId(pageIdRem);
                        index.replace(idxReplc, objPromote.getFirstKey());

                        if (index.addKey(objPromote.getSecondKey(), objPromote.getSecondSubPageId())) {
                            promote = false;
                        } else {
                            newIndex = new RTreeIndex<>(se.create(), this.getObjectClass());
                            objPromote = this.splitIndex(se, index, newIndex, objPromote.getSecondKey(), objPromote.getSecondSubPageId());
//							nodeLeft = index;
//							nodeRight = newIndex;
                            pageIdRem = node.getPageId();
                            promote = true;
                        }
                    }

                    level--;
                }

                if (promote == true) {
                    // Promote
                    newIndex = new RTreeIndex<>(se.create(), this.getObjectClass());
                    newIndex.addKey(objPromote.getFirstKey(), objPromote.getFistSubPageId());
                    newIndex.addKey(objPromote.getSecondKey(), objPromote.getSecondSubPageId());
                    // Circularly link
                    newIndex.writePreviousPageId(newIndex.getPageId());
                    newIndex.writeNextPageId(newIndex.getPageId());
                    //ajust rootPageId										
                    descriptor.writeRootPageId(newIndex.getPageId());
                    descriptor.incTreeHeight();
                }
            }
        }
        se.close();
        //statistic for add
        diskAccess = se.getBlockAccess() - diskAccess;
        averageForAdd.incrementDiskAccess(diskAccess);
        verifications = this.geometry.getCalculatedOverlap() - verifications;
        averageForAdd.incrementVerification(verifications);
        time = System.nanoTime() - time;
        averageForAdd.incrementTime(time);
        averageForAdd.incrementMeasurement();

        return true;
    }
    
    
    @Override
    public Uuid find(R key) {
        long time = System.nanoTime();
        Session se = this.getWorkspace().openSession();
        long diskAccess = se.getBlockAccess();
        long verifications = this.geometry.getCalculatedOverlap();
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        RTreeDescriptor descriptor = new RTreeDescriptor(se.load(pageIdDescriptor));
        int total, overlap;
        long pageId;
        R storedKey;
        Uuid uuidFinded = null;

        Stack<Long> qualifies = new Stack<>();
        qualifies.push(descriptor.readRootPageId());
        do {
            pageId = qualifies.pop();
            Node node = se.load(pageId);

            if (RTreeIndex.matchNodeType(node)) { // não é folha
                RTreeIndex<R> index = new RTreeIndex<>(node, this.getObjectClass());
                total = index.readNumberOfKeys();
                overlap = 0;

                for (int i = 0; i < total; i++) {
                    storedKey = index.buildKey(i);

                    if (geometry.isOverlap(storedKey, key)) {
                        qualifies.add(qualifies.size() - overlap, index.readSubPageId(i));
                        overlap++;
                    }
                }
            } else { // folha
                RTreeLeaf<R> leaf = new RTreeLeaf<>(node, this.getObjectClass());
                int idx = leaf.indexOfKey(key);
                if (idx != -1) {
                    uuidFinded = leaf.readEntityUuid(idx);
                    break;
                }
            }
        } while (!qualifies.isEmpty());
        se.close();
        //statistic for add
        diskAccess = se.getBlockAccess() - diskAccess;
        averageForFind.incrementDiskAccess(diskAccess);
        verifications = this.geometry.getCalculatedOverlap() - verifications;
        averageForFind.incrementVerification(verifications);
        time = System.nanoTime() - time;
        averageForFind.incrementTime(time);
        averageForFind.incrementMeasurement();
        return uuidFinded;
    }

    @Override
    public long getRootPageId() {
        Session se = this.getWorkspace().openSession();
        long rootPageId = 0;
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        RTreeDescriptor descriptor = new RTreeDescriptor(se.load(pageIdDescriptor));
        rootPageId = descriptor.readRootPageId();

        se.close();
        return rootPageId;
    }

    /**
     *
     * @return
     */
    public PerformanceMeasurement getAverageForAdd() {
        return averageForAdd;
    }

    /**
     *
     * @return
     */
    public PerformanceMeasurement getAverageForFind() {
        return averageForFind;
    }

    public int height() {
        int height = 0;

        Session se = this.getWorkspace().openSession();
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        RTreeDescriptor descriptor = new RTreeDescriptor(se.load(pageIdDescriptor));
        height = descriptor.readTreeHeight();
        se.close();
        return height;
    }

    private RTreePromotion splitLeaf(Session se, RTreeLeaf<R> fullLeaf, RTreeLeaf<R> newLeaf, R key, Uuid uuid) {
        int i, j;
        int total = fullLeaf.readNumberOfKeys() + 1;

        // Copying keys and Uuids
        R[] vecKey = (R[]) new Rectangle[total];
        Uuid[] vecUuid = new Uuid[total];
        for (i = 0; i < total - 1; i++) {
            vecKey[i] = fullLeaf.buildKey(i);
            vecUuid[i] = fullLeaf.readEntityUuid(i);
        }
        vecKey[i] = key;
        vecUuid[i] = uuid;

        // Choose promote
        double ocup = -1.0d;

        RTreePromotion objPromote;

        double d;
        int idx1 = 0, idx2 = 1;

        for (i = 0; i < total - 1; i++) // Quadratic Split
        {
            for (j = i + 1; j < total; j++) {
                R mbrUnion = geometry.union(vecKey[i], vecKey[j]);
                // Calc Dead space: union(mbr1, mbr2) - (mbr1 + mbr2 - intersec(mbr1, mbr2))
                d = geometry.occupancy(mbrUnion) //mbrUnion.occupancy()
                        - (geometry.occupancy(vecKey[i])
                        + geometry.occupancy(vecKey[j]));

                if (d > ocup) {
                    ocup = d;
                    idx1 = i;
                    idx2 = j;
                }
            }
        }

        //cleanning fullIndex and inserting vecKey[idx1]
        fullLeaf.clear();
        fullLeaf.addKey(vecKey[idx1], vecUuid[idx1]);
        //cleanning newIndex and inserting vecKey[idx2]
        newLeaf.clear();
        newLeaf.addKey(vecKey[idx2], vecUuid[idx2]);
        //promoted MBR
        objPromote = new RTreePromotion(vecKey[idx1], fullLeaf.getPageId(), vecKey[idx2], newLeaf.getPageId());
        // Distributing keys
        for (i = 0; i < total; i++) {
            if (!(i == idx1 || i == idx2)) {
                R union1 = this.geometry.union(objPromote.getFirstKey(), vecKey[i]);
                double ocup1 = geometry.occupancy(union1) - geometry.occupancy(objPromote.getFirstKey());
                R union2 = this.geometry.union(objPromote.getSecondKey(), vecKey[i]);
                double ocup2 = geometry.occupancy(union2) - geometry.occupancy(objPromote.getSecondKey());

                if (ocup1 < ocup2) {
                    if (!fullLeaf.addKey(vecKey[i], vecUuid[i])) {
                        throw new RuntimeException("add fail");
                    }
                    objPromote.setFirstKey(union1);
                } else if (ocup1 > ocup2) {
                    if (!newLeaf.addKey(vecKey[i], vecUuid[i])) {
                        throw new RuntimeException("add fail");
                    }
                    objPromote.setSecondKey(union2);
                } else {
                    double ocupProm1 = geometry.occupancy(objPromote.getFirstKey());
                    double ocupProm2 = geometry.occupancy(objPromote.getSecondKey());
                    if (ocupProm1 < ocupProm2) {
                        if (!fullLeaf.addKey(vecKey[i], vecUuid[i])) {
                            throw new RuntimeException("add fail");
                        }
                        objPromote.setFirstKey(union1);
                    } else if (ocupProm1 > ocupProm2) {
                        if (!newLeaf.addKey(vecKey[i], vecUuid[i])) {
                            throw new RuntimeException("add fail");
                        }
                        objPromote.setSecondKey(union2);
                    } else {
                        double dist1 = objPromote.getFirstKey().distanceTo(vecKey[i]);
                        double dist2 = objPromote.getSecondKey().distanceTo(vecKey[i]);
						averageForAdd.incrementVerification(2);

                        if (dist1 < dist2) {
                            if (!fullLeaf.addKey(vecKey[i], vecUuid[i])) {
                                throw new RuntimeException("add fail");
                            }
                            objPromote.setFirstKey(union1);
                        } else if (dist1 > dist2) {
                            if (!newLeaf.addKey(vecKey[i], vecUuid[i])) {
                                throw new RuntimeException("add fail");
                            }
                            objPromote.setSecondKey(union2);
                        } else if (fullLeaf.readNumberOfKeys() <= newLeaf.readNumberOfKeys()) {
                            if (!fullLeaf.addKey(vecKey[i], vecUuid[i])) {
                                throw new RuntimeException("add fail");
                            }
                            objPromote.setFirstKey(union1);
                        } else {
                            if (!newLeaf.addKey(vecKey[i], vecUuid[i])) {
                                throw new RuntimeException("add fail");
                            }
                            objPromote.setSecondKey(union2);
                        }
                    }
                }
            }
        }

        // Circularly link
        newLeaf.writeNextPageId(fullLeaf.readNextPageId());
        fullLeaf.writeNextPageId(newLeaf.getPageId());
        newLeaf.writePreviousPageId(fullLeaf.getPageId());
        long nextPageId = newLeaf.readNextPageId();
        if (nextPageId != fullLeaf.getPageId()) {
            RTreeLeaf<R> nextLeaf = new RTreeLeaf<>(se.load(nextPageId), this.getObjectClass());
            nextLeaf.writePreviousPageId(newLeaf.getPageId());
        } else {
            fullLeaf.writePreviousPageId(newLeaf.getPageId());
        }
        return objPromote;
    }

    private RTreePromotion splitIndex(Session se, RTreeIndex<R> fullIndex, RTreeIndex<R> newIndex, R key, long subId) {
        int i, j;
        int total = fullIndex.readNumberOfKeys() + 1;

        // Copying keys and Subs
        R[] vecKey = (R[]) new Rectangle[total];
        long[] vecSub = new long[total];
        for (i = 0; i < total - 1; i++) {
            vecKey[i] = fullIndex.buildKey(i);
            vecSub[i] = fullIndex.readSubPageId(i);
        }
        vecKey[i] = key;
        vecSub[i] = subId;

        // Choose promote
        double ocup = -1.0d;
        RTreePromotion objPromote;

        double d;
        int idx1 = 0, idx2 = 1;

        for (i = 0; i < total - 1; i++) // Quadratic Split
        {
            for (j = i + 1; j < total; j++) {
                R mbrUnion = geometry.union(vecKey[i], vecKey[j]);
                // Calc Dead space: union(mbr1, mbr2) - (mbr1 + mbr2 - intersec(mbr1, mbr2))
                d = geometry.occupancy(mbrUnion)
                        - (geometry.occupancy(vecKey[i])
                        + geometry.occupancy(vecKey[j]));

                if (d > ocup) {
                    ocup = d;
                    idx1 = i;
                    idx2 = j;
                }
            }
        }
        //cleanning fullIndex and inserting vecKey[idx1]
        fullIndex.clear();
        fullIndex.addKey(vecKey[idx1], vecSub[idx1]);
        //cleanning newIndex and inserting vecKey[idx2]
        newIndex.clear();
        newIndex.addKey(vecKey[idx2], vecSub[idx2]);
        //promoted MBR
        objPromote = new RTreePromotion(vecKey[idx1], fullIndex.getPageId(), vecKey[idx2], newIndex.getPageId());
        // Distributing keys
        for (i = 0; i < total; i++) {
            if (!(i == idx1 || i == idx2)) {
                R union1 = geometry.union(objPromote.getFirstKey(), vecKey[i]);
                double ocup1 = geometry.occupancy(union1) - geometry.occupancy(objPromote.getFirstKey());
                R union2 = geometry.union(objPromote.getSecondKey(), vecKey[i]);
                double ocup2 = geometry.occupancy(union2) - geometry.occupancy(objPromote.getSecondKey());

                if (ocup1 < ocup2) {
                    if (!fullIndex.addKey(vecKey[i], vecSub[i])) {
                        throw new RuntimeException("add fail");
                    }
                    objPromote.setFirstKey(union1);
                } else if (ocup1 > ocup2) {
                    if (!newIndex.addKey(vecKey[i], vecSub[i])) {
                        throw new RuntimeException("add fail");
                    }
                    objPromote.setSecondKey(union2);
                } else {
                    double ocupProm1 = geometry.occupancy(objPromote.getFirstKey());
                    double ocupProm2 = geometry.occupancy(objPromote.getSecondKey());
                    if (ocupProm1 < ocupProm2) {
                        if (!fullIndex.addKey(vecKey[i], vecSub[i])) {
                            throw new RuntimeException("add fail");
                        }
                        objPromote.setFirstKey(union1);
                    } else if (ocupProm1 > ocupProm2) {
                        if (!newIndex.addKey(vecKey[i], vecSub[i])) {
                            throw new RuntimeException("add fail");
                        }
                        objPromote.setSecondKey(union2);
                    } else {
                        double dist1 = objPromote.getFirstKey().distanceTo(vecKey[i]);
                        double dist2 = objPromote.getSecondKey().distanceTo(vecKey[i]);
						averageForAdd.incrementVerification(2);

                        if (dist1 < dist2) {
                            if (!fullIndex.addKey(vecKey[i], vecSub[i])) {
                                throw new RuntimeException("add fail");
                            }
                            objPromote.setFirstKey(union1);
                        } else if (dist1 > dist2) {
                            if (!newIndex.addKey(vecKey[i], vecSub[i])) {
                                throw new RuntimeException("add fail");
                            }
                            objPromote.setSecondKey(union2);
                        } else if (fullIndex.readNumberOfKeys() <= newIndex.readNumberOfKeys()) {
                            if (!fullIndex.addKey(vecKey[i], vecSub[i])) {
                                throw new RuntimeException("add fail");
                            }
                            objPromote.setFirstKey(union1);
                        } else {
                            if (!newIndex.addKey(vecKey[i], vecSub[i])) {
                                throw new RuntimeException("add fail");
                            }
                            objPromote.setSecondKey(union2);
                        }
                    }
                }
            }
        }

        // Circularly linked list
        newIndex.writeNextPageId(fullIndex.readNextPageId());
        fullIndex.writeNextPageId(newIndex.getPageId());
        newIndex.writePreviousPageId(fullIndex.getPageId());
        long nextPageId = newIndex.readNextPageId();
        if (nextPageId != fullIndex.getPageId()) {
            RTreeIndex<R> nextIndex = new RTreeIndex<>(se.load(nextPageId), this.getObjectClass());
            nextIndex.writePreviousPageId(newIndex.getPageId());
        } else {
            fullIndex.writePreviousPageId(newIndex.getPageId());
        }
        return objPromote;
    }

    class RTreePromotion {

        private R firstKey;
        private long fistSubPageId;
        private R secondKey;
        private long secondSubPageId;

        public RTreePromotion(R firstKey, long fistSubPageId, R secondKey, long secondSubPageId) {
            this.firstKey = firstKey;
            this.fistSubPageId = fistSubPageId;
            this.secondKey = secondKey;
            this.secondSubPageId = secondSubPageId;
        }

        public R getFirstKey() {
            return firstKey;
        }

        public long getFistSubPageId() {
            return fistSubPageId;
        }

        public R getSecondKey() {
            return secondKey;
        }

        public long getSecondSubPageId() {
            return secondSubPageId;
        }

        public void setFirstKey(R firstKey) {
            this.firstKey = firstKey;
        }

        public void setSecondKey(R secondKey) {
            this.secondKey = secondKey;
        }
    }

    /**
     *
     */
    public void showAll() {
        Session se = this.getWorkspace().openSession();
        RTreeDescriptor descriptor = null;

        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        descriptor = new RTreeDescriptor(se.load(pageIdDescriptor));

        Node node = se.load(descriptor.readRootPageId());

        while (RTreeIndex.matchNodeType(node)) {
            RTreeIndex<R> index = new RTreeIndex<>(node, this.getObjectClass());
            long subPageId = index.readSubPageId(0);
            node = se.load(subPageId);
        }

        long firstPageId = node.getPageId();
        long actualPageId = firstPageId;

        int count = 1;
        do {
            RTreeLeaf<R> leaf = new RTreeLeaf<>(se.load(actualPageId), this.getObjectClass());

            for (int i = 0; i < leaf.readNumberOfKeys(); i++) {
                R storedKey = leaf.buildKey(i);
                System.out.println(count + ": " + storedKey);
                count++;
            }

            actualPageId = leaf.readNextPageId();
        } while (actualPageId != firstPageId);
        se.close();
    }

    /**
     *
     */
    public void bfs() {
        Session se = this.getWorkspace().openSession();
        RTreeDescriptor descriptor = null;
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        descriptor = new RTreeDescriptor(se.load(pageIdDescriptor));

        LinkedList<RTreeNode> fila = new LinkedList<>();

        Node nod = se.load(descriptor.readRootPageId());
        RTreeNode node;

        if (RTreeIndex.matchNodeType(nod)) {
            node = new RTreeIndex<>(nod, this.getObjectClass());
        } else {
            node = new RTreeLeaf<>(nod, this.getObjectClass());
        }

        fila.addLast(node);

        while (!fila.isEmpty()) {
            RTreeNode n = fila.removeFirst();

            int total = n.readNumberOfKeys();

            System.out.println("===== Page ID: " + n.getPageId() + " =====");
            for (int i = 0; i < total; i++) {
                R obj = (R) n.buildKey(i);
                System.out.print("Chave: " + obj);

                if (RTreeIndex.matchNodeType(n)) {
                    RTreeIndex<R> index = new RTreeIndex<>(n, this.getObjectClass());
                    long sub = index.readSubPageId(i);
                    Node nod2 = se.load(sub);
                    RTreeNode node2;

                    if (RTreeIndex.matchNodeType(nod2)) {
                        node2 = new RTreeIndex<>(nod2, this.getObjectClass());
                    } else {
                        node2 = new RTreeLeaf<>(nod2, this.getObjectClass());
                    }
                    fila.addLast(node2);

                    System.out.print(" \tPrev.: " + index.readPreviousPageId()
                            + " \tNext.: " + index.readNextPageId()
                            + " \tSub.: " + index.readSubPageId(i) + "\n");
                } else {
                    RTreeLeaf<R> leaf = new RTreeLeaf<>(n, this.getObjectClass());

                    System.out.print(" \tPrev.: " + leaf.readPreviousPageId()
                            + " \tNext.: " + leaf.readNextPageId()
                            + " \tUUID.: " + leaf.readEntityUuid(i) + "\n");
                }
            }
        }

        se.close();
    }

    /**
     * Remove key that has the same uuid.
     *
     * @param key
     * @return
     */
    @Override
    public boolean remove(R key) {
        return false;
    }

}
