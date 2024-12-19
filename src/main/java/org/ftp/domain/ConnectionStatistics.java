package org.ftp.domain;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
public class ConnectionStatistics {
  private int id;
  private LocalDateTime connectionTime;
  private LocalDateTime disconnectionTime;
  private int userId;
}
