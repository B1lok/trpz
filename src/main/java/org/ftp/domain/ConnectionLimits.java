package org.ftp.domain;

import lombok.Data;


@Data
public class ConnectionLimits {
  private int id;
  private int maxConnections;
  private int userId;
}
