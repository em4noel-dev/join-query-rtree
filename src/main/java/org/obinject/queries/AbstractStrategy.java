/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.obinject.queries;

import java.util.Collection;
import org.obinject.storage.AbstractStructure;

/**
 *
 * @author windows
 * @param <T>
*/
public abstract class AbstractStrategy<T> {

    private final PerformanceMeasurement performanceMeasurement = new AveragePerformance();
    private final AbstractStructure<T> structure;
    
    public AbstractStrategy(AbstractStructure<T> structure) {
        this.structure = structure;
    }

    public abstract Collection<? extends Object> solve();

    public PerformanceMeasurement getPerformanceMeasurement() {
        return performanceMeasurement;
    }

    public AbstractStructure<T> getStructure() {
        return structure;
    }
    
}
