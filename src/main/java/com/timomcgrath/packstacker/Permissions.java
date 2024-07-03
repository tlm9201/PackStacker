package com.timomcgrath.packstacker;

public enum Permissions {
  PACK_LOAD_SELF("pack.load.self"),
  PACK_LOAD_OTHERS("pack.load.others");
  private final String value;

  Permissions(String value) {
    this.value = value;
  }

  public String get() {
    return value;
  }
}
