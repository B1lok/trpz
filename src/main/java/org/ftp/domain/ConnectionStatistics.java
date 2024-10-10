package org.ftp.domain;

import java.util.Date;
import lombok.Data;


@Data
public class ConnectionStatistics {
  private int id;
  private Date connectionTime;
  private Date disconnectionTime;
  private int userId;
}
