package com.github.ideahut.sbms.shared.moment;

public abstract class MomentHolder {

	private static final ThreadLocal<MomentAttributes> holder = new ThreadLocal<MomentAttributes>();

	private static final ThreadLocal<MomentAttributes> inheritableHolder = new InheritableThreadLocal<MomentAttributes>();
	
	public static void removeMomentAttributes() {
		holder.remove();
		inheritableHolder.remove();
	}
	
	public static void setMomentAttributes(MomentAttributes attributes, boolean inheritable) {
		if (attributes == null) {
			removeMomentAttributes();
		} else {
			if (inheritable) {
				inheritableHolder.set(attributes);
				holder.remove();
			} else {
				holder.set(attributes);
				inheritableHolder.remove();
			}
		}
	}
	
	public static void setMomentAttributes(MomentAttributes attributes) {
		setMomentAttributes(attributes, false);
	}
	
	public static MomentAttributes getMomentAttributes() {
		MomentAttributes attributes = holder.get();
		if (attributes == null) {
			attributes = inheritableHolder.get();
		}
		return attributes;
	}
	
	public static MomentAttributes findMomentAttributes(boolean inheritable) {
		MomentAttributes attributes = getMomentAttributes();
		if (attributes == null) {
			setMomentAttributes(new MomentAttributes(), inheritable);
			attributes = getMomentAttributes();
		}
		return attributes;
	}
	
	public static MomentAttributes findMomentAttributes() {
		return findMomentAttributes(false);
	}
	
}
