package com.pojogen.internal;

import java.math.BigDecimal;

import com.pojogen.api.annotation.PojoGen;

@PojoGen
public interface Ohlc {

	BigDecimal getOpen();

	BigDecimal getHigh();

	BigDecimal getLow();

	BigDecimal getClose();
}
