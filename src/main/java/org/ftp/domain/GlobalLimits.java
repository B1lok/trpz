package org.ftp.domain;

import java.util.Date;
import lombok.Data;


@Data
public class GlobalLimits {
  private int id;
  private int maxConnections;
  private int maxSpeed;
  private Date updatedAt;
  private int updatedById;
}
