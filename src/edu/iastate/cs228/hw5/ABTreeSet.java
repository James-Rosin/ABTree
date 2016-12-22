package edu.iastate.cs228.hw5;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author James Rosin
 */

public class ABTreeSet<E extends Comparable<? super E>> extends AbstractSet<E> {

	final class ABTreeIterator implements Iterator<E> {
		private Node cursor;
		private Node pending;

		ABTreeIterator() {
			cursor = root;
			if (cursor != null)
				while (cursor.left != null)
					cursor = cursor.left;
		}

		@Override
		public boolean hasNext() {
			return cursor != null;
		}

		@Override
		public E next() {
			if (!hasNext()) throw new NoSuchElementException();
			pending = cursor;
			cursor = (Node) successor(cursor);
			return pending.data;
		}

		@Override
		public void remove() {
			if (pending == null) throw new IllegalStateException();
			if (pending.left != null && pending.right != null)
			{
				cursor = pending;
			}
			Node temp = pending.parent;
			unlinkNode(pending);
			doSelfBalance(temp);
			pending = null;
		}
	}

	final class Node implements BSTNode<E> {
		private int count;
		private E data;
		private Node left, right, parent;

		Node(E data, BSTNode<E> parent){
			this.data = data;
			this.count = 0;
			left = null;
			right = null;
			this.parent = (Node) parent;
		}

		@Override
		public int count() {
			return count;
		}

		@Override
		public E data() {
			return data;
		}

		@Override
		public BSTNode<E> left() {
			return left;
		}

		@Override
		public BSTNode<E> parent() {
			return parent;
		}

		@Override
		public BSTNode<E> right() {
			return right;
		}

		@Override
		public String toString() {
			return data.toString();
		}
	}

	private Node root;
	private int top;
	private int bottom;
	private boolean isSelfBalancing;

	/**
	 * Default constructor. Builds a non-self-balancing tree.
	 */
	public ABTreeSet() {
		this(false, 0, 0);
	}

	/**
	 * If <code>isSelfBalancing</code> is <code>true</code> <br>
	 * builds a self-balancing tree with the default value alpha = 2/3.
	 * <p>
	 * If <code>isSelfBalancing</code> is <code>false</code> <br>
	 * builds a non-self-balancing tree.
	 * 
	 * @param isSelfBalancing
	 */
	public ABTreeSet(boolean isSelfBalancing) {
		this(isSelfBalancing, 2, 3);
	}

