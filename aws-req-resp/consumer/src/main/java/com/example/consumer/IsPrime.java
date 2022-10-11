package com.example.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IsPrime {
	private static Logger logger = LoggerFactory.getLogger(IsPrime.class);
	public static void main(String[] args) {
		IsPrime isPrime = new IsPrime();
		
		boolean isValidPrime = isPrime.IsPrime(7);
		logger.info("IsPrime : {}",isValidPrime);
	}
	
	boolean IsPrime(int n)
	{
	    if (n == 2 || n == 3)
	        return true;

	    if (n <= 1 || n % 2 == 0 || n % 3 == 0)
	        return false;

	    for (int i = 5; i * i <= n; i += 6)
	    {
	        if (n % i == 0 || n % (i + 2) == 0)
	            return false;
	    }

	    return true;
	} 

}
