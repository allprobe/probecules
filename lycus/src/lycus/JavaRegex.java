//package lycus;
//
//import java.io.Console;
//import java.util.regex.Pattern;
//import java.util.regex.Matcher;
//
//public class JavaRegex {
//
//	public static void main(String[] args) {
//				Pattern pattern = Pattern.compile("ex");
//				Matcher matcher = pattern.matcher("exfjkwjexhfdf");
//				boolean found = false;
//				while (matcher.find()) {
//					System.out.printf("I found the text" + " \"%s\" starting at "
//							+ "index %d and ending at index %d.%n",
//							matcher.group(), matcher.start(), matcher.end());
//					found = true;
//				}
//				if (!found) {
//					System.out.println("No match found.%n");
//				}
//			}
//}