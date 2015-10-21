package com.cisco.cta.taxii.adapter.persistence;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TaxiiStatusDelegateTest {

    @Test
    public void getDefaultValue() throws Exception {
        TaxiiStatusDelegate tsd = new TaxiiStatusDelegate(null);
        assertThat(tsd.getDefaultValue(), is(new TaxiiStatus()));
    }

}