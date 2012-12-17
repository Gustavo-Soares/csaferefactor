package test;

import java.io.File;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;

class DeltaPrinter implements IResourceDeltaVisitor {
	
	private String changedContent; 
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
             System.out.print("Resource ");
             System.out.print(res.getFullPath());
             System.out.println(" has changed.");
             if (res.getFileExtension() != null && res.getFileExtension().equals("java")) {
            	 IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
            	 for (IMarkerDelta iMarkerDelta : markerDeltas) {
					System.out.println("markers: " + iMarkerDelta);
				}
            		 System.out.println("get java content");
            	 
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
	public String getChangedContent() {
		return changedContent;
	}
	public void setChangedContent(String changedContent) {
		this.changedContent = changedContent;
	}
 }