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

import gnu.io.UnsupportedCommOperationException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;

public interface AbstractSerialConnection {

    public void begin(int baud) throws IOException, UnsupportedCommOperationException, InterruptedException;

    public void end() throws IOException, InterruptedException;

    public void write(String str) throws IOException;

    public void waitForString(String str)throws IOException;

    public String readUntil(String str)throws IOException;

    public InputStream getRawInput();

    public BufferedReader getInput();

    public BufferedWriter getOutput();
}
