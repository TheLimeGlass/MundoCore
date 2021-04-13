package us.tlatoani.mundocore.registration;

import java.util.List;
import java.util.Optional;

/**
 * Created by Tlatoani on 12/3/17.
 */
public interface AsymmetricComparator<A, B> {

    int compare(A a, B b);

}
