package org.ftp.command.impl;

import org.ftp.UserSession;
import org.ftp.command.CommandProcessor;
import org.ftp.command.CommandResponse;
import org.ftp.domain.Group;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.GroupRepository;
import org.ftp.repository.impl.UserRepository;

public class CreateGroupCommandProcessor extends CommandProcessor {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  public CreateGroupCommandProcessor() {
    this.userRepository = RepositoryFactory.getUserRepository();
    this.groupRepository = RepositoryFactory.getGroupRepository();
  }

  @Override
  protected boolean allowAccess(UserSession session, String[] arguments) {
    return userRepository.isAdmin(session.getCredentials().getUsername());
  }

  @Override
  protected boolean validArguments(UserSession session, String[] arguments) {
    return arguments.length == 2;
  }

  @Override
  protected CommandResponse process(UserSession session, String[] arguments) {
    String groupName = arguments[1];
    if (groupRepository.existsByName(groupName)) {
      return new CommandResponse.Builder()
          .withCode(400)
          .withMessage("Group already exists.")
          .build();
    }
    Group group = Group.builder()
        .name(groupName)
        .build();
    groupRepository.create(group);

    return new CommandResponse.Builder()
        .withCode(200)
        .withMessage("Group created successfully.")
        .build();

  }
}
