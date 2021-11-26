package com.ugav.algo;

interface TreeNode {

	TreeNode parent();

	TreeNode next();

	TreeNode prev();

	TreeNode child();

	void setParent(TreeNode x);

	void setNext(TreeNode x);

	void setPrev(TreeNode x);

	void setChild(TreeNode x);

}
