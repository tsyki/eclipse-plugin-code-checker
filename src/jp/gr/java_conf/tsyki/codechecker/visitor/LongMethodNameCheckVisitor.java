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
	public boolean visit(SimpleName node) {
		// NOTE メソッドの宣言はMethodDeclarationであるが、このノードはアノテーションや修飾子を含んだノードであるため、
		// マーカーをハイライトさせる時に余計な部分までハイライトしてしまう。このため、名称部分をvisitした時にチェックする
		if (node.getParent() instanceof MethodDeclaration) {
			if (node.getIdentifier().length() > maxLength) {
				try {
					IMarker marker = unit.getCorrespondingResource().createMarker(IMarker.PROBLEM);
					marker.setAttribute(IMarker.MESSAGE, "メソッド名は" + maxLength + "文字以内としてください。");
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					marker.setAttribute(IMarker.LINE_NUMBER,
							((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition()));
					marker.setAttribute(IMarker.CHAR_START, node.getStartPosition());
					marker.setAttribute(IMarker.CHAR_END, node.getStartPosition() + node.getLength());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return super.visit(node);
	}
}
