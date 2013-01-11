package csaferefactor;

import java.io.File;
import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Random;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import randoop.ExecutableSequence;
import randoop.main.GenTests;
import saferefactor.core.Parameters;
import saferefactor.core.analysis.Report;
import saferefactor.core.analysis.TransformationAnalyzer;
import saferefactor.core.analysis.naive.ASMBasedAnalyzer;
import saferefactor.core.generation.RandoopAdapter;
import saferefactor.core.util.Constants;
import saferefactor.core.util.FileUtil;
import saferefactor.core.util.Project;
import saferefactor.core.util.ast.ConstructorImp;
import saferefactor.core.util.ast.Method;
import saferefactor.core.util.ast.MethodImp;
import saferefactor.rmi.client.CheckBehaviorChange;
import saferefactor.rmi.common.RemoteExecutor;

import csaferefactor.util.ProjectLogger;

class ChangeVisitor implements IResourceDeltaVisitor {

	private static final String SAFEREFACTOR_MARKER = "csaferefactor.saferefactorproblem";

	public boolean visit(IResourceDelta delta) {
		IResource res = delta.getResource();
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			break;
		case IResourceDelta.REMOVED:
			break;
		case IResourceDelta.CHANGED:

//			if (isJavFile(delta, res)) {
//
//
//			}
			break;
		}
		return true;
	}

	private boolean isJavFile(IResourceDelta delta, IResource res) {
		return res.getFileExtension() != null
				&& res.getFileExtension().equals("java")
				&& (delta.getFlags() & IResourceDelta.CONTENT) == 0;
	}
}