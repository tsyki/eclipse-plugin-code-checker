package jp.gr.java_conf.tsyki.codechecker.handlers;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import jp.gr.java_conf.tsyki.codechecker.visitor.ArgumentTypeCheckVisitor;
import jp.gr.java_conf.tsyki.codechecker.visitor.CodeCheckerConstants;
import jp.gr.java_conf.tsyki.codechecker.visitor.LongMethodNameCheckVisitor;
import jp.gr.java_conf.tsyki.codechecker.visitor.StructPrintVisitor;

public class CodeCheckHandler extends AbstractHandler {

	// 配下のICompilationUnitを再帰的に引数のcompilationUnitsに追加する
	private void addCompilationUnits(Object target, Collection<ICompilationUnit> compilationUnits) {
		// javaファイル選択時のtargetの型はCompilationUnitであり、そのまま追加
		// NOTE CompilationUnitはIParentも実装しているため先に判定
		if (target instanceof ICompilationUnit) {
			compilationUnits.add((ICompilationUnit) target);
		}
		// プロジェクト選択時のtargetの型はJavaProjectであり、IParentを実装している。
		// srcフォルダ選択時のtargetの型はPackageFragmentRootであり、IParentを実装している。
		// srcフォルダ選択時のtargetの型はPackageFragmentであり、IParentを実装している。
		else if (target instanceof IParent) {
			// プロジェクトを選択した際のgetChildrenの中にはjarの中身も含まれており、チェック不要なためその場合は終了する
			if (isBinaryPackage(target)) {
				return;
			}
			IJavaElement[] children;
			try {
				children = ((IParent) target).getChildren();
			} catch (JavaModelException e) {
				e.printStackTrace();
				return;
			}
			for (IJavaElement child : children) {
				addCompilationUnits(child, compilationUnits);
			}
		}

	}

	// 対象パッケージがjarの中にあるかどうか
	private boolean isBinaryPackage(Object target) {
		if (target instanceof IPackageFragmentRoot) {
			try {
				if (((IPackageFragmentRoot) target).getKind() == IPackageFragmentRoot.K_BINARY) {
					return true;
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
				return true;
			}
		}
		return false;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 右クリックメニューで選択された対象を取得
		ISelection selection = HandlerUtil.getActiveMenuSelectionChecked(event);
		Collection<ICompilationUnit> targetJavaFiles = new ArrayList<>();
		// 選択された対象配下のjavaファイルの情報を取得
		if (selection instanceof TreeSelection) {
			Object selected = ((TreeSelection) selection).getFirstElement();
			addCompilationUnits(selected, targetJavaFiles);
		}
		ASTParser parser = ASTParser.newParser(AST.JLS17);
		for (ICompilationUnit targetJavaFile : targetJavaFiles) {
			// 事前に既存のマーカーを削除
			deleteMarkers(targetJavaFile);
			parser.setSource(targetJavaFile);
			// resolveBindingを使えるように
			parser.setResolveBindings(true);
			ASTNode node = parser.createAST(new NullProgressMonitor());
			// ASTの構造を出力する例
			StructPrintVisitor visitor = new StructPrintVisitor();
			node.accept(visitor);
			// 警告を出力する例
			LongMethodNameCheckVisitor checkVisitor = new LongMethodNameCheckVisitor(targetJavaFile, 30);
			node.accept(checkVisitor);
			// 親クラスを再帰的に取得する例
			ArgumentTypeCheckVisitor bindingCheckVisitor = new ArgumentTypeCheckVisitor(targetJavaFile);
			node.accept(bindingCheckVisitor);
		}
		return null;
	}

	private void deleteMarkers(ICompilationUnit targetJavaFile) {
		try {
			IMarker[] oldMarkers = targetJavaFile.getCorrespondingResource()
					.findMarkers(CodeCheckerConstants.MARKER_TYPE, false, IResource.DEPTH_ZERO);
			for (IMarker oldMarker : oldMarkers) {
				oldMarker.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showTargetJavaFiles(ExecutionEvent event, Collection<ICompilationUnit> targetJavaFiles)
			throws ExecutionException {
		StringBuilder sb = new StringBuilder();
		for (ICompilationUnit targetJavaFile : targetJavaFiles) {
			sb.append(targetJavaFile.getElementName());
			sb.append("\n");
		}
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(window.getShell(), "Selected", sb.toString());
	}
}
