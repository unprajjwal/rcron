# Use a lightweight JDK base
FROM openjdk:21-slim

# Set working directory
WORKDIR /app

# Copy the built jar
COPY target/rcron-1.0-SNAPSHOT.jar app.jar

# Expose default port
EXPOSE 9090

# Run the main class
ENTRYPOINT ["java", "-cp", "app.jar", "com.rcron.Main"]