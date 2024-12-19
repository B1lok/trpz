package org.ftp.domain;

import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
public class ConnectionLimits {
  private int id;
  private int maxConnections;
  private int maxSpeed;
  private int userId;
}
