package au.edu.qut.processmining.miners.heuristic.oracle;

import java.util.*;

/**
 * Created by Adriano on 1/11/2016.
 */
public class OracleItem implements Comparable {
    private Set<Integer> past;
    private Set<Integer> future;

    private String oracle;
    private String oraclePast;
    private String oracleFuture;

    private Set<OracleItem> xorBrothers;
    private Set<OracleItem> andBrothers;

    public OracleItem() {
        past = new HashSet<>();
        future = new HashSet<>();
        xorBrothers = new HashSet<>();
        andBrothers = new HashSet<>();
    }

    public void fillPast(int p) { past.add(p); }
    public void fillFuture(int f) { future.add(f); }

    public void fillPast(Collection<Integer> past) { this.past.addAll(past); }
    public void fillFuture(Collection<Integer> future) { this.future.addAll(future); }

    public Set<OracleItem> getXorBrothers() { return xorBrothers; }
    public Set<OracleItem> getAndBrothers() { return andBrothers; }

    public void engrave() {
        int i;
        int size;
        ArrayList<Integer> past = new ArrayList<>(this.past);
        ArrayList<Integer> future = new ArrayList<>(this.future);

        ArrayList<Integer> present = new ArrayList<>();
        present.addAll(this.past);
        present.addAll(this.future);

        i=0;
        Collections.sort(past);
        size = past.size();
        oraclePast = ":";
        while( i < size ) oraclePast += ":" + past.get(i++) + ":";
        oraclePast += ":";

        i=0;
        Collections.sort(future);
        size = future.size();
        oracleFuture = ":";
        while( i < size ) oracleFuture += ":" + future.get(i++) + ":";
        oracleFuture += ":";

        i=0;
        Collections.sort(present);
        size = present.size();
        oracle = ":";
        while( i < size ) oracle += ":" + present.get(i++) + ":";
        oracle += ":";
    }

    public boolean isXOR(OracleItem oItem) { return oItem.oracleFuture.equals(oracleFuture); }
    public static OracleItem mergeXORs(Set<OracleItem> xorBrothers) {
        /*
        * merging two or more XOR oracle items means:
        * 1. create a new oracle item that contains all the oracle items to be merged as xorBrothers
        * 2. the new oracle item will have the same shared future of all the xorBrothers in input (see also isXOR)
        * 3. the new oracle item will have the union of the pasts of all the xorBrothers in input
        */

        OracleItem oiUnion = new OracleItem();
        oiUnion.xorBrothers.addAll(xorBrothers);

        for( OracleItem xor : xorBrothers ) {
            oiUnion.future.addAll(xor.future);
            break;
        }

        for( OracleItem xor : xorBrothers ) oiUnion.past.addAll(xor.past);

        oiUnion.engrave();
        return oiUnion;
    }

    public boolean isAND(OracleItem oItem) { return oItem.oracle.equals(oracle); }
    public static OracleItem mergeANDs(Set<OracleItem> andBrothers) {
        /*
        * merging two or more AND oracle items means:
        * 1. create a new oracle item that contains all the oracle items to be merged as andBrothers
        * 2. the new oracle item will have only the part of shared future of all the andBrothers in input
        * 3. the new oracle item will have the union of the pasts of all the andBrothers in input
        */

        OracleItem oiUnion = new OracleItem();
        oiUnion.andBrothers.addAll(andBrothers);

        for( OracleItem and : andBrothers ) oiUnion.future.addAll(and.future);
        for( OracleItem and : andBrothers ) oiUnion.future.retainAll(and.future);

        for( OracleItem and : andBrothers ) oiUnion.past.addAll(and.past);

        oiUnion.engrave();
        return oiUnion;
    }

    @Override
    public String toString() { return (oraclePast + "|" + oracleFuture); }

    @Override
    public boolean equals(Object o) {
        if( o instanceof OracleItem ) {
            return o.toString().equals(this.toString());
        } else return false;
    }

    @Override
    public int compareTo(Object o) {
        if( o instanceof OracleItem ) {
            return o.toString().compareTo(this.toString());
        } else return -1;
    }
}
