package com.gcplot.model.gc;

public interface MemoryDetails {

    long pageSize();

    long physicalTotal();

    long physicalFree();

    long swapTotal();

    long swapFree();

    MemoryDetails EMPTY = new MemoryDetails() {
        @Override
        public long pageSize() {
            return 0;
        }

        @Override
        public long physicalTotal() {
            return 0;
        }

        @Override
        public long physicalFree() {
            return 0;
        }

        @Override
        public long swapTotal() {
            return 0;
        }

        @Override
        public long swapFree() {
            return 0;
        }
    };

}
