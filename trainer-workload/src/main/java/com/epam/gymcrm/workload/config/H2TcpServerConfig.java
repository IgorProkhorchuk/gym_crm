package com.epam.gymcrm.workload.config;

import java.sql.SQLException;
import org.h2.tools.Server;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "trainer-workload.h2.tcp.enabled", havingValue = "true")
public class H2TcpServerConfig {

  /**
   * Starts H2 TCP server for local database debugging.
   *
   * @param port TCP port
   * @return running H2 TCP server wrapper
   * @throws SQLException when H2 server cannot be started
   */
  @Bean
  public H2TcpServer h2TcpServer(
      @Value("${trainer-workload.h2.tcp.port:9092}") int port
  ) throws SQLException {
    Server server = Server.createTcpServer(
        "-tcp",
        "-tcpPort",
        String.valueOf(port),
        "-tcpAllowOthers"
    ).start();
    return new H2TcpServer(server);
  }

  public record H2TcpServer(Server server) implements DisposableBean {
    @Override
    public void destroy() {
      server.stop();
    }
  }
}
