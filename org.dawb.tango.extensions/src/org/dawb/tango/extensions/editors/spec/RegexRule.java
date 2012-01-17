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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;

public class RegexRule extends SingleLineRule {

	private Pattern pattern;
	public RegexRule(final Pattern pattern, IToken token) {
		super("", "", token); //$NON-NLS-1$//$NON-NLS-2$
		this.pattern = pattern;
	}
	protected boolean sequenceDetected(ICharacterScanner scanner,
										char[] sequence,
										boolean eofAllowed) {
		int c = scanner.read();
		
		final Matcher matcher = pattern.matcher(new String(sequence));
        if (matcher.matches()) {
			if (c == '?') {
				// processing instruction - abort
				scanner.unread();
				return false;
			}
			if (c == '!') {
				scanner.unread();
				return false;
			}
			return true;
		} else {
			scanner.unread();
		}

		return false;
	}
}
