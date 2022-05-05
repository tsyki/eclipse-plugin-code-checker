package jp.gr.java_conf.tsyki.codechecker.visitor;

import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * AST�̍\�����o�͂���Visitor
 */
public class StructPrintVisitor extends ASTVisitor {
	private Stack<ASTNode> nodeStack = new Stack<>();

	@Override
	public void preVisit(ASTNode node) {
		super.preVisit(node);
		nodeStack.push(node);
		// ���݂̃c���[�̐[���ɉ������C���f���g��ǉ�
		String indent = String.join("",
				Stream.generate(() -> " ").limit(nodeStack.size()).collect(Collectors.toList()));
		// �m�[�h��toString�͂��̃m�[�h���\��Java�R�[�h��Ԃ��̂ŁA�K���Ȓ����ɐ؂�l�߂ďo��
		String nodeValue = node.toString().replaceAll("\n", " ");
		if (nodeValue.length() >= 30) {
			nodeValue = nodeValue.substring(0, 30) + "...";
		}
		System.out.println(indent + node.getClass().getSimpleName() + " " + nodeValue);
	}

	@Override
	public void postVisit(ASTNode node) {
		super.postVisit(node);
		nodeStack.pop();
	}
}
