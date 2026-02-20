package org.nikitakapustkin.adapters.out.persistence.jpa.entity;

public enum OutboxStatus {
  NEW,
  PROCESSING,
  SENT,
  FAILED
}
