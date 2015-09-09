/*
 *  Copyright (c) 2011-2015 The original author or authors
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.mail.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mail.MailConfig;
import io.vertx.core.Future;

/**
 * this encapsulates open connection, initial dialogue and authentication
 *
 * @author <a href="http://oss.lehmann.cx/">Alexander Lehmann</a>
 */
class SMTPStarter {

  private static final Logger log = LoggerFactory.getLogger(SMTPStarter.class);

  Vertx vertx;
  SMTPConnection connection;
  MailConfig config;
  Handler<AsyncResult<Void>> handler;

  SMTPStarter(Vertx vertx, SMTPConnection connection, MailConfig config, Handler<AsyncResult<Void>> handler) {
    this.vertx = vertx;
    this.connection = connection;
    this.config = config;
    this.handler = handler;
  }

  void start() {
    log.debug("connection.openConnection");
    connection.openConnection(config, this::serverGreeting, this::handleError);
  }

  private void serverGreeting(String message) {
    log.debug("SMTPInitialDialogue");
    new SMTPInitialDialogue(connection, config, v -> doAuthentication(), this::handleError).start(message);
  }

  private void doAuthentication() {
    log.debug("SMTPAuthentication");
    new SMTPAuthentication(connection, config, v -> handler.handle(Future.succeededFuture(null)), this::handleError).start();
  }

  private void handleError(Throwable throwable) {
    log.debug("handleError:" + throwable);
    if (connection != null) {
      connection.setBroken();
    }
    handler.handle(Future.failedFuture(throwable));
  }

}
