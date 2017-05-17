package tracker;

import java.util.ArrayList;

import network.Subscriber;

public class TrackedInfo {
	protected ArrayList<Subscriber> childs = null;
	protected Subscriber parent = null;
	protected Boolean active = null;
	
	public TrackedInfo(){
		childs = new ArrayList<Subscriber>();
		active = false;
	}
	
	public void setChilds(ArrayList<Subscriber> childs){
		this.childs = childs;
	}
	
	public void setParent(Subscriber parent){
		this.parent = parent;
	}
	
	public void setActivity(Boolean active){
		this.active = active;
	}

}
