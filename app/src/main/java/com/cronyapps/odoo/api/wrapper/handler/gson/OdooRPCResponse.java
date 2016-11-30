package com.cronyapps.odoo.api.wrapper.handler.gson;

public class OdooRPCResponse {
    public int id;
    public float jsonrpc;
    public OdooResult result;
    public OdooResult error;
    public OdooResult[] records;
}
