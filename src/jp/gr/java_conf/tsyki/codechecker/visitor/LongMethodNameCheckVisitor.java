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
	public boolean visit(SimpleName node) {
		// NOTE ���\�b�h�̐錾��MethodDeclaration�ł��邪�A���̃m�[�h�̓A�m�e�[�V������C���q���܂񂾃m�[�h�ł��邽�߁A
		// �}�[�J�[���n�C���C�g�����鎞�ɗ]�v�ȕ����܂Ńn�C���C�g���Ă��܂��B���̂��߁A���̕�����visit�������Ƀ`�F�b�N����
		if (node.getParent() instanceof MethodDeclaration) {
			if (node.getIdentifier().length() > maxLength) {
				try {
					IMarker marker = unit.getCorrespondingResource().createMarker(IMarker.PROBLEM);
					marker.setAttribute(IMarker.MESSAGE, "���\�b�h����" + maxLength + "�����ȓ��Ƃ��Ă��������B");
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
