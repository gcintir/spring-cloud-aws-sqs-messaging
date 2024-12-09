package org.projects.amazon.sqs.messaging;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class MessagingServiceIT {

    @Autowired
    private MessageProducerService messageProducerService;

    static SqsClient sqsClient;

    static final String FIFO_QUEUE_NAME = "fifo-queue.fifo";

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.4.0")
    ).withServices(SQS);

    @BeforeAll
    static void beforeAll() {
        localStack.start();

        sqsClient = SqsClient.builder()
                .endpointOverride(localStack.getEndpointOverride(SQS))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())
                ))
                .region(Region.of(localStack.getRegion()))
                .build();

        Map<QueueAttributeName, String> attributes = new HashMap<>();
        attributes.put(QueueAttributeName.FIFO_QUEUE, "true");
        attributes.put(QueueAttributeName.CONTENT_BASED_DEDUPLICATION, "true");
        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                .queueName(FIFO_QUEUE_NAME)
                .attributes(attributes)
                .build();
        sqsClient.createQueue(createQueueRequest);
    }

    @BeforeEach
    public void resetVariables() {
        MessageConsumerService.reset();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.sqs.endpoint", () -> localStack.getEndpointOverride(SQS));
        registry.add("spring.cloud.aws.region.static", () -> localStack.getRegion());
        registry.add("spring.cloud.aws.credentials.access-key", () -> localStack.getAccessKey());
        registry.add("spring.cloud.aws.credentials.secret-key", () -> localStack.getSecretKey());
    }

    @Test
    public void givenValidStringMessage_whenSendStringMessage_thenConsumeStringMessage () {
        String validStringMessage = "valid sqs message";

        messageProducerService.sendStringMessage(validStringMessage);

        Awaitility.await().atMost(2, SECONDS).untilAsserted(() -> {
            Assertions.assertEquals(validStringMessage, MessageConsumerService.receivedStringMessageList.get(0));
        });
    }

    @Test
    public void givenValidStringMessageWithUniqueId_whenSendStringMessageWithUniqueIdInHeader_thenConsumeStringMessageWithUniqueIdInHeader () {
        String validStringMessage = "valid sqs message";
        String uniqueId = UUID.randomUUID().toString();

        messageProducerService.sendStringMessageWithUniqueIdInHeader(validStringMessage, uniqueId);

        Awaitility.await().atMost(2, SECONDS).untilAsserted(() -> {
            Assertions.assertEquals(validStringMessage, MessageConsumerService.receivedStringMessageList.get(0));
            Assertions.assertEquals(uniqueId, MessageConsumerService.lastReceivedId);
        });
    }

    @Test
    public void givenValidStringMessageList_whenSendStringMessageList_thenConsumeStringMessageList () {
        List<String> stringMessageList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            stringMessageList.add(String.valueOf(System.currentTimeMillis()));
        }

        messageProducerService.sendStringMessageList(stringMessageList);

        Awaitility.await().atMost(2, SECONDS).untilAsserted(() -> {
            Assertions.assertEquals(stringMessageList.size(), MessageConsumerService.receivedStringMessageList.size());
        });
    }

    @Test
    public void givenValidStringMessage_whenSendStringMessageWithDelay_thenConsumeStringMessageWithDelay () {
        String validStringMessage = "valid sqs message";
        int delayInSeconds = 1;

        messageProducerService.sendStringMessageWithDelay(validStringMessage, delayInSeconds);

        Awaitility.await().during(delayInSeconds, SECONDS).untilAsserted(() -> {
            Assertions.assertEquals(0, MessageConsumerService.receivedStringMessageList.size());
        });
        Awaitility.await().atMost(2, SECONDS).untilAsserted(() -> {
            Assertions.assertEquals(1, MessageConsumerService.receivedStringMessageList.size());
        });
    }

    @Test
    public void givenValidStringMessage_whenSendStringMessageList_thenConsumeStringMessageInParallel () {
        String validStringMessage = "valid sqs message";

        for (int i = 0; i < 50; i++) {
            messageProducerService.sendStringMessagetoQueue5(validStringMessage);
        }

        Awaitility.await().atMost(3, SECONDS).untilAsserted(() -> {
            Assertions.assertEquals(50, MessageConsumerService.receivedStringMessageList.size());
            Assertions.assertEquals(5, MessageConsumerService.consumerThreadIdList.size());
        });
    }

    @Test
    public void givenDuplicateStringMessage_whenSendStringMessageToFifoQueue_thenConsumeDuplicateStringMessageFromFifoQueue () {
        String validStringMessage = "valid sqs message";

        for (int i = 0; i < 5; i++) {
            messageProducerService.sendStringMessageToFifoQueue(validStringMessage);
        }
        messageProducerService.sendStringMessageToFifoQueue(validStringMessage.toUpperCase());

        Awaitility.await().atMost(2, SECONDS).untilAsserted(() -> {
            Assertions.assertEquals(2, MessageConsumerService.receivedStringMessageList.size());
        });
    }

    @Test
    public void givenValidDeviceTemperature_whenSendDeviceTemperature_thenConsumeDeviceTemperatureMessage () throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            DeviceTemperature validDeviceTemperature = DeviceTemperature.builder()
                    .deviceId(new Random().nextInt(50) + "")
                    .temperature(new Random().nextInt(5, 50))
                    .build();
            messageProducerService.sendDeviceTemperature(validDeviceTemperature);
        }

        Awaitility.await().atMost(2, SECONDS).untilAsserted(() -> {
            Assertions.assertEquals(5, MessageConsumerService.deviceTemperatureList.size());
        });
    }
}
