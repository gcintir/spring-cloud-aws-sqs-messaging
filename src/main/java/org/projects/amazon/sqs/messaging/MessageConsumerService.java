package org.projects.amazon.sqs.messaging;

import io.awspring.cloud.sqs.annotation.SqsListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageConsumerService {

    public static List<String> receivedStringMessageList = new ArrayList<>();
    public static Set<Long> consumerThreadIdList = new HashSet<>();
    public static List<DeviceTemperature> deviceTemperatureList = new ArrayList<>();
    public static String lastReceivedId = "";

    @SqsListener(value = "${queue_1.name}")
    public void consumeStringMessage(String stringMessage) {
        receivedStringMessageList.add(stringMessage);
    }

    @SqsListener(value = "${queue_2.name}")
    public void consumeStringMessageWithUniqueIdInHeader(Message<String> message) {
        receivedStringMessageList.add(message.getPayload());
        lastReceivedId = (String) message.getHeaders().get("uniqueId");
    }

    @SqsListener(value = "${queue_3.name}")
    public void consumeStringMessageList(List<Message<String>> messageList) {
        receivedStringMessageList.addAll(messageList.stream().map(Message::getPayload).toList());
    }

    @SqsListener(value = "${queue_4.name}")
    public void consumeStringMessageWithDelay(Message<String> message) {
        receivedStringMessageList.add(message.getPayload());
    }


    /**
     * maxConcurrentMessages (for concurrency control): controls the maximum number of messages that can be processed concurrently
     * by a single @SqsListener (default: 10)
     *  If you want to increase or decrease parallel processing, adjust this value.
     *
     * maxMessagesPerPoll (for pooling control): defines how many messages should be retrieved in a single poll from the queue (default: 10)
     * Helps in reducing the number of API calls and improving performance if batching messages is more efficient.
     */
    @SqsListener(value = "${queue_5.name}", maxConcurrentMessages = "5", maxMessagesPerPoll = "2")
    public void consumeStringMessageInParallel(String stringMessage) {
        receivedStringMessageList.add(stringMessage);
        consumerThreadIdList.add(Thread.currentThread().getId());
    }

    // content-based deduplication enabled
    @SqsListener(value = "${queue_6.name}")
    public void consumeDuplicateStringMessageFromFifoQueue(String stringMessage) {
        receivedStringMessageList.add(stringMessage);
    }

    // controlling polling timeout

    /**
     * consumer side: Message Deserialization: When an SQS message is received, it arrives as a JSON string
     * The message is then deserialized back into a Java object using the appropriate deserialization logic (e.g., Jackson).
     */
    @SqsListener(value = "${queue_7.name}",pollTimeoutSeconds = "5")
    public void consumeDeviceTemperatureMessage(List<Message<DeviceTemperature>> deviceTemperatureMessageList) {
        deviceTemperatureMessageList.forEach(deviceTemperatureMessage -> deviceTemperatureList.add(deviceTemperatureMessage.getPayload()));
    }

    public static void reset() {
        receivedStringMessageList.clear();
        lastReceivedId = "";
        consumerThreadIdList.clear();
        deviceTemperatureList.clear();
    }
}
