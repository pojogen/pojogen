package com.pojogen.internal;

@javax.annotation.Generated(value = "com.pojogen.api.annotation.PojoGen")
final class ImmutableOhlc implements Ohlc {
	private final java.math.BigDecimal open;
	private final java.math.BigDecimal high;
	private final java.math.BigDecimal low;
	private final java.math.BigDecimal close;

	ImmutableOhlc(java.math.BigDecimal open, java.math.BigDecimal high, java.math.BigDecimal low, java.math.BigDecimal close) {
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
	}

	@Override
	public java.math.BigDecimal getOpen() {
		return this.open;
	}

	@Override
	public java.math.BigDecimal getHigh() {
		return this.high;
	}

	@Override
	public java.math.BigDecimal getLow() {
		return this.low;
	}

	@Override
	public java.math.BigDecimal getClose() {
		return this.close;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj.getClass() != this.getClass())
			return false;
		ImmutableOhlc rhs = (ImmutableOhlc) obj;
		return new org.apache.commons.lang3.builder.EqualsBuilder().append(this.open, rhs.open).append(this.high, rhs.high)
				.append(this.low, rhs.low).append(this.close, rhs.close).isEquals();
	}

	@Override
	public int hashCode() {
		return new org.apache.commons.lang3.builder.HashCodeBuilder().append(this.open).append(this.high).append(this.low)
				.append(this.close).toHashCode();
	}
}