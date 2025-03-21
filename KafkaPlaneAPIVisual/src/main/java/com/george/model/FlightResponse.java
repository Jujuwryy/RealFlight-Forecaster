package com.george.model;

import java.util.List;

public class FlightResponse {
    private Pagination pagination;
    private List<Flight> data;

    public static class Pagination {
        private int limit;
        private int offset;
        private int count;
        private int total;

        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
        public int getOffset() { return offset; }
        public void setOffset(int offset) { this.offset = offset; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
    }

    public Pagination getPagination() { return pagination; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }
    public List<Flight> getData() { return data; }
    public void setData(List<Flight> data) { this.data = data; }
    
    @Override
    public String toString() {
        return "FlightResponse{data=" + (data != null ? data : "null") + "}";
    }
}