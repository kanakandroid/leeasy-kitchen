package com.cronyapps.odoo.api.wrapper.handler;

import com.cronyapps.odoo.api.wrapper.impl.IOdooResponse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public class ResponseQueue {
    private static ResponseQueue mResponseQueue;
    private Map<String, Queue<IOdooResponse>> mOdooResponseQueue = new HashMap<>();

    public static ResponseQueue getSingleTone() {
        if (mResponseQueue == null) {
            mResponseQueue = new ResponseQueue();
        }
        return mResponseQueue;
    }

    public void add(int id, IOdooResponse callback) {
        if (!mOdooResponseQueue.containsKey("queue_" + id)) {
            Queue<IOdooResponse> responses = new LinkedList<>();
            responses.add(callback);
            mOdooResponseQueue.put("queue_" + id, responses);
        }
    }

    public IOdooResponse get(int id) {
        if (mOdooResponseQueue.containsKey("queue_" + id)) {
            return mOdooResponseQueue.get("queue_" + id).poll();
        }
        return null;
    }

    public void remove(int id) {
        if (mOdooResponseQueue.containsKey("queue_" + id)) {
            mOdooResponseQueue.remove("queue_" + id);
        }
    }

    public boolean contain(int id) {
        return mOdooResponseQueue.containsKey("queue_" + id);
    }
}
