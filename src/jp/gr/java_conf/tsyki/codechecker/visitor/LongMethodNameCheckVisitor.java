package jp.gr.java_conf.tsyki.codechecker.visitor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * �������郁�\�b�h���Ɍx�����o��Visitor
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
				marker.setAttribute(IMarker.MESSAGE, "���\�b�h����" + maxLength + "�����ȓ��Ƃ��Ă��������B");
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
