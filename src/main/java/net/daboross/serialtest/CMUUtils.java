/*
 * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.daboross.serialtest;

import java.nio.charset.Charset;
import java.util.Collection;

public class CMUUtils {

    public static final Charset CHARSET = Charset.forName("ASCII");

    public static byte[] toBytes(String str) {
        return str.getBytes(CHARSET);
    }

    public static String toString(byte... bytes) {
        return new String(bytes, CHARSET);
    }


    public static int average(Collection<Integer> ints) {
        int sum = 0;
        for (int i : ints) {
            sum += i;
        }
        return sum / ints.size();
    }
}
