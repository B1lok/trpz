package org.ftp.command.impl;

import static org.ftp.util.FileSystemUtils.getServerRootId;

import java.util.Optional;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.DirectoryRepository;

public class PwdCommandProcessor extends CommandProcessor {

  private final DirectoryRepository directoryRepository;

  public PwdCommandProcessor() {
    this.directoryRepository = RepositoryFactory.getDirectoryRepository();
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 1;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    Optional<String> directoryName = directoryRepository.findDirectoryNameById(
        session.getCurrDirectoryId());
    if (directoryName.isEmpty()) {
      session.setCurrDirectoryId(session.getCredentials().getUserDirectoryId());
      return new CommandResponse.Builder()
          .withCode(550)
          .withMessage(
              "Previous directory is no longer accessible. Resetting to the home directory.")
          .build();
    }
    return new CommandResponse.Builder()
        .withCode(257)
        .withMessage("\"" + directoryName.get() + "\" - is the current directory.")
        .build();
  }
}
