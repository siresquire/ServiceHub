package com.servicehub.exception;

import com.servicehub.model.enums.RequestStatus;

public class InvalidServiceRequestTransition extends RuntimeException {
  public InvalidServiceRequestTransition(String message) {
    super(message);
  }

  public InvalidServiceRequestTransition(RequestStatus current, RequestStatus newStatus) {
    super("Invalid status transition from " + current + " to " + newStatus);
  }

}
