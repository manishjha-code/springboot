package com.example;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.sqs.AmazonSQSRequester;
import com.amazonaws.services.sqs.AmazonSQSRequesterClientBuilder;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@SpringBootApplication
@RestController
public class EvenOddApplication implements CommandLineRunner{
	Logger logger = LoggerFactory.getLogger(EvenOddApplication.class);
	private static AmazonSQSRequester sqsRequester;
    @Value("${cloud.aws.end-point.uri}")
	private  String requestQueueUrl;

    public static void main(String[] args) {
        SpringApplication.run(EvenOddApplication.class, args);
    }


	@Override
	public void run(String... args) throws Exception {
		logger.info("Inisde run");
		
		SqsClient sqsClient =  SqsClient.create();
		//AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		sqsRequester = AmazonSQSRequesterClientBuilder.standard()
                                                      .withAmazonSQS(sqsClient)
                                                      .build();
		boolean running = true;
		
		ExecutorService executorService = Executors.newFixedThreadPool(10);

		List<Future<?>> futureList= new ArrayList<>();
		for(int i = 0; i< 10; i++) {

			try {
				Future<?> future = executorService.submit(()->{
					while (running) {
					try {
						requestResponseLoop();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}});
				futureList.add(future);
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		futureList.forEach(f->{
			try {
				f.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
	}
	
	private void requestResponseLoop() throws InterruptedException, ExecutionException {
		int randomInt = ThreadLocalRandom.current().nextInt();
		String requestBody = Integer.toUnsignedString(randomInt) ;
		logger.info("ThreaName : {} ,Sending request: {} ",requestBody,Thread.currentThread());
		SendMessageRequest request =  SendMessageRequest.builder().queueUrl(requestQueueUrl).messageBody(requestBody).build();

		
		try {
			String responseBody = sqsRequester.sendMessageAndGetResponse(
					request, 5, TimeUnit.MINUTES).body();
			logger.info("Received response: " + responseBody);
		} catch (TimeoutException e) {
			logger.info("Got tired of waiting for response :(");
		}
	}
}