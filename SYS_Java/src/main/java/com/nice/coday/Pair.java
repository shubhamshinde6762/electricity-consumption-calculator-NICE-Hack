package com.nice.coday;

class Pair<K extends Comparable<K>, V> implements Comparable<Pair<K, V>> {
    K key;
    V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(Pair<K, V> other) {
        int keyComparison = this.key.compareTo(other.key);

        if (keyComparison != 0) {
            return keyComparison;
        }

        String value1 = this.value.toString();
        String value2 = other.value.toString();

        if (value1.equals("ChargingStation") && !value2.equals("ChargingStation")) {
            return -1;
        } else if (!value1.equals("ChargingStation") && value2.equals("ChargingStation")) {
            return 1;
        }

        return value1.compareTo(value2);
    }
}