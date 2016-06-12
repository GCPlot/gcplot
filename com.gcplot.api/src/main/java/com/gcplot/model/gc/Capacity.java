package com.gcplot.model.gc;

public interface Capacity {

    long usedBefore();

    long usedAfter();

    long total();

    Capacity NONE = new Capacity() {
        @Override
        public long usedBefore() {
            return 0;
        }

        @Override
        public long usedAfter() {
            return 0;
        }

        @Override
        public long total() {
            return 0;
        }
    };

}
