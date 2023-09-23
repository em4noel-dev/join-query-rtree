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
package org.obinject.meta;

import java.util.Random;

/**
 * A class that represents an immutable Universally Unique Identifier (Uuid). An
 * Uuid represents a 128-bit value.
 *
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 */
public class Uuid implements Comparable<Uuid>{

    /**
     * The most significant 64 bits of this Uuid.
     */
    private long mostSigBits;
    /**
     * The least significant 64 bits of this Uuid.
     */
    private long leastSigBits;
    private static final Random rand = new Random();

    /**
     * Constructs a new {@code Uuid} using the specified data.
     * {@code mostSigBits} is used for the most significant 64 bits of the
     * {@code Uuid} and {@code leastSigBits} becomes the least significant 64
     * bits of the {@code Uuid}.
     *
     * @param mostSigBits The most significant bits of the {@code Uuid}
     * @param leastSigBits The least significant bits of the {@code Uuid}
     */
    public Uuid(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    /**
     * Compares this Uuid with the specified Uuid.
     * <p>
     * The first of two UUIDs is greater than the second if the most significant
     * field in which the UUIDs differ is greater for the first Uuid.
     *
     * @param uuid {@code Uuid} to which this {@code Uuid} is to be compared.
     * @return -1, 0 or 1 as this {@code Uuid} is less than, equal to, or
     * greater than {@code val}.
     */
    @Override
    public int compareTo(Uuid uuid) {
        return (this.getMostSignificantBits() < uuid.getMostSignificantBits() ? -1
                : (this.getMostSignificantBits() > uuid.getMostSignificantBits() ? 1
                        : (this.getLeastSignificantBits() < uuid.getLeastSignificantBits() ? -1
                                : (this.getLeastSignificantBits() > uuid.getLeastSignificantBits() ? 1
                                        : 0))));
    }

    /**
     * Returns val represented by the specified number of hex digits.
     */
    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    /**
     * Compares this object to the specified object. The result is {@code
     * true} if and only if the argument is not {@code null}, is a {@code Uuid}
     * object, has the same variant, and contains the same value, bit for bit,
     * as this {@code Uuid}.
     *
     * @param uuid The object to be compared.
     * @return {@code true} if the objects are the same; {@code false}
     * otherwise.
     */
    public boolean equals(Uuid uuid) {
        return ((this.getMostSignificantBits() == uuid.getMostSignificantBits())
                && (this.getLeastSignificantBits() == uuid.getLeastSignificantBits()));
    }

    /**
     * Creates an {@code Uuid} from the string standard representation as
     * described in the {@link #toString} method.
     *
     * @param value A string that specifies a {@code Uuid}.
     * @return A {@code Uuid} with the specified value.
     * @throws IllegalArgumentException If name does not conform to the string
     * representation as described in {@link #toString}.
     */
    public static Uuid fromString(String value) {
        String[] components = value.split("-");
        if (components.length != 5) {
            throw new IllegalArgumentException("Invalid Uuid string: " + value);
        }
        for (int i = 0; i < 5; i++) {
            components[i] = "0x" + components[i];
        }

        long mostSigBits = Long.decode(components[0]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[1]);
        mostSigBits <<= 16;
        mostSigBits |= Long.decode(components[2]);

        long leastSigBits = Long.decode(components[3]);
        leastSigBits <<= 48;
        leastSigBits |= Long.decode(components[4]);

        return new Uuid(mostSigBits, leastSigBits);
    }

    /**
     * Static factory to retrieve random an {@code Uuid}.
     *
     * @return A randomly generated {@code Uuid}.
     */
    public static Uuid generator() {
        long leastPartMost = rand.nextInt();
        long most = rand.nextInt();
        most <<= 32;
        most += leastPartMost;

        long leastPartLeast = rand.nextInt();
        long least = rand.nextInt();
        least <<= 32;
        least += leastPartLeast;

        return new Uuid(most, least);
    }

    /**
     * Returns the least significant 64 bits of this Uuid's 128 bit value.
     *
     * @return The least significant 64 bits of this Uuid's 128 bit value.
     */
    public long getLeastSignificantBits() {
        return leastSigBits;
    }

    /**
     * Returns the most significant 64 bits of this Uuid's 128 bit value.
     *
     * @return The most significant 64 bits of this Uuid's 128 bit value.
     */
    public long getMostSignificantBits() {
        return mostSigBits;
    }

    public void setMostSignificantBits(long mostSigBits) {
        this.mostSigBits = mostSigBits;
    }

    public void setLeastSignificantBits(long leastSigBits) {
        this.leastSigBits = leastSigBits;
    }

    /**
     * Returns a {@code String} object representing this {@code Uuid}.
     *
     * @return A string representation of this {@code Uuid}.
     */
    @Override
    public String toString() {
        return (digits(mostSigBits >> 32, 8) + "-"
                + digits(mostSigBits >> 16, 4) + "-"
                + digits(mostSigBits, 4) + "-"
                + digits(leastSigBits >> 48, 4) + "-"
                + digits(leastSigBits, 12)).toUpperCase();
    }
}
