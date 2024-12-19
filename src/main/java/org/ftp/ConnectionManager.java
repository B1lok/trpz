package org.ftp;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.ftp.domain.ConnectionLimits;
import org.ftp.repository.RepositoryFactory;
import org.ftp.repository.impl.ConnectionLimitsRepository;

public class ConnectionManager {

  private final ConcurrentHashMap<String, AtomicInteger> userConnections = new ConcurrentHashMap<>();
  private final ConnectionLimitsRepository connectionLimitsRepository;

  public ConnectionManager() {
    this.connectionLimitsRepository = RepositoryFactory.getConnectionLimitsRepository();
  }

  public synchronized boolean canConnect(String username) {
    Optional<ConnectionLimits> optionalLimits = connectionLimitsRepository.findByUsername(username);

    if (optionalLimits.isPresent()) {
      ConnectionLimits userLimits = optionalLimits.get();

      return !userConnections.containsKey(username)
          || userConnections.get(username).get() < userLimits.getMaxConnections();
    }
    return true;
  }

  public synchronized void addConnection(String username) {
    if (userConnections.containsKey(username)) {
      userConnections.get(username).incrementAndGet();
    } else {
      userConnections.put(username, new AtomicInteger(1));
    }
  }

  public synchronized void removeConnection(String username) {
    if (userConnections.containsKey(username)) {
      userConnections.get(username).decrementAndGet();
    }
    if (userConnections.containsKey(username) && userConnections.get(username).get() == 0) {
      userConnections.remove(username);
    }
  }
}
