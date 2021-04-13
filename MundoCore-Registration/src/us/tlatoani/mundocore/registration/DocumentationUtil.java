package us.tlatoani.mundocore.registration;

import java.util.List;
import java.util.Optional;

public class DocumentationUtil {

    public static <T, U> Optional<U> binarySearchList(List<U> list, T value, AsymmetricComparator<T, U> comparator) {
        for (int low = 0, high = list.size() - 1, mid = (low + high) / 2; low <= high; mid = (low + high) / 2) {
            U pos = list.get(mid);
            int result = comparator.compare(value, pos);
            if (result == 0) {
                return Optional.of(pos);
            } else if (result > 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return Optional.empty();
    }

    public static <T, U> Optional<U> binarySearchCeiling(List<U> list, T value, AsymmetricComparator<T, U> comparator) {
        int low = 0, high = list.size() - 1;
        for (int mid = (low + high) / 2; low <= high; mid = (low + high) / 2) {
            U pos = list.get(mid);
            int result = comparator.compare(value, pos);
            if (result == 0) {
                return Optional.of(pos);
            } else if (result > 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return high == list.size() - 1 ? Optional.empty() : Optional.of(list.get(high + 1));
    }

    public static String capitalize(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    public static Optional<Integer> parseIntOptional(String posInt) {
        try {
            return Optional.of(Integer.parseInt(posInt));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static boolean wordsStartWith(String s1, String s2) {
        String[] words1 = s1.split(" ");
        String[] words2 = s2.split(" ");
        if (words1.length < words2.length) {
            return false;
        }
        for (int i = 0; i < words2.length; i++) {
            if (!words1[i].startsWith(words2[i])) {
                return false;
            }
        }
        return true;
    }
}
