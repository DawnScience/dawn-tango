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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;


public class SpecConfiguration extends SourceViewerConfiguration {
	private SpecDoubleClickStrategy doubleClickStrategy;
	private SpecTagScanner tagScanner;
	private SpecScanner scanner;
	private ColorManager colorManager;

	public SpecConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			SpecPartitionScanner.SPEC_COMMENT,
			SpecPartitionScanner.SPEC_SCAN };
	}
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new SpecDoubleClickStrategy();
		return doubleClickStrategy;
	}

	protected SpecScanner getDefaultScanner() {
		if (scanner == null) {
			scanner = new SpecScanner(colorManager);
			scanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(ISpecColorConstants.DEFAULT))));
		}
		return scanner;
	}
	protected SpecTagScanner getSpecScanScanner() {
		if (tagScanner == null) {
			tagScanner = new SpecTagScanner(colorManager);
			tagScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(ISpecColorConstants.SPEC_SCAN))));
		}
		return tagScanner;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr =
			new DefaultDamagerRepairer(getSpecScanScanner());
		reconciler.setDamager(dr, SpecPartitionScanner.SPEC_SCAN);
		reconciler.setRepairer(dr, SpecPartitionScanner.SPEC_SCAN);

		dr = new DefaultDamagerRepairer(getDefaultScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		NonRuleBasedDamagerRepairer ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(ISpecColorConstants.SPEC_COMMENT)));
		reconciler.setDamager(ndr, SpecPartitionScanner.SPEC_COMMENT);
		reconciler.setRepairer(ndr, SpecPartitionScanner.SPEC_COMMENT);

		return reconciler;
	}

}
