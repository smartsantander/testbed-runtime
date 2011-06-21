/**********************************************************************************************************************
 * Copyright (c) 2010, coalesenses GmbH                                                                               *
 * All rights reserved.                                                                                               *
 *                                                                                                                    *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the   *
 * following conditions are met:                                                                                      *
 *                                                                                                                    *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following *
 *   disclaimer.                                                                                                      *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the        *
 *   following disclaimer in the documentation and/or other materials provided with the distribution.                 *
 * - Neither the name of the coalesenses GmbH nor the names of its contributors may be used to endorse or promote     *
 *   products derived from this software without specific prior written permission.                                   *
 *                                                                                                                    *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE      *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY   *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                *
 **********************************************************************************************************************/

package de.uniluebeck.itm.wsn.devicedrivers.trisos;


import de.uniluebeck.itm.wsn.devicedrivers.exceptions.FileLoadException;
import de.uniluebeck.itm.wsn.devicedrivers.generic.BinFileDataBlock;
import de.uniluebeck.itm.wsn.devicedrivers.generic.ChipType;
import de.uniluebeck.itm.wsn.devicedrivers.generic.IDeviceBinFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


public class TrisosBinFile implements IDeviceBinFile {

    private static final Logger log = LoggerFactory.getLogger(TrisosBinFile.class);
    private String description;
    private File hexFile = null;
    private final int blockSize = 4096;
    private final int startAddress = 0x3000;
    private String filename = null;

    /**
     * max bytes per line in a data packet
     */
    public final static int linesize = 45;

    private int blockIterator = 0;

    /**
     * checksum reset every 20 lines or end of block
     */
    public long crc = 0;

    private byte[] bytes = null;

    private int length = -1;

    protected TrisosBinFile() throws IOException {
            // TODO: Do nothing here?
    }

    public TrisosBinFile(byte[] bytes, String description) throws IOException {
            this.description = description;
            Properties props = new Properties();
            FileInputStream in = new FileInputStream("../conf/trisos-device-config.properties");
            props.load(in);
            in.close();
            String binFileName = props.getProperty("trisos.programmer.program.binfile");
            FileOutputStream os = new FileOutputStream(new File(binFileName));
            os.write(bytes);
            os.close();
            load(bytes);
    }

    public TrisosBinFile(String filename) throws Exception {
            this(new File(filename));
            this.filename = filename;
    }

    public TrisosBinFile(File hexFile) throws Exception {

            this.hexFile = hexFile;
            this.description = hexFile.getAbsolutePath();

            if (!hexFile.exists() || !hexFile.canRead()) {
                    log.error("Unable to open file: " + hexFile.getAbsolutePath());
                    throw new Exception("Unable to open file: " + hexFile.getAbsolutePath());
            }

            try {

                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(hexFile));
                    byte[] data = new byte[(int) hexFile.length()];
                    bis.read(data, 0, data.length);
                    load(data);

            } catch (Exception e) {
                    log.debug("Unable to load file: " + e, e);
                    throw new FileLoadException();
            }

    }

    private void load(byte[] data) throws IOException {

            length = 0;
            if ((int) data.length % blockSize != 0) {
                    int block_count = ((int) data.length / blockSize) + 1;
                    length = block_count * blockSize;
            } else {
                    length = (int) data.length;
            }

            bytes = new byte[length];
            System.arraycopy(data, 0, bytes, 0, data.length);

            for (int i = data.length; i < length; i++) {
                    bytes[i] = (byte) 0xff;
            }

            System.out.println("Extending file 2 to " + this.length);
            System.out.println("Last bytes: " + this.bytes[this.length - 2] + " " + this.bytes[this.length - 1]);

            // log.debug("Read " + length + " bytes from " + binFile.getAbsolutePath());
            // log.debug("Total of blocks in " + binFile.getName() + ": " + getBlockCount());
            // int displayBytes = 200;
            // log.debug("Bin File starts with: " + Tools.toHexString(bytes, 0, bytes.length < displayBytes ?
            // bytes.length : displayBytes));
    }

    @Override
    public ChipType getFileType() {
        return ChipType.TRISOS;
    }

    @Override
    public boolean isCompatible(ChipType deviceType) {
        return deviceType.equals(getFileType());
    }

    @Override
    public void resetBlockIterator() {
        System.err.println("Trisos resetBlockIterator called");
    }

    @Override
    public boolean hasNextBlock() {
        System.err.println("Trisos hasNextBlock called");
        return true;
    }

    @Override
    public BinFileDataBlock getNextBlock() {
        System.err.println("Trisos getNextBlock called");
        return null;
    }

    @Override
    public int getBlockCount() {
        System.err.println("Trisos getBlockCount called");
        return -1;
    }

    @Override
    public int getLength() {
        System.err.println("Trisos getLength called");
        return -1;
    }

    public File getFile()
    {
        return this.hexFile;
    }

    public String getFilename()
    {
            return this.filename;
    }


}