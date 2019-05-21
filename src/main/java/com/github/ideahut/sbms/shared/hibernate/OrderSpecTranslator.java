package com.github.ideahut.sbms.shared.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Order;

import com.github.ideahut.sbms.shared.hibernate.OrderSpec.OrderSpecItem;

public class OrderSpecTranslator {
	public static List<Order> asOrders(OrderSpec orderSpec) {
		List<Order> orders = new ArrayList<Order>();
		for (OrderSpecItem specItem : orderSpec.getSpecItems()) {
			orders.add(specItem.getOrderType() == OrderSpec.OrderType.Ascending ? Order.asc(specItem.getField()) : Order.desc(specItem.getField()));

		}
		return orders;
	}

	public static String asString(OrderSpec orderSpec) {
		List<String> stringList = new ArrayList<String>();
		for (OrderSpecItem specItem : orderSpec.getSpecItems()) {
			StringBuffer buffer = new StringBuffer()
					.append(specItem.getField())
					.append(specItem.getOrderType() == OrderSpec.OrderType.Ascending ? " ASC " : " DESC ");
			stringList.add(buffer.toString());
		}
		return StringUtils.join(stringList.toArray(new String[] {}), ",");
	}
}
