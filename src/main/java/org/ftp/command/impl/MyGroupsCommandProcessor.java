package org.ftp.command.impl;

import java.util.List;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.Group;
import org.ftp.domain.User;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.GroupRepository;
import org.ftp.repository.impl.UserRepository;

public class MyGroupsCommandProcessor extends CommandProcessor {
  private final UserRepository userRepository;

  public MyGroupsCommandProcessor() {
    this.userRepository = RepositoryFactory.getUserRepository();
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 1;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    User user = userRepository.getUserWithGroupsByUsername(session.getCredentials().getUsername())
        .get();
    List<Group> groups = userRepository.findGroupsByUserId(user.getId());

    StringBuilder responseMessage = new StringBuilder();
    responseMessage.append("Groups for user: ").append(user.getUsername()).append("\n");
    responseMessage.append("=============================================\n");

    if (groups.isEmpty()) {
      responseMessage.append("You are not a member of any groups.\n");
    } else {
      responseMessage.append(String.format("%-20s\n", "Group Name"));
      responseMessage.append("---------------------------------------------\n");

      for (Group group : groups) {
        responseMessage.append(String.format("%-20s\n", group.getName()));
      }
    }

    responseMessage.append("=============================================\n");
    responseMessage.append("Total Groups: ").append(groups.size());

    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage(responseMessage.toString())
        .build();
  }
}
