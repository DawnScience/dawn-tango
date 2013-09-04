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

import org.dawnsci.io.spec.SpecSyntax;
import org.dawb.tango.extensions.TangoUtils;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;



public class SpecPartitionScanner extends RuleBasedPartitionScanner {
	
	public final static String SPEC_SCAN    = "__spec_scan"; //$NON-NLS-1$
	public final static String SPEC_COMMENT = "__spec_comment"; //$NON-NLS-1$

	public SpecPartitionScanner() {

		IToken specComment = new Token(SPEC_SCAN);
		IToken specScan    = new Token(SPEC_COMMENT);

		IPredicateRule[] rules = new IPredicateRule[2];

		rules[0] = new RegexRule(SpecSyntax.COMMENT,   specComment);  //$NON-NLS-1$//$NON-NLS-2$
		rules[1] = new RegexRule(SpecSyntax.SCAN_LINE, specScan);

		setPredicateRules(rules);
	}
}
