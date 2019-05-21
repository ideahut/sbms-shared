package com.github.ideahut.sbms.shared.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderSpec implements Serializable {

	private List<OrderSpecItem> specItems = new ArrayList<OrderSpecItem>();

	public List<OrderSpecItem> getSpecItems() {
		return specItems;
	}

	public static enum OrderType {
		Ascending, Descending;
	}

	public static class OrderSpecItem {
		private String field;

		private OrderType orderType;

		public OrderSpecItem(String field, OrderType orderType) {
			this.field = field;
			this.orderType = orderType;
		}

		public String getField() {
			return field;
		}

		public OrderType getOrderType() {
			return orderType;
		}

	}

	private static final long serialVersionUID = 5621652478751024832L;

	private OrderSpec() {
	}

	public static OrderSpec create(String field, OrderType orderType) {
		OrderSpec orderSpec = new OrderSpec();
		orderSpec.add(field, orderType);
		return orderSpec;
	}

	public OrderSpec add(String field, OrderType orderType) {
		specItems.add(new OrderSpecItem(field, orderType));
		return this;
	}


}