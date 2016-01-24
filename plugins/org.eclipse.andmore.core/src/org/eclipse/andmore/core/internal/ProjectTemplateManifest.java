/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.andmore.core.internal;

import java.util.List;

public class ProjectTemplateManifest {

	public static class FileTemplate {
		private String src;
		private String dest;
		private boolean open;
		private boolean show;
		private boolean skipTemplate;

		public String getSrc() {
			return src;
		}

		public String getDest() {
			return dest;
		}

		public boolean isOpen() {
			return open;
		}

		public boolean isShow() {
			return show;
		}

		public boolean isSkipTemplate() {
			return skipTemplate;
		}
	}

	private List<FileTemplate> files;
	private List<String> srcEntries;

	public List<FileTemplate> getFiles() {
		return files;
	}

	public List<String> getSrcEntries() {
		return srcEntries;
	}

}
