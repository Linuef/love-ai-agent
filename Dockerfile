FROM maven:3.9-amazoncorretto-21 AS builder

RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo Asia/Shanghai > /etc/timezone

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine-jdk

RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo Asia/Shanghai > /etc/timezone

WORKDIR /app

COPY --from=builder /app/target/yu-ai-agent-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8123

CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]