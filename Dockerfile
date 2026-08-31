# ESTÁGIO 1: Construção (Build)
# Usa uma imagem oficial do Maven com Java 17 para compilar o código
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copia os arquivos de dependência primeiro para aproveitar o cache do Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o resto do código e gera o arquivo .jar (ignorando os testes para ser mais rápido)
COPY src ./src
RUN mvn clean package -DskipTests

# ESTÁGIO 2: Execução (Run)
# Usa uma imagem do Java 17 ultra leve apenas para rodar o sistema
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia apenas o .jar compilado do Estágio 1, descartando o resto (deixa o sistema minúsculo)
COPY --from=build /app/target/*.jar app.jar

# Libera a porta 8080 para o mundo exterior
EXPOSE 8080

# Comando que o servidor vai executar quando o container ligar
ENTRYPOINT ["java", "-jar", "app.jar"]