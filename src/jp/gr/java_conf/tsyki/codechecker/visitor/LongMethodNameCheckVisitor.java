package jp.gr.java_conf.tsyki.codechecker.visitor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * 長すぎるメソッド名に警告を出すVisitor
 */
public class LongMethodNameCheckVisitor extends ASTVisitor {
	private ICompilationUnit unit;

	private int maxLength;

	public LongMethodNameCheckVisitor(ICompilationUnit unit, int maxLength) {
		this.unit = unit;
		this.maxLength = maxLength;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		SimpleName nameNode = node.getName();
		if (nameNode.getLength() > maxLength) {
			try {
				IMarker marker = unit.getCorrespondingResource().createMarker(CodeCheckerConstants.MARKER_TYPE);
				marker.setAttribute(IMarker.MESSAGE, "メソッド名は" + maxLength + "文字以内としてください。");
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				marker.setAttribute(IMarker.LINE_NUMBER,
						((CompilationUnit) node.getRoot()).getLineNumber(nameNode.getStartPosition()));
				marker.setAttribute(IMarker.CHAR_START, nameNode.getStartPosition());
				marker.setAttribute(IMarker.CHAR_END, nameNode.getStartPosition() + nameNode.getLength());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.visit(node);
	}

}
