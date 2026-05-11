# ================= BUILD =================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN apt-get update && apt-get install -y dos2unix
RUN dos2unix mvnw
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw package -DskipTests


# ================= RUNTIME =================
FROM eclipse-temurin:21-jre

WORKDIR /app

# ✅ SEM ENV FIXO (isso vai pro docker-compose)

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]