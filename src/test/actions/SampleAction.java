package test.actions;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;
import org.eclipse.jface.dialogs.MessageDialog;

import test.EditorListener;
import test.ProjectLogger;
import test.SafeRefactorChangeListener;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class SampleAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public static final String ELCL_SESSION = "icons/full/elcl16/session.gif"; //$NON-NLS-1$
	public static final String ELCL_DUMP = "icons/full/elcl16/dump.gif"; //$NON-NLS-1$

	public static final String EVIEW_COVERAGE = "icons/full/eview16/coverage.gif"; //$NON-NLS-1$
	public static final String EVIEW_EXEC = "icons/full/eview16/exec.gif"; //$NON-NLS-1$

	public static final String OBJ_SESSION = "icons/full/obj16/session.gif"; //$NON-NLS-1$
	public static final String OBJ_MARKERFULL = "icons/full/obj16/markerfull.gif"; //$NON-NLS-1$
	public static final String OBJ_MARKERNO = "icons/full/obj16/markerno.gif"; //$NON-NLS-1$
	public static final String OBJ_MARKERPARTIAL = "icons/full/obj16/markerpartial.gif"; //$NON-NLS-1$

	/**
	 * The constructor.
	 */
	public SampleAction() {
	}

	
	public static CompilationUnit getCompilationUnit(ICompilationUnit icu,
	        IProgressMonitor monitor) {
	    final ASTParser parser = ASTParser.newParser(AST.JLS3);
	    parser.setSource(icu);
	    parser.setResolveBindings(true);
	    final CompilationUnit ret = (CompilationUnit) parser.createAST(monitor);
	    return ret;
	}
	
	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {

		// IResourceChangeListener listener;
		// try {
		// listener = new SafeRefactorChangeListener();
		// ResourcesPlugin.getWorkspace().addResourceChangeListener(
		// listener,
		// IResourceChangeEvent.PRE_BUILD
		// | IResourceChangeEvent.POST_BUILD
		// | IResourceChangeEvent.POST_CHANGE);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		if (page == null)
			return;
		IEditorPart editorPart = page.getActiveEditor();

		ITextEditor editor = (ITextEditor) editorPart
				.getAdapter(ITextEditor.class);

		IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editorPart
				.getEditorInput());
		
		CompilationUnit compilationUnit = getCompilationUnit((ICompilationUnit) javaElement, new NullProgressMonitor());
		int offset = compilationUnit.getStartPosition();
		int length = compilationUnit.getLength();

//		ISelectionProvider selectionProvider = ((ITextEditor) editor)
//				.getSelectionProvider();
//		ISelection selection = selectionProvider.getSelection();
//		if (selection instanceof ITextSelection) {
//			ITextSelection textSelection = (ITextSelection) selection;
//			int offset = textSelection.getOffset(); // etc.
//			try {
//				IJavaElement elementAt = root.getElementAt(offset);
//				
//			} catch (JavaModelException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

		IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
		try {
			createMarkerForResource(file,offset,length);
			System.out.println("marker criada");
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		// IDocumentProvider provider = editor.getDocumentProvider();
		// IDocument document = provider.getDocument(editor.getEditorInput());

		// document.
		// System.out.println("editor tracker:");
		// test.EditorTracker editorTracker = new
		// test.EditorTracker(PlatformUI.getWorkbench());

		// document.addDocumentListener(new IDocumentListener() {
		//
		// @Override
		// public void documentAboutToBeChanged(DocumentEvent event) {
		//
		// }
		//
		// @Override
		// public void documentChanged(DocumentEvent event) {
		// System.out.println("test");
		//
		// }
		// });
		// }
	}

	public void createMarkerForResource(IResource file, int offset, int length) throws CoreException {
		IMarker marker = file.createMarker("test.slicemarker");
		marker.setAttribute(IMarker.CHAR_START, offset);
		marker.setAttribute(IMarker.CHAR_END, length);

	}

	private IToken createToken(Color color) {
		TextAttribute textAttribute = new TextAttribute(color);
		return new Token(textAttribute);
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	public static void addAnnotation(IMarker marker, ITextSelection selection,
			ITextEditor editor) {
		// The DocumentProvider enables to get the document currently loaded in
		// the editor
		IDocumentProvider idp = editor.getDocumentProvider();
		// This is the document we want to connect to. This is taken from
		// the current editor input.
		IDocument document = idp.getDocument(editor.getEditorInput());
		// The IannotationModel enables to add/remove/change annotation to a
		// Document
		// loaded in an Editor
		IAnnotationModel iamf = idp.getAnnotationModel(editor.getEditorInput());
		// Note: The annotation type id specify that you want to create one of
		// your
		// annotations
		SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(
				"com.ibm.example.myannotation", marker);
		// Finally add the new annotation to the model
		iamf.connect(document);
		iamf.addAnnotation(ma,
				new Position(selection.getOffset(), selection.getLength()));
		iamf.disconnect(document);
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}