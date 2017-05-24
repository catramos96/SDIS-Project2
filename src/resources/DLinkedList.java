package resources;

import network.Subscriber;

public class DLinkedList<N> {
	protected DLNode<N> first = null;
	protected DLNode<N> last = null;
	
	public DLinkedList(){};
	
	public DLinkedList(N first){
		addFirst(first);
	};
	
	public DLNode<N> addFirst(N newObject){
		DLNode<N> n = new DLNode<N>(newObject);
		if(first != null){
			n.setNext(first);
			first.setPrevious(n);
			if(first.getPrevious() == null){
				last = first;
			}
			first = n;
		}
		return n;
	}
	
	public DLNode<N> addLast(N newObject){
		DLNode<N> n = new DLNode<N>(newObject);
		
		if(last == null){
			if(first == null)
				first = n;
			else{
				first.setNext(n);
				last = n;
			}
		}
		return n;
	}
	
	public DLNode<N> addAfter(DLNode<N> node, N newObject){
		DLNode<N> n = new DLNode<N>(newObject);
		
		if(node.equals(last)){
			return addLast(newObject);
		}
		else{
			n.setPrevious(node);
			n.setNext(node.getNext());
			node.setNext(n);
			node.getNext().setPrevious(n);
		}
		
		return n;
	}
	
	public DLNode<N> addBefore(DLNode<N> node, N newObject){
		DLNode<N> n = new DLNode<N>(newObject);

		if(node.equals(first)){
			return addFirst(newObject);
		}
		else{
			n.setNext(node);
			n.setPrevious(node.getPrevious());
			node.getPrevious().setNext(n);
			node.setPrevious(n);
		}
		return n;
	}
	
	public void removeFirst(){
		if(first != null){
			first = first.getNext();
			
			if(first != null)
				first.setPrevious(null);
		}
	}
	
	public void removeLast(){
		if(last !=  null){
			last = last.getPrevious();
			last.setNext(null);
			
			if(last.equals(first))
				last = null;	
		}
	}
	
	public void removeNode(DLNode<N> node){
		if(node.equals(last)){
			removeLast();
		}
		else if(node.equals(first)){
			removeFirst();
		}
		else{
			node.getPrevious().setNext(node.getNext());
			node.getNext().setPrevious(node.getPrevious());
		}
	}
}
