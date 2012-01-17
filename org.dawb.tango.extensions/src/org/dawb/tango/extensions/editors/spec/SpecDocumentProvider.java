/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.tango.extensions.editors.spec;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public class SpecDocumentProvider extends TextFileDocumentProvider {

	protected void setupDocument(IDocument document) {
		if (document != null) {
			IDocumentPartitioner partitioner =
				new FastPartitioner(
					new SpecPartitionScanner(),
					new String[] {
						SpecPartitionScanner.SPEC_COMMENT,
						SpecPartitionScanner.SPEC_SCAN });
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
	}

	protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
		return null;
	}
}
