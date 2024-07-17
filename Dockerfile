FROM openjdk:latest
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY szbldb-1.0.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]