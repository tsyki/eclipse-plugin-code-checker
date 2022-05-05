package jp.gr.java_conf.tsyki.codechecker.visitor;

import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * ASTの構造を出力するVisitor
 */
public class StructPrintVisitor extends ASTVisitor {
	private Stack<ASTNode> nodeStack = new Stack<>();

	@Override
	public void preVisit(ASTNode node) {
		super.preVisit(node);
		nodeStack.push(node);
		// 現在のツリーの深さに応じたインデントを追加
		String indent = String.join("",
				Stream.generate(() -> " ").limit(nodeStack.size()).collect(Collectors.toList()));
		// ノードのtoStringはそのノードが表すJavaコードを返すので、適当な長さに切り詰めて出力
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
