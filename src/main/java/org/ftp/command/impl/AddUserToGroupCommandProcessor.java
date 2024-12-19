package org.ftp.command.impl;

import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.GroupRepository;
import org.ftp.repository.impl.UserRepository;

public class AddUserToGroupCommandProcessor extends CommandProcessor {
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  public AddUserToGroupCommandProcessor() {
    this.userRepository = RepositoryFactory.getUserRepository();
    this.groupRepository = RepositoryFactory.getGroupRepository();
  }

  @Override
  protected boolean allowAccess(UserSession session, String[] arguments) {
    return userRepository.isAdmin(session.getCredentials().getUsername());
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 3;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String username = arguments[1];
    String groupName = arguments[2];


    var userOptional = userRepository.findByUsername(username);
    if (userOptional.isEmpty()) {
      return new CommandResponse.Builder()
          .withCode(404)
          .withMessage("User " + username + " does not exist.")
          .build();
    }
    var groupOptional = groupRepository.findByGroupName(groupName);
    if (groupOptional.isEmpty()) {
      return new CommandResponse.Builder()
          .withCode(404)
          .withMessage("Group " + groupName + " does not exist.")
          .build();
    }

    int userId = userOptional.get().getId();
    int groupId = groupOptional.get().getId();
    if (groupRepository.isUserMemberOfGroup(userId, groupId)) {
      return new CommandResponse.Builder()
          .withCode(409)
          .withMessage("User " + username + " is already a member of group " + groupName + ".")
          .build();
    }
    groupRepository.addUserToGroup(userId, groupId);

    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage("User " + username + " added to group " + groupName + " successfully.")
        .build();
  }
}
