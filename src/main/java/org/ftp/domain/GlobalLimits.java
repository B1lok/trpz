package org.ftp.domain;

import java.time.LocalDateTime;
import java.util.Date;
import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
public class GlobalLimits {
  private int id;
  private int maxConnections;
  private int maxSpeed;
  private LocalDateTime updatedAt;
  private int updatedById;
}
