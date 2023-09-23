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

/**
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Carlos Ferro <carlosferro@gmail.com>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 *
 */
public class AveragePerformance implements PerformanceMeasurement {

    private long measurements = 0;
    private long totalDiskAcess = 0;
    private long totalTime = 0;
    private long totalVerifications = 0;

    /**
     *
     * @return
     */
    @Override
    public long amountOfMeasurements() {
        return measurements;
    }

    /**
     *
     * @return
     */
    @Override
    public long getTotalDiskAcess() {
        return totalDiskAcess;
    }

    /**
     *
     * @return
     */
    @Override
    public long getTotalTime() {
        return totalTime;
    }

    /**
     *
     * @return
     */
    @Override
    public long getTotalVerifications() {
        return totalVerifications;
    }

    /**
     *
     * @return
     */
    @Override
    public double measuredDiskAccess() {
        if (measurements == 0) {
            return 0;
        }
        return ((double) totalDiskAcess) / ((double) measurements);
    }

    /**
     *
     * @return
     */
    @Override
    public double measuredTime() {
        if (measurements == 0) {
            return 0;
        }
        return ((double) totalTime) / ((double) measurements);
    }

    /**
     *
     * @return
     */
    @Override
    public double measuredVerifications() {
        if (measurements == 0) {
            return 0;
        }
        return ((double) totalVerifications) / ((double) measurements);
    }

    /**
     *
     */
    @Override
    public void resetMeasurements() {
        measurements = 0;
        totalDiskAcess = 0;
        totalTime = 0;
        totalVerifications = 0;
    }

    @Override
    public void incrementMeasurement() {
        measurements++;
    }

    @Override
    public void incrementDiskAccess(long diskAccess) {
        totalDiskAcess += diskAccess;
    }

    @Override
    public void incrementVerification(long verifications) {
        totalVerifications += verifications;
    }

    @Override
    public void incrementTime(long time) {
        totalTime += time;
    }
}
