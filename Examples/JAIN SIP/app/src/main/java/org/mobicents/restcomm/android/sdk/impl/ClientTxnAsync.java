package org.mobicents.restcomm.android.sdk.impl;

import android.javax.sip.ClientTransaction;
import android.javax.sip.SipProvider;
import android.javax.sip.TransactionUnavailableException;
import android.javax.sip.message.Request;
import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.ExecutionException;

/**
 * Execute ClientTransaction asynchronously.
 */
public class ClientTxnAsync extends AsyncTask<ClientTxnAsync.Data, Void, ClientTransaction> {
    private static final String TAG = "ClientTxnAsync";

    static class Data {
        public Request mRequest;
        public SipProvider mSipProvider;

        public Data(Request r, SipProvider p) {
            mRequest = r;
            mSipProvider = p;
        }
    }

    @Override
    protected ClientTransaction doInBackground(Data... params) {
        Data d = params[0];
        ClientTransaction transaction = null;
        try {
            Log.d(TAG, "Creating new client txn from request: " + d.mRequest);
            transaction = d.mSipProvider
                    .getNewClientTransaction(d.mRequest);
        } catch (TransactionUnavailableException e) {
            Log.e("ClientTxnAsync", "Exception", e);
        }
        return transaction;
    }

    public static ClientTransaction run(Data data) {
        try {
            return new ClientTxnAsync().execute(data).get();
        } catch (InterruptedException e) {
            Log.e(TAG, "Exception", e);
        } catch (ExecutionException e) {
            Log.e(TAG, "Exception", e);
        }

        return null;
    }
}
