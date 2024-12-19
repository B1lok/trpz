package org.ftp.domain;

import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
public class Group {
  private int id;
  private String name;
}
