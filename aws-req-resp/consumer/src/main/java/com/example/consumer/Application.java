package com.example.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.amazonaws.services.sqs.AmazonSQSResponder;
import com.amazonaws.services.sqs.AmazonSQSResponderClientBuilder;
import com.amazonaws.services.sqs.MessageContent;
import com.amazonaws.services.sqs.util.SQSMessageConsumer;
import com.amazonaws.services.sqs.util.SQSMessageConsumerBuilder;

import software.amazon.awssdk.services.sqs.SqsClient;



@SpringBootApplication
public class Application implements CommandLineRunner{
	private static Logger logger = LoggerFactory.getLogger(Application.class);
	 public static void main(String[] args) {
	        SpringApplication.run(Application.class, args);
	    }
	 public static boolean running = true;
	    @Autowired
	    IsPrime isPrime;
	 
	    @Value("${cloud.aws.end-point.uri}")
	    private String queueUrl;    
	    
	@Override
	public void run(String... args) throws Exception {
		logger.info("inside consumer");
		logger.info("Starting up supplier using queue: " + queueUrl);
		
		SqsClient sqsClient =  SqsClient.create();
		AmazonSQSResponder responder = AmazonSQSResponderClientBuilder.standard()
		        .withAmazonSQS(sqsClient)
                .build();

		SQSMessageConsumer consumer = SQSMessageConsumerBuilder.standard()
				.withAmazonSQS(responder.getAmazonSQS())
				.withQueueUrl(queueUrl)
				.withConsumer(message -> {
					String msg = MessageContent.fromMessage(message).getMessageBody();					
					Boolean flag = false;
					try {
						flag = isPrime.IsPrime (Integer.parseInt(msg));
					} catch (NumberFormatException e) {
						
						logger.error("msg: {} is not a number ",msg);
					}
					logger.info("msg : {} , isPrime : {} ",msg,flag);
					responder.sendResponseMessage(MessageContent.fromMessage(message),
							                      new MessageContent(flag.toString()));
				}).build();
		consumer.start();
		
		while (running) {};
	
		
	}
}
