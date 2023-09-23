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
package org.obinject.block;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.obinject.meta.Uuid;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public class PullPage extends Page {

    /**
     *
     */
    protected int position;

    /**
     *
     * @param array
     * @param position
     */
    public PullPage(byte[] array, int position) {
        super(array);
        this.position = position;
    }

    /**
     *
     * @return
     */
    public boolean pullBoolean() {
        boolean value = this.readBoolean(position);
        position++;
        return value;
    }

    /**
     *
     * @return
     */
    public byte pullByte() {
        byte value = this.readByte(position);
        position++;
        return value;
    }

    /**
     *
     * @return
     */
    public char pullCharacter() {
        char value = this.readCharacter(position);
        position += sizeOfCharacter;
        return value;
    }

    /**
     *
     * @return
     */
    public Calendar pullCalendar() {
        byte type = pullByte();
        switch (type) {
            case 1:
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTimeInMillis(pullLong());
                return gc;
            default:
                pullLong();
                return null;
        }
    }

    /**
     *
     * @return
     */
    public double pullDouble() {
        return Double.longBitsToDouble(pullLong());
    }

    /**
     *
     * @return
     */
    public Date pullDate() {
        long value = pullLong();
        if (value == Long.MIN_VALUE) {
            return null;
        } else {
            return new Date(value);
        }
    }

    /**
     *
     * @return
     */
    public float pullFloat() {
        return Float.intBitsToFloat(pullInteger());
    }

    /**
     *
     * @return
     */
    public int pullInteger() {
        int value = this.readInteger(position);
        position += sizeOfInteger;
        return value;
    }

    /**
     *
     * @return
     */
    public long pullLong() {
        long value = this.readLong(position);
        position += sizeOfLong;
        return value;
    }

    /**
     *
     * @return
     */
    public short pullShort() {
        short value = this.readShort(position);
        position += sizeOfShort;
        return value;
    }

    /**
     *
     * @return
     */
    public String pullString() {
        int length = this.readInteger(position);
        position += sizeOfInteger;
        String s = null;
        if(length > 0){
            s = this.readString(position, length);
        }
        position += length;
        return s;
    }

    /**
     *
     * @return
     */
    public Uuid pullUuid() {
        long most = this.pullLong();
        long least = this.pullLong();
        return new Uuid(most, least);
    }

    /**
     *
     * @return
     */
    public Object pullMatrix() {
        byte type = this.pullByte();
        int dimensions = this.pullInteger();

        if (dimensions == 1) {
            switch (type) {
                case 'Z':
                    boolean vetZ[] = new boolean[this.pullInteger()];
                    for (int i = 0; i < vetZ.length; i++) {
                        vetZ[i] = this.pullBoolean();
                    }
                    return vetZ;
                case 'B':
                    byte vetB[] = new byte[this.pullInteger()];
                    for (int i = 0; i < vetB.length; i++) {
                        vetB[i] = this.pullByte();
                    }
                    return vetB;
                case 'C':
                    char vetC[] = new char[this.pullInteger()];
                    for (int i = 0; i < vetC.length; i++) {
                        vetC[i] = (char) this.pullByte();
                    }
                    return vetC;
                case 'D':
                    double vetD[] = new double[this.pullInteger()];
                    for (int i = 0; i < vetD.length; i++) {
                        vetD[i] = this.pullDouble();
                    }
                    return vetD;
                case 'F':
                    float vetF[] = new float[this.pullInteger()];
                    for (int i = 0; i < vetF.length; i++) {
                        vetF[i] = this.pullFloat();
                    }
                    return vetF;
                case 'I':
                    int vetI[] = new int[this.pullInteger()];
                    for (int i = 0; i < vetI.length; i++) {
                        vetI[i] = this.pullInteger();
                    }
                    return vetI;
                case 'J':
                    long vetJ[] = new long[this.pullInteger()];
                    for (int i = 0; i < vetJ.length; i++) {
                        vetJ[i] = this.pullLong();
                    }
                    return vetJ;
                case 'L':
                    Uuid vetL[] = new Uuid[this.pullInteger()];
                    for (int i = 0; i < vetL.length; i++) {
                        vetL[i] = this.pullUuid();
                    }
                    return vetL;
                case 'S':
                    short vetS[] = new short[this.pullInteger()];
                    for (int i = 0; i < vetS.length; i++) {
                        vetS[i] = this.pullShort();
                    }
                    return vetS;
            }

            return null;
        } else {
            return pullMatrixRecursive(type, dimensions, 1, this.pullInteger());
        }
    }

    private Object[] pullMatrixRecursive(byte type, int dimensions, int actualDimension, int dimensionLength) {

        Object[] o = null;

        switch (type) {
            case 'Z':
                o = new boolean[dimensionLength][];
                break;
            case 'B':
                o = new byte[dimensionLength][];
                break;
            case 'C':
                o = new char[dimensionLength][];
                break;
            case 'D':
                o = new double[dimensionLength][];
                break;
            case 'F':
                o = new float[dimensionLength][];
                break;
            case 'I':
                o = new int[dimensionLength][];
                break;
            case 'J':
                o = new long[dimensionLength][];
                break;
            case 'L':
                o = new Uuid[dimensionLength][];
                break;
            case 'S':
                o = new short[dimensionLength][];
                break;
        }

        for (int i = 0; i < dimensionLength; i++) {
            if (actualDimension == dimensions - 1) {
                switch (type) {
                    case 'Z':
                        boolean vetZ[] = new boolean[this.pullInteger()];
                        for (int j = 0; j < vetZ.length; j++) {
                            vetZ[j] = this.pullBoolean();
                        }
                        o[i] = vetZ;
                        break;
                    case 'B':
                        byte vetB[] = new byte[this.pullInteger()];
                        for (int j = 0; j < vetB.length; j++) {
                            vetB[j] = this.pullByte();
                        }
                        o[i] = vetB;
                        break;
                    case 'C':
                        char vetC[] = new char[this.pullInteger()];
                        for (int j = 0; j < vetC.length; j++) {
                            vetC[j] = (char) this.pullByte();
                        }
                        o[i] = vetC;
                        break;
                    case 'D':
                        double vetD[] = new double[this.pullInteger()];
                        for (int j = 0; j < vetD.length; j++) {
                            vetD[j] = this.pullDouble();
                        }
                        o[i] = vetD;
                        break;
                    case 'F':
                        float vetF[] = new float[this.pullInteger()];
                        for (int j = 0; j < vetF.length; j++) {
                            vetF[j] = this.pullFloat();
                        }
                        o[i] = vetF;
                        break;
                    case 'I':
                        int vetI[] = new int[this.pullInteger()];
                        for (int j = 0; j < vetI.length; j++) {
                            vetI[j] = this.pullInteger();
                        }
                        o[i] = vetI;
                        break;
                    case 'J':
                        long vetJ[] = new long[this.pullInteger()];
                        for (int j = 0; j < vetJ.length; j++) {
                            vetJ[j] = this.pullLong();
                        }
                        o[i] = vetJ;
                        break;
                    case 'L':
                        Uuid vetL[] = new Uuid[this.pullInteger()];
                        for (int j = 0; j < vetL.length; j++) {
                            vetL[j] = this.pullUuid();
                        }
                        o[i] = vetL;
                        break;
                    case 'S':
                        short vetS[] = new short[this.pullInteger()];
                        for (int j = 0; j < vetS.length; j++) {
                            vetS[j] = this.pullShort();
                        }
                        o[i] = vetS;
                        break;
                }
            } else {
                o = pullMatrixRecursive(type, dimensions, actualDimension + 1, this.pullInteger());
            }
        }

        return o;
    }
}
