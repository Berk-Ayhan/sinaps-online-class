# 1. Aşama: Maven build için
FROM maven:3.9.8-eclipse-temurin-21 AS build

# 2. Proje dosyalarını konteynere kopyala
COPY src /home/app/src
COPY pom.xml /home/app

# 3. Maven ile projeyi build et
RUN mvn -f /home/app/pom.xml clean package -DskipTests

# 2. Aşama: JDK imajı ve build edilmiş JAR dosyasının eklenmesi
FROM eclipse-temurin:21-jdk-alpine

# 4. Build aşamasından JAR dosyasını al ve konteynere kopyala
COPY --from=build /home/app/target/*.jar app.jar

# 5. Uygulamanın çalıştırılma komutunu belirle
ENTRYPOINT ["java","-jar","/app.jar"]
