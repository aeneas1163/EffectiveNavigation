package com.alphan.mcan.snoozecharity.data.persistence;

import android.provider.BaseColumns;

/**
 * Donation entry information for the given cherity
 * 1 to n mapping
 * Created by mcni on 11/24/14.
 */
public final class DonationModelContract {

    public static abstract class PendingDonation implements BaseColumns { //this adds the ID column
        public static final String TABLE_NAME = "pendingdonation";
        public static final String COLUMN_NAME_CHARITY_INDEX = "charityindex";
        public static final String COLUMN_NAME_DONATION_AMOUNT = "amount";
    }

    public static abstract class PaidDonation implements BaseColumns { //this adds the ID column
        public static final String TABLE_NAME = "paiddonation";
        public static final String COLUMN_NAME_CHARITY_INDEX = "charityindex";
        public static final String COLUMN_NAME_DONATION_AMOUNT = "amount";
        public static final String COLUMN_NAME_DONATION_DATE = "donationDate";
    }
}
