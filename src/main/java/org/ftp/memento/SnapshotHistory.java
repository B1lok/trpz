package org.ftp.memento;

import java.util.Stack;
import org.ftp.memento.Memento;

public class SnapshotHistory {
  private final Stack<Memento> sessionHistory;

  public SnapshotHistory() {
    this.sessionHistory = new Stack<>();
  }

  public void saveSnapshot(Memento memento) {
    sessionHistory.push(memento);
  }

  public Memento restoreState() {
    if (!sessionHistory.isEmpty()) {
      return sessionHistory.pop();
    }
    return null;
  }
}
