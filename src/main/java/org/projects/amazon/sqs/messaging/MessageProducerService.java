package org.projects.amazon.sqs.messaging;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class MessageProducerService {

    @Autowired
    private SqsTemplate sqsTemplate;

    @Value("${queue_1.name}")
    private String queue1Name;

    @Value("${queue_2.name}")
    private String queue2Name;

    @Value("${queue_3.name}")
    private String queue3Name;

    @Value("${queue_4.name}")
    private String queue4Name;

    @Value("${queue_5.name}")
    private String queue5Name;

    @Value("${queue_7.name}")
    private String queue7Name;

    @Value("${queue_6.name}")
    private String fifoQueueName;

    public void sendStringMessage(String stringMessage) {
        sqsTemplate.send(queue1Name, stringMessage);
    }

    public void sendStringMessageWithUniqueIdInHeader(String stringMessage, String uniqueId) {
       sqsTemplate.send(queue2Name,
               MessageBuilder.withPayload(stringMessage)
               .setHeader("uniqueId", uniqueId)
               .build());
    }

    public void sendStringMessageList(List<String> stringMessageList) {
        sqsTemplate.sendMany(queue3Name, stringMessageList.stream().map(message -> MessageBuilder.withPayload(message).build()).toList());
    }

    public void sendStringMessageWithDelay(String stringMessage, Integer delayInSeconds) {
        sqsTemplate.send(sqsSendOptions -> {
            sqsSendOptions
                    .queue(queue4Name)
                    .payload(stringMessage)
                    .delaySeconds(delayInSeconds);
        });
    }

    public void sendStringMessagetoQueue5(String stringMessage) {
        sqsTemplate.send(queue5Name, stringMessage);
    }

    public void sendStringMessageToFifoQueue(String stringMessage) {
        sqsTemplate.send(fifoQueueName, stringMessage);
    }

    /**
     * Producer side: Object Serialization:If you're sending an object (like a Java POJO), it is serialized into a JSON string before being sent to the SQS queue.
     * This can be done manually or automatically using libraries like Jackson.
     */
    public void sendDeviceTemperature(DeviceTemperature deviceTemperature) {
        sqsTemplate.send(queue7Name, deviceTemperature);
    }

}
