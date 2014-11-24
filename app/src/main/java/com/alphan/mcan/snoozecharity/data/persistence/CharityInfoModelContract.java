package com.alphan.mcan.snoozecharity.data.persistence;

import android.provider.BaseColumns;

/**
 * information of the supplied charities.
 * Created by mcni on 11/24/14.
 */
public final class CharityInfoModelContract {

    public static abstract class Donation implements BaseColumns { //this adds the ID column
        public static final String TABLE_NAME = "charity";
        public static final String COLUMN_NAME_CHARITY_NAME = "amount";
        public static final String COLUMN_NAME_CHARITY_INFO_MESSAGE = "amount";
        public static final String COLUMN_NAME_CHARITY_PAYMENT_INFO = "amount";
        //TODO: add more stuff here?
    }
}
