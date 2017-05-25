package resources;

import com.siyeh.ig.security.SystemSetSecurityManagerInspection;
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
			if(last == null)
			    last = first;
			first = n;
		}

		first = n;

		return n;
	}
	
	public DLNode<N> addLast(N newObject){
		DLNode<N> n = new DLNode<N>(newObject);
		
		if(last == null){
			if(first == null)
				first = n;
			else{
				first.setNext(n);
				n.setPrevious(first);
				last = n;
			}
		}
        else{
		    n.setPrevious(last);
		    last.setNext(n);
		    last = n;
        }

		return n;
	}
	
	public DLNode<N> addAfter(DLNode<N> node, N newObject){
		DLNode<N> n = new DLNode<N>(newObject);
		
		if(node == last){
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

		if(node == first){
			return addFirst(newObject);
		}
		else{
			n.setNext(node);
			node.getPrevious().setNext(n);
            n.setPrevious(node.getPrevious());
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
			
			if(last == first)
				last = null;
		}
		else
			removeFirst();
	}
	
	public void removeNode(DLNode<N> node){
		if(node == last){
			removeLast();
		}
		else if(node == first){
			removeFirst();
		}
		else{
			node.getPrevious().setNext(node.getNext());
			node.getNext().setPrevious(node.getPrevious());
		}
	}
	
	public void displayList(){
		System.out.println("TOP");
		if(first != null)
			first.displayNode();
		System.out.println("BOTTOM");
		if(last != null)
			last.displayNode();
		else if(first != null)
		    first.displayNode();
	}

	public DLNode<N> getNode(N object){
		DLNode<N> current = first;
        System.out.println("  Search: " +  object.toString());

		while(current != null){
            System.out.println("     " + current.getObject().toString());
			if(current.getObject().equals(object))
				return current;
			else
				current = current.getNext();
		}

		return null;
	}

	public DLNode<N> getFirst(){return first;}

	public DLNode<N> getLast(){return last;}
}
