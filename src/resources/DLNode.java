package resources;

public class DLNode<M>{
	private DLNode<M> next = null;
	private DLNode<M> previous = null;
	private M object = null;
	
	public DLNode(){};
	public DLNode(M object){this.object = object;};
	public void setNext(DLNode<M> next){this.next = next;};
	public void setPrevious(DLNode<M> previous){this.previous = previous;};
	public DLNode<M> getNext(){return next;};
	public DLNode<M> getPrevious(){return previous;};
	public M getObject(){return object;};
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object other){
		
		return (this.next.equals(((DLNode<M>) other).getNext()) && 
				this.previous.equals(((DLNode<M>) other).getObject()) &&
				this.object.equals((M) other));
	}
}