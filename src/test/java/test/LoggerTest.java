package test;
import com.archer.log.Logger;

public class LoggerTest {

	public static void stack1() {
		stack2();
	}

	public static void stack2() {
		Logger log = Logger.getLogger();
		log.info("nihao {}", "shijie");
	}
	
	public static void main(String[] args) {
		stack1();
	}
}
