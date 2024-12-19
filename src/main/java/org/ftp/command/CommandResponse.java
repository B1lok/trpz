package org.ftp.command;

public class CommandResponse {

  private final int code;
  private final String message;

  private CommandResponse(Builder builder) {
    this.code = builder.code;
    this.message = builder.message;
  }

  @Override
  public String toString() {
    if (message.split("\n").length > 1)
      return code + "-" + message + "\n" + code + " End of response";
    else
      return code + " " + message;
  }

  public static class Builder {

    private int code;
    private String message;

    public Builder withCode(int code) {
      this.code = code;
      return this;
    }

    public Builder withMessage(String message) {
      this.message = message;
      return this;
    }

    public CommandResponse build() {
      return new CommandResponse(this);
    }
  }
}
