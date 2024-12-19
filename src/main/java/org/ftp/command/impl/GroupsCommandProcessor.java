package org.ftp.command.impl;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.Group;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.GroupRepository;
import org.ftp.repository.impl.UserRepository;

public class GroupsCommandProcessor extends CommandProcessor {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  public GroupsCommandProcessor() {
    this.userRepository = RepositoryFactory.getUserRepository();
    this.groupRepository = RepositoryFactory.getGroupRepository();
  }

  @Override
  protected boolean allowAccess(UserSession session, String[] arguments) {
    return userRepository.isAdmin(session.getCredentials().getUsername());
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 1;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    List<Group> groups = groupRepository.readAll();
    Map<Group, List<String>> groupMembers = groups.stream()
        .collect(
            toMap(
                identity(),
                group -> userRepository.findUsernamesByGroupId(group.getId())
            )
        );
    StringBuilder responseMessage = new StringBuilder("Groups with Members:\n");
    responseMessage.append(String.format("%-15s %-30s\n", "Group Name", "Members"));
    responseMessage.append("------------------------------------------------------------\n");

    for (Map.Entry<Group, List<String>> entry : groupMembers.entrySet()) {
      Group group = entry.getKey();
      List<String> members = entry.getValue();

      responseMessage.append(String.format("%-15s ", group.getName()));

      if (members.isEmpty()) {
        responseMessage.append("No members\n");
      } else {
        responseMessage.append(String.join(", ", members)).append("\n");
      }
    }

    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage(responseMessage.toString())
        .build();
  }
}
