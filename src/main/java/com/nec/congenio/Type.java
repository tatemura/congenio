package com.nec.congenio;


public enum Type {
	OBJECT,
	ARRAY,
	NUMBER,
	STRING,
	BOOL,
	NULL;

	public String attr() {
		return name().toLowerCase();
	}
}
