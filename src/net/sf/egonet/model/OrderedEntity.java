package net.sf.egonet.model;

public class OrderedEntity extends Entity implements Comparable<OrderedEntity> {
	private Integer ordering;

	@Override
	public int compareTo(OrderedEntity entity) {
		int orderingCompare = compareFields(ordering,entity.ordering);
		if(orderingCompare == 0) {
			return compareFields(getId(),entity.getId());
		}
		return orderingCompare;
	}
	
	private <C extends Comparable<C>> int compareFields(C a, C b) {
		if(a == null && b == null) {
			return 0;
		}
		if(a == null) {
			return -1;
		}
		if(b == null) {
			return 1;
		}
		return a.compareTo(b);
	}
	
	public void setOrdering(Integer ordering) {
		this.ordering = ordering;
	}

	public Integer getOrdering() {
		return ordering;
	}
}
