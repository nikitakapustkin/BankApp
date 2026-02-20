package org.nikitakapustkin.security.adapters.out.outbox;

public enum OutboxStatus {
  NEW,
  PROCESSING,
  SENT,
  FAILED
}
