# 🛠️ Mastering AWS SQS Integration with Spring Cloud AWS: Hands-On Examples and Testcontainers-Powered Testing

Welcome to the **Spring Cloud AWS SQS Integration** project! This repository demonstrates how to seamlessly integrate **Amazon SQS** with **Spring Boot** using **Spring Cloud AWS**. We explore practical messaging scenarios and ensure reliability through **Testcontainers**-powered integration tests and **LocalStack** for local simulation.

---

## 🚀 Features

- **Simplified SQS Integration**: Leverage abstractions like `SqsTemplate` and `@SqsListener`.
- **Real-World Scenarios**: Hands-on examples for sending and receiving messages.
- **Reliable Testing**: Integration tests using **Testcontainers** to emulate real-world behavior.
- **Cost-Effective Development**: Utilize **LocalStack** to simulate Amazon SQS in a local environment.

---

## 📚 Table of Contents

- [Getting Started](#-getting-started)
- [How It Works](#-how-it-works)
- [Examples](#-examples)
- [Testing](#-testing)
- [Contributing](#-contributing)

---

## 🔧 Getting Started

### Prerequisites
- Java 17+
- Docker (for Testcontainers and LocalStack)
- Maven

### Installation
1. Clone the repository:
   ```bash  
   git clone https://github.com/gcintir/spring-cloud-aws-sqs-messaging.git
   cd spring-cloud-aws-sqs-integration  

2. Build the project:
   ```bash  
   mvn clean package

---

## 🛠️ How It Works

### Sending Messages
Use the SqsTemplate to send messages to an SQS queue with ease.

### Receiving Messages
Annotate your method with @SqsListener to automatically handle incoming messages.

### Local Testing
Simulate SQS behavior locally using LocalStack and validate your implementation with Testcontainers.

---

## 💡 Examples

### Send Message:
```bash  
   sqsTemplate.convertAndSend("queueName", "Hello, SQS!");
```

### Receive Message:
```bash  
   @SqsListener("queueName")
   public void handleMessage(String message) {
      System.out.println("Received message: " + message);
   }
```

---

## ✅ Testing
This project uses Testcontainers to ensure tests are run in an environment that mimics AWS. To test locally:
```bash  
   mvn test
```

---

## 🤝 Contributing
Contributions are welcome! If you have an idea, feature request, or bug report, please open an issue or submit a pull request.