	/**
	 * If <code>isSelfBalancing</code> is <code>true</code> <br>
	 * builds a self-balancing tree with alpha = top / bottom.
	 * <p>
	 * If <code>isSelfBalancing</code> is <code>false</code> <br>
	 * builds a non-self-balancing tree (top and bottom are ignored).
	 * 
	 * @param isSelfBalancing
	 * @param top
	 * @param bottom
	 * @throws IllegalArgumentException
	 *             if (1/2 < alpha < 1) is false
	 */
	public ABTreeSet(boolean isSelfBalancing, int top, int bottom) {
		if((double)top / bottom <= 0.5 || (double)top / bottom >= 1)
			throw new IllegalArgumentException();
		this.isSelfBalancing = isSelfBalancing;
		if(this.isSelfBalancing){
			root = new Node(null, null);
			this.top = top;
			this.bottom = bottom;
		}
		else{
			root = null;
			top = 0;
			bottom = 0;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws NullPointerException
	 *             if e is null.
	 */
	@Override
	public boolean add(E e) {
		if(e == null)
			throw new NullPointerException();
		if(root == null){
			root = new Node(e, null);
			root.count++;
			return true;
		}
		Node current = root;

		while(true){
			int comp = current.data.compareTo(e);
			if(comp == 0)
				return false;
			else if(comp > 0){
				if(current.left() != null)
					current = (Node) current.left();
				else{
					current.left = new Node(e, current);
					current = current.left;
					current.count++;
					doSelfBalance(current);
					while(current.parent!= null){
						current.parent.count++;
						current = current.parent;
					}
					return true;
				}
			}
			else{
				if(current.right() != null)
					current = (Node) current.right();
				else{
					current.right = new Node(e, current);
					current = current.right;
					current.count++;
					doSelfBalance(current);
					while(current.parent!= null){
						current.parent.count++;
						current = current.parent;
					}
					return true;
				}
			}
		}
	}

	@Override
	public boolean contains(Object o) {
		return getBSTNode((E) o) != null;
	}

	/**
	 * @param e
	 * @return BSTNode that contains e, null if e does not exist
	 */
	public BSTNode<E> getBSTNode(E e) {
		Node current = root;
		while(current != null){
			int comp = current.data.compareTo(e);
			if(comp == 0)
				return current;
			else if(comp > 0)
				current = current.left;
			else
				current = current.right;
		}
		return null;
	}

	/**
	 * Returns an in-order list of all nodes in the given sub-tree.
	 * 
	 * @param root
	 * @return an in-order list of all nodes in the given sub-tree.
	 */
	public List<BSTNode<E>> inorderList(BSTNode<E> root) {
		List<BSTNode<E>> list = new ArrayList<BSTNode<E>>();
		Node current = (Node) root;
		inorderHelper(current, list);
		return list;
	}

	@Override
	public Iterator<E> iterator() {
		return new ABTreeIterator();
	}

	/**
	 * Returns an pre-order list of all nodes in the given sub-tree.
	 * 
	 * @param root
	 * @return an pre-order list of all nodes in the given sub-tree.
	 */
	public List<BSTNode<E>> preorderList(BSTNode<E> root) {
		List<BSTNode<E>> list = new ArrayList<BSTNode<E>>();
		Node current = (Node) root;
		preorderHelper(current, list);
		return list;
	}



	/**
	 * Performs a re-balance operation on the subtree rooted at the given node.
	 * 
	 * @param bstNode
	 */
	public void rebalance(BSTNode<E> bstNode) {
		if(top == 0 || bottom == 0)
			return;
		ArrayList<BSTNode<E>> arrL = (ArrayList<BSTNode<E>>) inorderList(bstNode);
		if((((Node) bstNode).left.count * bottom <= root.count * top && ((Node) bstNode).right.count * bottom <= root.count * top) || ((Node)bstNode).parent == root)
			return;
		else{
			rebalanceRecHelper(arrL);
		}
	}

	private void rebalanceRecHelper(ArrayList<BSTNode<E>> arrL){
		if(arrL.size() <= 1)
			return;
		int pos;
		if(arrL.size()%2 == 0)
			pos = arrL.size()/2-1;
		else
			pos = arrL.size()/2;
		Node newRoot = (Node) arrL.get(pos);
		ArrayList<BSTNode<E>> arrLLeft = (ArrayList<BSTNode<E>>) arrL.subList(0, pos-1);
		ArrayList<BSTNode<E>> arrLRight = (ArrayList<BSTNode<E>>) arrL.subList(pos+1, arrL.size());
		if(arrLLeft.size() == 0)
			newRoot.left = null;
		if(arrLRight.size() == 0)
			newRoot.right = null;
		if(arrLLeft.size() == 1){
			newRoot.left = (Node) arrLLeft.get(0);
			newRoot.left.parent = newRoot;
		}
		else
			rebalanceRecHelper(arrLLeft);
		if(arrLRight.size() == 1){
			newRoot.right = (Node) arrLRight.get(0);
			newRoot.right.parent = newRoot;
		}
		else
			rebalanceRecHelper(arrLRight);
	}

	@Override
	public boolean remove(Object o) {
		E key = (E)o;
		BSTNode<E> n = getBSTNode(key);
		if(n == null)
			return false;
		Node temp = ((Node) n).parent;
		unlinkNode((Node) n);
		doSelfBalance(temp);
		return true;
	}

	/**
	 * Returns the root of the tree.
	 * 
	 * @return the root of the tree.
	 */
	public BSTNode<E> root() {
		return root;
	}

	public void setSelfBalance(boolean isSelfBalance) {
		this.isSelfBalancing = isSelfBalance;
	}

	@Override
	public int size() {
		if(root != null)
			return root.count;
		else
			return 0;
	}

	public BSTNode<E> successor(BSTNode<E> node) {
		if(node == null)
			throw new NullPointerException();
		else if(node.right() != null){
			BSTNode<E> current = node.right();
			while(current.left() != null)
				current = current.left();
			return current;
		}
		else{
			BSTNode<E> current = node.parent();
			BSTNode<E> child = node;
			while(current != null && current.right() != null && current.right() == child){
				child = current;
				current = current.parent();
			}
			return current;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		toStringRec(root, builder, 0);
		return builder.toString();

	}

	private void toStringRec(Node cur, StringBuilder sb, int depth){
		for(int i = 0; i < depth; i++)
			sb.append("    ");
		if(cur == null){
			sb.append("null\n");
			return;
		}
		sb.append(cur.data.toString()+"\n");
		if(cur.left != null || cur.right != null)
		{
			toStringRec(cur.left, sb, depth +1);
			toStringRec(cur.right, sb, depth +1);
		}
	}
	private void inorderHelper(Node cur, List<BSTNode<E>> list){
		if(cur == null)
			return;
		inorderHelper(cur.left, list);
		list.add(cur);
		inorderHelper(cur.right, list);
	}

	private void preorderHelper(Node cur, List<BSTNode<E>> list) {
		if(cur == null)
			return;
		list.add(cur);
		preorderHelper(cur.left, list);
		preorderHelper(cur.right, list);
	}

	void unlinkNode(Node n){
		if(n.left != null && n.right != null){
			Node s = (Node) successor(n);
			n.data = s.data;
			n = s;
		}
		Node replace = null;
		if(n.left != null)
			replace = (Node) n.left;
		else if(n.right != null)
			replace = n.right;
		if(n == root)
			root = replace;
		else{
			if(n == n.parent.left)
				n.parent.left = replace;
			else 
				n.parent.right = replace;
		}
		if(replace != null){
			replace.parent = n.parent;

		}
		n.count--;
		while(n.parent != null){
			n.parent.count--;
			n = n.parent;
		}
	}

	private void doSelfBalance(Node n){
		if(isSelfBalancing){
			while(n.parent != null){
				rebalance(n);
				n = n.parent;
			}
		}
	}
}
