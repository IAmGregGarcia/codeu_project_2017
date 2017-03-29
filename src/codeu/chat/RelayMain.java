// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat;

import java.io.IOException;

import codeu.chat.relay.Server;
import codeu.chat.relay.ServerFrontEnd;
import codeu.chat.util.Logger;
import codeu.chat.util.Timeline;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;
import codeu.chat.util.connections.ServerConnectionSource;

final class RelayMain {

  private static final Logger.Log LOG = Logger.newLog(RelayMain.class);

  public static void main(String[] args) {

    Logger.enableConsoleOutput();

    try {
      Logger.enableFileOutput("chat_relay_log.log");
    } catch (IOException ex) {
      LOG.error(ex, "Failed to set logger to write to file");
    }

    LOG.info("============================= START OF LOG =============================");

    final int myPort = Integer.parseInt(args[0]);

    try (final ConnectionSource source = ServerConnectionSource.forPort(myPort)) {

      LOG.info("Starting relay...");

      startRelay(source);

    } catch (IOException ex) {
      LOG.error(ex, "Failed to establish server accept port");
    }
  }

  private static void startRelay(ConnectionSource source) {

    final Server relay = new Server(1024, 16);
    LOG.info("Relay object created.");

    final ServerFrontEnd frontEnd = new ServerFrontEnd(relay);
    LOG.info("Relay front end object created.");

    final Timeline timeline = new Timeline();
    LOG.info("Relay timeline created.");

    // TODO: Load team information

    LOG.info("Starting relay main loop...");

    while (true) {
      try {

        LOG.info("Established connection...");
        final Connection connection = source.connect();
        LOG.info("Connection established.");

        timeline.scheduleNow(new Runnable() {
          @Override
          public void run() {
            try {
              frontEnd.handleConnection(connection);
            } catch (Exception ex) {
              LOG.error(ex, "Exception handling connection.");
            }
          }
        });

      } catch (IOException ex) {
        LOG.error(ex, "Failed to establish connection.");
      }
    }
  }
}
