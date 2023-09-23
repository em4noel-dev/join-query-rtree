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
import org.obinject.meta.Entity;
import org.obinject.meta.Uuid;

/**
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public class PushPage extends Page {

    /**
     *
     */
    protected int position;
    
    /**
     *
     * @param array
     * @param position
     */
    public PushPage(byte[] array, int position) {
        super(array);
        this.position = position;
    }

    /**
     *
     * @param value
     */
    public void pushBoolean(boolean value) {
        this.writeBoolean(position, value);
        position++;
    }

    /**
     *
     * @param value
     */
    public void pushByte(byte value) {
        this.writeByte(position, value);
        position++;
    }

    /**
     *
     * @param calendar
     */
    public void pushCalendar(Calendar calendar) {
        if (calendar instanceof GregorianCalendar) {
            pushByte((byte) 1); //GregorianCalendar
        }
        else{
            pushByte((byte) 0);
        }
        pushLong(calendar.getTimeInMillis());
    }

    /**
     *
     * @param value
     */
    public void pushCharacter(char value) {
        this.writeCharacter(position, value);
        position += sizeOfCharacter;
    }

    /**
     *
     * @param date
     */
    public void pushDate(Date date) {
        if (date == null) {
            pushLong(Long.MIN_VALUE);
        } else {
            pushLong(date.getTime());
        }
    }

    /**
     *
     * @param value
     */
    public void pushDouble(double value) {
        pushLong(Double.doubleToRawLongBits(value));  
    }

    /**
     *
     * @param value
     */
    public void pushFloat(float value) {
        pushInteger(Float.floatToRawIntBits(value));
    }

    /**
     *
     * @param value
     */
    public void pushInteger(int value) {
        this.writeInteger(position, value);
        position += sizeOfInteger;
    }

    /**
     *
     * @param value
     */
    public void pushLong(long value) {
        this.writeLong(position, value);
        position += sizeOfLong;
    }

    /**
     *
     * @param value
     */
    public void pushShort(short value) {
        this.writeShort(position, value);
        position += sizeOfShort;
    }

    /**
     *
     * @param value
     */
    public void pushString(String value) {
        if (value == null) {
            writeInteger(position, 0);
            position += sizeOfInteger;
        }else{
            writeInteger(position, value.getBytes().length);
            position += sizeOfInteger;
            writeString(position, value.getBytes());
            position += value.getBytes().length;         
        }
    }

    /**
     *
     * @param value
     */
    public void pushUuid(Uuid value) {
        this.pushLong(value.getMostSignificantBits());
        this.pushLong(value.getLeastSignificantBits());
    }

    /**
     *
     * @param matrix
     */
    public void pushMatrix(Object matrix) {
        String strType = matrix.getClass().getName();
        int dimensions = strType.length() - 1;
        byte type = (byte) strType.charAt(dimensions);

        this.pushByte(type);
        this.pushInteger(dimensions);
        this.pushMatrixRecursive(matrix, type, dimensions);
    }

    private void pushMatrixRecursive(Object matrix, byte type, int dimension) {
        if (dimension == 1) {
            switch (type) {
                case 'Z':
                    boolean vetZ[] = (boolean[]) matrix;
                    this.pushInteger(vetZ.length);
                    for (int i = 0; i < vetZ.length; i++) {
                        this.pushBoolean(vetZ[i]);
                    }
                    break;
                case 'B':
                    byte vetB[] = (byte[]) matrix;
                    this.pushInteger(vetB.length);
                    for (int i = 0; i < vetB.length; i++) {
                        this.pushByte(vetB[i]);
                    }
                    break;
                case 'C':
                    char vetC[] = (char[]) matrix;
                    this.pushInteger(vetC.length);
                    for (int i = 0; i < vetC.length; i++) {
                        this.pushCharacter(vetC[i]);
                    }
                    break;
                case 'D':
                    double vetD[] = (double[]) matrix;
                    this.pushInteger(vetD.length);
                    for (int i = 0; i < vetD.length; i++) {
                        this.pushDouble(vetD[i]);
                    }
                    break;
                case 'F':
                    float vetF[] = (float[]) matrix;
                    this.pushInteger(vetF.length);
                    for (int i = 0; i < vetF.length; i++) {
                        this.pushFloat(vetF[i]);
                    }
                    break;
                case 'I':
                    int vetI[] = (int[]) matrix;
                    this.pushInteger(vetI.length);
                    for (int i = 0; i < vetI.length; i++) {
                        this.pushInteger(vetI[i]);
                    }
                    break;
                case 'J':
                    long vetJ[] = (long[]) matrix;
                    this.pushInteger(vetJ.length);
                    for (int i = 0; i < vetJ.length; i++) {
                        this.pushLong(vetJ[i]);
                    }
                    break;
                case 'L':
                    Entity vetL[] = (Entity[]) matrix;
                    this.pushInteger(vetL.length);
                    for (int i = 0; i < vetL.length; i++) {
                        this.pushUuid(vetL[i].getUuid());
                    }
                    break;
                case 'S':
                    short vetS[] = (short[]) matrix;
                    this.pushInteger(vetS.length);
                    for (int i = 0; i < vetS.length; i++) {
                        this.pushShort(vetS[i]);
                    }
                    break;
            }
        } else {
            Object vetO[] = (Object[]) matrix;
            this.pushInteger(vetO.length);
            for (int i = 0; i < vetO.length; i++) {
                pushMatrixRecursive(vetO[i], type, dimension - 1);
            }
        }
    }
}
