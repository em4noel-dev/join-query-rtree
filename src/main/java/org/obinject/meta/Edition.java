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

/**
 * The Edition domain establishes metric distances to chains of symbols by using
 * the Edition interface.<p/>
 *
 * Point delegates to indexed keys the responsibilities of the serialization and
 * the metric distance calculation.<p/>
 *
 * Metric distance calculation implemented for a chain of symbols are the
 * Levenshtein family and their variations.<p/>
 *
 * @param <K> The CRTP parameter is used in method signature that computes the
 * distance.
 *
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * @author Luiz Olmes Carvalho <olmes@icmc.usp.br>
 * @author Thatyana de Faria Piola Seraphim <thatyana@unifei.edu.br>
 * @author Caetano Traina Júnior <caetano@icmc.usp.br>
 */
public interface Edition<K extends Edition<K> & Entity<? super K>> extends Metric<K> {

    /**
     *
     * @return
     */
    public String getString();
}
