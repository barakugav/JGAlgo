package com.ugav.jgalgo;

public interface GraphCapabilities {

	public boolean vertexAdd();
	public boolean vertexRemove();
	public boolean edgeAdd();
	public boolean edgeRemove();
	public boolean parallelEdges();
	public boolean selfEdges();
	public boolean directed();

}
