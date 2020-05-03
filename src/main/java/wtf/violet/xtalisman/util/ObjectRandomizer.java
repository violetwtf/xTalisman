/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman.util;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Red_Epicness on 8/23/2016 at 12:11 AM.
 */
public class ObjectRandomizer<T extends ObjectRandomizer.Probable> {

    private final Iterable<T> objects;
    private float weightSum = 0;

    public ObjectRandomizer(Iterable<T> objects) {
        this.objects = objects;
        for (T object : objects) {
            weightSum += object.getWeight();
        }
    }

    public static <I> ObjectRandomizer<ProbableImpl<I>> getEqual(Collection<I> objects) {
        return new ObjectRandomizer<>(objects.stream().map(ProbableImpl::new).collect(Collectors.toSet()));
    }

    public T getRandom() {
        double p = Math.random();
        double cumulativeProbability = 0.0;
        for (T object : objects) {
            cumulativeProbability += (object.getWeight() / weightSum);
            if (p <= cumulativeProbability) {
                return object;
            }
        }
        throw new RuntimeException("This shouldn't happen!");
    }

    public Iterable<T> getObjects() {
        return objects;
    }

    public interface Probable {

        float getWeight();

    }

    public static class ProbableImpl<P> implements Probable {

        private P value;

        public ProbableImpl(P value) {
            this.value = value;
        }

        public P getValue() {
            return value;
        }

        @Override
        public float getWeight() {
            return 1;
        }
    }

}