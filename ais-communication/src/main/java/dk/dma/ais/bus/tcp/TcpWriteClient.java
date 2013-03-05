/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.bus.tcp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writing TCP client.
 */
@ThreadSafe
public class TcpWriteClient extends TcpClient {

    private static final Logger LOG = LoggerFactory.getLogger(TcpWriteClient.class);

    public TcpWriteClient(IClientStoppedListener stopListener, Socket socket, TcpClientConf conf) {
        super(stopListener, socket, conf);
    }

    @Override
    public void run() {
        try {
            // Open output stream
            OutputStream outputStream;
            if (conf.isGzipCompress()) {
                outputStream = new GZIPOutputStream(socket.getOutputStream(), conf.getGzipBufferSize());
            } else {
                outputStream = socket.getOutputStream();
            }
            PrintWriter writer = new PrintWriter(outputStream);
            List<String> list = new ArrayList<>();

            // Pull-write loop
            while (true) {
                // Pull from queue
                pull(list);
                // Write to client
                for (String str : list) {
                    writer.println(str);                   
                }
                if (writer.checkError()) {
                    throw new IOException("Connection to client lost");
                }
            }
        } catch (IOException e) {
            LOG.info("Connection lost to client");
        } catch (InterruptedException e) {
            e.printStackTrace();
            // TODO
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

        stopping();
    }

    /**
     * Send message
     * @param msg
     */
    public void send(String msg) {
        push(msg);
    }

}
