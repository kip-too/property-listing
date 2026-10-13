import java.util.ArrayList;
import java.util.Collections;

public class PalindromeArrayList {

    public static boolean isPalindrome(String input) {
        // Handle null or empty strings
        if (input == null || input.isEmpty()) {
            return true;
        }
        // Clean the string: convert to lowercase and remove non-alphanumeric characters
        String cleanStr = input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        // 1. Populate characters into an ArrayList
        ArrayList<Character> originalList = new ArrayList<>();
        for (char c : cleanStr.toCharArray()) {
            originalList.add(c);
        }
        // 2. Create a separate copy to reverse
        ArrayList<Character> reversedList = new ArrayList<>(originalList);
        // 3. Reverse the copy using built-in Collections utility
        Collections.reverse(reversedList);
        // 4. Compare the original list with the reversed list
        return originalList.equals(reversedList);
    }

    public static void main(String[] args) {
        String test1 = "EKITIKE";
        String test1 = "RACECAR";
        String test3 = "TELL";

        System.out.println("'" + test1 + "' is a palindrome? " + isPalindrome(test1));
        System.out.println("'" + test2 + "' is a palindrome? " + isPalindrome(test2));
        System.out.println("'" + test3 + "' is a palindrome? " + isPalindrome(test3));
    }
}
