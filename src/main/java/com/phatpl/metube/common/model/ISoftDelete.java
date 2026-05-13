package com.phatpl.metube.common.model;

public interface ISoftDelete {
  boolean isDeleted();

  void delete();
}
