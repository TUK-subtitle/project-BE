FROM amazoncorretto:21
ENV TZ=Asia/Seoul
COPY build/libs/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]