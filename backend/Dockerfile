FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Set JAVA_HOME explicitly for Gradle toolchain
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH=$JAVA_HOME/bin:$PATH

COPY . .

# Make executable + build
RUN chmod +x gradlew && \
    ./gradlew build -x test && \
    cp build/libs/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
