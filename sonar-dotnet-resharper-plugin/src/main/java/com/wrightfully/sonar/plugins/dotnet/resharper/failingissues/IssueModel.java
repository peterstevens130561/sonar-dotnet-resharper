package com.wrightfully.sonar.plugins.dotnet.resharper.failingissues;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMInputCursor;

/*
 * Sonar .NET Plugin :: ReSharper
 * Copyright (C) 2013 John M. Wright
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

public class IssueModel {
	private String message ;
	private String file ;
	private String line ;
	private String id ;
	
	public IssueModel(SMInputCursor violationsCursor) throws XMLStreamException {
		setId(violationsCursor.getAttrValue("TypeId"));
		setMessage(violationsCursor.getAttrValue("Message"));
		setFile(violationsCursor.getAttrValue("File"));
		setLine(violationsCursor.getAttrValue("Line"));
	}
	
	public IssueModel() {
	}
	public final String getMessage() {
		return message;
	}
	public final void setMessage(String message) {
		this.message = message;
	}
	public final String getFile() {
		return file;
	}
	public final void setFile(String file) {
		this.file = file;
	}
	public final String getLine() {
		return line;
	}
	public final void setLine(String line) {
		this.line = line;
	}
	public final String getId() {
		return id;
	}
	public final void setId(String id) {
		this.id = id;
	}

	
}
