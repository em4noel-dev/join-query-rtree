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

import org.obinject.block.SequentialDescriptor;
import org.obinject.block.SequentialNode;
import org.obinject.device.Session;
import org.obinject.device.Workspace;
import org.obinject.meta.Entity;
import org.obinject.meta.Uuid;
import org.obinject.queries.AveragePerformance;
import org.obinject.queries.PerformanceMeasurement;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 *
 * @param <E>
 */
public abstract class Sequential<E extends Entity<E>> extends AbstractEntityStructure<E> {

    private final PerformanceMeasurement averageForAdd = new AveragePerformance();
    private final PerformanceMeasurement averageForFind = new AveragePerformance();

    /**
     *
     * @param workspace
     */
    public Sequential(Workspace workspace) {
        super(workspace);

        Session se = this.getWorkspace().openSession();
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        SequentialDescriptor descriptor = new SequentialDescriptor(se.load(pageIdDescriptor));

        se.close();
    }

    @Override
    public boolean add(E entity) {
        long time = System.nanoTime();
        SequentialNode<E> newNode;
        SequentialNode<E> end;

        Session se = this.getWorkspace().openSession();
        long diskAccess = se.getBlockAccess();
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        SequentialDescriptor descriptor = new SequentialDescriptor(se.load(pageIdDescriptor));

        if (descriptor.readBeginPageId() == 0) {
            //create SequentialNode
            newNode = new SequentialNode<>(se.create(), this.getObjectClass());
            // Circularly link
            newNode.writePreviousPageId(newNode.getPageId());
            newNode.writeNextPageId(newNode.getPageId());

            descriptor.writeBeginPageId(newNode.getPageId());
            descriptor.writeEndPageId(newNode.getPageId());
        }//endif
        end = new SequentialNode<>(se.load(descriptor.readEndPageId()), this.getObjectClass());

        //adding object in end node
        if (end.addEntity(entity) == false) {
            // node is full, creating new SEQUENTIALNODE
            newNode = new SequentialNode<>(se.create(), this.getObjectClass());
            //adding object
            newNode.addEntity(entity);
            // Circularly link
            newNode.writeNextPageId(descriptor.readBeginPageId());
            newNode.writePreviousPageId(end.getPageId());
            end.writeNextPageId(newNode.getPageId());
            SequentialNode<E> beginNode = new SequentialNode<>(se.load(descriptor.readBeginPageId()), this.getObjectClass());
            beginNode.writePreviousPageId(newNode.getPageId());
            //ajust new end node
            descriptor.writeEndPageId(newNode.getPageId());
        }

        //clean home		
        se.close();
        //statistic for add
        diskAccess = se.getBlockAccess() - diskAccess;
        averageForAdd.incrementDiskAccess(diskAccess);
        time = System.nanoTime() - time;
        averageForAdd.incrementTime(time);
        averageForAdd.incrementMeasurement();
        return true;
    }

    @Override
    public long getRootPageId() {
        Session se = this.getWorkspace().openSession();
        long rootPageId = 0;
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        SequentialDescriptor descriptor = new SequentialDescriptor(se.load(pageIdDescriptor));
        rootPageId = descriptor.readBeginPageId();

        se.close();

        return rootPageId;
    }

    @Override
    public E find(Uuid uuid) {
        long time = System.nanoTime();
        E entity = null;
        Session se = this.getWorkspace().openSession();
        long diskAccess = se.getBlockAccess();
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        SequentialDescriptor descriptor = new SequentialDescriptor(se.load(pageIdDescriptor));
        long actualPageId = descriptor.readBeginPageId();
        long firstPageId = actualPageId;

        if (actualPageId != 0) {

            do {
                SequentialNode<E> actualSeqNode = new SequentialNode<>(se.load(actualPageId), this.getObjectClass());

                entity = actualSeqNode.findUuid(uuid);
                averageForFind.incrementVerification(actualSeqNode.getVerifications());
                actualPageId = actualSeqNode.readNextPageId();
            } while (entity == null && actualPageId != firstPageId);

        }
        se.close();
        //statistic for add
        diskAccess = se.getBlockAccess() - diskAccess;
        averageForFind.incrementDiskAccess(diskAccess);
        time = System.nanoTime() - time;
        averageForFind.incrementTime(time);
        averageForFind.incrementMeasurement();
        return entity;
    }

    /**
     *
     * @param entity
     * @return
     */
    public Uuid find(E entity) {
        long time = System.nanoTime();
        Uuid uuid = null;

        Session se = this.getWorkspace().openSession();
        long diskAccess = se.getBlockAccess();
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        SequentialDescriptor descriptor = new SequentialDescriptor(se.load(pageIdDescriptor));
        long actualPageId = descriptor.readBeginPageId();
        long firstPageId = actualPageId;

        if (actualPageId != 0) {
            do {
                SequentialNode<E> actualSeqNode = new SequentialNode<>(se.load(actualPageId), this.getObjectClass());

                uuid = actualSeqNode.findEntity(entity);
                averageForFind.incrementVerification(actualSeqNode.getVerifications());
                actualPageId = actualSeqNode.readNextPageId();
            } while (uuid == null && actualPageId != firstPageId);

        }
        se.close();
        //statistic for add
        diskAccess = se.getBlockAccess() - diskAccess;
        averageForFind.incrementDiskAccess(diskAccess);
        time = System.nanoTime() - time;
        averageForFind.incrementTime(time);
        averageForFind.incrementMeasurement();
        return uuid;
    }

    /**
     *
     */
    public void showAll() {
        Session se = this.getWorkspace().openSession();
        long actualPageId = 0;
        long pageIdDescriptor = se.findPageIdDescriptor(this.getClassUuid());
        SequentialDescriptor descriptor = new SequentialDescriptor(se.load(pageIdDescriptor));
        actualPageId = descriptor.readBeginPageId();
    }

    public PerformanceMeasurement getAverageForAdd() {
        return averageForAdd;
    }

    public PerformanceMeasurement getAverageForFind() {
        return averageForFind;
    }

    @Override
    public boolean remove(E key) {
        return false;
    }
}
