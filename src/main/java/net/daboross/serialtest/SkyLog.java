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

import java.util.regex.Pattern;

public class SkyLog {

    public static void log(String msg, Object... args) {
        System.out.println(String.format(msg, args));
        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            ((Throwable) args[args.length - 1]).printStackTrace();
        }
    }

    public static void out(String msg) {
        System.out.print(msg.replaceAll(Pattern.quote("\r"), "\n"));
    }

    public static void ex(Throwable ex) {
        System.err.println("Unexpected " + ex.getClass().getSimpleName() + ":");
        ex.printStackTrace(System.err);
    }

    public static void err(String msg, Object... args) {
        System.err.println(String.format(msg, args));
    }
}
