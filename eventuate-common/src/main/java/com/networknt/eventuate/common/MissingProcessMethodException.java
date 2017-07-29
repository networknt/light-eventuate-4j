package com.networknt.eventuate.common;

public class MissingProcessMethodException extends EventuateCommandProcessingFailedUnexpectedlyException {
  private final Command command;

  public MissingProcessMethodException(Throwable e, Command command) {
    super(e);
    this.command = command;
  }

  public Command getCommand() {
    return command;
  }
}
