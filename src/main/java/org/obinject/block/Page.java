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

import java.util.Collection;
import org.obinject.meta.Uuid;

/**
 * <p>
 * A {@code Page} is a block to store a node.</p>
 *
 * <blockquote><pre>
 * {@code
 * Design:
 *
 * +----------+-------------------------------------------------------------+
 * | modified |                                                             |
 * |          |                                                             |
 * | boolean  |<------------------------ free space ----------------------->|
 * |----------|                                                             |
 * |  header  |                                                             |
 * +----------+-------------------------------------------------------------+
 * }
 * </pre></blockquote>
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public abstract class Page {

    /**
     *
     */
    protected byte[] array;
    /**
     *
     */
    protected static final int bitsPerByte = 8;
    /**
     *
     */
    protected static final int byteMask = 0xff;
    /**
     *
     */
    public static final int sizeOfBoolean = 1;
    /**
     *
     */
    public static final int sizeOfByte = 1;
    /**
     *
     */
    public static final int sizeOfCharacter = 2;
    /**
     *
     */
    public static final int sizeOfCalendar = 9;
    /**
     *
     */
    public static final int sizeOfDate = 8;
    /**
     *
     */
    public static final int sizeOfDouble = 8;
    /**
     *
     */
    public static final int sizeOfFloat = 4;
    /**
     *
     */
    public static final int sizeOfInteger = 4;
    /**
     *
     */
    public static final int sizeOfLong = 8;
    /**
     *
     */
    public static final int sizeOfShort = 2;
    /**
     *
     */
    public static final int sizeOfUuid = 16;

    /**
     *
     * @param array
     */
    public Page(byte[] array) {
        this.array = array;
    }

    /**
     *
     * @param matrix1
     * @param matrix2
     * @return
     */
    public static boolean matricesAreEqual(Object matrix1, Object matrix2) {
        String strType1 = matrix1.getClass().getName();
        int dim1 = strType1.length() - 1;
        char type1 = strType1.charAt(dim1);

        String strType2 = matrix2.getClass().getName();
        int dim2 = strType2.length() - 1;
        char type2 = strType2.charAt(dim2);

        if (dim2 != dim2 || type1 != type2) {
            return false;
        } else {
            return matricesAreEqualRecursive(dim1, type1, matrix1, matrix2);
        }
    }

    private static boolean matricesAreEqualRecursive(int dimension, char type,
            Object matrix1, Object matrix2) {
        if (dimension == 1) {
            switch (type) {
                case 'Z':
                    boolean vetZ1[] = (boolean[]) matrix1;
                    boolean vetZ2[] = (boolean[]) matrix2;
                    if (vetZ1.length == vetZ2.length) {
                        for (int i = 0; i < vetZ1.length; i++) {
                            if (vetZ1[i] != vetZ2[2]) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                case 'B':
                    byte vetB1[] = (byte[]) matrix1;
                    byte vetB2[] = (byte[]) matrix2;
                    if (vetB1.length == vetB2.length) {
                        for (int i = 0; i < vetB1.length; i++) {
                            if (vetB1[i] != vetB2[2]) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                case 'C':
                    char vetC1[] = (char[]) matrix1;
                    char vetC2[] = (char[]) matrix2;
                    if (vetC1.length == vetC2.length) {
                        for (int i = 0; i < vetC1.length; i++) {
                            if (vetC1[i] != vetC2[2]) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                case 'D':
                    double vetD1[] = (double[]) matrix1;
                    double vetD2[] = (double[]) matrix2;
                    if (vetD1.length == vetD2.length) {
                        for (int i = 0; i < vetD1.length; i++) {
                            if (vetD1[i] != vetD2[2]) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                case 'F':
                    float vetF1[] = (float[]) matrix1;
                    float vetF2[] = (float[]) matrix2;
                    if (vetF1.length == vetF2.length) {
                        for (int i = 0; i < vetF1.length; i++) {
                            if (vetF1[i] != vetF2[2]) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                case 'I':
                    int vetI1[] = (int[]) matrix1;
                    int vetI2[] = (int[]) matrix2;
                    if (vetI1.length == vetI2.length) {
                        for (int i = 0; i < vetI1.length; i++) {
                            if (vetI1[i] != vetI2[2]) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                case 'J':
                    long vetJ1[] = (long[]) matrix1;
                    long vetJ2[] = (long[]) matrix2;
                    if (vetJ1.length == vetJ2.length) {
                        for (int i = 0; i < vetJ1.length; i++) {
                            if (vetJ1[i] != vetJ2[2]) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
                case 'S':
                    short vetS1[] = (short[]) matrix1;
                    short vetS2[] = (short[]) matrix2;
                    if (vetS1.length == vetS2.length) {
                        for (int i = 0; i < vetS1.length; i++) {
                            if (vetS1[i] != vetS2[2]) {
                                return false;
                            }
                        }
                        return true;
                    } else {
                        return false;
                    }
            }
        } else {
            Object vetObj1[] = (Object[]) matrix1;
            Object vetObj2[] = (Object[]) matrix2;
            if (vetObj1.length == vetObj2.length) {
                for (int i = 0; i < vetObj1.length; i++) {
                    return matricesAreEqualRecursive(dimension - 1, type, vetObj1[i], vetObj2[i]);
                }

            }

        }

        return false;
    }

    /**
     *
     * @return
     */
    public static int sizeOfHeader() {
        return sizeOfBoolean;
    }

    /**
     *
     * @return
     */
    public int sizeOfArray() {
        return array.length;
    }

    /**
     *
     * @param entity
     * @return
     */
    public static int sizeOfEntity(Object entity) {
        if (entity != null) {
            return sizeOfInteger + Page.sizeOfUuid;
        } else {
            return sizeOfInteger;
        }
    }

    /**
     *
     * @param collection
     * @return
     */
    public static int sizeOfEntityCollection(Collection collection) {
        if (collection != null) {
            return sizeOfInteger + (collection.size() * Page.sizeOfUuid);
        } else {
            return sizeOfInteger;
        }
    }

    /**
     *
     * @param str
     * @return
     */
    public static int sizeOfString(String str) {
        if (str != null) {
            return sizeOfInteger + str.getBytes().length;
        } else {
            return sizeOfInteger;
        }
    }

    /**
     *
     * @param matrix
     * @return
     */
    public static int sizeOfMatrix(Object matrix) {
        String strType = matrix.getClass().getName();
        int dimensions = strType.length() - 1;
        char type = strType.charAt(dimensions);

        return sizeOfByte + sizeOfInteger + sizeOfMatrixRecursive(dimensions, type, matrix);
    }

    private static int sizeOfMatrixRecursive(int dimension, char type, Object matrix) {
        int total = sizeOfInteger;

        if (dimension == 1) {
            switch (type) {
                case 'Z':
                    boolean vetZ[] = (boolean[]) matrix;
                    total += vetZ.length * sizeOfBoolean;
                    break;
                case 'B':
                    byte vetB[] = (byte[]) matrix;
                    total += vetB.length * sizeOfByte;
                    break;
                case 'C':
                    char vetC[] = (char[]) matrix;
                    total += vetC.length * sizeOfByte;
                    break;
                case 'D':
                    double vetD[] = (double[]) matrix;
                    total += vetD.length * sizeOfDouble;
                    break;
                case 'F':
                    float vetF[] = (float[]) matrix;
                    total += vetF.length * sizeOfFloat;
                    break;
                case 'I':
                    int vetI[] = (int[]) matrix;
                    total += vetI.length * sizeOfInteger;
                    break;
                case 'J':
                    long vetJ[] = (long[]) matrix;
                    total += vetJ.length * sizeOfLong;
                    break;
                case 'S':
                    short vetS[] = (short[]) matrix;
                    total += vetS.length * sizeOfShort;
                    break;
            }
        } else {
            Object vetO[] = (Object[]) matrix;

            for (int i = 0; i < vetO.length; i++) {
                total += sizeOfMatrixRecursive(dimension - 1, type, vetO[i]);
            }
        }

        return total;
    }

    public boolean readModified() {
        return readBoolean(0);
    }

    /**
     *
     * @param pos
     * @return
     */
    protected boolean readBoolean(int pos) {
        return (array[pos] == 1);
    }

    /**
     *
     * @param pos
     * @return
     */
    protected byte readByte(int pos) {
        return array[pos];
    }

    protected char readCharacter(int pos) {
        char value = 0;
        value <<= bitsPerByte;
        value += array[pos] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 1] & byteMask;
        return value;
    }

    /**
     *
     * @param pos
     * @return
     */
    protected double readDouble(int pos) {
        return Double.longBitsToDouble(readLong(pos));
    }

    /**
     *
     * @param pos
     * @return
     */
    protected float readFloat(int pos) {
        return Float.intBitsToFloat(readInteger(pos));
    }

    /**
     *
     * @param pos
     * @return
     */
    protected int readInteger(int pos) {
        int value = 0;
        value <<= bitsPerByte;
        value += array[pos] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 1] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 2] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 3] & byteMask;
        return value;
    }

    /**
     *
     * @param pos
     * @return
     */
    protected long readLong(int pos) {
        long value = 0;
        value <<= bitsPerByte;
        value += array[pos] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 1] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 2] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 3] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 4] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 5] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 6] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 7] & byteMask;
        return value;
    }

    /**
     *
     * @param pos
     * @return
     */
    protected short readShort(int pos) {
        short value = 0;
        value <<= bitsPerByte;
        value += array[pos] & byteMask;
        value <<= bitsPerByte;
        value += array[pos + 1] & byteMask;
        return value;
    }

    /**
     *
     * @param pos
     * @return
     */
    protected Uuid readUuid(int pos) {
        long most = this.readLong(pos);
        long least = this.readLong(pos + sizeOfLong);
        return new Uuid(most, least);
    }

    /**
     *
     * @param pos
     * @param length
     * @return
     */
    protected String readString(int pos, int length) {
        return new String(array, pos, length);
    }

    /**
     *
     * @param pos
     * @param value
     */
    protected void writeBoolean(int pos, boolean value) {
        array[pos] = value ? (byte) 1 : (byte) 0;
        writeModified(true);
    }

    /**
     *
     * @param pos
     * @param value
     */
    protected void writeByte(int pos, byte value) {
        array[pos] = value;
        writeModified(true);
    }

    protected void writeCharacter(int pos, char value) {
        array[pos + sizeOfCharacter - 1] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfCharacter - 2] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        writeModified(true);
    }

    /**
     *
     * @param pos
     * @param value
     */
    protected void writeDouble(int pos, double value) {
        writeLong(pos, Double.doubleToRawLongBits(value));
    }

    /**
     *
     * @param pos
     * @param value
     */
    protected void writeFloat(int pos, float value) {
        writeInteger(pos, Float.floatToRawIntBits(value));
    }

    /**
     *
     * @param pos
     * @param value
     */
    protected void writeInteger(int pos, int value) {
        array[pos + sizeOfInteger - 1] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfInteger - 2] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfInteger - 3] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfInteger - 4] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        writeModified(true);
    }

    /**
     *
     * @param pos
     * @param value
     */
    protected void writeLong(int pos, long value) {
        array[pos + sizeOfLong - 1] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfLong - 2] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfLong - 3] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfLong - 4] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfLong - 5] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfLong - 6] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfLong - 7] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfLong - 8] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        writeModified(true);
    }

    /**
     *
     * @param pos
     * @param value
     */
    protected void writeShort(int pos, int value) {
        array[pos + sizeOfShort - 1] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        array[pos + sizeOfShort - 2] = (byte) (value & byteMask);
        value >>= bitsPerByte;
        writeModified(true);
    }

    /**
     *
     * @param pos
     * @param value
     */
    protected void writeUuid(int pos, Uuid value) {
        writeLong(pos, value.getMostSignificantBits());
        writeLong(pos + sizeOfLong, value.getLeastSignificantBits());
    }

    protected void writeString(int pos, byte[] value) {
        if (value != null) {
            for (int i = 0; i < value.length; i++) {
                array[pos + i] = value[i];
            }
        }
    }

    protected void moveLeft(int dest, int source, int length) {
        for (int i = 0; i < length; i++) {
            array[dest + i] = array[source + i];
        }
        writeModified(true);
    }

    protected void moveRight(int dest, int source, int length) {
        for (int i = 0; i < length; i++) {
            array[dest + length - i - 1] = array[source + length - i - 1];
        }
        writeModified(true);
    }

    /**
     * Writes boolean value to modified info. WARNING: the array write must be
     * done hardcode (array[0]) so that an infinite looping doesn't happen.
     *
     * If writeModified calls writeBoolean, then: writeModified -> writeBoolean
     * -> writeModified -> writeBoolean.... ... and so on.
     *
     * Thus, the position of Modified boolean must be inserted hardcode.
     *
     * @param pos
     * @param value
     */
    private void writeModified(boolean value) {
        array[0] = value ? (byte) 1 : (byte) 0;
    }
}
