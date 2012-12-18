package continousrefactoring;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

class DeltaPrinter implements IResourceDeltaVisitor {
	
	private String changedContent;
	private final ASTRewrite rewriter;
	private final IDocument document;
	private final List<ITrackedNodePosition> tracks;
	private final IFile file; 
    public DeltaPrinter(IFile file, ASTRewrite rewriter, IDocument document, List<ITrackedNodePosition> tracks) {
		this.file = file;
		this.rewriter = rewriter;
		// TODO Auto-generated constructor stub
		this.document = document;
		this.tracks = tracks;
	}
	public boolean visit(IResourceDelta delta) {
       IResource res = delta.getResource();
       switch (delta.getKind()) {
          case IResourceDelta.ADDED:
             System.out.print("Resource ");
             System.out.print(res.getFullPath());
             System.out.println(" was added.");
             break;
          case IResourceDelta.REMOVED:
             System.out.print("Resource ");
             System.out.print(res.getFullPath());
             System.out.println(" was removed.");
             break;
          case IResourceDelta.CHANGED:
        	  IResourceDelta[] affectedChildren = delta.getAffectedChildren();
        	  System.out.println(affectedChildren);
        	
//             System.out.print("Resource ");
//             System.out.print(res.getFullPath());
//             System.out.println(" has changed.");
//             System.out.println("resource type: " + res);
             if (res.getFileExtension() != null && res.getFileExtension().equals("java")) {
            	 
 				
 				try {
 					TextEdit edits = this.rewriter.rewriteAST();
					createMarkerForResource(file, edits.getOffset(),
							edits.getLength());
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
 				
     			
//     			for (ITrackedNodePosition track : tracks) {
//     				try {
//						createMarkerForResource(file, track.getStartPosition(),
//								track.getLength());
//					} catch (CoreException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//     				System.out.println("marker criada");
//     			}
            	

            	 
             }
             
             
//             int flags = delta.getFlags();
//             if ((flags & IResourceDelta.CONTENT) != 0) {
//                   System.out.println("--> Content Change");
//             }
//             if ((flags & IResourceDelta.REPLACED) != 0) {
//                   System.out.println("--> Content Replaced");
//             }
//             if ((flags & IResourceDelta.MARKERS) != 0) {
//                   System.out.println("--> Marker Change");
//                   IMarkerDelta[] markers = delta.getMarkerDeltas();
//                   System.out.println("Delta: " + markers);
//                   // if interested in markers, check these deltas
//             }
             
             break;
       }
       return true; // visit the children
    }
	
	public void createMarkerForResource(IResource file, int offset, int length)
			throws CoreException {
		IMarker marker = file.createMarker("test.slicemarker");
		marker.setAttribute(IMarker.CHAR_START, offset);
		marker.setAttribute(IMarker.CHAR_END, length);

	}
	public String getChangedContent() {
		return changedContent;
	}
	public void setChangedContent(String changedContent) {
		this.changedContent = changedContent;
	}
 }