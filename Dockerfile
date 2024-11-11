FROM amd64/amazoncorretto:17
WORKDIR /app
COPY ./build/libs/server-1.0.0-SNAPSHOT.jar /app/AUTO-REPO.jar
CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "-Dspring.profiles.active=dev", "AUTO-REPO.jar"]